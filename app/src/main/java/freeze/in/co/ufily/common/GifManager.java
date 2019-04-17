package freeze.in.co.ufily.common;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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


    static public Uri getLocalImageUri(Activity activities, byte[] aInput, String imageType)
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


}
