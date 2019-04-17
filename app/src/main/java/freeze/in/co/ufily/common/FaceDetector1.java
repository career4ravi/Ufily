package freeze.in.co.ufily.common;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.util.List;

import static com.google.android.gms.vision.face.FaceDetector.ACCURATE_MODE;

/**
 * Created by rtiragat on 11/6/2016.
 */
public class FaceDetector1 {


    private SparseArray<Face> mFaces;
    private Activity mActivity;

    public FaceDetector1(Activity activity) {

        mFaces = null;
        mActivity = activity;
    }

    public SparseArray<Face> createFaceMap(Bitmap bitmap)
    {

        //use this only for specific person to get all parts in persons face.
        // setLandmarkType(FaceDetector.ALL_LANDMARKS)
        FaceDetector detector;
        detector = new FaceDetector.Builder(mActivity.getApplicationContext())
                .setTrackingEnabled(false)
                .setMode(ACCURATE_MODE)
                .build();


        if (detector.isOperational()) {
            Log.d(UfilyConstants.APP, "FaceDetector:createFaceMap");
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<Face> faces = detector.detect(frame);

            mFaces = faces;

            for (int i = 0; i < faces.size(); ++i) {
                Log.d(UfilyConstants.APP, "FaceDetector:createFaceMap:"+i);
                Face face = faces.valueAt(i);
            }




        }

        detector.release();

        return mFaces;
    }


    public static  List<Landmark> getAllLandmarks(Activity activity,Bitmap faceImage)
    {

        FaceDetector detector;
        detector = new FaceDetector.Builder(activity.getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        if (detector.isOperational()) {

            Frame frame = new Frame.Builder().setBitmap(faceImage).build();
            SparseArray<Face> faces = detector.detect(frame);

            if(faces.size() > 1)
            {
                return null;
            }
            //atleast 1
            if(faces.size() == 1)
            {
                Face face = faces.valueAt(0);
                if(face!=null) {
                    return face.getLandmarks();
                }
            }



        }

        detector.release();

        return null;
    }



    public UfilyPart getEyeCoordinates(Activity activity,Bitmap ufilyImage)
    {


        // A new face detector is created for detecting the face and its landmarks.
        //
        // Setting "tracking enabled" to false is recommended for detection with unrelated
        // individual images (as opposed to video or a series of consecutively captured still
        // images).  For detection on unrelated individual images, this will give a more accurate
        // result.  For detection on consecutive images (e.g., live video), tracking gives a more
        // accurate (and faster) result.
        //
        // By default, landmark detection is not enabled since it increases detection time.  We
        // enable it here in order to visualize detected landmarks.

        FaceDetector detector;
        detector = new FaceDetector.Builder(mActivity.getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();


        UfilyPart ufilyEyes = new UfilyPart();


        /*
        Bitmap newBitmap = Bitmap.createBitmap(ufilyImage);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAlpha(255);
        */
        //double imageWidth = ufilyImage.getWidth();
        //double imageHeight = ufilyImage.getHeight();
        //double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

        if (detector.isOperational()) {
            Log.d(UfilyConstants.APP, "FaceDetector:operation");
            Frame frame = new Frame.Builder().setBitmap(ufilyImage).build();
            SparseArray<Face> faces = detector.detect(frame);

            boolean leftEye = false;
            boolean rightEye = false;
            for (int i = 0; (i < faces.size()) && ((!leftEye) || (!rightEye)); ++i) {
                Face face = faces.valueAt(i);


                for (Landmark landmark : face.getLandmarks()) {

                    if (landmark.getType() ==  Landmark.LEFT_EYE)
                    {
                        int cx = (int) (landmark.getPosition().x );
                        int cy = (int) (landmark.getPosition().y );
                        ufilyEyes.mleftPart.mX = cx;
                        ufilyEyes.mleftPart.mY = cy;
                        Log.d(UfilyConstants.APP, "FaceDetector Left eye landmark:x" + cx + " y:" + cy);
                        leftEye = true;
                        //canvas.drawCircle(cx, cy, 10, paint);
                    }
                    else if(landmark.getType() ==  Landmark.RIGHT_EYE)
                    {
                        int cx = (int) (landmark.getPosition().x );
                        int cy = (int) (landmark.getPosition().y );
                        ufilyEyes.mRightPart.mX = cx;
                        ufilyEyes.mRightPart.mY = cy;
                        Log.d(UfilyConstants.APP, "FaceDetector Right eye landmark:x" + cx + " y:" + cy);
                        rightEye  =true;
                    }



                }
            }




        }


        detector.release();

/*
        // This is a temporary workaround for a bug in the face detector with respect to operating
        // on very small images.  This will be fixed in a future release.  But in the near term, use
        // of the SafeFaceDetector class will patch the issue.
        Detector<android.media.FaceDetector.Face> safeDetector = new SafeFaceDetector(detector);

        // Create a frame from the bitmap and run face detection on the frame.
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = safeDetector.detect(frame);

        if (!safeDetector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        //FaceView overlay = (FaceView) findViewById(R.id.faceView);
        //overlay.setContent(bitmap, faces);

        // Although detector may be used multiple times for different images, it should be released
        // when it is no longer needed in order to free native resources.
        safeDetector.release();
*/
        return ufilyEyes;

    }

}