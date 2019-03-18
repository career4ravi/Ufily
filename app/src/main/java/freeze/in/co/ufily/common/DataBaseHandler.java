package freeze.in.co.ufily.common;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by rtiragat on 9/25/2016.
 */
public class DataBaseHandler extends SQLiteOpenHelper {


    SQLiteDatabase mDb;
    ArrayList <Sticker> mStickerList;

    private static DataBaseHandler single_instance = null;

    // static method to create instance of Singleton class
    public static DataBaseHandler getDBInstance() {
        return single_instance;
    }

    public static  void initializeDb(Context context)
    {
         single_instance = new DataBaseHandler(context);
    }


    public DataBaseHandler(Context context)
    {
      super(context,UfilyConstants.UFILY_DATABASE_NAME,null,UfilyConstants.UFILY_DATABASE_VERSION);
      mStickerList = new ArrayList<>();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(UfilyConstants.APP, this.getClass() + "OnCreate()");
        mDb = db;
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + UfilyConstants.UFILY_TABLE_CONTACTS + "("
                + UfilyConstants.UFILY_KEY_ID + " INTEGER PRIMARY KEY," + UfilyConstants.UFILY_KEY_NAME + " TEXT,"
                + UfilyConstants.UFILY_KEY_IMAGE + " TEXT," + UfilyConstants.UFILY_KEY_IMAGE1 + " TEXT" + ");";

        Log.d(UfilyConstants.APP, this.getClass()+ "OnCreate():"+ CREATE_CONTACTS_TABLE);

         db.execSQL(CREATE_CONTACTS_TABLE);
    }


    public boolean isOpen()
    {
        return mDb.isOpen();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        Log.d(UfilyConstants.APP,"DataBaseHandler::onUpgrade"+"old:"+oldVersion+"new:"+newVersion);

        db.execSQL("DROP TABLE IF EXISTS " + UfilyConstants.UFILY_TABLE_CONTACTS);        // Create tables again
        onCreate(db);
    }

    public Ufily getUfily(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        if(db == null)
            return null;

        Cursor cursor = db.query(UfilyConstants.UFILY_TABLE_CONTACTS, new String[] { UfilyConstants.UFILY_KEY_ID,
                        UfilyConstants.UFILY_KEY_NAME, UfilyConstants.UFILY_KEY_IMAGE,UfilyConstants.UFILY_KEY_IMAGE1 }, UfilyConstants.UFILY_KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);


        if (cursor != null)
            cursor.moveToFirst();
        else
            return null;

        Ufily ufily = new Ufily(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),cursor.getString(3));

        db.close();
        return ufily;
    }

    public Ufily getUfilyByName(String imageName)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        if(db == null)
            return null;

        Cursor cursor = db.query(UfilyConstants.UFILY_TABLE_CONTACTS, new String[] { UfilyConstants.UFILY_KEY_ID,
                        UfilyConstants.UFILY_KEY_NAME, UfilyConstants.UFILY_KEY_IMAGE,UfilyConstants.UFILY_KEY_IMAGE1 }, UfilyConstants.UFILY_KEY_NAME + "=?",
                new String[] { imageName }, null, null, null, null);


        if (cursor != null)
            cursor.moveToFirst();
        else
            return null;

        Ufily ufily = new Ufily(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),cursor.getString(3));

        db.close();
        return ufily;
    }

    public List<Ufily> getAllUfily()
    {

        List<Ufily> ufilyList = new ArrayList<Ufily>();
        // Select All Query
        String selectQuery = "SELECT * FROM "+ UfilyConstants.UFILY_TABLE_CONTACTS + " ORDER BY id";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if ((cursor != null)  && cursor.moveToFirst()) {
            do {
                Ufily ufily = new Ufily();
                ufily.setId(Integer.parseInt(cursor.getString(0)));
                ufily.setName(cursor.getString(1));
                ufily.setImagePath(cursor.getString(2));
                ufily.setImagePath1(cursor.getString(3));
                // Adding contact to list
                ufilyList.add(ufily);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // close inserting data from database
        db.close();
        return ufilyList;
    }

    public boolean addUfily(Ufily ufily)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        if(db==null)
            return false;

        ContentValues values = new ContentValues();
        values.put(UfilyConstants.UFILY_KEY_ID,ufily.getId());
        values.put(UfilyConstants.UFILY_KEY_NAME, ufily.getName()); //
        values.put(UfilyConstants.UFILY_KEY_IMAGE, ufily.getImagePath()); //
        values.put(UfilyConstants.UFILY_KEY_IMAGE1, ufily.getImagePath1());


        mStickerList.add(new Sticker(ufily.getName(),null));
         // Inserting Row
        db.insert(UfilyConstants.UFILY_TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection

        return true;
    }
    //not required as of now
   public boolean updateUfily(Ufily ufily)
    {
        return true;
    }

    public boolean deleteUfily(Ufily ufily)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        if(db == null)
            return false;

        mStickerList.clear();
        db.delete(UfilyConstants.UFILY_TABLE_CONTACTS, UfilyConstants.UFILY_KEY_ID + " = ?",
                new String[]{String.valueOf(ufily.getId())});
        db.close();

        //TODO create a separate thread to handle this
        new GifGenerateTask().execute(Uri.parse(ufily.getImagePath()),Uri.parse(ufily.getImagePath1()));



        return true;
    }


    private class GifGenerateTask extends AsyncTask<Object,Void,List<Uri>> {

        private ProgressDialog nDialog;

        protected List<Uri> doInBackground(Object... object) {
            List<Uri> uriList = new ArrayList<>();

            Uri gifPath = (Uri)object[0];
            Uri imagePath = (Uri)object[1];

        File deleteFile = new File(gifPath.getPath());

        if(deleteFile.delete()) {
            uriList.add(gifPath);
        }
        deleteFile = new File(imagePath.getPath());
        if(deleteFile.delete()) {
            uriList.add(imagePath);
        }
            return uriList;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        protected void onPostExecute(List<Uri> result) {
           Log.d(UfilyConstants.APP,"Deleted files");
        }



    }

    public int getUfilyCount()
    {
        String countQuery = "SELECT * FROM " + UfilyConstants.UFILY_TABLE_CONTACTS;
        SQLiteDatabase db = this.getWritableDatabase();
        int count =0;
        if(db.isOpen()) {
            Cursor cursor = db.rawQuery(countQuery, null);
            count = cursor.getCount();
            cursor.close();
            db.close();
        }
        return count;
    }

    public List<Sticker> getStickerList(){  return mStickerList;}


}
