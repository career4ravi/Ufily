package freeze.in.co.ufily.common;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by rtiragat on 9/20/2015.
 */
public class FileHelper {

    private Context mContext;
    private static FileHelper  mFileHelper=new FileHelper();
    private String mRecentUfilyPath;

    static public FileHelper getFileHelper()
    {
        return mFileHelper;
    }

    public void initFileHelper(Context context)
    {
      if(context != null) {
          mContext= context;
      }

    }

    private static void determineRightUfilySize(Context context)
    {
        if(context!=null)
        {
            float density = context.getResources().getDisplayMetrics().density;
            Log.d(UfilyConstants.APP,"Phone pixel density:"+density);
            if(density >= 4.0f)
            {
                Log.d(UfilyConstants.APP,"Density:"+"xxxhdpi");
            }
            else if(density >= 3.0f)
            {
                Log.d(UfilyConstants.APP,"Density:"+"xxhdpi");
            }
            else if(density >= 2.0f)
            {
                Log.d(UfilyConstants.APP,"Density:"+"xhdpi");
            }
            else if(density >= 1.5f)
            {
                Log.d(UfilyConstants.APP,"Density:"+"hdpi");
            }
            else if(density >= 1.0f)
            {
                Log.d(UfilyConstants.APP,"Density:"+"mdpi");
            }
            else if(density >= 0.75f)
            {
                Log.d(UfilyConstants.APP,"Density:"+"ldpi");
            }
            else
            {
                Log.d(UfilyConstants.APP,"Density:"+"unknown or very less");
            }

        }

    }



    //determine the resolution of the phone and decide the image size.
    public void writeImage(Bitmap image)
    {
        FileOutputStream outputStream= null;

        try {

            ContextWrapper cw = new ContextWrapper(mContext);
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            mRecentUfilyPath = directory.getAbsolutePath();
            File mypath=new File(directory,UfilyConstants.RECENTLY_CREATED_UFILY);
            outputStream = new FileOutputStream(mypath);
            image.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            Log.d(UfilyConstants.APP, getClass().getSimpleName() + ":" + "writing image to " + mypath.getAbsolutePath()+"of bytes"+image.getByteCount());

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }


    public Bitmap readImage()
    {
        try {
             File file = new File(mRecentUfilyPath,UfilyConstants.RECENTLY_CREATED_UFILY);
             Log.d(UfilyConstants.APP, getClass().getSimpleName() + ":" + "reading image from"+file.getAbsoluteFile());
             return BitmapFactory.decodeStream(new FileInputStream(file));
            }

         catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
