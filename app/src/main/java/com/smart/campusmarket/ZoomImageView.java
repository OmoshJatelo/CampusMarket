package com.smart.campusmarket;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * An image you can pinch to zoom, drag when zoomed, and double-tap to zoom in or out.
 * When it is not zoomed it lets the surrounding pager handle left/right swipes.
 */
public class ZoomImageView extends AppCompatImageView {

    private static final float MAX_ZOOM = 4f;        // relative to the "fit on screen" size
    private static final float DOUBLE_TAP_ZOOM = 2.5f;

    private final Matrix matrix = new Matrix();
    private final float[] values = new float[9];
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestures;

    private float baseScale = 1f;   // the scale at which the whole picture fits
    private float lastX, lastY;

    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);

        scaleDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        zoomBy(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
                        return true;
                    }
                });

        gestures = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isZoomed()) fitToView();
                else zoomTo(baseScale * DOUBLE_TAP_ZOOM, e.getX(), e.getY());
                return true;
            }
        });
    }

    // ---------------------------------------------------------------- layout

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        fitToView();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fitToView();
    }

    private void fitToView() {
        Drawable d = getDrawable();
        if (matrix == null || d == null || getWidth() == 0 || getHeight() == 0) return;
        float dw = d.getIntrinsicWidth();
        float dh = d.getIntrinsicHeight();
        if (dw <= 0 || dh <= 0) return;

        baseScale = Math.min(getWidth() / dw, getHeight() / dh);
        matrix.reset();
        matrix.postScale(baseScale, baseScale);
        matrix.postTranslate((getWidth() - dw * baseScale) / 2f, (getHeight() - dh * baseScale) / 2f);
        setImageMatrix(matrix);
    }

    // ----------------------------------------------------------------- zoom

    private float currentScale() {
        matrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    private boolean isZoomed() {
        return currentScale() > baseScale * 1.01f;
    }

    private void zoomBy(float factor, float px, float py) {
        float current = currentScale();
        float target = Math.max(baseScale, Math.min(current * factor, baseScale * MAX_ZOOM));
        applyScale(target / current, px, py);
    }

    private void zoomTo(float targetScale, float px, float py) {
        float target = Math.max(baseScale, Math.min(targetScale, baseScale * MAX_ZOOM));
        applyScale(target / currentScale(), px, py);
    }

    private void applyScale(float factor, float px, float py) {
        matrix.postScale(factor, factor, px, py);
        clampToBounds();
        setImageMatrix(matrix);
    }

    /** Keeps the picture from being dragged off the screen, and centres it when smaller than the view. */
    private void clampToBounds() {
        Drawable d = getDrawable();
        if (d == null) return;
        RectF r = new RectF(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        matrix.mapRect(r);

        float dx = 0, dy = 0;
        if (r.width() <= getWidth()) dx = (getWidth() - r.width()) / 2f - r.left;
        else if (r.left > 0) dx = -r.left;
        else if (r.right < getWidth()) dx = getWidth() - r.right;

        if (r.height() <= getHeight()) dy = (getHeight() - r.height()) / 2f - r.top;
        else if (r.top > 0) dy = -r.top;
        else if (r.bottom < getHeight()) dy = getHeight() - r.bottom;

        matrix.postTranslate(dx, dy);
    }

    // ----------------------------------------------------------------- touch

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        scaleDetector.onTouchEvent(e);
        gestures.onTouchEvent(e);

        boolean zoomed = isZoomed();
        if (getParent() != null) {
            // Zoomed in or pinching: keep the touches. Otherwise let the pager swipe.
            getParent().requestDisallowInterceptTouchEvent(zoomed || e.getPointerCount() > 1);
        }

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = e.getX();
                lastY = e.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (zoomed && !scaleDetector.isInProgress() && e.getPointerCount() == 1) {
                    matrix.postTranslate(e.getX() - lastX, e.getY() - lastY);
                    clampToBounds();
                    setImageMatrix(matrix);
                }
                lastX = e.getX();
                lastY = e.getY();
                break;
            case MotionEvent.ACTION_POINTER_UP: {
                int remaining = e.getActionIndex() == 0 ? 1 : 0;   // avoid a jump when one finger lifts
                lastX = e.getX(remaining);
                lastY = e.getY(remaining);
                break;
            }
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}