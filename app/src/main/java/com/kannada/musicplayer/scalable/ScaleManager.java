package com.kannada.musicplayer.scalable;

import android.graphics.Matrix;

public class ScaleManager {
    private Size mVideoSize;
    private Size mViewSize;

    public ScaleManager(Size size, Size size2) {
        this.mViewSize = size;
        this.mVideoSize = size2;
    }

    public Matrix getScaleMatrix(ScalableType scalableType) {
        switch (AnonymousClass1.SwitchMapvideoplayerscalableScalableType[scalableType.ordinal()]) {
            case 1:
                return getNoScale();
            case 2:
                return fitXY();
            case 3:
                return fitCenter();
            case 4:
                return fitStart();
            case 5:
                return fitEnd();
            case 6:
                return getOriginalScale(PivotPoint.LEFT_TOP);
            case 7:
                return getOriginalScale(PivotPoint.LEFT_CENTER);
            case 8:
                return getOriginalScale(PivotPoint.LEFT_BOTTOM);
            case 9:
                return getOriginalScale(PivotPoint.CENTER_TOP);
            case 10:
                return getOriginalScale(PivotPoint.CENTER);
            case 11:
                return getOriginalScale(PivotPoint.CENTER_BOTTOM);
            case 12:
                return getOriginalScale(PivotPoint.RIGHT_TOP);
            case 13:
                return getOriginalScale(PivotPoint.RIGHT_CENTER);
            case 14:
                return getOriginalScale(PivotPoint.RIGHT_BOTTOM);
            case 15:
                return getCropScale(PivotPoint.LEFT_TOP);
            case 16:
                return getCropScale(PivotPoint.LEFT_CENTER);
            case 17:
                return getCropScale(PivotPoint.LEFT_BOTTOM);
            case 18:
                return getCropScale(PivotPoint.CENTER_TOP);
            case 19:
                return getCropScale(PivotPoint.CENTER);
            case 20:
                return getCropScale(PivotPoint.CENTER_BOTTOM);
            case 21:
                return getCropScale(PivotPoint.RIGHT_TOP);
            case 22:
                return getCropScale(PivotPoint.RIGHT_CENTER);
            case 23:
                return getCropScale(PivotPoint.RIGHT_BOTTOM);
            case 24:
                return startInside();
            case 25:
                return centerInside();
            case 26:
                return endInside();
            default:
                return null;
        }
    }

    private Matrix getMatrix(float f, float f2, float f3, float f4) {
        Matrix matrix = new Matrix();
        matrix.setScale(f, f2, f3, f4);
        return matrix;
    }

    static  class AnonymousClass1 {
        static final  int[] SwitchMapvideoplayerscalablePivotPoint;
        static final  int[] SwitchMapvideoplayerscalableScalableType;

        static {
            int[] iArr = new int[PivotPoint.values().length];
            SwitchMapvideoplayerscalablePivotPoint = iArr;
            try {
                iArr[PivotPoint.LEFT_TOP.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                SwitchMapvideoplayerscalablePivotPoint[PivotPoint.LEFT_CENTER.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                SwitchMapvideoplayerscalablePivotPoint[PivotPoint.LEFT_BOTTOM.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                SwitchMapvideoplayerscalablePivotPoint[PivotPoint.CENTER_TOP.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                SwitchMapvideoplayerscalablePivotPoint[PivotPoint.CENTER.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                SwitchMapvideoplayerscalablePivotPoint[PivotPoint.CENTER_BOTTOM.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                SwitchMapvideoplayerscalablePivotPoint[PivotPoint.RIGHT_TOP.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                SwitchMapvideoplayerscalablePivotPoint[PivotPoint.RIGHT_CENTER.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                SwitchMapvideoplayerscalablePivotPoint[PivotPoint.RIGHT_BOTTOM.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            int[] iArr2 = new int[ScalableType.values().length];
            SwitchMapvideoplayerscalableScalableType = iArr2;
            iArr2[ScalableType.NONE.ordinal()] = 1;
            SwitchMapvideoplayerscalableScalableType[ScalableType.FIT_XY.ordinal()] = 2;
            SwitchMapvideoplayerscalableScalableType[ScalableType.FIT_CENTER.ordinal()] = 3;
            SwitchMapvideoplayerscalableScalableType[ScalableType.FIT_START.ordinal()] = 4;
            SwitchMapvideoplayerscalableScalableType[ScalableType.FIT_END.ordinal()] = 5;
            SwitchMapvideoplayerscalableScalableType[ScalableType.LEFT_TOP.ordinal()] = 6;
            SwitchMapvideoplayerscalableScalableType[ScalableType.LEFT_CENTER.ordinal()] = 7;
            SwitchMapvideoplayerscalableScalableType[ScalableType.LEFT_BOTTOM.ordinal()] = 8;
            SwitchMapvideoplayerscalableScalableType[ScalableType.CENTER_TOP.ordinal()] = 9;
            SwitchMapvideoplayerscalableScalableType[ScalableType.CENTER.ordinal()] = 10;
            SwitchMapvideoplayerscalableScalableType[ScalableType.CENTER_BOTTOM.ordinal()] = 11;
            SwitchMapvideoplayerscalableScalableType[ScalableType.RIGHT_TOP.ordinal()] = 12;
            SwitchMapvideoplayerscalableScalableType[ScalableType.RIGHT_CENTER.ordinal()] = 13;
            SwitchMapvideoplayerscalableScalableType[ScalableType.RIGHT_BOTTOM.ordinal()] = 14;
            SwitchMapvideoplayerscalableScalableType[ScalableType.LEFT_TOP_CROP.ordinal()] = 15;
            SwitchMapvideoplayerscalableScalableType[ScalableType.LEFT_CENTER_CROP.ordinal()] = 16;
            SwitchMapvideoplayerscalableScalableType[ScalableType.LEFT_BOTTOM_CROP.ordinal()] = 17;
            SwitchMapvideoplayerscalableScalableType[ScalableType.CENTER_TOP_CROP.ordinal()] = 18;
            SwitchMapvideoplayerscalableScalableType[ScalableType.CENTER_CROP.ordinal()] = 19;
            SwitchMapvideoplayerscalableScalableType[ScalableType.CENTER_BOTTOM_CROP.ordinal()] = 20;
            SwitchMapvideoplayerscalableScalableType[ScalableType.RIGHT_TOP_CROP.ordinal()] = 21;
            SwitchMapvideoplayerscalableScalableType[ScalableType.RIGHT_CENTER_CROP.ordinal()] = 22;
            SwitchMapvideoplayerscalableScalableType[ScalableType.RIGHT_BOTTOM_CROP.ordinal()] = 23;
            SwitchMapvideoplayerscalableScalableType[ScalableType.START_INSIDE.ordinal()] = 24;
            SwitchMapvideoplayerscalableScalableType[ScalableType.CENTER_INSIDE.ordinal()] = 25;
            try {
                SwitchMapvideoplayerscalableScalableType[ScalableType.END_INSIDE.ordinal()] = 26;
            } catch (NoSuchFieldError unused10) {
            }
        }
    }

    private Matrix getMatrix(float f, float f2, PivotPoint pivotPoint) {
        switch (AnonymousClass1.SwitchMapvideoplayerscalablePivotPoint[pivotPoint.ordinal()]) {
            case 1:
                return getMatrix(f, f2, 0.0f, 0.0f);
            case 2:
                return getMatrix(f, f2, 0.0f, ((float) this.mViewSize.getHeight()) / 2.0f);
            case 3:
                return getMatrix(f, f2, 0.0f, (float) this.mViewSize.getHeight());
            case 4:
                return getMatrix(f, f2, ((float) this.mViewSize.getWidth()) / 2.0f, 0.0f);
            case 5:
                return getMatrix(f, f2, ((float) this.mViewSize.getWidth()) / 2.0f, ((float) this.mViewSize.getHeight()) / 2.0f);
            case 6:
                return getMatrix(f, f2, ((float) this.mViewSize.getWidth()) / 2.0f, (float) this.mViewSize.getHeight());
            case 7:
                return getMatrix(f, f2, (float) this.mViewSize.getWidth(), 0.0f);
            case 8:
                return getMatrix(f, f2, (float) this.mViewSize.getWidth(), ((float) this.mViewSize.getHeight()) / 2.0f);
            case 9:
                return getMatrix(f, f2, (float) this.mViewSize.getWidth(), (float) this.mViewSize.getHeight());
            default:
                throw new IllegalArgumentException("Illegal PivotPoint");
        }
    }

    private Matrix getNoScale() {
        return getMatrix(((float) this.mVideoSize.getWidth()) / ((float) this.mViewSize.getWidth()), ((float) this.mVideoSize.getHeight()) / ((float) this.mViewSize.getHeight()), PivotPoint.LEFT_TOP);
    }

    private Matrix getFitScale(PivotPoint pivotPoint) {
        float width = ((float) this.mViewSize.getWidth()) / ((float) this.mVideoSize.getWidth());
        float height = ((float) this.mViewSize.getHeight()) / ((float) this.mVideoSize.getHeight());
        float min = Math.min(width, height);
        return getMatrix(min / width, min / height, pivotPoint);
    }

    private Matrix fitXY() {
        return getMatrix(1.0f, 1.0f, PivotPoint.LEFT_TOP);
    }

    private Matrix fitStart() {
        return getFitScale(PivotPoint.LEFT_TOP);
    }

    private Matrix fitCenter() {
        return getFitScale(PivotPoint.CENTER);
    }

    private Matrix fitEnd() {
        return getFitScale(PivotPoint.RIGHT_BOTTOM);
    }

    private Matrix getOriginalScale(PivotPoint pivotPoint) {
        return getMatrix(((float) this.mVideoSize.getWidth()) / ((float) this.mViewSize.getWidth()), ((float) this.mVideoSize.getHeight()) / ((float) this.mViewSize.getHeight()), pivotPoint);
    }

    private Matrix getCropScale(PivotPoint pivotPoint) {
        float width = ((float) this.mViewSize.getWidth()) / ((float) this.mVideoSize.getWidth());
        float height = ((float) this.mViewSize.getHeight()) / ((float) this.mVideoSize.getHeight());
        float max = Math.max(width, height);
        return getMatrix(max / width, max / height, pivotPoint);
    }

    private Matrix startInside() {
        if (this.mVideoSize.getHeight() > this.mViewSize.getWidth() || this.mVideoSize.getHeight() > this.mViewSize.getHeight()) {
            return fitStart();
        }
        return getOriginalScale(PivotPoint.LEFT_TOP);
    }

    private Matrix centerInside() {
        if (this.mVideoSize.getHeight() > this.mViewSize.getWidth() || this.mVideoSize.getHeight() > this.mViewSize.getHeight()) {
            return fitCenter();
        }
        return getOriginalScale(PivotPoint.CENTER);
    }

    private Matrix endInside() {
        if (this.mVideoSize.getHeight() > this.mViewSize.getWidth() || this.mVideoSize.getHeight() > this.mViewSize.getHeight()) {
            return fitEnd();
        }
        return getOriginalScale(PivotPoint.RIGHT_BOTTOM);
    }
}
