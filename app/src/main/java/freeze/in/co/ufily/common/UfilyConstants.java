package freeze.in.co.ufily.common;

import freeze.in.co.ufily.R;

/**
 * Created by rtiragat on 9/20/2015.
 */



 public class UfilyConstants {
     public static String CACHE = "UfilyCache";


     public static  String RECENTLY_CREATED_UFILY="Recently_Created_Ufily.jpg";
     public static String APP = "UFILY";

     public static String SHARE_UFILY = "Share Ufily";

     public static int UFILY_REQUEST_CAMERA=1000;
     public static int UFILY_SELECT_FILE=1001;

    public static final int UFILY_DATABASE_VERSION = 2;
    // Database Name
    public static final String UFILY_DATABASE_NAME = "ufiles.db";
    // Contacts table name
    public static final String UFILY_TABLE_CONTACTS = "ufilycontacts";


    // Contacts Table Columns names
    public static final String UFILY_KEY_ID = "id";
    public static final String UFILY_KEY_NAME = "name";
    public static final String UFILY_KEY_IMAGE = "image";
    public static final String UFILY_KEY_IMAGE1 = "image_png";

    public static final String UFILY_IMG_TAG ="ufily_imagetag";


    public static final int UFILY_LOG_MESSAGE_ID = 10000;

    public static final String UFILY_LOG_MESSAGE_TAG = "UFILY_LOG_MESSAGE_TAG";

    public static final int UFILY_LOVE_SYMBOL_ARRAY[] = {R.drawable.frame0,
                                                        R.drawable.frame1,
                                                        R.drawable.frame2,
                                                        R.drawable.frame3,
                                                        R.drawable.frame4,
                                                        R.drawable.frame5};

    public static final int UFILY_FIRE_SYMBOL_ARRAY[] = {R.drawable.fire0,
                                                        R.drawable.fire1,
                                                        R.drawable.fire2,
                                                        R.drawable.fire3,
                                                        R.drawable.fire4,
                                                         R.drawable.fire5};


    public static final int UFILY_THUGLIFE_SYMBOL_ARRAY[] = {R.drawable.cigar0,
            R.drawable.cigar1,
            R.drawable.cigar2,
            R.drawable.cigar3,
            R.drawable.cigar4,
            R.drawable.cigar5,
            R.drawable.cigar6,
            R.drawable.cigar7,
            R.drawable.cigar8,
    };


    public static final int UFILY_BLOWKISS_SYMBOL_ARRAY[] = {R.drawable.blowkiss0,
            R.drawable.blowkiss1,
            R.drawable.blowkiss2,
            R.drawable.blowkiss3,
            R.drawable.blowkiss4,
            R.drawable.blowkiss5,
            R.drawable.blowkiss6,
            R.drawable.blowkiss7,
    };


    public static final int UFILY_KISSINGLIPS_SYMBOL_ARRAY[] = {R.drawable.kisses1,
            R.drawable.kisses2,
    };



    public static enum UFILY_SMILEY {
        UFILY_LOVE_EYES,UFILY_DEVIL_LOOK,UFILY_THUGLIFE_LOOK,UFILY_BLOWKISS_LOOK,UFILY_KISSING_LIPS
    }


    public static final int UFILY_SHARE_GIF_TAG = 10001;

    public static final String CONTENT_PROVIDER_AUTHORITY  = "freeze.in.co.ufily.stickercontentprovider";

    public static final String EXTRA_PACK_IDENTIFIER  = "freeze.in.co.ufily.EXTRA_IDENTIFIER";

    public static final String EXTRA_PACK_NAME  = "freeze.in.co.ufily.EXTRA_PACK_NAME";





}
