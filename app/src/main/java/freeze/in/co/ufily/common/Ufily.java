package freeze.in.co.ufily.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by rtiragat on 9/25/2016.
 */

//Ufily means your face smiley. it represents a object or a persons custom made smiley
//Ufily contains person name/user name who created this ufily,the associated image,type of emoticon.
public class Ufily {

    private int mId;
    private String mName;
    private String mImagePath;
    private String mImagePath1;
    private Bitmap mImage;

    public Ufily()
    {

    }

    Ufily(int mid,String name,String ImagePath,String ImagePath1)
    {
        mId = mid;
        mName = name;
        mImagePath = ImagePath;
        mImagePath1 = ImagePath1;
    }

    Ufily(int mid,String name,byte[] imagebytes)
    {
        mId = mid;
        mName = name;
        mImage = convertByteToBitmap(imagebytes);
    }

    public int getId()
    {
        return mId;
    }

    public String getName()
    {
          return mName;
    }


    public Bitmap getImage()
    {
         return mImage;
    }

    public String getImagePath()
    {
        return mImagePath;
    }

    public String getImagePath1()
    {
        return mImagePath1;
    }


    public byte[] getImageBytes()
    {
        return convertBitmapToByte(mImage);
    }

    public void setId(int id)
    {
       mId = id;
    }

    public void setName(String name)
    {
        mName = name;
    }

    public void setImage(byte[] imageArray)
    {
        mImage =  convertByteToBitmap(imageArray);
    }

    public void setImage(Bitmap image)
    {
       mImage =  image;
    }


    public void setImagePath(String imagePath)
    {
        mImagePath =  imagePath;
    }


    public void setImagePath1(String imagePath1)
    {
        mImagePath1 =  imagePath1;
    }

    private byte[] convertBitmapToByte(Bitmap bitmap)
    {
        // convert bitmap to byte
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte imageInByte[] = stream.toByteArray();
        return imageInByte;
    }


    private Bitmap convertByteToBitmap(byte[] byteArray)
    {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}
