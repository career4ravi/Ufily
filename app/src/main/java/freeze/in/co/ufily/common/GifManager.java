package freeze.in.co.ufily.common;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.vision.face.Landmark;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import freeze.in.co.ufily.R;

/**
 * Created by rtiragat on 2/1/2017.
 */
public class GifManager {

    ExecutorService mExecutor;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message inputMessage) {

            switch(inputMessage.what)
            {
                case UfilyConstants.UFILY_LOG_MESSAGE_ID:
                    Log.d(UfilyConstants.APP,inputMessage.getData().getString(UfilyConstants.UFILY_LOG_MESSAGE_TAG));
                    break;
                default:
                    Log.d(UfilyConstants.APP,"ignoring junk message");
                    break;


            }

        }
    };


    private static final GifManager self = new GifManager() ;
    private GifManager()
    {
        Log.d(UfilyConstants.APP, "GifManager::GifManager()");
        //mExecutor = Executors.newFixedThreadPool(5);
        mExecutor = Executors.newCachedThreadPool();
    }

    public static GifManager getInstance()
    {
        return self;
    }

    //only when activity is stopped.
    public void  shutDown()
    {
        mExecutor.shutdown();
    }

    public void cancel()
    {


    }

    void logMessageOnUIThread(String msg)
    {
        if (!msg.equals(null) && !msg.equals("")) {
            Message msgObj = mHandler.obtainMessage();
            Bundle b = new Bundle();
            msgObj.what = UfilyConstants.UFILY_LOG_MESSAGE_ID;
            b.putString(UfilyConstants.UFILY_LOG_MESSAGE_TAG, msg);
            msgObj.setData(b);
            mHandler.sendMessage(msgObj);
        }
    }


    public Uri createGifImage(Activity activity, Bitmap backGroundImage, UfilyConstants.UFILY_SMILEY ufily_smiley)
    {
        int i=0;
        UfilyGif ufilyGif = null;
        switch (ufily_smiley) {
            case UFILY_LOVE_EYES :
                ufilyGif = new LoveEyesUfilyGif(mExecutor, activity, backGroundImage);
                break;
            case UFILY_DEVIL_LOOK:
                ufilyGif = new DevilLookUfilyGif(mExecutor, activity, backGroundImage);
                break;
            case UFILY_THUGLIFE_LOOK:
                ufilyGif = new ThugLifeLookUfilyGif(mExecutor, activity, backGroundImage);
                break;
            case UFILY_BLOWKISS_LOOK:
                ufilyGif = new BlowKissLookUfilyGif(mExecutor, activity, backGroundImage);
                break;
            case UFILY_KISSING_LIPS:
                ufilyGif = new KissingLipsUfilyGif(mExecutor, activity, backGroundImage);
                break;
            default:
                logMessageOnUIThread("GifManager::createGifImage  nothing to do ");
                break;
        }
        Uri result = null;
        if(ufilyGif != null) {
            long startTime= new Date().getTime();
            result = ufilyGif.createGifImage();
            long endTime= new Date().getTime();
            printTime(startTime,endTime);
        }
        return result;
    }


    public Uri createWebpImage(Activity activity, Bitmap backGroundImage, UfilyConstants.UFILY_SMILEY ufily_smiley)
    {
        int i=0;
        UfilyGif ufilyWebp = null;
        switch (ufily_smiley) {
            case UFILY_LOVE_EYES :
                ufilyWebp = new LoveEyesUfilyGif(mExecutor, activity, backGroundImage);
                break;
            case UFILY_DEVIL_LOOK:
                ufilyWebp = new DevilLookUfilyGif(mExecutor, activity, backGroundImage);
                break;
            case UFILY_THUGLIFE_LOOK:
                ufilyWebp = new ThugLifeLookUfilyGif(mExecutor, activity, backGroundImage);
                break;
            case UFILY_BLOWKISS_LOOK:
                ufilyWebp = new BlowKissLookUfilyGif(mExecutor, activity, backGroundImage);
                break;
            case UFILY_KISSING_LIPS:
                ufilyWebp = new KissingLipsUfilyGif(mExecutor, activity, backGroundImage);
                break;
            default:
                logMessageOnUIThread("GifManager::createGifImage  nothing to do ");
                break;
        }
        Uri result = null;
        if(ufilyWebp != null) {
            long startTime= new Date().getTime();
            result = ufilyWebp.createWebpImage();
            long endTime= new Date().getTime();
            printTime(startTime,endTime);
        }
        return result;
    }


    private void printTime(long startTime,long endTime)
    {

        long diff = endTime-startTime;

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        logMessageOnUIThread(diffDays+" days ,"+diffHours+" hours,"+diffMinutes+" mintues,"+diffSeconds+" secs");
    }
    //to be moved to ImageHelper Class
    private Uri getLocalGifUri(Activity activities, byte[] aInput,String imageType)
    {
        logMessageOnUIThread("getLocalGifUri");

        Uri gifUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            String fileName = "share_image_" + System.currentTimeMillis() + imageType;
            File file =  new File(activities.getExternalFilesDir(Environment.DIRECTORY_PICTURES),fileName);
            FileOutputStream fout = new FileOutputStream(file);


            OutputStream output = null;
            try{
                output = new BufferedOutputStream(fout);
                output.write(aInput);
            }
            finally{
                output.close();
            }
            gifUri = Uri.fromFile(file);

            if(gifUri == null)
            {
                logMessageOnUIThread("no uri");
            }
            else {
                logMessageOnUIThread(gifUri.getPath());

            }
        }
        catch(FileNotFoundException ex){
            ex.printStackTrace();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
        return gifUri;
    }

    private Uri getLocalImageUri(Activity activities, byte[] aInput, String imageType)
    {
        Log.d(UfilyConstants.APP,"getLocalGifUri");
        Uri gifUri = null;
        try {


            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            String fileName = "share_image_" + System.currentTimeMillis() + imageType;

            String mDir = Environment.DIRECTORY_PICTURES;
            File mPath = Environment.getExternalStoragePublicDirectory(mDir);

            File musicPublicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            Log.d(UfilyConstants.APP,"getLocalImageUri:Public picture directory:"+ musicPublicDir.getAbsolutePath());

            //File file =  new File(activities.getExternalFilesDir(Environment.DIRECTORY_PICTURES),fileName);

            File file =  new File(musicPublicDir.getAbsoluteFile()+"/"+fileName);
            //file.setReadable(true);
            //file.setExecutable(true);
            //file.setWritable(true);

            Log.d(UfilyConstants.APP,"getLocalImageUri:Picture path:"+ file.getAbsolutePath());

            FileOutputStream fout = new FileOutputStream(file);


            OutputStream output = null;
            try{
                output = new BufferedOutputStream(fout);
                output.write(aInput);
            }
            finally{
                output.close();
            }
            gifUri = Uri.fromFile(file);


            //scanFile(getActivity().getApplication(),file,"img/*");

            if(gifUri == null)
            {
                Log.d(UfilyConstants.APP,"no uri");
            }
            else {
                Log.d(UfilyConstants.APP,gifUri.getPath());
            }
        }
        catch (Exception e)
        {
            Log.d(UfilyConstants.APP,"exception"+e.getMessage());
        }

        return gifUri;
    }



    abstract private  class UfilyGif
    {
        private ExecutorService mExecutor;
        protected Activity mActivity;
        protected Bitmap mBackGroundImage;
        protected  List<Landmark> mLandmarks;
        UfilyGif(ExecutorService executor,Activity activity,Bitmap backGroundImage)
        {
            mExecutor = executor;
            mActivity = activity;
            mBackGroundImage = backGroundImage;
            mLandmarks = FaceDetector1.getAllLandmarks(mActivity,mBackGroundImage);
        }

        protected Uri combineBitmapToGif(List<Bitmap> gifBitmapList,int delay)
        {
            logMessageOnUIThread("UfilyGif::all Gif image parts created");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            encoder.start(bos);
            encoder.setDelay(delay);

            logMessageOnUIThread("UfilyGif::started creating Gif");
            for (Bitmap frame : gifBitmapList) {
                if(frame!=null) {
                    encoder.addFrame(frame);
                    logMessageOnUIThread("UfilyGif::added frame to Gif");
                    encoder.setTransparent(1);
                }
            }
            encoder.finish();
            logMessageOnUIThread("UfilyGif::finished creating Gif");

            return getLocalGifUri(mActivity,bos.toByteArray(),".gif");
        }


        protected void searchPart(int landmark1,int landmark2,UfilyPart ufilyPart)
        {
            boolean leftPartCorner = false;
            boolean rightPartCorner = false;
            if(mLandmarks==null)
                return;
            for (Landmark landmark : mLandmarks) {

                if (landmark.getType() == landmark1) {
                    int cx = (int) (landmark.getPosition().x);
                    int cy = (int) (landmark.getPosition().y);
                    ufilyPart.mleftPart.mX = cx;
                    ufilyPart.mleftPart.mY = cy;
                    Log.d(UfilyConstants.APP, "FaceDetector left part landmark:x" + cx + " y:" + cy);
                    leftPartCorner = true;
                    //canvas.drawCircle(cx, cy, 10, paint);
                } else if (landmark.getType() == landmark2) {
                    int cx = (int) (landmark.getPosition().x);
                    int cy = (int) (landmark.getPosition().y);
                    ufilyPart.mRightPart.mX = cx;
                    ufilyPart.mRightPart.mY = cy;
                    Log.d(UfilyConstants.APP, "FaceDetector right Part landmark:x" + cx + " y:" + cy);
                    rightPartCorner = true;
                }

                if (leftPartCorner && rightPartCorner) {
                    break;
                }
            }
        }






        //specific to inherited classes or to be implemented by derived classes
        public  abstract  Uri createGifImage();
        public abstract  Bitmap createGifImagePart(int symbol,int scaling);
        public abstract  Bitmap putOverlay(Bitmap scaledSymbol);

        public  abstract Uri createWebpImage();

    }


    private class LoveEyesUfilyGif extends  UfilyGif
    {
        UfilyPart mUfilyEyes;

        LoveEyesUfilyGif(ExecutorService executor,Activity activity,Bitmap backGroundImage)
        {
            super(executor,activity,backGroundImage);
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
        public    Uri createGifImage(){
            if(mExecutor ==null)
                return null;
            logMessageOnUIThread("LoveEyesUfilyGif::createGifImage()");
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
                logMessageOnUIThread("LoveEyesUfilyGif::while trying to get the result");
            }
            logMessageOnUIThread("LoveEyesUfilyGif::all Gif image parts created");

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

            logMessageOnUIThread("LoveEyesUfilyGif::createWebpImage()");
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
                logMessageOnUIThread("LoveEyesUfilyGif::while trying to get the result");
            }

            logMessageOnUIThread("LoveEyesUfilyGif::all Gif image parts created");

            Bitmap bitmap = gifBitmapList.get(0);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 80, stream);
            byte[] byteArray = stream.toByteArray();

            logMessageOnUIThread("LoveEyesUfilyGif::size of Webp"+byteArray.length/1024+"KB");
            return getLocalImageUri(mActivity,byteArray,".webp");

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


	private class ThugLifeLookUfilyGif extends  UfilyGif
	{
		UfilyPart mUfilyLips;
        UfilyPart mUfilyEyes;
		ThugLifeLookUfilyGif(ExecutorService executor,Activity activity,Bitmap backGroundImage)
		{
			super(executor,activity,backGroundImage);
			mUfilyLips = new UfilyPart();
            mUfilyEyes = new UfilyPart();
			//search part
            searchPart(Landmark.BOTTOM_MOUTH,mUfilyLips);
            searchPart(Landmark.LEFT_EYE,Landmark.RIGHT_EYE,mUfilyEyes);
            addCaptoFace();
		}

        public  Uri createWebpImage()
        {
            if(mExecutor ==null)
                return null;

            logMessageOnUIThread("LoveEyesUfilyGif::createWebpImage()");
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
                logMessageOnUIThread("LoveEyesUfilyGif::while trying to get the result");
            }

            logMessageOnUIThread("LoveEyesUfilyGif::all Gif image parts created");

            Bitmap bitmap = gifBitmapList.get(0);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, stream);
            byte[] byteArray = stream.toByteArray();
            return getLocalImageUri(mActivity,byteArray,".webp");

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
            logMessageOnUIThread("ThugLifeLookUfilyGif::createGifImage()");
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
                logMessageOnUIThread("ThugLifeLookUfilyGif::while trying to get the result");
            }
            logMessageOnUIThread("ThugLifeLookUfilyGif::all Gif image parts created");

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


    private class BlowKissLookUfilyGif extends  UfilyGif
    {
        UfilyPart mUfilyLips;
        BlowKissLookUfilyGif(ExecutorService executor,Activity activity,Bitmap backGroundImage)
        {
            super(executor,activity,backGroundImage);
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
        public    Uri createGifImage(){
            if(mExecutor ==null)
                return null;
            logMessageOnUIThread("BlowKissLookUfilyGif::createGifImage()");
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
                logMessageOnUIThread("BlowKissLookUfilyGif::while trying to get the result");
            }
            logMessageOnUIThread("BlowKissLookUfilyGif::all Gif image parts created");

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

            logMessageOnUIThread("LoveEyesUfilyGif::createWebpImage()");
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
                logMessageOnUIThread("LoveEyesUfilyGif::while trying to get the result");
            }

            logMessageOnUIThread("LoveEyesUfilyGif::all Gif image parts created");

            Bitmap bitmap = gifBitmapList.get(0);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, stream);
            byte[] byteArray = stream.toByteArray();
            return getLocalImageUri(mActivity,byteArray,".webp");

        }


    }

    private class DevilLookUfilyGif extends  UfilyGif
    {
        UfilyPart mUfilyLips;
        UfilyPart mUfilyCheeks;
        UfilyPart mUfilyEyes;


        DevilLookUfilyGif(ExecutorService executor,Activity activity,Bitmap backGroundImage)
        {
            super(executor,activity,backGroundImage);
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
        public    Uri createGifImage(){
            if(mExecutor ==null)
                return null;
            logMessageOnUIThread("LoveEyesUfilyGif::createGifImage()");
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
                logMessageOnUIThread("LoveEyesUfilyGif::while trying to get the result");
            }
            logMessageOnUIThread("LoveEyesUfilyGif::all Gif image parts created");

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

            logMessageOnUIThread("LoveEyesUfilyGif::createWebpImage()");
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
                logMessageOnUIThread("LoveEyesUfilyGif::while trying to get the result");
            }

            logMessageOnUIThread("LoveEyesUfilyGif::all Gif image parts created");

            Bitmap bitmap = gifBitmapList.get(0);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, stream);
            byte[] byteArray = stream.toByteArray();
            return getLocalImageUri(mActivity,byteArray,".webp");

        }



    }


    private class KissingLipsUfilyGif extends  UfilyGif
    {
        UfilyPart mUfilyCheeks;
        KissingLipsUfilyGif(ExecutorService executor,Activity activity,Bitmap backGroundImage)
        {
            super(executor,activity,backGroundImage);
            mUfilyCheeks = new UfilyPart();
            //search par
            searchPart(Landmark.LEFT_CHEEK,Landmark.RIGHT_CHEEK,mUfilyCheeks);
        }


        @Override
        public Bitmap createGifImagePart(int symbol,int scaling)
        {
            int scaledWidth;
            int scaledHeight;

            //check the distance between lip corners
            scaledWidth = mBackGroundImage.getWidth()/6;

            //get the image aspect ratio
            Bitmap kissImage = BitmapFactory.decodeResource(
                    mActivity.getApplicationContext().getResources(), R.drawable.kisses1);

            double aspectRatio = (double)kissImage.getHeight()/kissImage.getWidth();
            scaledHeight = (int) (aspectRatio *scaledWidth);

            Bitmap KissingLipsImage =
                    Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                            mActivity.getApplicationContext().getResources(),symbol),
                            scaledWidth,
                            scaledHeight,true);
            Log.d(UfilyConstants.APP,"");


            //Bitmap blowKissImage = BitmapFactory.decodeResource(
            //      mActivity.getApplicationContext().getResources(), symbol);

            return putOverlay(KissingLipsImage);
        }


        @Override
        public    Uri createGifImage(){
            if(mExecutor ==null)
                return null;
            logMessageOnUIThread("KissingLipsUfilyGif::createGifImage()");
            List<Future<Bitmap>> gifPartTaskList = new ArrayList<Future<Bitmap>>();
            for(int count = 0; count < UfilyConstants.UFILY_KISSINGLIPS_SYMBOL_ARRAY.length;count++)
            {
                gifPartTaskList.add(mExecutor.submit(
                        new GifPart(count,
                                this,
                                UfilyConstants.UFILY_KISSINGLIPS_SYMBOL_ARRAY[count],0
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
                logMessageOnUIThread("KissingLipsUfilyGif::while trying to get the result");
            }
            logMessageOnUIThread("KissingLipsUfilyGif::all Gif image parts created");

            return combineBitmapToGif(gifBitmapList,500);
        }


        @Override
        public Bitmap putOverlay(Bitmap overlay)
        {
            Bitmap newBitmap = Bitmap.createBitmap(mBackGroundImage);
            Canvas canvas = new Canvas(newBitmap);
            Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
            paint.setAlpha(255);
            //check for  detected or not
            if(mUfilyCheeks.isLeftPartDetected()) {

                Log.d(UfilyConstants.APP,"BlowKissLookUfilyGif::putOverlay center detected");

                int overlayX = mUfilyCheeks.mleftPart.mX;
                int overlayY = mUfilyCheeks.mleftPart.mY;

                //canvas.drawCircle(overlayX,overlayY,10,new Paint(Color.GREEN));
                //should be at the centre of lips
                overlayY = overlayY - overlay.getHeight()/2;
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

            logMessageOnUIThread("LoveEyesUfilyGif::createWebpImage()");
            List<Future<Bitmap>> gifPartTaskList = new ArrayList<Future<Bitmap>>();
            for(int count = 0; count < 1;count++)
            {
                gifPartTaskList.add(mExecutor.submit(
                        new GifPart(count,
                                this,
                                UfilyConstants.UFILY_KISSINGLIPS_SYMBOL_ARRAY[count],
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
                logMessageOnUIThread("LoveEyesUfilyGif::while trying to get the result");
            }

            logMessageOnUIThread("LoveEyesUfilyGif::all Gif image parts created");

            Bitmap bitmap = gifBitmapList.get(0);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, stream);
            byte[] byteArray = stream.toByteArray();
            return getLocalImageUri(mActivity,byteArray,".webp");

        }


    }





    private class GifPart implements Callable<Bitmap>
    {
        //@TODO symbols to be part of the class
        private int mId;
        private UfilyGif mUfilyGif;
        private  int mSymbol;
        private int mScaling;
        public GifPart(int id,UfilyGif ufilyGif,int symbol,int scaling)
        {
            mId = id;
            mUfilyGif = ufilyGif;
            mSymbol = symbol;
            mScaling = scaling;
        }
        @Override
        public Bitmap call() throws Exception{

            //logMessageOnUIThread("GifPart:: "+mId+" started");
            Bitmap gifPartImage = mUfilyGif.createGifImagePart(mSymbol,mScaling);
            //logMessageOnUIThread("GifPart:: "+mId+ " complete");
            return gifPartImage;
        }

    }








}
