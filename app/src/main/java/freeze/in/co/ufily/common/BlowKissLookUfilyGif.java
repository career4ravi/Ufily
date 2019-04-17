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

class BlowKissLookUfilyGif extends  UfilyGif
{
    UfilyPart mUfilyLips;
    ExecutorService mExecutor;
    BlowKissLookUfilyGif(ExecutorService executor, Activity activity, Bitmap backGroundImage)
    {
        super(executor,activity,backGroundImage);
        mExecutor = executor;
        mUfilyLips = new UfilyPart();
        //search part
        searchPart(Landmark.LEFT_MOUTH,Landmark.RIGHT_MOUTH,mUfilyLips);
    }


    @Override
    public Bitmap createGifImagePart(int symbol,int scaling)
    {
        int scaledWidth;
        int scaledHeight;

        //cigar is half the size of the face ..assumption
        scaledWidth = mBackGroundImage.getWidth()/2;

        //get the image aspect ratio
        Bitmap cigarImage = BitmapFactory.decodeResource(
                mActivity.getApplicationContext().getResources(), R.drawable.blowkiss0);

        double aspectRatio = (double)cigarImage.getHeight()/cigarImage.getWidth();
        scaledHeight = (int) (aspectRatio *scaledWidth);

        Bitmap blowKissImage =
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                        mActivity.getApplicationContext().getResources(),symbol),
                        scaledWidth,
                        scaledHeight,true);
        Log.d(UfilyConstants.APP,"");


        //Bitmap blowKissImage = BitmapFactory.decodeResource(
        //      mActivity.getApplicationContext().getResources(), symbol);



        return putOverlay(blowKissImage);
    }


    @Override
    public Uri createGifImage(){
        if(mExecutor ==null)
            return null;
        GifManager.getInstance().logMessageOnUIThread("BlowKissLookUfilyGif::createGifImage()");
        List<Future<Bitmap>> gifPartTaskList = new ArrayList<Future<Bitmap>>();
        for(int count = 0; count < UfilyConstants.UFILY_BLOWKISS_SYMBOL_ARRAY.length;count++)
        {
            gifPartTaskList.add(mExecutor.submit(
                    new GifPart(count,
                            this,
                            UfilyConstants.UFILY_BLOWKISS_SYMBOL_ARRAY[count],
                            0
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
            GifManager.getInstance().logMessageOnUIThread("BlowKissLookUfilyGif::while trying to get the result");
        }
        GifManager.getInstance().logMessageOnUIThread("BlowKissLookUfilyGif::all Gif image parts created");

        return combineBitmapToGif(gifBitmapList,100);
    }


    @Override
    public Bitmap putOverlay(Bitmap overlay)
    {
        Bitmap newBitmap = Bitmap.createBitmap(mBackGroundImage);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAlpha(255);
        //check for  detected or not
        if(mUfilyLips.isLeftPartDetected()) {

            Log.d(UfilyConstants.APP,"BlowKissLookUfilyGif::putOverlay center detected");

            int overlayX = mUfilyLips.mleftPart.mX;
            int overlayY = mUfilyLips.mleftPart.mY;

            //canvas.drawCircle(overlayX,overlayY,10,new Paint(Color.GREEN));


            //should be at the centre of lips
            overlayY = overlayY - overlay.getHeight();
            overlayX = overlayX - overlay.getWidth()/2;

            //canvas.drawCircle(overlayX,overlayY,10,new Paint(Color.RED));
            //canvas.drawCircle(overlayX,overlayY,10,new Paint(Color.GREEN));
            canvas.drawBitmap(overlay, overlayX, overlayY, paint);
        }

        //returns same thing . needs to be refined.
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
                            UfilyConstants.UFILY_BLOWKISS_SYMBOL_ARRAY[count],
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
