package com.ntcomplete.MapConcept.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import com.ntcomplete.MapConcept.R;

/**
 * @author: nick
 */
public class MapSurfaceView extends SurfaceView implements SurfaceHolder.Callback2 {

    private final Paint mOutlinePaint;
    private final Paint mFillPaint;

    private Bitmap mMapBitmap;

    private Context mContext;
    private CustomThread mThread;

    private int mLastTouchX, mLastTouchY;
    private int mDeltaX, mDeltaY;

    private int mActivePointId;
    private float mMidX, mMidY;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private float mScaleFactor = 1.0f;
    private float mScaleFactorDelta = 1.0f;

    private Matrix mMapMatrix;
    private float[] mCenter;

    // Information from http://android-developers.blogspot.in/2010/06/making-sense-of-multitouch.html
    private static final int INVALID_POINT_ID = -1;

    private boolean mDisplayDot = false;

    private float mRelatedX;
    private float mRelatedY;

    private Bitmap mDialogBitmap;
    private boolean mDisplayDialog = false;


    public MapSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if(!isInEditMode()) {
            mContext = context;
            mLastTouchX = 0;
            mLastTouchY = 0;

            mMidX = mMidY = 0;

            mDeltaY = mDeltaX = 0;
            mActivePointId = INVALID_POINT_ID;

            mMapBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map);

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            float scaleFactor = (float)size.x / (float)mMapBitmap.getWidth();

            mMapMatrix = new Matrix();
            mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
            mGestureDetector = new GestureDetector(context, new SingleTapListener());

            mMapMatrix.setScale(scaleFactor, scaleFactor);
            mCenter = new float[2];
        }

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        mOutlinePaint = new Paint();
        mOutlinePaint.setColor(Color.BLUE);
        mOutlinePaint.setStrokeWidth(7);
        mOutlinePaint.setStyle(Paint.Style.STROKE);

        mFillPaint = new Paint();
        mFillPaint.setColor(Color.WHITE);
        mFillPaint.setStyle(Paint.Style.FILL);
    }

    public void setDialogBitmap(Bitmap bitmap) {
        mDialogBitmap = bitmap;
    }

    public void setDisplayDialog(boolean displayDialog) {
        mDisplayDialog = displayDialog;
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mThread = new CustomThread(surfaceHolder, mContext, this);
        mThread.setRunning(true);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mThread.setRunning(false);
        boolean retry = true;
        while(retry) {
            try {
                mThread.join();
                retry = false;
            } catch (Exception e) {

                Log.v("MapSurfaceView", e.getMessage());
            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerIndex;

        mScaleDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);

        if(event.getPointerCount() > 1) {
            float x1 = event.getX(0);
            float x2 = event.getX(1);

            float y1 = event.getY(0);
            float y2 = event.getY(1);

            mMidX = (x1 + x2) / 2;
            mMidY = (y1 + y2) / 2;

        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchX = (int) event.getX();
                mLastTouchY = (int) event.getY();

                mActivePointId = event.getPointerId(0);
                break;

            case MotionEvent.ACTION_MOVE:
                pointerIndex = event.findPointerIndex(mActivePointId);

                if(!mScaleDetector.isInProgress()) {
                    mDeltaX = (int) event.getX(pointerIndex) - mLastTouchX;
                    mDeltaY = (int) event.getY(pointerIndex) - mLastTouchY;
                }

                mLastTouchX = (int) event.getX(pointerIndex);
                mLastTouchY = (int) event.getY(pointerIndex);
                mMapMatrix.postTranslate(mDeltaX, mDeltaY);

                mCenter[0] = mRelatedX;
                mCenter[1] = mRelatedY;
                mMapMatrix.mapPoints(mCenter);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointId = INVALID_POINT_ID;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // Extract the index of the pointer that left the touch sensor
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                int pointerId = event.getPointerId(pointerIndex);
                if(pointerId == mActivePointId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;

                    mLastTouchX = (int) event.getX(newPointerIndex);
                    mLastTouchY = (int) event.getY(newPointerIndex);
                    mActivePointId = event.getPointerId(newPointerIndex);
                }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!isInEditMode()) {

            canvas.drawColor(Color.GRAY);

            canvas.drawBitmap(mMapBitmap, mMapMatrix, null);

            if(mDisplayDot) {
                canvas.drawCircle(mCenter[0], mCenter[1], 20, mFillPaint);
                canvas.drawCircle(mCenter[0], mCenter[1], 20, mOutlinePaint);
            }

            if(mDialogBitmap != null && mDisplayDialog) {
                float dialogX = mCenter[0] - (mDialogBitmap.getWidth() / 2);
                float dialogY = mCenter[1] - (mDialogBitmap.getHeight());

                canvas.drawBitmap(mDialogBitmap, dialogX, dialogY, null);
            }

            mScaleFactorDelta = 1.f;

        }
    }


    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float oldScaleFactor = mScaleFactor;
            mScaleFactor *= detector.getScaleFactor();

            mScaleFactorDelta = mScaleFactor / oldScaleFactor;

            mMapMatrix.postScale(mScaleFactorDelta, mScaleFactorDelta, mMidX, mMidY);

            invalidate();
            return true;
        }
    }

    public class SingleTapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {


            mDisplayDot = true;

            float[] coords = new float[]{e.getX(), e.getY()};
            Matrix m = new Matrix();
            mMapMatrix.invert(m);

            m.mapPoints(coords);
            mRelatedX = coords[0];
            mRelatedY = coords[1];


            mCenter[0] = mRelatedX;
            mCenter[1] = mRelatedY;
            mMapMatrix.mapPoints(mCenter);

            invalidate();


            return super.onSingleTapConfirmed(e);
        }
    }




    public class CustomThread extends Thread {
        private boolean mRunning;
        private Canvas mCanvas;
        private SurfaceHolder mSurfaceHolder;
        private MapSurfaceView mSurfaceView;

        public CustomThread(SurfaceHolder holder, Context context, MapSurfaceView surfaceView) {
            mSurfaceHolder = holder;
            mContext = context;
            mSurfaceView = surfaceView;
        }

        void setRunning(boolean running) {
            mRunning = running;
        }

        @Override
        public void run() {
            super.run();
            while(mRunning) {
                mCanvas = mSurfaceHolder.lockCanvas();
                if(mCanvas != null) {
                    mSurfaceView.onDraw(mCanvas);
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }

            }
        }
    }


}
