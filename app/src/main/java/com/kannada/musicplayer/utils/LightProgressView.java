package com.demo.musicvideoplayer.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import com.demo.musicvideoplayer.R;

public class LightProgressView extends View {
    public float circleR;
    public Path mCirclePath;
    private int mHaloColor;
    private float mHaloHeight;
    private float mHaloWidth;
    private RectF mLayer;
    private PathMeasure mMeasure;
    private int mMoonColor;
    private int mNumOfHalo;
    private float mOneOFHaleDegrees;
    private float mOneOFHaleProgress;
    public Paint mOpPaint;
    public Paint mPaint;
    private float mProgress;
    private Path mQuadPath;
    private float magicNum;

    public LightProgressView(Context context) {
        super(context);
        this.mProgress = 0.0f;
        this.circleR = 0.0f;
        this.mHaloHeight = 7.0f;
        this.mHaloWidth = 2.0f;
        this.mNumOfHalo = 16;
        this.mOneOFHaleDegrees = 360.0f / ((float) 16);
        this.mOneOFHaleProgress = 1.0f / ((float) 16);
        this.magicNum = 0.43f;
        this.mMoonColor = -1;
        this.mHaloColor = -1;
    }

    public LightProgressView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, -1);
    }

    public LightProgressView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mProgress = 0.0f;
        this.circleR = 0.0f;
        this.mHaloHeight = 7.0f;
        this.mHaloWidth = 2.0f;
        this.mNumOfHalo = 16;
        this.mOneOFHaleDegrees = 360.0f / ((float) 16);
        this.mOneOFHaleProgress = 1.0f / ((float) 16);
        this.magicNum = 0.43f;
        this.mMoonColor = -1;
        this.mHaloColor = -1;
        initAttr(context, attributeSet);
        init();
    }

    public LightProgressView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mProgress = 0.0f;
        this.circleR = 0.0f;
        this.mHaloHeight = 7.0f;
        this.mHaloWidth = 2.0f;
        this.mNumOfHalo = 16;
        this.mOneOFHaleDegrees = 360.0f / ((float) 16);
        this.mOneOFHaleProgress = 1.0f / ((float) 16);
        this.magicNum = 0.43f;
        this.mMoonColor = -1;
        this.mHaloColor = -1;
        initAttr(context, attributeSet);
        init();
    }

    private void initAttr(Context context, AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.LightProgressView);
        this.mHaloHeight = dp2px(this.mHaloHeight);
        this.mHaloWidth = dp2px(this.mHaloWidth);
        this.mHaloHeight = obtainStyledAttributes.getDimension(1, this.mHaloHeight);
        this.mHaloWidth = obtainStyledAttributes.getDimension(2, this.mHaloWidth);
        this.mNumOfHalo = obtainStyledAttributes.getInteger(5, this.mNumOfHalo);
        this.magicNum = obtainStyledAttributes.getFloat(3, this.magicNum);
        this.mMoonColor = obtainStyledAttributes.getColor(4, this.mMoonColor);
        this.mHaloColor = obtainStyledAttributes.getColor(0, this.mHaloColor);
        obtainStyledAttributes.recycle();
    }

    private void init() {
        this.mMeasure = new PathMeasure();
        this.mQuadPath = new Path();
        this.mCirclePath = new Path();
        this.mLayer = new RectF();
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setColor(this.mMoonColor);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Paint.Style.FILL);
        Paint paint2 = new Paint();
        this.mOpPaint = paint2;
        paint2.setColor(this.mMoonColor);
        this.mOpPaint.setAntiAlias(true);
        this.mOpPaint.setStyle(Paint.Style.FILL);
        this.mOpPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        setWillNotDraw(false);
    }

    @Override
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        float paddingLeft = (float) getPaddingLeft();
        float paddingRight = (float) getPaddingRight();
        float paddingTop = (float) getPaddingTop();
        float paddingBottom = (float) getPaddingBottom();
        float f = this.mHaloHeight + (this.mHaloWidth * 2.0f);
        float f2 = (float) i;
        float f3 = (float) i2;
        this.mLayer.set(f + paddingLeft, f + paddingTop, (f2 - f) - paddingRight, (f3 - f) - paddingBottom);
        this.mCirclePath.reset();
        this.circleR = i > i2 ? (((f3 - (f * 2.0f)) - paddingTop) - paddingBottom) / 2.0f : (((f2 - (f * 2.0f)) - paddingLeft) - paddingRight) / 2.0f;
        this.mCirclePath.addCircle(this.mLayer.centerX(), this.mLayer.centerY(), this.circleR, Path.Direction.CW);
        this.mMeasure.setPath(this.mCirclePath, false);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPath(canvas);
    }

    private void drawPath(Canvas canvas) {
        float f = this.mProgress;
        float[] fArr = {0.0f, 0.0f};
        float[] fArr2 = {0.0f, 0.0f};
        getBeginPoint(f, fArr);
        getSecondPoint(f, fArr2);
        this.mQuadPath.reset();
        this.mQuadPath.moveTo(fArr[0], fArr[1]);
        float[] contrlPoint = getContrlPoint(new float[]{fArr[0], fArr[1]}, fArr2);
        this.mQuadPath.quadTo(contrlPoint[0], contrlPoint[1], fArr2[0], fArr2[1]);
        Pair<Float, Float> angle = getAngle(fArr, fArr2);
        RectF rectF = new RectF(this.mLayer);
        rectF.left -= 2.0f;
        rectF.top -= 2.0f;
        rectF.right += 2.0f;
        rectF.bottom += 2.0f;
        this.mQuadPath.arcTo(rectF, ((Float) angle.first).floatValue(), ((Float) angle.second).floatValue());
        this.mQuadPath.moveTo(fArr[0], fArr[1]);
        this.mQuadPath.close();
        canvas.saveLayer(this.mLayer, (Paint) null, 31);
        this.mPaint.setColor(this.mMoonColor);
        canvas.drawPath(this.mCirclePath, this.mPaint);
        canvas.drawPath(this.mQuadPath, this.mOpPaint);
        canvas.restore();
        canvas.save();
        canvas.translate(this.mLayer.centerX(), this.mLayer.centerY());
        int i = this.mNumOfHalo - ((int) (f / this.mOneOFHaleProgress));
        float f2 = this.mHaloWidth / 2.0f;
        this.mPaint.setColor(this.mHaloColor);
        for (int i2 = 0; i2 < i; i2++) {
            canvas.drawRoundRect(new RectF(-f2, (-this.mLayer.centerY()) + ((float) getPaddingTop()), f2, (this.mHaloHeight - this.mLayer.centerY()) + ((float) getPaddingTop())), f2, f2, this.mPaint);
            canvas.rotate(this.mOneOFHaleDegrees);
        }
        canvas.restore();
    }

    private void getBeginPoint(float f, float[] fArr) {
        if (((double) f) <= 0.5d) {
            PathMeasure pathMeasure = this.mMeasure;
            pathMeasure.getPosTan(pathMeasure.getLength() * ((f * -0.2f) + 0.1f), fArr, (float[]) null);
            return;
        }
        PathMeasure pathMeasure2 = this.mMeasure;
        pathMeasure2.getPosTan(pathMeasure2.getLength() * ((f * -0.2f) + 1.1f), fArr, (float[]) null);
    }

    private void getSecondPoint(float f, float[] fArr) {
        if (((double) f) <= 0.1d) {
            PathMeasure pathMeasure = this.mMeasure;
            pathMeasure.getPosTan(pathMeasure.getLength() * ((f * -1.0f) + 0.1f), fArr, (float[]) null);
            return;
        }
        PathMeasure pathMeasure2 = this.mMeasure;
        pathMeasure2.getPosTan(pathMeasure2.getLength() * ((f * -0.7777778f) + 1.0777777f), fArr, (float[]) null);
    }

    private Pair<Float, Float> getAngle(float[] fArr, float[] fArr2) {
        float f;
        float f2;
        float f3;
        float centerX = this.mLayer.centerX();
        float centerY = this.mLayer.centerY();
        if (fArr2[0] <= centerX || fArr2[1] <= centerY) {
            if (fArr2[0] > centerX) {
                f3 = fArr2[1] < centerY ? (float) Math.toDegrees(Math.asin((double) ((centerY - fArr2[1]) / this.circleR))) : 0.0f;
            } else if (fArr2[1] < centerY) {
                f3 = 180.0f - ((float) Math.toDegrees(Math.asin((double) ((centerY - fArr2[1]) / this.circleR))));
            } else {
                f3 = ((float) Math.toDegrees(Math.asin((double) ((fArr2[1] - centerY) / this.circleR)))) + 180.0f;
            }
            f2 = f3 - ((float) Math.toDegrees(Math.asin((double) ((centerY - fArr[1]) / this.circleR))));
            f = 360.0f - f3;
        } else {
            f = (float) Math.toDegrees(Math.asin((double) ((fArr2[1] - centerY) / this.circleR)));
            f2 = ((float) Math.toDegrees(Math.asin((double) ((fArr[1] - centerY) / this.circleR)))) - f;
        }
        return new Pair<>(Float.valueOf(f), Float.valueOf(f2));
    }

    private float[] getContrlPoint(float[] fArr, float[] fArr2) {
        float centerX = this.mLayer.centerX();
        float centerY = this.mLayer.centerY();
        float sqrt = (float) Math.sqrt((double) (((fArr[0] - fArr2[0]) * (fArr[0] - fArr2[0])) + ((fArr[1] - fArr2[1]) * (fArr[1] - fArr2[1]))));
        float f = (fArr[0] - fArr2[0]) / (fArr2[1] - fArr[1]);
        float f2 = ((fArr[1] + fArr2[1]) / 2.0f) - ((((fArr[0] * fArr[0]) - (fArr2[0] * fArr2[0])) / 2.0f) / (fArr2[1] - fArr[1]));
        float[] fArr3 = {0.0f, 0.0f};
        float sqrt2 = (float) (1.0d / Math.sqrt((double) ((f * f) + 1.0f)));
        if (f < 0.0f) {
            fArr3[0] = ((fArr[0] + fArr2[0]) / 2.0f) - ((sqrt2 * sqrt) * this.magicNum);
        } else if (f <= 0.0f) {
            fArr3[0] = (fArr[0] + fArr2[0]) / 2.0f;
        } else if (fArr[0] <= centerX || fArr[1] <= centerY || fArr2[0] <= centerX) {
            fArr3[0] = ((fArr[0] + fArr2[0]) / 2.0f) + (sqrt2 * sqrt * this.magicNum);
        } else {
            fArr3[0] = ((fArr[0] + fArr2[0]) / 2.0f) - ((sqrt2 * sqrt) * this.magicNum);
        }
        fArr3[1] = (f * fArr3[0]) + f2;
        return fArr3;
    }

    private float dp2px(float f) {
        return TypedValue.applyDimension(1, f, getContext().getResources().getDisplayMetrics());
    }

    public void setProgress(float f) {
        this.mProgress = 1.0f - f;
        postInvalidate();
    }
}
