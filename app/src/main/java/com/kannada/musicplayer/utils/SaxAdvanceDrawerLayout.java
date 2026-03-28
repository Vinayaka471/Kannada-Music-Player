package com.demo.musicvideoplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;

public class SaxAdvanceDrawerLayout extends DrawerLayout {
    static final boolean $assertionsDisabled = false;
    private static final String TAG = "AdvanceDrawerLayout";
    private float contrastThreshold = 3.0f;

    public float defaultDrawerElevation;
    private boolean defaultFitsSystemWindows;

    public int defaultScrimColor = -1728053248;
    public View drawerView;
    private FrameLayout frameLayout;
    HashMap<Integer, Setting> settings = new HashMap<>();
    private int statusBarColor;

    public SaxAdvanceDrawerLayout(Context context) {
        super(context);
        init(context, (AttributeSet) null, 0);
    }

    public SaxAdvanceDrawerLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet, 0);
    }

    public SaxAdvanceDrawerLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context, attributeSet, i);
    }

    private void init(Context context, AttributeSet attributeSet, int i) {
        this.defaultDrawerElevation = getDrawerElevation();
        if (Build.VERSION.SDK_INT >= 16) {
            this.defaultFitsSystemWindows = getFitsSystemWindows();
        }
        if (!isInEditMode() && Build.VERSION.SDK_INT >= 21) {
            this.statusBarColor = getActivity().getWindow().getStatusBarColor();
        }
        addDrawerListener(new DrawerListener() {
            @Override
            public void onDrawerClosed(View view) {
            }

            @Override
            public void onDrawerOpened(View view) {
            }

            @Override
            public void onDrawerStateChanged(int i) {
            }

            @Override
            public void onDrawerSlide(View view, float f) {
                SaxAdvanceDrawerLayout.this.updateSlideOffset(view, f);
            }
        });
        FrameLayout frameLayout2 = new FrameLayout(context);
        this.frameLayout = frameLayout2;
        frameLayout2.setPadding(0, 0, 0, 0);
        super.addView(this.frameLayout);
    }

    public void addView(View view, ViewGroup.LayoutParams layoutParams) {
        view.setLayoutParams(layoutParams);
        addView(view);
    }

    public void addView(View view) {
        if (view instanceof NavigationView) {
            super.addView(view);
            return;
        }
        CardView cardView = new CardView(getContext());
        cardView.setRadius(0.0f);
        cardView.addView(view);
        cardView.setCardElevation(0.0f);
        if (Build.VERSION.SDK_INT < 21) {
            cardView.setContentPadding(-6, -9, -6, -9);
        }
        this.frameLayout.addView(cardView);
    }

    public void setViewScale(int i, float f) {
        Setting setting;
        int drawerViewAbsoluteGravity = getDrawerViewAbsoluteGravity(i);
        if (!this.settings.containsKey(Integer.valueOf(drawerViewAbsoluteGravity))) {
            setting = createSetting();
            this.settings.put(Integer.valueOf(drawerViewAbsoluteGravity), setting);
        } else {
            setting = this.settings.get(Integer.valueOf(drawerViewAbsoluteGravity));
        }
        setting.percentage = f;
        if (Build.VERSION.SDK_INT >= 14 && f < 1.0f) {
            setStatusBarBackground((Drawable) null);
            setSystemUiVisibility(0);
        }
        setting.scrimColor = 0;
        setting.drawerElevation = 0.0f;
    }

    public void setViewElevation(int i, float f) {
        Setting setting;
        int drawerViewAbsoluteGravity = getDrawerViewAbsoluteGravity(i);
        if (!this.settings.containsKey(Integer.valueOf(drawerViewAbsoluteGravity))) {
            setting = createSetting();
            this.settings.put(Integer.valueOf(drawerViewAbsoluteGravity), setting);
        } else {
            setting = this.settings.get(Integer.valueOf(drawerViewAbsoluteGravity));
        }
        setting.scrimColor = 0;
        setting.drawerElevation = 0.0f;
        setting.elevation = f;
    }

    public void setViewScrimColor(int i, int i2) {
        Setting setting;
        int drawerViewAbsoluteGravity = getDrawerViewAbsoluteGravity(i);
        if (!this.settings.containsKey(Integer.valueOf(drawerViewAbsoluteGravity))) {
            setting = createSetting();
            this.settings.put(Integer.valueOf(drawerViewAbsoluteGravity), setting);
        } else {
            setting = this.settings.get(Integer.valueOf(drawerViewAbsoluteGravity));
        }
        setting.scrimColor = i2;
    }

    public void setDrawerElevation(int i, float f) {
        Setting setting;
        int drawerViewAbsoluteGravity = getDrawerViewAbsoluteGravity(i);
        if (!this.settings.containsKey(Integer.valueOf(drawerViewAbsoluteGravity))) {
            setting = createSetting();
            this.settings.put(Integer.valueOf(drawerViewAbsoluteGravity), setting);
        } else {
            setting = this.settings.get(Integer.valueOf(drawerViewAbsoluteGravity));
        }
        setting.elevation = 0.0f;
        setting.drawerElevation = f;
    }

    public void setRadius(int i, float f) {
        Setting setting;
        int drawerViewAbsoluteGravity = getDrawerViewAbsoluteGravity(i);
        if (!this.settings.containsKey(Integer.valueOf(drawerViewAbsoluteGravity))) {
            setting = createSetting();
            this.settings.put(Integer.valueOf(drawerViewAbsoluteGravity), setting);
        } else {
            setting = this.settings.get(Integer.valueOf(drawerViewAbsoluteGravity));
        }
        setting.radius = f;
    }

    public Setting getSetting(int i) {
        return this.settings.get(Integer.valueOf(getDrawerViewAbsoluteGravity(i)));
    }

    public void setDrawerElevation(float f) {
        this.defaultDrawerElevation = f;
        super.setDrawerElevation(f);
    }

    public void setScrimColor(int i) {
        this.defaultScrimColor = i;
        super.setScrimColor(i);
    }

    public void useCustomBehavior(int i) {
        int drawerViewAbsoluteGravity = getDrawerViewAbsoluteGravity(i);
        if (!this.settings.containsKey(Integer.valueOf(drawerViewAbsoluteGravity))) {
            this.settings.put(Integer.valueOf(drawerViewAbsoluteGravity), createSetting());
        }
    }

    public void removeCustomBehavior(int i) {
        int drawerViewAbsoluteGravity = getDrawerViewAbsoluteGravity(i);
        if (this.settings.containsKey(Integer.valueOf(drawerViewAbsoluteGravity))) {
            this.settings.remove(Integer.valueOf(drawerViewAbsoluteGravity));
        }
    }

    public void openDrawer(final View view, boolean z) {
        super.openDrawer(view, z);
        post(new Runnable() {
            @Override
            public void run() {
                SaxAdvanceDrawerLayout advanceDrawerLayout = SaxAdvanceDrawerLayout.this;
               // View view = view;
                advanceDrawerLayout.updateSlideOffset(view, advanceDrawerLayout.isDrawerOpen(view) ? 1.0f : 0.0f);
            }
        });
    }


    public void updateSlideOffset(View view, float f) {
        int drawerViewAbsoluteGravity = getDrawerViewAbsoluteGravity((int) GravityCompat.START);
        int drawerViewAbsoluteGravity2 = getDrawerViewAbsoluteGravity(view);
        boolean z = Build.VERSION.SDK_INT >= 17 && (getLayoutDirection() == 1 || getActivity().getWindow().getDecorView().getLayoutDirection() == 1 || getResources().getConfiguration().getLayoutDirection() == 1);
        for (int i = 0; i < this.frameLayout.getChildCount(); i++) {
            CardView cardView = (CardView) this.frameLayout.getChildAt(i);
            Setting setting = this.settings.get(Integer.valueOf(drawerViewAbsoluteGravity2));
            if (setting != null) {
                cardView.setRadius((float) ((int) (setting.radius * f)));
                super.setScrimColor(setting.scrimColor);
                super.setDrawerElevation(setting.drawerElevation);
                ViewCompat.setScaleY(cardView, 1.0f - ((1.0f - setting.percentage) * f));
                cardView.setCardElevation(setting.elevation * f);
                float f2 = setting.elevation;
                boolean z2 = z ? drawerViewAbsoluteGravity2 != drawerViewAbsoluteGravity : drawerViewAbsoluteGravity2 == drawerViewAbsoluteGravity;
                int width = view.getWidth();
                updateSlideOffset(cardView, setting, z2 ? ((float) width) + f2 : ((float) (-width)) - f2, f, z2);
            } else {
                super.setScrimColor(this.defaultScrimColor);
                super.setDrawerElevation(this.defaultDrawerElevation);
            }
        }
    }

    public void setContrastThreshold(float f) {
        this.contrastThreshold = f;
    }

    public Activity getActivity() {
        return getActivity(getContext());
    }

    public Activity getActivity(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    public void updateSlideOffset(CardView cardView, Setting setting, float f, float f2, boolean z) {
        ViewCompat.setX(cardView, f * f2);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        View view = this.drawerView;
        if (view != null) {
            updateSlideOffset(view, isDrawerOpen(view) ? 1.0f : 0.0f);
        }
    }

    public int getDrawerViewAbsoluteGravity(int i) {
        return GravityCompat.getAbsoluteGravity(i, ViewCompat.getLayoutDirection(this)) & 7;
    }

    public int getDrawerViewAbsoluteGravity(View view) {
        return getDrawerViewAbsoluteGravity(((LayoutParams) view.getLayoutParams()).gravity);
    }

    public Setting createSetting() {
        return new Setting();
    }

    public class Setting {
        float drawerElevation;
        float elevation;
        boolean fitsSystemWindows;
        float percentage = 1.0f;
        float radius;
        int scrimColor;

        Setting() {
            this.scrimColor = SaxAdvanceDrawerLayout.this.defaultScrimColor;
            this.elevation = 0.0f;
            this.drawerElevation = SaxAdvanceDrawerLayout.this.defaultDrawerElevation;
        }

        public float getDrawerElevation() {
            return this.drawerElevation;
        }

        public float getElevation() {
            return this.elevation;
        }

        public float getPercentage() {
            return this.percentage;
        }

        public float getRadius() {
            return this.radius;
        }

        public int getScrimColor() {
            return this.scrimColor;
        }
    }
}
