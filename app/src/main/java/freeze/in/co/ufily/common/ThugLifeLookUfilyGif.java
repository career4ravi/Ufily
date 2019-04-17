package freeze.in.co.ufily.common;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.vision.face.Landmark;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import freeze.in.co.ufily.R;

/**
 * Created by rtiragat on 4/15/2019.
 */

class ThugLifeLookUfilyGif extends  UfilyGif
{
    UfilyPart mUfilyLips;
    UfilyPart mUfilyEyes;
    ExecutorService mExecutor;
    ThugLifeLookUfilyGif(ExecutorService executor, Activity activity, Bitmap backGroundImage)
    {
        super(executor,activity,backGroundImage);
        mExecutor = executor;
        mUfilyLips = new UfilyPart();
        mUfilyEyes = new UfilyPart();
        //search part
        searchPart(Landmark.BOTTOM_MOUTH,mUfilyLips);
        searchPart(Landmark.LEFT_EYE,Landmark.RIGHT_EYE,mUfilyEyes);
        addCaptoFace();
    }

    public Uri createWebpImage()
    {
        if(mExecutor ==null)
            return null;

        GifManager.getInstance().logMessageOnUIThread("LoveEyesUfilyGif::createWebpImage()");
        List<Future<Bitmap>> gifPartTaskList = new ArrayList<Future<Bitmap>>();
        for(int count = 0; count < 1;count++)
        {
            gifPartTaskList.add(mExecutor.submit(
                    new GifPart(count,
                            this,
                            UfilyConstants.UFILY_THUGLIFE_SYMBOL_ARRAY[count],
                            0
                    )));
        }
        List<Bitmap> gifBitmapList = new ArrayList<Bitmap>();
        try {

            for (Future<Bitmap> future : gifPartTaskList) {
                //check for a null bitmap
                gifBitmapList.add(future.get());
            }
        }

        catch (Exception e)
        {
            //@TODO handle interpted exception
            GifManager.getInstance().logMessageOnUIThread("LoveEyesUfilyGif::while trying to get the result");
        }

        GifManager.getInstance().logMessageOnUIThread("LoveEyesUfilyGif::all Gif image parts created");

        Bitmap bitmap = gifBitmapList.get(0);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return GifManager.getLocalImageUri(mActivity,byteArray,".webp");

    }



    private void addCaptoFace()
    {

        int scaledWidth;
        int scaledHeight;

        scaledWidth = mBackGroundImage.getWidth();

        //get the image aspect ratio
        Bitmap capImage = BitmapFactory.decodeResource(
                mActivity.getApplicationContext().getResources(), R.drawable.thuglife);

        double aspectRatio = (double)capImage.getHeight()/capImage.getWidth();

        //scaledHeight =    (int)(aspectRatio * scaledWidth);
        scaledHeight = mBackGroundImage.getWidth()/4;
        Bitmap scaledCap = Bitmap.createScaledBitmap(capImage,scaledWidth,
                scaledHeight,true);


        int distanceBetweenEyes = mUfilyEyes.mRightPart.mX - mUfilyEyes.mleftPart.mX;

        if(distanceBetweenEyes < 0)
        {
            distanceBetweenEyes = 0 -(distanceBetweenEyes);
        }


        int eyeCenterX = (mUfilyEyes.mRightPart.mX + mUfilyEyes.mleftPart.mX)/2;
        int eyeCenterY = (mUfilyEyes.mRightPart.mY + mUfilyEyes.mleftPart.mY)/2;


        eyeCenterY = eyeCenterY - (distanceBetweenEyes/2);

        Bitmap newBitmap = Bitmap.createBitmap(mBackGroundImage);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAlpha(255);

        eyeCenterX = eyeCenterX - scaledCap.getWidth()/2;
        eyeCenterY = eyeCenterY - scaledCap.getHeight();
        canvas.drawBitmap(scaledCap,eyeCenterX,eyeCenterY,paint);


        mBackGroundImage = newBitmap;

    }

    private void searchPart(int landmark1,UfilyPart ufilyPart)
    {

        boolean middlePart = false;
        if(mLandmarks==null) return;
        for (Landmark landmark : mLandmarks) {

            if (landmark != null && landmark.getType() == landmark1) {
                int cx = (int) (landmark.getPosition().x);
                int cy = (int) (landmark.getPosition().y);
                ufilyPart.mCenterPart.mX = cx;
                ufilyPart.mCenterPart.mY = cy;
                //Log.d(UfilyConstants.APP, "FaceDetector centre part landmark:x" + cx + " y:" + cy);
                middlePart = true;
                //canvas.drawCircle(cx, cy, 10, paint);
            }

            if (middlePart) {
                break;
            }
        }
    }


    @Override
    public Bitmap createGifImagePart(int symbol,int scaling)
    {
        int scaledWidth;
        int scaledHeight;
        //cigar is half the size of the face ..assumption
        scaledWidth = mBackGroundImage.getHeight()/3;

        //get the image aspect ratio
        Bitmap cigarImage = BitmapFactory.decodeResource(
                mActivity.getApplicationContext().getResources(), R.drawable.cigar0);

        double aspectRatio = (double)cigarImage.getHeight()/cigarImage.getWidth();
        scaledHeight = (int) (aspectRatio *scaledWidth);

        Bitmap scaledSymbol =
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                        mActivity.getApplicationContext().getResources(),symbol),
                        scaledWidth,
                        scaledHeight,true);
        Log.d(UfilyConstants.APP,"");

        return putOverlay(scaledSymbol);
    }


    @Override
    public    Uri createGifImage(){
        if(mExecutor ==null)
            return null;
        GifManager.getInstance().logMessageOnUIThread("ThugLifeLookUfilyGif::createGifImage()");
        List<Future<Bitmap>> gifPartTaskList = new ArrayList<Future<Bitmap>>();
        for(int count = 0; count < UfilyConstants.UFILY_THUGLIFE_SYMBOL_ARRAY.length;count++)
        {
            gifPartTaskList.add(mExecutor.submit(
                    new GifPart(count,
                            this,
                            UfilyConstants.UFILY_THUGLIFE_SYMBOL_ARRAY[count],0
                    )));
        }
        List<Bitmap> gifBitmapList = new ArrayList<Bitmap>();

        try {

            for (Future<Bitmap> future : gifPartTaskList) {
                gifBitmapList.add(future.get());
            }
        }
        catch (Exception e)
        {
            GifManager.getInstance().logMessageOnUIThread("ThugLifeLookUfilyGif::while trying to get the result");
        }
        GifManager.getInstance().logMessageOnUIThread("ThugLifeLookUfilyGif::all Gif image parts created");

        return combineBitmapToGif(gifBitmapList,100);
    }


    @Override
    public Bitmap putOverlay(Bitmap overlay)
    {
        Bitmap newBitmap = Bitmap.createBitmap(mBackGroundImage);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAlpha(255);
        //check for eyes detected or not
        if(mUfilyLips.isCenterPartDetected()) {

            Log.d(UfilyConstants.APP,"ThugLifeLookUfilyGif::putOverlay center detected");

            int overlayX = mUfilyLips.mCenterPart.mX;
            int overlayY = mUfilyLips.mCenterPart.mY;

            //should be at the centre of lips
            overlayY = overlayY - overlay.getHeight()/2;
            //canvas.drawCircle(overlayX,overlayY,10,new Paint(Color.GREEN));
            canvas.drawBitmap(overlay, overlayX, overlayY, paint);
        }

        //returns same thing . needs to be refined.
        return newBitmap;
    }


}
