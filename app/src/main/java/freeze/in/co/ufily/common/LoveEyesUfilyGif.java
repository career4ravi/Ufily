package freeze.in.co.ufily.common;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.vision.face.Landmark;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by rtiragat on 4/15/2019.
 */

class LoveEyesUfilyGif extends  UfilyGif
{
    UfilyPart mUfilyEyes;
    ExecutorService mExecutor;

    LoveEyesUfilyGif(ExecutorService executor, Activity activity, Bitmap backGroundImage)
    {
        super(executor,activity,backGroundImage);
        mExecutor = executor;
        mUfilyEyes = new UfilyPart();
        searchEyes();
    }

    private void searchEyes()
    {
        boolean leftEye = false;
        boolean rightEye = false;

        if(mLandmarks==null)
            return;

        for (Landmark landmark : mLandmarks) {

            if (landmark.getType() == Landmark.LEFT_EYE) {
                int cx = (int) (landmark.getPosition().x);
                int cy = (int) (landmark.getPosition().y);
                mUfilyEyes.mleftPart.mX = cx;
                mUfilyEyes.mleftPart.mY = cy;
                Log.d(UfilyConstants.APP, "FaceDetector Left eye landmark:x" + cx + " y:" + cy);
                leftEye = true;
                //canvas.drawCircle(cx, cy, 10, paint);
            } else if (landmark.getType() == Landmark.RIGHT_EYE) {
                int cx = (int) (landmark.getPosition().x);
                int cy = (int) (landmark.getPosition().y);
                mUfilyEyes.mRightPart.mX = cx;
                mUfilyEyes.mRightPart.mY = cy;
                Log.d(UfilyConstants.APP, "FaceDetector Right eye landmark:x" + cx + " y:" + cy);
                rightEye = true;
            }

            if (rightEye && leftEye) {
                break;
            }
        }
    }

    @Override
    public Uri createGifImage(){
        if(mExecutor ==null)
            return null;
        GifManager.getInstance().logMessageOnUIThread("LoveEyesUfilyGif::createGifImage()");
        List<Future<Bitmap>> gifPartTaskList = new ArrayList<Future<Bitmap>>();
        for(int count = 0; count < UfilyConstants.UFILY_LOVE_SYMBOL_ARRAY.length;count++)
        {
            gifPartTaskList.add(mExecutor.submit(
                    new GifPart(count,
                            this,
                            UfilyConstants.UFILY_LOVE_SYMBOL_ARRAY[count],
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

        return combineBitmapToGif(gifBitmapList,100);

    }

    public Bitmap createGifImagePart(int symbol,int scaling)
    {
        int scaledWidth;
        int scaledHeight;
        //not sure if we could use it this way ?
        float imageSize = Math.max(mBackGroundImage.getHeight(), mBackGroundImage.getWidth());

        Log.d(UfilyConstants.APP,"mBackGroundImage.getHeight():"+mBackGroundImage.getHeight());
        Log.d(UfilyConstants.APP,"mBackGroundImage.getWidth():"+mBackGroundImage.getWidth());

        //this needs to be fine tunes.
        if(scaling == 0) {
            scaledHeight = scaledWidth = (int) (imageSize / 4);
        }
        else
        {
            scaledHeight = scaledWidth = (int) (imageSize / scaling);
        }
        if(Thread.interrupted())
            return null;

        Log.d(UfilyConstants.APP,"scaledHeight."+scaledHeight);


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
        //check for eyes detected or not
        int overlayX = mUfilyEyes.mleftPart.mX;
        int overlayY = mUfilyEyes.mleftPart.mY;

        int overlayRightX = mUfilyEyes.mRightPart.mX;
        int overlayRightY = mUfilyEyes.mRightPart.mY;


        overlayX = overlayX - overlay.getWidth()/2;
        overlayY = overlayY - overlay.getHeight()/2;

        overlayRightX = overlayRightX - overlay.getWidth() / 2;
        overlayRightY = overlayRightY - overlay.getHeight()/2;
        //left eye
        if(mUfilyEyes.isLeftPartDetected())
            canvas.drawBitmap(overlay, overlayX, overlayY, paint);
        //right eye
        if(mUfilyEyes.iRightPartDetected())
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
                            UfilyConstants.UFILY_LOVE_SYMBOL_ARRAY[count],
                            8
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
        bitmap.compress(Bitmap.CompressFormat.WEBP, 80, stream);
        byte[] byteArray = stream.toByteArray();

        GifManager.getInstance().logMessageOnUIThread("LoveEyesUfilyGif::size of Webp"+byteArray.length/1024+"KB");
        return GifManager.getLocalImageUri(mActivity,byteArray,".webp");

    }


    /**
     * returns the bytesize of the give bitmap
     */
    public  int byteSizeOf(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

}
