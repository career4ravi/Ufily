package freeze.in.co.ufily;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.felipecsl.gifimageview.library.GifImageView;
import com.google.android.gms.vision.face.Face;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import freeze.in.co.ufily.common.FaceDetectView;
import freeze.in.co.ufily.common.FaceDetector1;
import freeze.in.co.ufily.common.FragmentChangeListener;
import freeze.in.co.ufily.common.GifManager;
import freeze.in.co.ufily.common.Ufily;
import freeze.in.co.ufily.common.UfilyConstants;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.DIRECTORY_PICTURES;
import static freeze.in.co.ufily.common.DataBaseHandler.getDBInstance;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 *
 * interface.
 */
public class UfilyFunnyFaceMaker extends Fragment implements View.OnTouchListener,View.OnClickListener,GifImageView.OnAnimationStop {

    @Override
    public void onAnimationStop() {
        Log.d(UfilyConstants.APP,"OnAnimation Stopped");
        //mGifImageView.stopAnimation();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UfilyFunnyFaceMaker() {
    }

    private FragmentChangeListener mActivityCallBack;
    private boolean isGifInProgress;
    private Bitmap mBackGroundImage;
    private SparseArray<Face> mFaces;
    FaceDetectView mFaceDetectView;
    String mCurrentPhotoPath;


    private ImageButton mCameraButton;
    private ImageButton mGalleryButton;
    private  ImageButton mShareButon;

    //should be moved to array or convert them to horzonatal list
    private RadioButton mLoveEyesButton;
    private RadioButton mDevilLookButton;
    private RadioButton mThugLifeLookButton;
    private RadioButton mBlowKissLookButton;
    private RadioButton mKissLookButton;


    GifImageView mGifImageView;
    FrameLayout    mFrameLayoutSemiTransparent;

    private boolean mIsImageLoadComplete;
    private boolean mIsVisibleToUser;



    UfilyConstants.UFILY_SMILEY mUserUfilyTypeSelected;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(UfilyConstants.APP,"UfilyFamousFragment::onCreate()");
        super.onCreate(savedInstanceState);
        mBackGroundImage = null;
        mFaces = null;
        mIsImageLoadComplete = false;
        mGifImageView = null;
        mActivityCallBack = (FragmentChangeListener) getActivity();
        mUserUfilyTypeSelected = UfilyConstants.UFILY_SMILEY.UFILY_LOVE_EYES;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
        showFacesInImage();
        if(mGifImageView != null && mGifImageView.getVisibility() == View.VISIBLE)
        {
            if(!mGifImageView.isAnimating()) {
                mGifImageView.startAnimation();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(UfilyConstants.APP,"UfilyFamousFragment::onCreateView()");
        isGifInProgress = false;
        View view = inflater.inflate(R.layout.fragment_ufily_famous, container, false);
        mFaceDetectView = (FaceDetectView)view.findViewById(R.id.faceDetectView);
        mFaceDetectView.setOnTouchListener(this);


        mGifImageView = (GifImageView) view.findViewById(R.id.gifImageView);
        mFrameLayoutSemiTransparent = (FrameLayout) view.findViewById(R.id.framelLayoutSemiTransparent);

        mCameraButton= (ImageButton)view.findViewById(R.id.CameraButton);
        mGalleryButton =(ImageButton)view.findViewById(R.id.GalleryButton);
        mShareButon = (ImageButton)view.findViewById(R.id.shareButton);
        mLoveEyesButton = (RadioButton) view.findViewById(R.id.radio_love_eyes);
        mDevilLookButton = (RadioButton) view.findViewById(R.id.radio_devil_look);
        mThugLifeLookButton = (RadioButton) view.findViewById(R.id.radio_thug_life);
        mBlowKissLookButton = (RadioButton) view.findViewById(R.id.radio_blow_kiss);
        mKissLookButton = (RadioButton) view.findViewById(R.id.radio_kiss);

        mLoveEyesButton.setOnClickListener(this);
        mDevilLookButton.setOnClickListener(this);
        mThugLifeLookButton.setOnClickListener(this);
        mBlowKissLookButton.setOnClickListener(this);
        mKissLookButton.setOnClickListener(this);

        mCameraButton.setOnClickListener(this);
        mGalleryButton.setOnClickListener(this);
        mShareButon.setOnClickListener(this);
        mGifImageView.setOnTouchListener(this);
        mGifImageView.setOnAnimationStop(this);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(UfilyConstants.APP,"UfilyFamousFragment::onResume()");
        showFacesInImage();
		if(mGifImageView.getVisibility() == View.VISIBLE)
		{
			if(!mGifImageView.isAnimating()) {
                    mGifImageView.startAnimation();
                }
			
		}
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {


        Log.d(UfilyConstants.APP,"onTouch: view id:"+v.getId());

        if(v.getId() == R.id.faceDetectView)
        {
            //ignore if image not loaded yet.
            if(event.getAction() == MotionEvent.ACTION_DOWN && mIsImageLoadComplete && mIsVisibleToUser)
            {
                FaceDetectView faceDetectView = (FaceDetectView)v;
                Face face = detectSelectedFace(faceDetectView.getMappedFaces(),v,event,faceDetectView.getScale());
                if(face!=null)
                    Log.d(UfilyConstants.APP, "Face width:" + face.getWidth() + " Face Height:" + face.getHeight());

                if((face!=null) && !isGifInProgress) {

                    new GifGenerateTask().execute(faceDetectView.getBitmap(),face);
                }
                return true;
            }
        }
        else if(v.getId() == R.id.WatsAppButtonView)
        {

            Uri contentUri = Uri.parse("content://freeze.in.co.ufily.stickercontentprovider");

            Cursor cursor = getActivity().getContentResolver().query(contentUri , null, null, null, null);
            if(cursor!=null)
            {

                Log.d(UfilyConstants.APP,"Inside cursor for sticker provdider");
                cursor.moveToFirst();
                // Loop in the cursor to get each row.
                do{

                    String[] columnNames = cursor.getColumnNames();
                    for (int i = 0; i < columnNames.length; i++) {
                        int column1Index= cursor.getColumnIndex(columnNames[i]);
                        String column1Value= cursor.getString(column1Index);
                        Log.d(UfilyConstants.APP,"ColumnValue:"+column1Value);
                    }
                    // Get column 1 value.
                }while(cursor.moveToNext());
            }

            Intent intent = new Intent(getActivity(), AddStickerPackActivity.class);
            //TODO
            //List<StickerPack> sticker = ((MainActivity)getActivity()).getDatabaseHandler().getStickerList();
            intent.putExtra(UfilyConstants.EXTRA_PACK_IDENTIFIER,"face_smile_identifier");
            intent.putExtra(UfilyConstants.EXTRA_PACK_NAME,"Ravi");
            startActivity(intent);

        }
        else
        {
            if(mGifImageView.getVisibility() == View.VISIBLE)
            {
                mFrameLayoutSemiTransparent.setVisibility(View.INVISIBLE);

                if(mGifImageView.isAnimating()) {
                    mGifImageView.stopAnimation();
                }
                mGifImageView.setVisibility(View.INVISIBLE);
                mShareButon.setVisibility(View.INVISIBLE);
            }

        }



        return false;
    }



    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.GalleryButton:
                selectImage();
				mIsImageLoadComplete = false;
                break;
            case R.id.CameraButton:
                mIsImageLoadComplete = false;
                dispatchTakePictureIntent();
                break;
            case R.id.shareButton:

                if(mGifImageView.isAnimating())
                {
                    mGifImageView.stopAnimation();
                }
                Log.d(UfilyConstants.APP,"onClick::shareButton");
                //@TODO do we need to hide  the GIfImageView before sharing ?
                Uri shareUri = (Uri)(mGifImageView.getTag());
//                (MainActivity)getActivity().shareGif(shareGif);
                break;

            case R.id.radio_devil_look:
                Log.d(UfilyConstants.APP,"clicked on devil look");
                if(((RadioButton)v).isChecked())
                {
                    mUserUfilyTypeSelected = UfilyConstants.UFILY_SMILEY.UFILY_DEVIL_LOOK;
                }
                break;
            case R.id.radio_love_eyes:
                Log.d(UfilyConstants.APP,"clicked on  love eyes");
                if(((RadioButton)v).isChecked())
                {
                    mUserUfilyTypeSelected = UfilyConstants.UFILY_SMILEY.UFILY_LOVE_EYES;
                }
                break;
            case R.id.radio_thug_life:
                Log.d(UfilyConstants.APP,"clicked on  thug life");
                if(((RadioButton)v).isChecked())
                {
                    mUserUfilyTypeSelected = UfilyConstants.UFILY_SMILEY.UFILY_THUGLIFE_LOOK;
                }
                break;
            case R.id.radio_blow_kiss:
                Log.d(UfilyConstants.APP,"clicked on  blowkiss");
                if(((RadioButton)v).isChecked())
                {
                    mUserUfilyTypeSelected = UfilyConstants.UFILY_SMILEY.UFILY_BLOWKISS_LOOK;
                }
                break;
            case R.id.radio_kiss:
                Log.d(UfilyConstants.APP,"clicked on  kissing lips");
                if(((RadioButton)v).isChecked())
                {
                    mUserUfilyTypeSelected = UfilyConstants.UFILY_SMILEY.UFILY_KISSING_LIPS;
                }

            default:
                break;
        }
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(UfilyConstants.APP, "on Activity result of edit fragment requestcode:"+requestCode);

        //if user cancels the selctions
        if(resultCode != RESULT_OK)
            return;

        if (requestCode == UfilyConstants.UFILY_SELECT_FILE) {

            Log.d(UfilyConstants.APP, "in the section request gallery access");

            if(data == null)
                return;

            Uri selectedImageUri = data.getData();

            if(selectedImageUri!= null) {
                Log.d(UfilyConstants.APP, "got the image uri:"+selectedImageUri.getPath());
                //DetectFaceTask detectFaceTask = new DetectFaceTask();
                //detectFaceTask.execute(selectedImageUri);
                loadImageWithGlide(selectedImageUri);

            }

        }
        else if(requestCode == UfilyConstants.UFILY_REQUEST_CAMERA)
        {
            Log.d(UfilyConstants.APP,"Request camera success");
            galleryAddPic();
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            loadImageWithGlide(contentUri);

        }
    }

    private void loadImageWithGlide(Uri selectedImageUri)
    {
        Glide.with(getActivity().getApplicationContext())
                .load(selectedImageUri)    // you can pass url too
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        // you can do something with loaded bitmap here
                        Log.d(UfilyConstants.APP, "Loaded image from Gallery");
                        mBackGroundImage = resource;
                        mIsImageLoadComplete = true;

                        FaceDetector1 faceDetector1 = new FaceDetector1(getActivity());
                        if(mBackGroundImage!=null)
                            mFaces = faceDetector1.createFaceMap(mBackGroundImage);

                        showFacesInImage();
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        Log.d(UfilyConstants.APP, "Loaded failed"+e.getMessage());
                        //TODO pop up a dialog message
                    }
                });

    }

    private Face detectSelectedFace(SparseArray<Face> faces,View v,MotionEvent event,double scale)
    {

        if(faces == null)
            return null;

        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.valueAt(i);
            
            float startX = (float)((face.getPosition().x) * scale);
            float startY = (float)((face.getPosition().y) * scale);

            float endX = (float)((face.getPosition().x + face.getWidth()) * scale);
            float endY = (float)((face.getPosition().y + face.getHeight()) * scale);

            Float eventX = event.getX();
            Float eventY = event.getY();

            if((  eventX >= startX) && ( eventX <= endX) &&
                    (eventY >= startY) && (eventY <= endY) )
            {
                return face;
            }

        }

        return null;
    }


    private class GifGenerateTask extends AsyncTask<Object,Void,List<Uri>> {

        private ProgressDialog nDialog;

        protected List<Uri> doInBackground(Object... object) {
            Log.d(UfilyConstants.APP, "GifGenerateTask::doInBackground");
            Log.d(UfilyConstants.APP, "before Image processing Time:" + new Date().toString());

            Bitmap input = (Bitmap)object[0];
            Face face = (Face)object[1];

            Bitmap output = makeRectangleBitmap(input,face);
            Bitmap output1= makeCircularBitmap(output,face);
            Bitmap output2= addBorderToBitmap(output1,4,Color.TRANSPARENT);
            Bitmap output3= addBorderToBitmap(output2,4,Color.TRANSPARENT);

            GifManager gifManager = GifManager.getInstance();
            List<Uri> uriList = new ArrayList<>();


            int sizeofWebp = byteSizeOf(output3);

            Log.d(UfilyConstants.APP,"GifGenerateTask::doInBackground()"+"After:sizeofWebp:"+sizeofWebp/1024+"KB");

            //code to create a png image
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            output3.compress(Bitmap.CompressFormat.WEBP, 100, stream);
            byte[] byteArray = stream.toByteArray();


            sizeofWebp = byteSizeOf(output3);
            Log.d(UfilyConstants.APP,"GifGenerateTask::doInBackground()"+"After:sizeofWebp:"+sizeofWebp/1024+"KB");

            //0 uri is GIF and png
            uriList.add(gifManager.createGifImage(getActivity(),output, mUserUfilyTypeSelected));
            //TODO should be chnage back to PNG
            //uriList.add(getLocalImageUri(getActivity(),byteArray,".webp"));
            uriList.add(gifManager.createWebpImage(getActivity(),output3,mUserUfilyTypeSelected));

            //add images to DB and report to display GIF
            addImagesToDb(uriList.get(0),uriList.get(1));
            return uriList;
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


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(getActivity()); //Here I get an error: The constructor ProgressDialog(PFragment) is undefined
            nDialog.setMessage("Loading..");
            nDialog.setTitle("Creating GIF");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();

        }
        protected void onPostExecute(List<Uri> result) {
            nDialog.dismiss();
            Log.d(UfilyConstants.APP, "GifGenerateTask::onPostExecute");
            isGifInProgress = false;
            //displayGif(result.get(0));
            //mActivityCallBack.onUfilyEditingDone(result.get(0),result.get(1));
            //add images to DB
        }



        private Bitmap makeCircularBitmap(Bitmap input,Face face)
        {
            int sideLength =(int)(Math.min(face.getHeight(),face.getWidth()));
            Log.d(UfilyConstants.APP,"face side length:"+sideLength);

            Bitmap output = Bitmap.createBitmap(sideLength,sideLength,Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(output);

            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);

            //whats app expects that sticker art should be gap between 16 px
            //sideLength = sideLength;
            Rect rect = new Rect(0, 0, sideLength, sideLength);
            RectF rectF = new RectF(rect);
            canvas.drawOval(rectF, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            float left = (sideLength-input.getWidth())/2;
            float top = (sideLength-input.getHeight())/2;
            canvas.drawBitmap(input, left,top, paint);


            Bitmap output1 = null;
            //upscaling or downscaling
            output1 = Bitmap.createScaledBitmap(output,512-16,512-16,true);

            return output1;
        }

        protected Bitmap addBorderToBitmap(Bitmap srcBitmap, int borderWidth, int borderColor){
            // Initialize a new Bitmap to make it bordered bitmap
            Bitmap dstBitmap = Bitmap.createBitmap(
                    srcBitmap.getWidth() + borderWidth*2, // Width
                    srcBitmap.getHeight() + borderWidth*2, // Height
                    Bitmap.Config.ARGB_8888 // Config
            );

        /*
            Canvas
                The Canvas class holds the "draw" calls. To draw something, you need 4 basic
                components: A Bitmap to hold the pixels, a Canvas to host the draw calls (writing
                into the bitmap), a drawing primitive (e.g. Rect, Path, text, Bitmap), and a paint
                (to describe the colors and styles for the drawing).
        */
            // Initialize a new Canvas instance
            Canvas canvas = new Canvas(dstBitmap);

            // Initialize a new Paint instance to draw border
            Paint paint = new Paint();
            paint.setColor(borderColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(borderWidth);
            paint.setAntiAlias(true);

        /*
            Rect
                Rect holds four integer coordinates for a rectangle. The rectangle is represented by
                the coordinates of its 4 edges (left, top, right bottom). These fields can be accessed
                directly. Use width() and height() to retrieve the rectangle's width and height.
                Note: most methods do not check to see that the coordinates are sorted correctly
                (i.e. left <= right and top <= bottom).
        */
        /*
            Rect(int left, int top, int right, int bottom)
                Create a new rectangle with the specified coordinates.
        */

            // Initialize a new Rect instance
        /*
            We set left = border width /2, because android draw border in a shape
            by covering both inner and outer side.
            By padding half border size, we included full border inside the canvas.
        */
            Rect rect = new Rect(
                    borderWidth / 2,
                    borderWidth / 2,
                    canvas.getWidth() - borderWidth / 2,
                    canvas.getHeight() - borderWidth / 2
            );

        /*
            public void drawRect (Rect r, Paint paint)
                Draw the specified Rect using the specified Paint. The rectangle will be filled
                or framed based on the Style in the paint.

            Parameters
                r : The rectangle to be drawn.
                paint : The paint used to draw the rectangle

        */
            // Draw a rectangle as a border/shadow on canvas
            canvas.drawRect(rect,paint);

        /*
            public void drawBitmap (Bitmap bitmap, float left, float top, Paint paint)
                Draw the specified bitmap, with its top/left corner at (x,y), using the specified
                paint, transformed by the current matrix.

                Note: if the paint contains a maskfilter that generates a mask which extends beyond
                the bitmap's original width/height (e.g. BlurMaskFilter), then the bitmap will be
                drawn as if it were in a Shader with CLAMP mode. Thus the color outside of the
                original width/height will be the edge color replicated.

                If the bitmap and canvas have different densities, this function will take care of
                automatically scaling the bitmap to draw at the same density as the canvas.

            Parameters
                bitmap : The bitmap to be drawn
                left : The position of the left side of the bitmap being drawn
                top : The position of the top side of the bitmap being drawn
                paint : The paint used to draw the bitmap (may be null)
        */

            // Draw source bitmap to canvas
            canvas.drawBitmap(srcBitmap, borderWidth, borderWidth, null);

        /*
            public void recycle ()
                Free the native object associated with this bitmap, and clear the reference to the
                pixel data. This will not free the pixel data synchronously; it simply allows it to
                be garbage collected if there are no other references. The bitmap is marked as
                "dead", meaning it will throw an exception if getPixels() or setPixels() is called,
                and will draw nothing. This operation cannot be reversed, so it should only be
                called if you are sure there are no further uses for the bitmap. This is an advanced
                call, and normally need not be called, since the normal GC process will free up this
                memory when there are no more references to this bitmap.
        */
            srcBitmap.recycle();

            // Return the bordered circular bitmap
            return dstBitmap;
        }





        private Bitmap makeRectangleBitmap(Bitmap input,Face face)
        {
            int sideLength =(int)(Math.max(face.getHeight(),face.getWidth()));
            Log.d(UfilyConstants.APP,"face side length:"+sideLength);

            int startX = (int)face.getPosition().x;
            int startY = (int)face.getPosition().y;

            //float length =Math.max(face.getWidth(),face.getHeight());
            int endX = (int)(face.getWidth()+startX);
            int endY = (int)(face.getHeight()+startY);

            Log.d(UfilyConstants.APP,"face startX:"+startX +"startY:"+startY);

            Bitmap output = Bitmap.createBitmap((int)face.getWidth(),(int)face.getHeight(),Bitmap.Config.ARGB_8888);

            final Rect srcRect = new Rect(startX,startY, endX, endY);
            final Rect destRect = new Rect(0,0, (int)face.getWidth(),(int)face.getHeight());
            Canvas canvas = new Canvas(output);

            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            canvas.drawColor(Color.TRANSPARENT);
            canvas.drawBitmap(input, srcRect, destRect, paint);

            return output;
        }
        private void addImagesToDb(Uri gifUri,Uri imageUri) {


            final List<String> pathSegments = imageUri.getPathSegments();
            if (pathSegments.size() < 1) {
                throw new IllegalArgumentException("path segments should be 3, uri is: " + imageUri);
            }
            String fileName = pathSegments.get(pathSegments.size() - 1);

            File imagefile = new File(imageUri.getPath());
            Log.d(UfilyConstants.APP, "fileName:" + fileName+" size:"+imagefile.length());

            if ((gifUri == null) || (imageUri == null)) {
                return;
            }



            Ufily ufily = new Ufily();
            File file = new File(gifUri.getPath());
            if (file.exists()) {
                ;
                Random rand = new Random();
                long selected = rand.nextInt(100000);
                ufily.setId((int) selected);
                ufily.setName(fileName);
                ufily.setImagePath(gifUri.getPath());
                ufily.setImagePath1(imageUri.getPath());
                getDBInstance().addUfily(ufily);
            }
        }
        //diplays the GIfImageView
        private void displayGif(Uri result)
        {
            Log.d(UfilyConstants.APP,"displayGif:"+result.getPath());
            mFrameLayoutSemiTransparent.setVisibility(View.VISIBLE);

            try {
                mGifImageView.setBytes(readBytes(result));
                mGifImageView.setTag(result);
                //@TODO dismissing the dialog can be moved here
                mGifImageView.startAnimation();
                //this is only a hack..not correct way to do
                if(mUserUfilyTypeSelected == UfilyConstants.UFILY_SMILEY.UFILY_KISSING_LIPS) {
                    new CountDownTimer(1000, 1000) {

                        public void onTick(long millisUntilFinished) {

                        }
                        public void onFinish() {
                            if (mGifImageView.isAnimating()) {
                                mGifImageView.stopAnimation();
                            }

                        }
                    }.start();
                }
                mGifImageView.getFramesDisplayDuration();
                mGifImageView.setVisibility(View.VISIBLE);
                mShareButon.setVisibility(View.VISIBLE);
            }
            catch (Exception e)
            {
                Log.d(UfilyConstants.APP,"exception while GIF player"+e.getMessage());
            }




        }

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


    public void scanFile(Context ctxt, File f, String mimeType) {
        MediaScannerConnection
                .scanFile(ctxt, new String[] {f.getAbsolutePath()},
                        new String[] {mimeType}, null);
    }


    //TODO remove code this needs to be moved inside a thread ..heavy processing not on UI thread.
    public byte[] readBytes(Uri uri) throws IOException {
        // this dynamically extends to take the bytes you read
        InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    private void showFacesInImage()
    {
        if(mIsImageLoadComplete && mIsVisibleToUser) {
            Log.d(UfilyConstants.APP,"showFacesInImage()");
            mFaceDetectView.setContent(mBackGroundImage, mFaces);
        }

    }

    public Bitmap getCircularBitmap(Bitmap square) {
        if (square == null) return null;
        Bitmap output = Bitmap.createBitmap(square.getWidth(), square.getHeight(),
                Bitmap.Config.ARGB_8888);


        final Rect rect = new Rect(0, 0, square.getWidth(), square.getHeight());
        Canvas canvas = new Canvas(output);

        int halfWidth = square.getWidth() / 2;
        int halfHeight = square.getHeight() / 2;

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        //to set transparent background
        canvas.drawColor(Color.TRANSPARENT);

        canvas.drawCircle(halfWidth, halfHeight, Math.min(halfWidth, halfHeight), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(square, rect, rect, paint);
        return output;
    }


    private Uri getLocalGifUri(byte[] aInput,String imageType)
    {

        Uri gifUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.

            File file =  new File(getActivity().getExternalFilesDir(DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + imageType);
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
                Log.d(UfilyConstants.APP,"no uri");
            }
            else {
                Log.d(UfilyConstants.APP, gifUri.getPath());
                //Toast.makeText(getActivity().getApplicationContext(),gifUri.getPath(),Toast.LENGTH_LONG);

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




    private void selectImage()
    {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        startActivityForResult(
                Intent.createChooser(intent, "Select File"),
                UfilyConstants.UFILY_SELECT_FILE);

    }


    private File createImageFile() throws IOException {
        // Create an image file name
        Log.d(UfilyConstants.APP,"createImageFile");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if(storageDir.exists())
        {
            Log.d(UfilyConstants.APP,"found external storage directory");
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        Log.d(UfilyConstants.APP,"createImageFile:"+mCurrentPhotoPath);

        return image;
    }


    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }




    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(UfilyConstants.APP,"exception while creating a file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                Uri photoURI = Uri.fromFile(photoFile);
                Log.d(UfilyConstants.APP,"dispatchTakePictureIntent:"+photoURI.getPath());
                        //FileProvider.getUriForFile(getActivity(),
                        //"freeze.in.co.ufily",
                        //photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, UfilyConstants.UFILY_REQUEST_CAMERA);
            }
        }
    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }


}
