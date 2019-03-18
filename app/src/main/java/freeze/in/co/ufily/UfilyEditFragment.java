package freeze.in.co.ufily;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import freeze.in.co.ufily.common.Coordinates;
import freeze.in.co.ufily.common.FragmentChangeListener;
import freeze.in.co.ufily.common.UfilyConstants;
import freeze.in.co.ufily.common.UfilyPart;
import simplecropview.CropImageView;
/**
 * A placeholder fragment containing a simple view.
 */
public class UfilyEditFragment extends Fragment implements  View.OnClickListener,View.OnLongClickListener,View.OnDragListener,View.OnTouchListener {

    /*
      Handle for UI elements
    */
    private CropImageView mCropView;
    private Button mButtonEdit;
    private Button mButtonDone;
    private ImageView mLoveImageview;
    private  ImageView mBackGroundImageView;
    private android.widget.FrameLayout.LayoutParams layoutParams;

    private Bitmap mUfilyBitmap;
    private String mCurrentPhotoPath;
    private View mEditUfilyView;
    private ViewGroup mContainer;
    private boolean mIsCropViewVisible;
    private Bitmap mHeartImage;
    private FragmentChangeListener mActivityCallback;

    private  double mImageScaleFactor;
    Coordinates mDropEventCoordinates;


    public UfilyEditFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mImageScaleFactor = (double)1.0;
        mEditUfilyView = inflater.inflate(R.layout.fragment_main, container, false);
        mIsCropViewVisible= true;

        mCropView = (CropImageView)mEditUfilyView.findViewById(R.id.cropImageView);

        mLoveImageview =(ImageView)mEditUfilyView.findViewById(R.id.heartImageView);
        mButtonEdit = (Button)mEditUfilyView.findViewById(R.id.button_edit);
        mButtonDone = (Button)mEditUfilyView.findViewById(R.id.button_done);

        mButtonEdit.setOnClickListener(this);
        mButtonDone.setOnClickListener(this);
        mLoveImageview.setTag(UfilyConstants.UFILY_IMG_TAG);
        mHeartImage = BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), R.drawable.frame0);
        //set all listeners
        mLoveImageview.setOnLongClickListener(this);

        mLoveImageview.setOnTouchListener(this);
        mDropEventCoordinates = new Coordinates();

        return mEditUfilyView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mActivityCallback = (FragmentChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentChangeListener");
        }
    }
    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.button_edit:
                if(!mIsCropViewVisible)
                    addCircleView();

                selectImage();

                break;
            case R.id.button_done:
                Log.d(UfilyConstants.APP, "pressed button done");

                if(mIsCropViewVisible)
                    //new BitmapCropTask().execute(mCropView);
                //else //mBackGroundView is visible
                {
                    //new GifGenerateTask().execute();
                }

                break;

        }

    }


    private void selectImage()
    {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("Select Photoo to create Ufily");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.d(UfilyConstants.APP,"Exception:"+ex.getMessage());
                    }
                    if(photoFile!=null) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photoFile));
                        Log.d(UfilyConstants.APP, "Requesting for camera");
                        startActivityForResult(intent, UfilyConstants.UFILY_REQUEST_CAMERA);
                    }
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");

                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            UfilyConstants.UFILY_SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();



    }

    private boolean isSafeIntent(Intent intent)
    {
        PackageManager packageManager = this.getActivity().getApplicationContext().getPackageManager();
        List activities = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;
        return isIntentSafe;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(UfilyConstants.APP, "on Activity result of edit fragment");

        if (requestCode == UfilyConstants.UFILY_SELECT_FILE) {

            Log.d(UfilyConstants.APP, "in the section request gallery access");
            Uri selectedImageUri = data.getData();

            if(selectedImageUri!= null) {

                    Glide.with(getActivity().getApplicationContext())
                            .load(selectedImageUri)    // you can pass url too
                            .asBitmap()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    // you can do something with loaded bitmap here
                                    Log.d(UfilyConstants.APP, "Loaded image from Gallery");
                                    launchUfilyEdit(resource);

                                }
                            });



            }

        }
        else if(requestCode == UfilyConstants.UFILY_REQUEST_CAMERA)
        {
            Log.d(UfilyConstants.APP,"Request camera success");
            //Bitmap bm = getResizedPic();
            //launchUfilyEdit(bm);
        }
    }



    @Override
    public boolean onLongClick(View v) {

        Log.d(UfilyConstants.APP, "OnLongClick()");


        Toast.makeText(getActivity().getApplicationContext(), "searching for eyes", Toast.LENGTH_SHORT);
        /*ClipData.Item item = new ClipData.Item((CharSequence)v.getTag());
        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};

        ClipData dragData = new ClipData(v.getTag().toString(),mimeTypes, item);
        View.DragShadowBuilder myShadow = new View.DragShadowBuilder(mLoveImageview);

        v.startDrag(dragData, myShadow, null, 0);*/
        return true;
    }



    @Override
    public boolean onDrag(View v, DragEvent event) {
        Log.d(UfilyConstants.APP, "OnDrag()");
        switch(event.getAction())
        {
            case DragEvent.ACTION_DRAG_STARTED:

                Log.d(UfilyConstants.APP, "Action is DragEvent.ACTION_DRAG_STARTED");
                if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {

                    // As an example of what your application might do,
                    // applies a blue color tint to the View to indicate that it can accept
                    // data.
                    //v.setColorFilter(Color.BLUE);
                    Log.d(UfilyConstants.APP, "Action is DragEvent.ACTION_DRAG_STARTED on view"+((v.getId()==R.id.backGroundImageView)?"backGroundView":"OtherView"));
                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate();

                    // returns true to indicate that the View can accept the dragged data.
                    return true;

                }

                // Returns false. During the current drag and drop operation, this View will
                // not receive events again until ACTION_DRAG_ENDED is sent.
                return false;
                // Do nothing
                //break;

            case DragEvent.ACTION_DRAG_ENTERED:
                Log.d(UfilyConstants.APP, "Action is DragEvent.ACTION_DRAG_ENTERED");
                int x_cord = (int) event.getX();
                int y_cord = (int) event.getY();
                break;

            case DragEvent.ACTION_DRAG_EXITED :
                Log.d(UfilyConstants.APP, "Action is DragEvent.ACTION_DRAG_EXITED");
                /*
                x_cord = (int) event.getX();
                y_cord = (int) event.getY();
                layoutParams.leftMargin = x_cord;
                layoutParams.topMargin = y_cord;
                v.setLayoutParams(layoutParams);
                */
                break;

            case DragEvent.ACTION_DRAG_LOCATION  :
                Log.d(UfilyConstants.APP, "Action is DragEvent.ACTION_DRAG_LOCATION");
                x_cord = (int) event.getX();
                y_cord = (int) event.getY();
                break;

            case DragEvent.ACTION_DRAG_ENDED   :
                Log.d(UfilyConstants.APP, "Action is DragEvent.ACTION_DRAG_ENDED");
                // Do nothing
                break;

            case DragEvent.ACTION_DROP:
                Log.d(UfilyConstants.APP, "ACTION_DROP event");
                // Do nothing
                layoutParams = (FrameLayout.LayoutParams)mLoveImageview.getLayoutParams();
                mLoveImageview.setVisibility(View.VISIBLE);
                x_cord = (int) event.getX();
                y_cord = (int) event.getY();
                layoutParams.leftMargin = x_cord-mLoveImageview.getWidth()/2;
                layoutParams.topMargin = y_cord-mLoveImageview.getHeight()/2;
                mLoveImageview.setLayoutParams(layoutParams);
                mLoveImageview.invalidate();

                //check if it is dropped on image only?
                mDropEventCoordinates.mX = x_cord;
                mDropEventCoordinates.mY = y_cord;
                //continue processing
                break;
            default: break;
        }
        return true;
    }
/*
    //might not work.
    private ImageCoordinates getImageCoordinates(int touchX,int touchY,int viewX,int viewY,ImageView backGroundImageView)
    {
        //check for touchX and touchY beyond the viewWidth
        if(touchX<0 || touchY <0 || touchX > backGroundImageView.getWidth() || touchY > backGroundImageView.getHeight()) {
            return null;
        }


        int touchWidth = viewX - touchX;
        int touchHeight = viewY - touchY;
        Log.d(UfilyConstants.APP,"viewX:"+viewX+"viewY:"+viewY);
        Log.d(UfilyConstants.APP,"touchX:"+touchX+"touchY:"+touchY);
        Log.d(UfilyConstants.APP,"touchWidth:"+touchWidth+"touchHeight:"+touchHeight);

        //currently the backgroundimage is match_parent so the image specs match with backGroundImageView specs
        int viewWidth = backGroundImageView.getWidth();
        int viewHeight = backGroundImageView.getHeight();
        Log.d(UfilyConstants.APP,"viewWidth:"+viewWidth+"viewHeight:"+viewHeight);


        Bitmap circleBitmap = (Bitmap)backGroundImageView.getTag();
        //not sure which one to use? getWidth() or getScaledWidth()

        int actualBitmapWidth = circleBitmap.getWidth();
        int actualBitmapHeight = circleBitmap.getHeight();
        Log.d(UfilyConstants.APP,"actualBitmapWidth:"+actualBitmapWidth+"actualBitmapHeight:"+actualBitmapHeight);


        double widthPercent = (double)touchWidth/(double)viewWidth;
        double heightPercent = (double)touchHeight/(double)viewHeight;


        //should we be careful here while conversion?
        int bitmapX = (int)(widthPercent * actualBitmapWidth);
        int bitmapY = (int)(heightPercent * actualBitmapHeight);


        return new ImageCoordinates(bitmapX,bitmapY);

    }

*/

    //the fit centerInside aspect ratio is maintained
    //get the center of the imageView,matches the center of the scaled image.
    //check that touchX and touchY not beyond the actaul image.
    //getscaled width and heigth of image.
    //get original with and height of image.
    private Coordinates getImageCoordinates(int touchX,int touchY,ImageView backGroundImageView)
    {
        Log.d(UfilyConstants.APP,"getImageCoordinates");
        Bitmap circleBitmap = (Bitmap)backGroundImageView.getTag();
        //not sure which one to use? getWidth() or getScaledWidth()

        Log.d(UfilyConstants.APP,"touchX:"+touchX+",touchY:"+touchY);
        Log.d(UfilyConstants.APP, "backGroundImage center X:" + (backGroundImageView.getX() + (int) (backGroundImageView.getWidth()/2))+ ",Y:"+
                (backGroundImageView.getY()+(int)(backGroundImageView.getHeight()/2)));

        int originalWidth = (int)(backGroundImageView.getX()+(int)(backGroundImageView.getWidth()/2)) - touchX;
        int originalHeight = (int)(backGroundImageView.getY()+(int)(backGroundImageView.getHeight()/2)) - touchY;

        Log.d(UfilyConstants.APP,"originalWidth:"+originalWidth+",originalHeight"+originalHeight);



        float scaledWidth = backGroundImageView.getScaleX() * originalWidth;
        float scaledHeight = backGroundImageView.getScaleY() * originalHeight;

        //float scaledWidth = (circleBitmap.getWidth()/256) * originalWidth;
        //float scaledHeight = (circleBitmap.getHeight()/256) * originalHeight;


        Log.d(UfilyConstants.APP, "Scale X:" + backGroundImageView.getScaleX() + ",Scale Y:" + backGroundImageView.getScaleY());
        Log.d(UfilyConstants.APP, "scaledWidth:" + scaledWidth + ",scaledHeight" + scaledHeight);


        int newCenterX = circleBitmap.getWidth()/2;
        int newCentreY = circleBitmap.getHeight()/2;

        Log.d(UfilyConstants.APP,"newCenterX:"+newCenterX+",newCenterY:"+newCentreY);

        //change the sign
        scaledWidth = 0 -  scaledWidth;
        scaledHeight = 0 - scaledHeight;


        int finalX = newCenterX + (int)scaledWidth;
        int finalY = newCentreY + (int)scaledHeight;

        Log.d(UfilyConstants.APP,"finalCenterX:"+finalX+",finalY:"+finalY);

        return new Coordinates(finalX,finalY);
    }

    public Bitmap putOverlay(Bitmap bitmap, Bitmap overlay,UfilyPart ufilyEyes) {



        Bitmap newBitmap = Bitmap.createBitmap(bitmap);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAlpha(255);

        //check for eyes detected or not
        int overlayX = ufilyEyes.mleftPart.mX;
        int overlayY = ufilyEyes.mleftPart.mY;

        int overlayRightX = ufilyEyes.mRightPart.mX;
        int overlayRightY = ufilyEyes.mRightPart.mY;



        overlayX = overlayX - overlay.getWidth()/2;
        overlayY = overlayY - overlay.getHeight()/2;

        overlayRightX = overlayRightX - overlay.getWidth() / 2;
        overlayRightY = overlayRightY - overlay.getHeight()/2;
        //left eye
        if(ufilyEyes.isLeftPartDetected())
            canvas.drawBitmap(overlay, overlayX, overlayY, paint);
        //right eye
        if(ufilyEyes.iRightPartDetected())
        canvas.drawBitmap(overlay,overlayRightX,overlayRightY,paint);

        return newBitmap;
    }






    @Override
    public boolean onTouch(View v, MotionEvent event) {
          Log.d(UfilyConstants.APP,"OnTouch()");


        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            /*
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(mLoveImageview);
            mLoveImageview.startDrag(data, shadowBuilder, mLoveImageview, 0);
            mLoveImageview.setVisibility(View.INVISIBLE);
            */
            Toast.makeText(getActivity().getApplicationContext(),"Searching for eyes",Toast.LENGTH_LONG);
            return true;
        }
        else if(event.getAction() == MotionEvent.ACTION_UP)
        {
            //mLoveImageview.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity().getApplicationContext(),"Searching for eyes",Toast.LENGTH_LONG);
            return true;
        }
        else {
            return false;
        }
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


    private Bitmap getResizedPic() {
        // Get the dimensions of the View
        int targetW = mCropView.getWidth();
        int targetH = mCropView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        Log.d(UfilyConstants.APP, "Resized pic");
        return bitmap;
    }



    private File createImageFile() throws IOException {
        // Create an image file name
        Log.d(UfilyConstants.APP,"createImageFile");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getRootDirectory();

        if(storageDir.exists())
        {
            Log.d(UfilyConstants.APP,"couldn't get external storage directory");
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();

        return image;
    }



    private void launchUfilyEdit(Bitmap bm)
    {
        mCropView.setImageBitmap(bm);
        mCropView.setInitialFrameScale((float) 0);
        mCropView.setCropMode(CropImageView.CropMode.CIRCLE);
        Toast.makeText(this.getActivity().getApplicationContext(), "Craft the best ufily!", Toast.LENGTH_SHORT).show();
    }


/*

    private class BitmapCropTask extends AsyncTask<CropImageView,Void,Bitmap> {

        protected Bitmap doInBackground(CropImageView... cropImageView) {
            Log.d(UfilyConstants.APP, "BitmapCropTask::doInBackground");

            return cropImageView[0].getCroppedBitmap();
        }
        protected void onPostExecute(Bitmap result)
        {
            Log.d(UfilyConstants.APP,"BitmapCropTask::onPostExecute");
            //getLocalBitmapUri(result);
            //mActivityCallback.onUfilyEditingDone(result);
            removeCircleView(result);

        }



    }
*/
/*
    private class GifGenerateTask extends AsyncTask<Void,Void,Uri> {

        private ProgressDialog nDialog;
        protected Uri doInBackground(Void... cropImageView) {
            Log.d(UfilyConstants.APP, "GifGenerateTask::doInBackground");
            Coordinates imageCoordinates = getImageCoordinates(mDropEventCoordinates.mX,mDropEventCoordinates.mY,(ImageView)mBackGroundImageView);
            Log.d(UfilyConstants.APP,"before Image processing Time:"+new Date().toString());
            Bitmap backGroundImage = (Bitmap)mBackGroundImageView.getTag();
            //Uri gifImage = createGIF(backGroundImage,imageCoordinates.mX,imageCoordinates.mY);

            FaceDetector1 faceDetector1 = new FaceDetector1(getActivity());
            //imageCoordinates.mX,imageCoordinates.mY,
            return createGIF(backGroundImage,faceDetector1.getEyeCoordinates(getActivity(),backGroundImage));


                    //createMP4(backGroundImage,imageCoordinates.mX,imageCoordinates.mY);
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

        protected void onPostExecute(Uri result)
        {
            nDialog.dismiss();
            Log.d(UfilyConstants.APP, "GifGenerateTask::onPostExecute");
            //((ImageView) mBackGroundImageView).setImageBitmap(result);
            mActivityCallback.onUfilyEditingDone(result);

        }



    }
*/
/*
    private Uri createGIF(Bitmap backGroundimage,UfilyEyes ufilyEyes)
    {


        //mImageScaleFactor = bitmap.getWidth()/256;
        int scaledWidth = (int)(mLoveImageview.getWidth()/mImageScaleFactor );
        int scaledHeight = (int)(mLoveImageview.getHeight()/mImageScaleFactor);

        Bitmap frame0 = putOverlay(backGroundimage,
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), R.drawable.frame0),
                        scaledWidth,
                        scaledHeight,true),
                        ufilyEyes);

        Bitmap frame1 = putOverlay(backGroundimage,
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), R.drawable.frame1),
                        scaledWidth,
                        scaledHeight,true),
                        ufilyEyes);

        Bitmap frame2 = putOverlay(backGroundimage,
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), R.drawable.frame2),
                        scaledWidth,
                        scaledHeight, true),
                        ufilyEyes);

        Bitmap frame3 = putOverlay(backGroundimage,
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), R.drawable.frame3),
                        scaledWidth,
                        scaledHeight,true),
                ufilyEyes);

        Bitmap frame4 = putOverlay(backGroundimage,
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), R.drawable.frame4),
                        scaledWidth,
                        scaledHeight, true),
                        ufilyEyes);



        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(bos);
        //new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".gif");
        encoder.setTransparent(1);
        encoder.setDelay(100);
        encoder.addFrame(frame0);
        encoder.setTransparent(1);
        encoder.addFrame(frame1);
        encoder.setTransparent(1);
        encoder.addFrame(frame2);
        encoder.setTransparent(1);
        encoder.addFrame(frame3);
        encoder.setTransparent(1);
        encoder.addFrame(frame4);

        encoder.finish();

        Log.d(UfilyConstants.APP, "After Image processing Time:" + new Date().toString());


        //getLocalGifUri(byteArray,".gif");
        //getLocalGifUri(buffer.array(),".jpeg");
        //getLocalBitmapUri(frame0);
        return getLocalGifUri(bos.toByteArray(),".gif");

    }
*/
    private Uri getLocalBitmapUri(Bitmap bitmap) {

        Uri bmpUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            File file =  new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return bmpUri;

    }

    /*
    private Uri createMP4(Bitmap backGroundimage,int overlay_x,int overlay_y)
    {

        Uri mp4Uri=null;
        try {


            Bitmap frame0 = putOverlay(backGroundimage,
                    BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), R.drawable.frame0),
                    overlay_x, overlay_y);




            Bitmap frame1 = putOverlay(backGroundimage,
                    BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), R.drawable.frame1),
                    overlay_x, overlay_y);

            Bitmap frame2 = putOverlay(backGroundimage,
                    BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), R.drawable.frame2),
                    overlay_x, overlay_y);

            Bitmap frame3 = putOverlay(backGroundimage,
                    BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), R.drawable.frame3),
                    overlay_x, overlay_y);

            Bitmap frame4 = putOverlay(backGroundimage,
                    BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), R.drawable.frame4),
                    overlay_x, overlay_y);






            File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".mp4");
            SequenceEncoder encoder = new SequenceEncoder(file);


            org.jcodec.common.model.Picture picture0 =
                    org.jcodec.common.model.Picture.create(frame0.getWidth(),frame0.getHeight()
                                                    , ColorSpace.RGB);

            fromBitmap(frame0, picture0);

            for(int i=0;i<5;i++)
                encoder.encodeNativeFrame(picture0);


            org.jcodec.common.model.Picture picture1 =
                    org.jcodec.common.model.Picture.create(frame1.getWidth(),frame1.getHeight()
                            , ColorSpace.RGB);

            fromBitmap(frame1, picture1);

            for(int i=0;i<5;i++)
                encoder.encodeNativeFrame(picture1);

            org.jcodec.common.model.Picture picture2 =
                    org.jcodec.common.model.Picture.create(frame2.getWidth(),frame2.getHeight()
                            , ColorSpace.RGB);

            fromBitmap(frame2, picture2);

            for(int i=0;i<5;i++)
                encoder.encodeNativeFrame(picture2);

            org.jcodec.common.model.Picture picture3 =
                    org.jcodec.common.model.Picture.create(frame3.getWidth(),frame3.getHeight()
                            , ColorSpace.RGB);

            fromBitmap(frame3, picture3);

            for(int i=0;i<5;i++)
                encoder.encodeNativeFrame(picture3);


            org.jcodec.common.model.Picture picture4 =
                    org.jcodec.common.model.Picture.create(frame4.getWidth(),frame4.getHeight()
                            , ColorSpace.RGB);

            fromBitmap(frame4, picture4);

            for(int i=0;i<5;i++)
                encoder.encodeNativeFrame(picture4);



            encoder.finish();

             mp4Uri= Uri.fromFile(file);


        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
return mp4Uri;
    }


    public static void fromBitmap(Bitmap src, org.jcodec.common.model.Picture dst) {
        int[] dstData = dst.getPlaneData(0);
        int[] packed = new int[src.getWidth() * src.getHeight()];

        src.getPixels(packed, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());

        for (int i = 0, srcOff = 0, dstOff = 0; i < src.getHeight(); i++) {
            for (int j = 0; j < src.getWidth(); j++, srcOff++, dstOff += 3) {
                int rgb = packed[srcOff];
                dstData[dstOff]     = (rgb >> 16) & 0xff;
                dstData[dstOff + 1] = (rgb >> 8) & 0xff;
                dstData[dstOff + 2] = rgb & 0xff;
            }
        }
    }
*/

    private Uri getLocalGifUri(byte[] aInput,String imageType)
    {

        Uri gifUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.

            File file =  new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + imageType);
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



    private void removeCircleView(Bitmap bitmap)
    {
        mIsCropViewVisible = false;
        mCropView.setVisibility(View.INVISIBLE);
        mBackGroundImageView =(ImageView)mEditUfilyView.findViewById(R.id.backGroundImageView);
        mBackGroundImageView.setVisibility(View.VISIBLE);

        Bitmap scaledBackGroundImage = Bitmap.createScaledBitmap(bitmap, 512, 512, true);
        mBackGroundImageView.setImageBitmap(scaledBackGroundImage);

        mImageScaleFactor = (Double)(Double.valueOf(bitmap.getWidth())/512);

        Log.d(UfilyConstants.APP, "mImageScaleFactor:" + mImageScaleFactor + "Original width:" + bitmap.getWidth());

        mBackGroundImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        mBackGroundImageView.setTag(scaledBackGroundImage);
        mBackGroundImageView.setOnDragListener(this);
    }

    private void addCircleView()
    {
        mIsCropViewVisible = true;
        mCropView.setVisibility(View.VISIBLE);
        mBackGroundImageView.setVisibility(View.INVISIBLE);
    }



    }


