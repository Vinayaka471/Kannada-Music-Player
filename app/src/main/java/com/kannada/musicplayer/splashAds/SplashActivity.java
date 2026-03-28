package com.kannada.musicplayer.splashAds;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kannada.musicplayer.Permission.PermissionAppPageActivity;
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.activities.MainActivity;
import com.kannada.musicplayer.ads.AdsCommon;
import com.kannada.musicplayer.ads.MyApplication;


public class SplashActivity extends AppCompatActivity {

    int check = 0;

    private void applyDisplayCutouts() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_home), (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());

            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }
        setContentView(R.layout.activity_splash);

        applyDisplayCutouts();

        MyApplication.setuser_balance(0);

        refreshItem();

    }

    private void refreshItem() {

        if(isNetworkConnected()){

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OpenAppAds();
                }
            }, 3000);

        } else {

            final Dialog dialog = new Dialog(SplashActivity.this, R.style.DialogTheme);
            dialog.setContentView(R.layout.no_internet);
            dialog.setCancelable(false);

            CardView imgRefresh = (CardView) dialog.findViewById(R.id.refresh);
            CardView imgExit = (CardView) dialog.findViewById(R.id.exit);
            ImageView imgNoInternet = (ImageView) dialog.findViewById(R.id.img);

            final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);
            imgNoInternet.startAnimation(animShake);

            imgNoInternet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imgNoInternet.startAnimation(animShake);
                }
            });
            imgRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshItem();
                    dialog.dismiss();
                }
            });
            imgExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    finish();
                    System.exit(0);
                }
            });
            dialog.show();
        }

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void OpenAppAds() {
        goNext(1);
    }


    private void goNext(int i) {
        check = check + 1;
        if(check == 1){
            loadOpenApp();
        }
    }

    private void loadOpenApp() {

        //one time call & load ads

        if (MyApplication.getuser_onetime() == 0) {
            Intent i = new Intent(SplashActivity.this, PrivacyTermsActivity.class);
            startActivity(i);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else if(MyApplication.getuser_permission() == 0){
            Intent i = new Intent(SplashActivity.this, PermissionAppPageActivity.class);
            startActivity(i);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else {
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

}

