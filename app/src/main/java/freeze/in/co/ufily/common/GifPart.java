package freeze.in.co.ufily.common;

import android.graphics.Bitmap;

import java.util.concurrent.Callable;

/**
 * Created by rtiragat on 4/15/2019.
 */

class GifPart implements Callable<Bitmap>
{
    //@TODO symbols to be part of the class
    private int mId;
    private UfilyGif mUfilyGif;
    private  int mSymbol;
    private int mScaling;
    GifPart(int id,UfilyGif ufilyGif,int symbol,int scaling)
    {
        mId = id;
        mUfilyGif = ufilyGif;
        mSymbol = symbol;
        mScaling = scaling;
    }
    @Override
    public Bitmap call() throws Exception{
        //logMessageOnUIThread("GifPart:: "+mId+" started");
         return mUfilyGif.createGifImagePart(mSymbol,mScaling);
        //logMessageOnUIThread("GifPart:: "+mId+ " complete");
    }

}
