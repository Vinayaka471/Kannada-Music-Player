package com.kannada.musicplayer.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResult;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.activities.MainActivity;
import com.kannada.musicplayer.ads.AdsCommon;
import com.kannada.musicplayer.ads.MyApplication;
import com.kannada.musicplayer.databinding.FragmentSettingsBinding;
import com.kannada.musicplayer.utils.AppConstants;
import com.kannada.musicplayer.utils.AppPref;
import com.kannada.musicplayer.utils.BetterActivityResult;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    private final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
    public FragmentSettingsBinding binding;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        FragmentSettingsBinding fragmentSettingsBinding = (FragmentSettingsBinding) DataBindingUtil.inflate(layoutInflater, R.layout.fragment_settings, viewGroup, false);
        this.binding = fragmentSettingsBinding;
        View root = fragmentSettingsBinding.getRoot();


        //Reguler Native Ads



        boolean IsShowHistory = AppPref.IsShowHistory();
        Integer valueOf = Integer.valueOf(R.drawable.iiswitch_on);
        Integer valueOf2 = Integer.valueOf(R.drawable.iiswitch_off);
        if (IsShowHistory) {
            Glide.with(getActivity()).load(valueOf).into(this.binding.imgShowHistory);
        } else {
            Glide.with(getActivity()).load(valueOf2).into(this.binding.imgShowHistory);
        }
        if (AppPref.IsShowMusic()) {
            Glide.with(getActivity()).load(valueOf).into(this.binding.imgShowMusic);
        } else {
            Glide.with(getActivity()).load(valueOf2).into(this.binding.imgShowMusic);
        }
        Clicks();
        return root;
    }

    private void Clicks() {
        this.binding.imgShowMusic.setOnClickListener(this);
        this.binding.imgShowHistory.setOnClickListener(this);
        this.binding.llShare.setOnClickListener(this);
        this.binding.llRateUs.setOnClickListener(this);
        this.binding.llPrivacy.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Integer valueOf = Integer.valueOf(R.drawable.iiswitch_off);
        Integer valueOf2 = Integer.valueOf(R.drawable.iiswitch_on);
        switch (id) {
            case R.id.imgShowHistory:
                if (AppPref.IsShowHistory()) {
                    AppPref.setShowHistory(false);
                    Glide.with(getActivity()).load(valueOf).into(this.binding.imgShowHistory);
                    return;
                }
                AppPref.setShowHistory(true);
                Glide.with(getActivity()).load(valueOf2).into(this.binding.imgShowHistory);
                return;
            case R.id.imgShowMusic:
                if (AppPref.IsShowMusic()) {
                    AppPref.setShowMusic(false);
                    Glide.with(getActivity()).load(valueOf).into(this.binding.imgShowMusic);
                    ((MainActivity) getActivity()).SetMusicVisibility(AppPref.IsShowMusic());
                    return;
                }
                AppPref.setShowMusic(true);
                Glide.with(getActivity()).load(valueOf2).into(this.binding.imgShowMusic);
                ((MainActivity) getActivity()).SetMusicVisibility(AppPref.IsShowMusic());
                return;
            case R.id.llPrivacy:
                Intent intentPrivacy = new Intent(Intent.ACTION_VIEW, Uri.parse(MyApplication.PrivacyPolicy));
                intentPrivacy.setPackage("com.android.chrome");
                startActivity(intentPrivacy);
                return;
            case R.id.llRateUs:
                final String rateapp = getActivity().getPackageName();
                Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + rateapp));
                startActivity(intent1);
                return;
            case R.id.llShare:
                AppConstants.shareApp(getActivity());
                return;
            default:
                return;
        }
    }

    @Override
    public void onDestroy() {
        Log.d("TAG", "onDestroy: ");
        super.onDestroy();
    }

}
