package com.kannada.musicplayer.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.kannada.musicplayer.R;

public class VolumeProgressView extends View {
    private int drawable1;
    private int drawable2;
    private int drawable3;
    private int mHaloColor;
    private float mHaloHeight;
    private float mHaloWidth;
    private RectF mLayer;
    private int mNumOfHalo;
    private float mOneOFHaleDegrees;
    private float mOneOFHaleProgress;
    public Paint mPaint;
    private float mProgress;
    private Bitmap volume1;
    private Bitmap volume2;
    private Bitmap volume3;

    public VolumeProgressView(Context context) {
        this(context, (AttributeSet) null);
    }

    public VolumeProgressView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, -1);
    }

    public VolumeProgressView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mProgress = 0.0f;
        this.mHaloHeight = 7.0f;
        this.mHaloWidth = 2.0f;
        this.mNumOfHalo = 16;
        this.mOneOFHaleDegrees = 360.0f / ((float) 16);
        this.mOneOFHaleProgress = 1.0f / ((float) 16);
        this.drawable1 = R.drawable.volume_low;
        this.drawable2 = R.drawable.volume_medium;
        this.drawable3 = R.drawable.volume_high;
        this.mHaloColor = -1;
        initAttr(context, attributeSet);
        init();
    }

    public VolumeProgressView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mProgress = 0.0f;
        this.mHaloHeight = 7.0f;
        this.mHaloWidth = 2.0f;
        this.mNumOfHalo = 16;
        this.mOneOFHaleDegrees = 360.0f / ((float) 16);
        this.mOneOFHaleProgress = 1.0f / ((float) 16);
        this.drawable1 = R.drawable.volume_low;
        this.drawable2 = R.drawable.volume_medium;
        this.drawable3 = R.drawable.volume_high;
        this.mHaloColor = -1;
        initAttr(context, attributeSet);
        init();
    }

    private void initAttr(Context context, AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.VolumeProgressView);
        this.mHaloHeight = dp2px(this.mHaloHeight);
        this.mHaloWidth = dp2px(this.mHaloWidth);
        this.mHaloHeight = obtainStyledAttributes.getDimension(1, this.mHaloHeight);
        this.mHaloWidth = obtainStyledAttributes.getDimension(2, this.mHaloWidth);
        this.mNumOfHalo = obtainStyledAttributes.getInteger(3, this.mNumOfHalo);
        this.drawable1 = obtainStyledAttributes.getResourceId(5, this.drawable1);
        this.drawable2 = obtainStyledAttributes.getResourceId(6, this.drawable2);
        this.drawable3 = obtainStyledAttributes.getResourceId(4, this.drawable3);
        this.mHaloColor = obtainStyledAttributes.getColor(0, this.mHaloColor);
        obtainStyledAttributes.recycle();
    }

    private void init() {
        this.mLayer = new RectF();
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setAntiAlias(true);
        this.mPaint.setColor(this.mHaloColor);
        this.mPaint.setStyle(Paint.Style.FILL);
        setWillNotDraw(false);
    }

    
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.volume1 = BitmapFactory.decodeResource(getResources(), this.drawable1);
        this.volume2 = BitmapFactory.decodeResource(getResources(), this.drawable2);
        this.volume3 = BitmapFactory.decodeResource(getResources(), this.drawable3);
    }

    
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        float f = this.mHaloHeight + (this.mHaloWidth * 2.0f);
        this.mLayer.set(((float) getPaddingLeft()) + f, ((float) getPaddingTop()) + f, (((float) i) - f) - ((float) getPaddingRight()), (((float) i2) - f) - ((float) getPaddingBottom()));
    }

    
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPath(canvas);
    }

    private void drawPath(Canvas canvas) {
        float f = this.mProgress;
        int i = (int) ((1.0f - f) / 0.33f);
        if (i == 0) {
            canvas.drawBitmap(this.volume1, (Rect) null, this.mLayer, this.mPaint);
        } else if (i == 1) {
            canvas.drawBitmap(this.volume2, (Rect) null, this.mLayer, this.mPaint);
        } else {
            canvas.drawBitmap(this.volume3, (Rect) null, this.mLayer, this.mPaint);
        }
        canvas.save();
        canvas.translate(this.mLayer.centerX(), this.mLayer.centerY());
        int i2 = this.mNumOfHalo - ((int) (f / this.mOneOFHaleProgress));
        float f2 = this.mHaloWidth / 2.0f;
        for (int i3 = 0; i3 < i2; i3++) {
            canvas.drawRoundRect(new RectF(-f2, (-this.mLayer.centerY()) + ((float) getPaddingTop()), f2, (this.mHaloHeight - this.mLayer.centerY()) + ((float) getPaddingTop())), f2, f2, this.mPaint);
            canvas.rotate(this.mOneOFHaleDegrees);
        }
        canvas.restore();
    }

    private float dp2px(float f) {
        return TypedValue.applyDimension(1, f, getContext().getResources().getDisplayMetrics());
    }

    public void setProgress(float f) {
        this.mProgress = 1.0f - f;
        postInvalidate();
    }

    
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Bitmap bitmap = this.volume1;
        if (bitmap != null && !bitmap.isRecycled()) {
            this.volume1.recycle();
            this.volume1 = null;
        }
        Bitmap bitmap2 = this.volume2;
        if (bitmap2 != null && !bitmap2.isRecycled()) {
            this.volume2.recycle();
            this.volume2 = null;
        }
        Bitmap bitmap3 = this.volume3;
        if (bitmap3 != null && !bitmap3.isRecycled()) {
            this.volume3.recycle();
            this.volume3 = null;
        }
    }
}
