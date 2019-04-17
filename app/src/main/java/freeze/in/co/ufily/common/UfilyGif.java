package freeze.in.co.ufily.common;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.vision.face.Landmark;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by rtiragat on 4/15/2019.
 */

abstract class UfilyGif
{
    private ExecutorService mExecutor;
    protected Activity mActivity;
    protected Bitmap mBackGroundImage;
    protected List<Landmark> mLandmarks;
    UfilyGif(ExecutorService executor,Activity activity,Bitmap backGroundImage)
    {
        mExecutor = executor;
        mActivity = activity;
        mBackGroundImage = backGroundImage;
        mLandmarks = FaceDetector1.getAllLandmarks(mActivity,mBackGroundImage);
    }

    protected Uri combineBitmapToGif(List<Bitmap> gifBitmapList, int delay)
    {
        //logMessageOnUIThread("UfilyGif::all Gif image parts created");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(bos);
        encoder.setDelay(delay);

        //logMessageOnUIThread("UfilyGif::started creating Gif");
        for (Bitmap frame : gifBitmapList) {
            if(frame!=null) {
                encoder.addFrame(frame);
                //logMessageOnUIThread("UfilyGif::added frame to Gif");
                encoder.setTransparent(1);
            }
        }
        encoder.finish();
        //logMessageOnUIThread("UfilyGif::finished creating Gif");

        return getLocalGifUri(mActivity,bos.toByteArray(),".gif");
    }

    //to be moved to ImageHelper Class
    private Uri getLocalGifUri(Activity activities, byte[] aInput,String imageType)
    {
        //logMessageOnUIThread("getLocalGifUri");

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
                GifManager.getInstance().logMessageOnUIThread("no uri");
            }
            else {
                GifManager.getInstance().logMessageOnUIThread(gifUri.getPath());

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
