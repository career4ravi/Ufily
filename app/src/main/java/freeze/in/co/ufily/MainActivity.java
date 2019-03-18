package freeze.in.co.ufily;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Random;

import freeze.in.co.ufily.common.FileHelper;
import freeze.in.co.ufily.common.FragmentChangeListener;
import freeze.in.co.ufily.common.Ufily;
import freeze.in.co.ufily.common.UfilyConstants;
import freeze.in.co.ufily.slidingtab.SlidingTabLayout;

import static freeze.in.co.ufily.common.DataBaseHandler.getDBInstance;


public class MainActivity extends AppCompatActivity implements FragmentChangeListener{

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */


    ViewPager mViewPager;
    UfilyPagerAdapter mUfilyPagerAdapter;
    SlidingTabLayout mTabs;

    private static Context mContext;

    //becareful not access this beforew creation of the context
    public static Context getGlobalContext()
    {
        return mContext;
    }


    /*static {
        System.loadLibrary("webp");
    }*/




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(UfilyConstants.APP, "OnCreate()");

        mContext = getApplicationContext();
        FileHelper.getFileHelper().initFileHelper(getApplicationContext());
        //setting the viewpager with the different fragment to display
        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) findViewById(R.id.ufily_pager);

        mUfilyPagerAdapter = new UfilyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mUfilyPagerAdapter);
        // Specify that tabs should be displayed in the action bar.

        //use slidingtablayout
        // Assiging the Sliding Tab Layout View
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        mTabs.setViewPager(mViewPager);






    }

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        Log.d(UfilyConstants.APP, "OnStart()");
        verifyStoragePermissions(this);
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {

            String[] string  = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity, string,
                    33);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(UfilyConstants.APP, "OnResume()");
        // The activity has become visible (it is now "resumed").
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(UfilyConstants.APP, "OnPause()");
        // Another activity is taking focus (this activity is about to be "paused").
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(UfilyConstants.APP, "OnStop()");
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(UfilyConstants.APP, "OnDestroyed()");
        // The activity is about to be destroyed.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onUfilyEditingDone(Uri gifUri,Uri imageUri){

        Log.d(UfilyConstants.APP,"GIf path:"+gifUri.getPath());
        Log.d(UfilyConstants.APP,"Image path:"+imageUri.getPath());

        final List<String> pathSegments = imageUri.getPathSegments();
        if (pathSegments.size() < 1) {
            throw new IllegalArgumentException("path segments should be 3, uri is: " + imageUri);
        }
        String fileName = pathSegments.get(pathSegments.size() - 1);

        if((gifUri == null) || (imageUri == null) )
        {
            return;
        }
        Toast.makeText(getApplicationContext(), "Sharing Ufily!", Toast.LENGTH_SHORT).show();

        Ufily ufily = new Ufily();
        File file = new File(gifUri.getPath());
        if(file.exists())
        {
            Log.d(UfilyConstants.APP,"fileName:"+fileName);
            Random rand = new Random();
            long  selected = rand.nextInt(100000);
            ufily.setId((int)selected);
            ufily.setName(fileName);
            ufily.setImagePath(gifUri.getPath());
            ufily.setImagePath1(imageUri.getPath());
            getDBInstance().addUfily(ufily);
        }
    }









    public void shareGif(Uri gifUri)
    {
        if(gifUri == null)
        {
            return;
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, gifUri);
        startActivity(Intent.createChooser(shareIntent, UfilyConstants.SHARE_UFILY));
    }


    private void shareMp4(Uri mp4Uri)
    {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("video/mp4");
        shareIntent.putExtra(Intent.EXTRA_STREAM, mp4Uri);
        startActivity(Intent.createChooser(shareIntent, UfilyConstants.SHARE_UFILY));
    }

    public void shareUfily(Bitmap bitmap)
    {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        //shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
        shareIntent.setType("image/gif");
        startActivity(Intent.createChooser(shareIntent, UfilyConstants.SHARE_UFILY));
    }















}
