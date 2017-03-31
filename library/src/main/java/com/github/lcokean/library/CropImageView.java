package com.github.lcokean.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 局下或局上裁剪（系统只有居中裁剪CenterCrop）
 *
 * @author pengj
 * @version 2017/2/23
 */

public class CropImageView extends android.support.v7.widget.AppCompatImageView {

    @IntDef({SCALETYPE_TOP_CROP, SCALETYPE_BOTTOM_CROP, SCALETYPE_LEFT_CROP, SCALETYPE_RIGHT_CROP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScaleType {
    }

    public static final int SCALETYPE_TOP_CROP = 1;
    public static final int SCALETYPE_BOTTOM_CROP = 2;
    public static final int SCALETYPE_LEFT_CROP = 3;
    public static final int SCALETYPE_RIGHT_CROP = 4;

    private int mScaleType = SCALETYPE_BOTTOM_CROP;
    private Matrix mCropMatrix;

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        super.setScaleType(ImageView.ScaleType.MATRIX);
        if (attrs != null) {
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.CropImageView);
            try {
                mScaleType = array.getInt(R.styleable.CropImageView_cropType, mScaleType);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                array.recycle();
            }
        }
    }

    public void setCropType(@ScaleType int scaleType) {
        mScaleType = scaleType;
    }

    @Override
    public void setScaleType(ImageView.ScaleType scaleType) {
        //super.setScaleType(ImageView.ScaleType.MATRIX);
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        configureBounds();
        return changed;
    }

    private void configureBounds() {
        if (getDrawable() == null) {
            return;
        }

        int dwidth = getDrawable().getIntrinsicWidth();
        int dheight = getDrawable().getIntrinsicHeight();

        int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int vheight = getHeight() - getPaddingTop() - getPaddingBottom();

        if (dwidth <= 0 || dheight <= 0) {
            getDrawable().setBounds(0, 0, vwidth, vheight);
        } else {
            // We need to do the scaling ourself, so have the drawable
            // use its native size.
            getDrawable().setBounds(0, 0, dwidth, dheight);

            mCropMatrix = new Matrix();

            float scale;
            float dx = 0, dy = 0;

            if (dwidth * vheight > vwidth * dheight) {
                float rate = mScaleType == SCALETYPE_RIGHT_CROP ? 1.0f : 0f;
                scale = (float) vheight / (float) dheight;
                dx = (vwidth - dwidth * scale) * rate;
            } else {
                float rate = mScaleType == SCALETYPE_BOTTOM_CROP ? 1.0f : 0f;
                scale = (float) vwidth / (float) dwidth;
                dy = (vheight - dheight * scale) * rate;
            }

            mCropMatrix.setScale(scale, scale);
            mCropMatrix.postTranslate(Math.round(dx), Math.round(dy));

            setImageMatrix(mCropMatrix);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getImageMatrix() == null || !getImageMatrix().equals(mCropMatrix)) {
            configureBounds();
        }
        super.onDraw(canvas);
    }
}
