package freeze.in.co.ufily.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.vision.face.Face;

/**
 * Created by rtiragat on 11/13/2016.
 */
public class FaceDetectView extends View{


    private Bitmap mBitmap;
    private SparseArray<Face> mFaces;
    //@TODO use separate scaling factor for width and height
    private double mScale;

    public FaceDetectView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //if onDraw is overridden this needs to be reset

    }

    /**
     * Sets the bitmap background and the associated face detections.
     */
    public void setContent(Bitmap bitmap, SparseArray<Face> faces) {
        mBitmap = bitmap;
        mFaces = faces;

        invalidate();
    }

    /**
     * Draws the bitmap background and the associated face landmarks.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((mBitmap != null) && (mFaces != null)) {
            double scale = drawBitmap(canvas);
            drawFaceRectangle(canvas, scale);
            mScale = scale;
            //drawFaceCircle(canvas,scale);
        }
    }

    /**
     * Draws the bitmap background, scaled to the device size.  Returns the scale for future use in
     * positioning the facial landmark graphics.
     */
    private double drawBitmap(Canvas canvas) {
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        double imageWidth = mBitmap.getWidth();
        double imageHeight = mBitmap.getHeight();
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

        int halfY = canvas.getHeight()/4;
        Rect destBounds = new Rect(0, 0, (int)(imageWidth * scale), (int)(imageHeight * scale));
        canvas.drawBitmap(mBitmap, null, destBounds, null);
        return scale;
    }

    /**
     * Draws a rectangle around each detected face
     */
    private void drawFaceRectangle(Canvas canvas, double scale) {
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        for (int i = 0; i < mFaces.size(); ++i) {
            Face face = mFaces.valueAt(i);

            float startX = (float) (face.getPosition().x * scale);
            float startY = (float) (face.getPosition().y * scale);
            float endX = (float) ((face.getPosition().x + face.getWidth()) * scale);
            float endY = (float) ((face.getPosition().y + face.getHeight()) * scale);
            canvas.drawRect(startX,startY,endX,endY,paint);

            Log.d(UfilyConstants.APP, "face "+i+":"+startX+","+startY+","+endX+","+endY);
        }
    }


    private void drawFaceCircle(Canvas canvas, double scale) {

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        for (int i = 0; i < mFaces.size(); ++i) {
            Face face = mFaces.valueAt(i);

            float centerX = face.getPosition().x + face.getWidth()/2;
            float centerY = face.getPosition().y + face.getHeight()/2;

            float radius = Math.max(face.getWidth() / 2, face.getHeight() / 2);
            canvas.drawCircle((float) (centerX * scale), (float) (centerY * scale), (float) (radius * scale), paint);

        }

    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    public SparseArray<Face> getMappedFaces()
    {
        return mFaces;
    }

    public Bitmap getBitmap()
    {
        return mBitmap;
    }

    public double getScale()
    {

        return mScale;
    }

}
