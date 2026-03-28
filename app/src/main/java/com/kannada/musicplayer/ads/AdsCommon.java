package com.kannada.musicplayer.ads;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.kannada.musicplayer.R;

public class AdsCommon {

    public static void OneTimeCall(Context context) {
        // No-op: Formerly loaded interstitial ads
    }

    /*Inter Ads Click*/
    public static void InterstitialAd(Activity context, Intent intent) {
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /*Inter Intent With Finish*/
    public static void InterstitialAdFinish(Activity context, Intent intent) {
        context.startActivity(intent);
        context.finish();
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /*Inter On Back Press*/
    public static void InterstitialAdBackClick(Activity context) {
        context.finish();
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /*Inter Only*/
    public static void InterstitialAdsOnly(Activity context) {
        // No-op: Removed interstitial only logic
    }

    /*Reguler Big Native Ads*/
    public static void RegulerBigNative(Context context, FrameLayout admob_native_frame, View nativeAdLayout, FrameLayout nativeMax) {
        if (admob_native_frame != null) admob_native_frame.setVisibility(View.GONE);
        if (nativeAdLayout != null) nativeAdLayout.setVisibility(View.GONE);
        if (nativeMax != null) nativeMax.setVisibility(View.GONE);
    }

    /*Reguler Small Native Ads*/
    public static void SmallNative(Context context, FrameLayout admob_small_native, View native_banner_ad_container) {
        if (admob_small_native != null) admob_small_native.setVisibility(View.GONE);
        if (native_banner_ad_container != null) native_banner_ad_container.setVisibility(View.GONE);
    }

    /*Reguler Banner Ads*/
    public static void RegulerBanner(Context context, RelativeLayout admob_banner, LinearLayout adContainer, FrameLayout qureka) {
        if (admob_banner != null) {
            admob_banner.setVisibility(View.VISIBLE);
            AdView adView = new AdView(context);
            adView.setAdUnitId(MyApplication.AdMob_Banner1);
            adView.setAdSize(AdSize.BANNER);
            
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            adView.setLayoutParams(params);
            
            admob_banner.removeAllViews();
            admob_banner.addView(adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    public static void LoadAdaptiveBanner(Activity activity, FrameLayout adContainerView) {
        AdView adView = new AdView(activity);
        adView.setAdUnitId(MyApplication.AdMob_Adaptive_Banner);
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        AdSize adSize = getAdSize(activity, adContainerView);
        adView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private static AdSize getAdSize(Activity activity, FrameLayout adContainerView) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

}
