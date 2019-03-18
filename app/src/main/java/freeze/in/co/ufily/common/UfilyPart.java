package freeze.in.co.ufily.common;

/**
 * Created by rtiragat on 11/11/2016.
 */
public class UfilyPart {

    public Coordinates mleftPart;
    public Coordinates mRightPart;
    public Coordinates mCenterPart;

    UfilyPart()
    {
        mleftPart = new Coordinates(-1,-1);
        mRightPart = new Coordinates(-1,-1);
        mCenterPart = new Coordinates(-1,-1);
    }

    //better approach dont allocate the object
    public  boolean isLeftPartDetected()
    {
        return ((mleftPart.mX != -1) && (mleftPart.mY != -1));
    }


    public  boolean isCenterPartDetected()
    {
        return ((mCenterPart.mX != -1) && (mCenterPart.mY != -1));
    }

    public  boolean iRightPartDetected()
    {
        return ((mRightPart.mX != -1) && (mRightPart.mY != -1));
    }
}

