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


class DevilLookUfilyGif extends  UfilyGif
{
    UfilyPart mUfilyLips;
    UfilyPart mUfilyCheeks;
    UfilyPart mUfilyEyes;

    ExecutorService mExecutor;
    DevilLookUfilyGif(ExecutorService executor, Activity activity, Bitmap backGroundImage)
    {
        super(executor,activity,backGroundImage);
        mExecutor = mExecutor;
        mUfilyLips = new UfilyPart();
        mUfilyCheeks = new UfilyPart();
        mUfilyEyes  = new UfilyPart();
        searchPart(Landmark.LEFT_MOUTH,Landmark.RIGHT_MOUTH,mUfilyLips);
        searchPart(Landmark.LEFT_CHEEK,Landmark.RIGHT_CHEEK,mUfilyCheeks);
        searchPart(Landmark.LEFT_EYE,Landmark.RIGHT_EYE,mUfilyEyes);
        //face image is added with trident
        addTrendent2FaceAndHorns();
    }

    private  void addTrendent2FaceAndHorns()
    {
        int scaledWidth;
        int scaledHeight;

        scaledHeight = mBackGroundImage.getHeight();

        //get the image aspect ratio
        Bitmap tridentImage = BitmapFactory.decodeResource(
                mActivity.getApplicationContext().getResources(), R.drawable.tridentsmall);

        double aspectRatio = (double)tridentImage.getWidth()/tridentImage.getHeight();

        scaledWidth =    (int)(aspectRatio * scaledHeight);

        Bitmap scaledTrident = Bitmap.createScaledBitmap(tridentImage,scaledWidth,
                scaledHeight,true);


        Bitmap leftHornImage = BitmapFactory.decodeResource(
                mActivity.getApplicationContext().getResources(), R.drawable.lefthorn);

        Bitmap rightHornImage = BitmapFactory.decodeResource(
                mActivity.getApplicationContext().getResources(), R.drawable.righthorn);


        int scaledHornWidth = mBackGroundImage.getWidth()/4;
        int scaledHornHeight = mBackGroundImage.getHeight()/4;

        Bitmap scaledLeftHorn = Bitmap.createScaledBitmap(leftHornImage,scaledHornWidth,
                scaledHornHeight,true);

        Bitmap scaledRightHorn = Bitmap.createScaledBitmap(rightHornImage,scaledHornWidth,
                scaledHornHeight,true);

        int distanceBetweenEyes = mUfilyEyes.mRightPart.mX - mUfilyEyes.mleftPart.mX;

        if(distanceBetweenEyes < 0)
        {
            distanceBetweenEyes = 0 -(distanceBetweenEyes);
        }

        Bitmap newBitmap = Bitmap.createBitmap(mBackGroundImage);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAlpha(255);


        int overlayLeftX = mUfilyCheeks.mleftPart.mX;
        int overlayLeftY = mUfilyCheeks.mleftPart.mY;

        overlayLeftX = overlayLeftX - scaledTrident.getWidth()/ 4;
        overlayLeftY = overlayLeftY - scaledTrident.getHeight()/2;


        int overLayHornLeftX = mUfilyEyes.mRightPart.mX - (int)(scaledLeftHorn.getWidth());
        int overLayHornLeftY = mUfilyEyes.mRightPart.mY - ( (distanceBetweenEyes*3/2) + scaledLeftHorn.getHeight()/2);

        int overLayHornRightX = mUfilyEyes.mleftPart.mX; //+ (int)(scaledRightHorn.getWidth());
        int overLayHornRightY = mUfilyEyes.mleftPart.mY - ( (distanceBetweenEyes*3/2) + scaledRightHorn.getHeight()/2);


        if(mUfilyEyes.iRightPartDetected())
            canvas.drawBitmap(scaledLeftHorn,overLayHornLeftX,overLayHornLeftY,paint);

        if(mUfilyEyes.isLeftPartDetected())
            canvas.drawBitmap(scaledRightHorn,overLayHornRightX,overLayHornRightY,paint);


        if(mUfilyCheeks.iRightPartDetected()) {
            //canvas.drawBitmap(scaledTrident, overlayLeftX, overlayLeftY, paint);

            Paint color = new Paint();
            color.setARGB(255,255,0,0);
            //canvas.drawCircle(overlayLeftX,overlayLeftX,10,color);
            //canvas.drawCircle(overlayLeftX - scaledSymbol.getWidth()/ 2,overlayLeftX - scaledSymbol.getHeight()/2,10,color);

        }

        mBackGroundImage = newBitmap;

    }



    @Override
    public Uri createGifImage(){
        if(mExecutor ==null)
            return null;
        GifManager.getInstance().logMessageOnUIThread("LoveEyesUfilyGif::createGifImage()");
        List<Future<Bitmap>> gifPartTaskList = new ArrayList<Future<Bitmap>>();
        for(int count = 0; count < UfilyConstants.UFILY_FIRE_SYMBOL_ARRAY.length;count++)
        {
            gifPartTaskList.add(mExecutor.submit(
                    new GifPart(count,
                            this,
                            UfilyConstants.UFILY_FIRE_SYMBOL_ARRAY[count]
                            ,0
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
            GifManager.getInstance().logMessageOnUIThread("LoveEyesUfilyGif::while trying to get the result");
        }
        GifManager.getInstance().logMessageOnUIThread("LoveEyesUfilyGif::all Gif image parts created");

        return combineBitmapToGif(gifBitmapList,100);
    }

    public Bitmap createGifImagePart(int symbol,int scaling)
    {
        int scaledWidth;
        int scaledHeight;
        //not sure if we could use it this way ?
        float imageSize = Math.max(mBackGroundImage.getHeight(), mBackGroundImage.getWidth());
        //this needs to be fine tunes.
        scaledWidth = (int)(imageSize/3);
        scaledHeight = mBackGroundImage.getHeight()/2;

        if(Thread.interrupted())
            return null;

        Bitmap scaledSymbol =
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                        mActivity.getApplicationContext().getResources(),symbol),
                        scaledWidth,
                        scaledHeight,true);

        Log.d(UfilyConstants.APP,"");
        return putOverlay(scaledSymbol);
    }

    @Override
    public Bitmap putOverlay(Bitmap overlay)
    {
        Bitmap newBitmap = Bitmap.createBitmap(mBackGroundImage);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAlpha(255);
        //check for Lips detected or not
        int overlayX = mUfilyLips.mleftPart.mX;
        int overlayY = mUfilyLips.mleftPart.mY;

        int overlayRightX = mUfilyLips.mRightPart.mX;
        int overlayRightY = mUfilyLips.mRightPart.mY;

        //check it is not out of bound here.
        overlayX = overlayX - 3*overlay.getWidth()/2;
        overlayY = overlayY - overlay.getHeight()/2;

        overlayRightX = overlayRightX + overlay.getWidth()/2;
        overlayRightY = overlayRightY  - overlay.getHeight()/2;
        //left Lip Corner
        if(mUfilyLips.isLeftPartDetected())
            canvas.drawBitmap(overlay, overlayX, overlayY, paint);
        //right Lip corner
        if(mUfilyLips.iRightPartDetected())
            canvas.drawBitmap(overlay,overlayRightX,overlayRightY,paint);

        return newBitmap;
    }

    public  Uri createWebpImage()
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
                            UfilyConstants.UFILY_FIRE_SYMBOL_ARRAY[count],
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



}