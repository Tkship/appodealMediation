package com.intowow.crystalexpress.demo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.Native;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeAdView;
import com.appodeal.ads.UserSettings;
import com.appodeal.ads.utils.Log;
import com.intowow.crystalexpress.demo.layout.AdTypeViewPager;
import com.intowow.crystalexpress.demo.layout.HorizontalNumberPicker;
import com.intowow.crystalexpress.demo.layout.SlidingTabLayout;
import com.intowow.sdk.I2WAPI;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
    private static final String CONSENT = "consent";
    private static final String ADMOB = "admob";
    public static final String APP_KEY = "16fb400d73c2895830c97692967a5dd80e5b6b5d89daea73";
    private String[] interstitial_networks, rewarded_video_networks, mrec_networks, native_networks;
    private List<NativeAd> nativeAds = new ArrayList<>();
    String mPlacementName = "default";
    boolean[] interstitialNetworks;
    boolean[] mrecNetworks;
    boolean[] rewardedNetworks;
    boolean[] nativeNetworks;
    boolean[] checkedValues;
    boolean consent;


    public enum AdType {
        Interstitial(Appodeal.INTERSTITIAL), RVideo(Appodeal.REWARDED_VIDEO), Mrec(Appodeal.MREC), Native(Appodeal.NATIVE);
        private final int mValue;

        AdType(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static AdType fromInteger(Integer x) {
            if (x == null) {
                return null;
            }
            switch (x) {
                case Appodeal.INTERSTITIAL:
                    return Interstitial;
                case Appodeal.REWARDED_VIDEO:
                    return RVideo;
                case Appodeal.MREC:
                    return Mrec;
                case Appodeal.NATIVE:
                    return Native;
            }
            return null;
        }
    }


    public enum AdTypePages {
        Interstitial(R.layout.interstitial, R.id.interstitialLayout),
        RVideo(R.layout.rewarded_video, R.id.rewardedVideoLayout),
        MREC(R.layout.mrec, R.id.MrecLayout),
        Native(R.layout.native_ad, R.id.nativeLayout);

        private final int mLayout;
        private final int mId;

        AdTypePages(int layout, int id) {
            mLayout = layout;
            mId = id;
        }

        public int getLayout() {
            return mLayout;
        }

        public int getId() {
            return mId;
        }
    }

    public static Intent getIntent(Context context, boolean consent) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(CONSENT, consent);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        consent = getIntent().getBooleanExtra(CONSENT, false);

        boolean isTestMode = false; // Set true to enable test mode
        boolean isOpenVerboseLog = true; // Set true to enable verbose log

        I2WAPI.init(getApplicationContext(), isTestMode, isOpenVerboseLog);
        android.util.Log.d("INTOWOWPOC","----- I2WAPI.init isTestMode = " + isTestMode);

        enableAdmobNetwork();

        if (Build.VERSION.SDK_INT >= 23 && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            Appodeal.requestAndroidMPermissions(this, new AppodealPermissionCallbacks(this));
        }

        TextView sdkTextView = findViewById(R.id.sdkTextView);
        sdkTextView.setText(getString(R.string.sdkTextView, Appodeal.getVersion()));

        CompoundButton testModeSwitch = findViewById(R.id.testModeSwitch);
        testModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Appodeal.setTesting(isChecked);
            }
        });

        Spinner logLevelSpinner = findViewById(R.id.logLevelList);
        Appodeal.setLogLevel(Log.LogLevel.none);
        ArrayAdapter<String> logLevelAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.logLevels));
        logLevelSpinner.setAdapter(logLevelAdapter);
        logLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Appodeal.setLogLevel(Log.LogLevel.none);
                        break;
                    case 1:
                        Appodeal.setLogLevel(Log.LogLevel.debug);
                        break;
                    case 2:
                        Appodeal.setLogLevel(Log.LogLevel.verbose);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ViewPager pager = (AdTypeViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(AdTypePages.values().length);
        pager.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if (child.findViewById(AdTypePages.Interstitial.getId()) != null && child.getTag() == null) {
                    child.setTag(true);
                    CompoundButton autoCacheInterstitialSwitch = findViewById(R.id.autoCacheInterstitialSwitch);
                    autoCacheInterstitialSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            Appodeal.setAutoCache(Appodeal.INTERSTITIAL, isChecked);
                            Button interstitialCacheButton = findViewById(R.id.interstitialCacheButton);
                            if (isChecked) {
                                interstitialCacheButton.setVisibility(View.GONE);
                            } else {
                                interstitialCacheButton.setVisibility(View.VISIBLE);
                            }
                        }
                    });

                    final CompoundButton onLoadedSwitch = findViewById(R.id.onLoadedInterstitialSwitch);
                    onLoadedSwitch.setText(getString(R.string.onLoadedInterstitialSwitch, "expensive"));
                    onLoadedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                onLoadedSwitch.setText(getString(R.string.onLoadedInterstitialSwitch, "both"));
                            } else {
                                onLoadedSwitch.setText(getString(R.string.onLoadedInterstitialSwitch, "expensive"));
                            }
                            Appodeal.setTriggerOnLoadedOnPrecache(Appodeal.INTERSTITIAL, isChecked);
                        }
                    });
                }

                if (child.findViewById(AdTypePages.RVideo.getId()) != null && child.getTag() == null) {
                    child.setTag(true);
                    CompoundButton autoCacheRewardedVideoSwitch = findViewById(R.id.autoCacheRewardedVideoSwitch);
                    autoCacheRewardedVideoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            Appodeal.setAutoCache(Appodeal.REWARDED_VIDEO, isChecked);
                            Button rewardedVideoCacheButton = findViewById(R.id.rewardedVideoCacheButton);
                            if (isChecked) {
                                rewardedVideoCacheButton.setVisibility(View.GONE);
                            } else {
                                rewardedVideoCacheButton.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }

                if (child.findViewById(AdTypePages.Native.getId()) != null && child.getTag() == null) {
                    child.setTag(true);
                    CompoundButton autoCacheNativeSwitch = findViewById(R.id.autoCacheNativeSwitch);
                    autoCacheNativeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            Appodeal.setAutoCache(Appodeal.NATIVE, isChecked);
                        }
                    });

                    Spinner nativeTemplateSpinner = findViewById(R.id.native_template_list);
                    ArrayAdapter<String> nativeTemplateAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.nativeTemplates));
                    nativeTemplateSpinner.setAdapter(nativeTemplateAdapter);
                    nativeTemplateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            updateNativeList(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    Spinner nativeTypeSpinner = findViewById(R.id.native_type_list);
                    ArrayAdapter<String> nativeTypeAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.nativeTypes));
                    nativeTypeSpinner.setAdapter(nativeTypeAdapter);
                    nativeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            switch (position) {
                                case 0:
                                    Appodeal.setNativeAdType(Native.NativeAdType.Auto);
                                    break;
                                case 1:
                                    Appodeal.setNativeAdType(Native.NativeAdType.NoVideo);
                                    break;
                                case 2:
                                    Appodeal.setNativeAdType(Native.NativeAdType.Video);
                                    break;
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if (child.findViewById(AdTypePages.MREC.getId()) != null && child.getTag() == null) {
                    child.setTag(true);
                    CompoundButton autoCacheMrecSwitch = findViewById(R.id.autoCacheMrecSwitch);
                    autoCacheMrecSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            Appodeal.setAutoCache(Appodeal.MREC, isChecked);
                            Button MrecCacheButton = findViewById(R.id.mrecCacheButton);
                            if (isChecked) {
                                MrecCacheButton.setVisibility(View.GONE);
                            } else {
                                MrecCacheButton.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {

            }
        });
        AdTypeAdapter adTypeAdapter = new AdTypeAdapter(getSupportFragmentManager());
        pager.setAdapter(adTypeAdapter);

        SlidingTabLayout slidingTabLayout = findViewById(R.id.slidingTabLayout);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(pager);
    }


    @Override
    public void onResume() {
        super.onResume();
        I2WAPI.onActivityResume(this);
        Appodeal.onResume(this, Appodeal.MREC);
    }

    @Override
    public void onPause() {
        super.onPause();
        I2WAPI.onActivityPause(this);
    }

    @Override
    public void onBackPressed() {
        ViewGroup root = findViewById(android.R.id.content);
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            Object tag = child.getTag();
            if (tag != null && tag.equals("appodeal")) {
                root.removeView(child);
                return;
            }
        }
        super.onBackPressed();
    }

    // Only enable admob ad network
    public void enableAdmobNetwork() {
        interstitial_networks = getResources().getStringArray(R.array.interstitial_networks);
        interstitialNetworks = new boolean[interstitial_networks.length];
        for (int i = 0; i < interstitial_networks.length; i++) {
            if(interstitial_networks [i].equals(ADMOB)) {
                interstitialNetworks[i] = true;
            } else {
                interstitialNetworks[i] = false;
            }
        }
        mrec_networks = getResources().getStringArray(R.array.mrec_networks);
        mrecNetworks = new boolean[mrec_networks.length];
        for (int i = 0; i < mrec_networks.length; i++) {
            if(mrec_networks [i].equals(ADMOB)) {
                mrecNetworks[i] = true;
            } else {
                mrecNetworks[i] = false;
            }
        }

        rewarded_video_networks = getResources().getStringArray(R.array.rewarded_video_networks);
        rewardedNetworks = new boolean[rewarded_video_networks.length];
        for (int i = 0; i < rewarded_video_networks.length; i++) {
            if(rewarded_video_networks [i].equals(ADMOB)) {
                rewardedNetworks[i] = true;
            } else {
                rewardedNetworks[i] = false;
            }
        }

        native_networks = getResources().getStringArray(R.array.native_networks);
        nativeNetworks = new boolean[native_networks.length];
        for (int i = 0; i < native_networks.length; i++) {
            nativeNetworks[i] = true;
        }
    }


    public void initSdkButton(View v) {
        //Add user settings
        Appodeal.getUserSettings(this)
                .setAge(25)
                .setGender(UserSettings.Gender.MALE);
        Appodeal.initialize(this, APP_KEY, Appodeal.NONE, consent);
        Appodeal.trackInAppPurchase(this, 10.0, "USD");
    }

    public void disableNetworks(boolean[] adNetworks, String[] networksList, AdType adType) {
        for (int i = 0; i < adNetworks.length; i++) {
            if (!adNetworks[i]) {
                Appodeal.disableNetwork(this, networksList[i], adType.getValue());
            }
        }
    }

    public void setChildDirectedTreatment(View v) {
        v.setEnabled(false);
        Appodeal.setChildDirectedTreatment(true);
    }


    public void interstitialChooseNetworks(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        checkedValues = interstitialNetworks.clone();
        builder.setTitle(getString(R.string.selectNetworks)).setMultiChoiceItems(interstitial_networks, checkedValues,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int item, boolean isChecked) {
                    }
                });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                interstitialNetworks = checkedValues;
                disableNetworks(interstitialNetworks, interstitial_networks, AdType.Interstitial);
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
    }

    public void initInterstitialSdkButton(View v) {
        Appodeal.initialize(this, APP_KEY, Appodeal.INTERSTITIAL, consent);
        Appodeal.setInterstitialCallbacks(new AppodealInterstitialCallbacks(this));
    }

    public void isInterstitialLoadedButton(View v) {
        if (Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
            Toast.makeText(this, "true", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "false", Toast.LENGTH_SHORT).show();
        }
    }

    public void isInterstitialLoadedPrecacheButton(View v) {
        if (Appodeal.isPrecache(Appodeal.INTERSTITIAL)) {
            Toast.makeText(this, "true", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "false", Toast.LENGTH_SHORT).show();
        }
    }

    public void interstitialCacheButton(View v) {
        Appodeal.cache(this, Appodeal.INTERSTITIAL);
    }

    public void interstitialShowButton(View v) {
        boolean isShown = Appodeal.show(this, Appodeal.INTERSTITIAL,"SPLASH_TEST");
        Toast.makeText(this, String.valueOf(isShown), Toast.LENGTH_SHORT).show();
    }

    public void rewardedVideoChooseNetworks(View v) {
        android.util.Log.d("main","-------rewardedVideoChooseNetworks");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        checkedValues = rewardedNetworks.clone();
        builder.setTitle(getString(R.string.selectNetworks)).setMultiChoiceItems(rewarded_video_networks, checkedValues,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int item, boolean isChecked) {
                    }
                });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rewardedNetworks = checkedValues;
                disableNetworks(rewardedNetworks, rewarded_video_networks, AdType.RVideo);
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
    }

    public void initRewardedVideoSdkButton(View v) {
        Appodeal.initialize(this, APP_KEY, Appodeal.REWARDED_VIDEO, consent);
        Appodeal.setRewardedVideoCallbacks(new AppodealRewardedVideoCallbacks(this));
    }

    public void isRewardedVideoLoadedButton(View v) {
        if (Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) {
            Toast.makeText(this, "true", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "false", Toast.LENGTH_SHORT).show();
        }
    }

    public void rewardedVideoCacheButton(View v) {
        Appodeal.cache(this, Appodeal.REWARDED_VIDEO);
    }

    public void rewardedVideoShowButton(View v) {
        boolean isShown = Appodeal.show(this, Appodeal.REWARDED_VIDEO,"REWARDED_VIDEO_1");
        Toast.makeText(this, String.valueOf(isShown), Toast.LENGTH_SHORT).show();
    }

    public void mrecChooseNetworks(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        checkedValues = mrecNetworks.clone();
        builder.setTitle(getString(R.string.selectNetworks)).setMultiChoiceItems(mrec_networks, checkedValues,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int item, boolean isChecked) {
                    }
                });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mrecNetworks = checkedValues;
                disableNetworks(mrecNetworks, mrec_networks, AdType.Mrec);
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
    }

    public void initMrecSdkButton(View v) {
        Appodeal.setMrecViewId(R.id.appodealMrecView);
        Appodeal.initialize(this, APP_KEY, Appodeal.MREC, consent);
        Appodeal.setMrecCallbacks(new AppodealMrecCallbacks(this));
    }

    public void isMrecLoadedButton(View v) {
        if (Appodeal.isLoaded(Appodeal.MREC)) {
            Toast.makeText(this, "true", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "false", Toast.LENGTH_SHORT).show();
        }
    }

    public void mrecCacheButton(View v) {
        Appodeal.cache(this, Appodeal.MREC);
    }

    public void mrecShowButton(View v) {
        Appodeal.setMrecViewId(R.id.appodealMrecView);
        boolean isShown = Appodeal.show(this, Appodeal.MREC,"MREC_POC");
        Toast.makeText(this, String.valueOf(isShown), Toast.LENGTH_SHORT).show();
    }

    public void mrecHideButton(View v) {
        Appodeal.hide(this, Appodeal.MREC);
    }

    public void mrecDestroyButton(View v) {
        Appodeal.destroy(Appodeal.MREC);
    }

    public void initNativeSdkButton(View v) {
        Appodeal.setNativeCallbacks(new AppodealNativeCallbacks(this));
        Appodeal.initialize(this, APP_KEY, Appodeal.NATIVE, consent);
        Appodeal.setAutoCacheNativeIcons(true);
        Appodeal.setAutoCacheNativeMedia(true);
    }

    public void nativeChooseNetworks(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        checkedValues = nativeNetworks.clone();
        builder.setTitle(getString(R.string.selectNetworks)).setMultiChoiceItems(native_networks, checkedValues,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int item, boolean isChecked) {
                    }
                });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nativeNetworks = checkedValues;
                disableNetworks(nativeNetworks, native_networks, AdType.Native);
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
    }


    public void nativeCacheButton(View v) {
        hideNativeAds();

        HorizontalNumberPicker numberPicker = findViewById(R.id.nativeAdsCountPicker);
        Appodeal.setNativeCallbacks(new AppodealNativeCallbacks(this));
        if (numberPicker.getNumber() == 1) {
            Appodeal.cache(this, Appodeal.NATIVE);
        } else {
            Appodeal.cache(this, Appodeal.NATIVE, numberPicker.getNumber());
        }
    }

    public void isNativeLoadedButton(View v) {
        if (Appodeal.isLoaded(Appodeal.NATIVE)) {
            Toast.makeText(this, "true", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "false", Toast.LENGTH_SHORT).show();
        }
    }

    public void nativeShowButton(View v) {
        hideNativeAds();
        HorizontalNumberPicker numberPicker = findViewById(R.id.nativeAdsCountPicker);
        nativeAds = Appodeal.getNativeAds(numberPicker.getNumber());
        LinearLayout nativeAdsListView = findViewById(R.id.nativeAdsListView);
        Spinner nativeTemplateSpinner = findViewById(R.id.native_template_list);
        NativeListAdapter nativeListViewAdapter = new NativeListAdapter(nativeAdsListView, nativeTemplateSpinner.getSelectedItemPosition());
        for (NativeAd nativeAd : nativeAds) {
            nativeListViewAdapter.addNativeAd(nativeAd);
        }
        nativeAdsListView.setTag(nativeListViewAdapter);
        nativeListViewAdapter.rebuild();
    }

    public void nativeHideButton(View v) {
        hideNativeAds();
    }

    public void unRegisterNativeAds(View v) {
        LinearLayout nativeListView = findViewById(R.id.nativeAdsListView);
        int childCount = nativeListView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            NativeAdView child = (NativeAdView) nativeListView.getChildAt(i);
            child.unregisterViewForInteraction();
        }
    }

    public void destroyNativeAds(View v) {
        LinearLayout nativeListView = findViewById(R.id.nativeAdsListView);
        int childCount = nativeListView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            NativeAdView child = (NativeAdView) nativeListView.getChildAt(i);
            child.destroy();
        }
    }

    public void registerNativeAds(View v) {
        LinearLayout nativeListView = findViewById(R.id.nativeAdsListView);
        int childCount = nativeListView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            NativeAdView child = (NativeAdView) nativeListView.getChildAt(i);
            child.registerView(nativeAds.get(i));
        }
    }

    private void hideNativeAds() {
        LinearLayout nativeListView = findViewById(R.id.nativeAdsListView);
        int childCount = nativeListView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            NativeAdView child = (NativeAdView) nativeListView.getChildAt(i);
            child.unregisterViewForInteraction();
            child.destroy();
        }
        nativeListView.removeAllViews();
        NativeListAdapter nativeListViewAdapter = (NativeListAdapter) nativeListView.getTag();
        if (nativeListViewAdapter != null) {
            nativeListViewAdapter.clear();
        }
    }

    private void updateNativeList(int position) {
        LinearLayout nativeListView = findViewById(R.id.nativeAdsListView);
        NativeListAdapter nativeListViewAdapter = (NativeListAdapter) nativeListView.getTag();
        if (nativeListViewAdapter != null) {
            nativeListViewAdapter.setTemplate(position);
            nativeListViewAdapter.rebuild();
        }
    }

    public void showInRecyclerView(View v) {
        Spinner nativeTemplateSpinner = findViewById(R.id.native_template_list);
        startActivity(NativeActivity.newIntent(this, nativeTemplateSpinner.getSelectedItemPosition()));
    }

    public static class AdTypeAdapter extends FragmentPagerAdapter {

        public AdTypeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return AdTypePages.values().length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new AdTypeFragment();
            Bundle args = new Bundle();
            args.putInt("layout", AdTypePages.values()[position].getLayout());
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return AdTypePages.values()[position].name();
        }
    }

    public static class AdTypeFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle args = getArguments();
            int layoutId = args.getInt("layout");
            return inflater.inflate(layoutId, container, false);
        }
    }
}
