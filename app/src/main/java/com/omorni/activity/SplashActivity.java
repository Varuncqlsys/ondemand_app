package com.omorni.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import com.omorni.R;
import com.omorni.service.UpdateLocationService;
import com.omorni.utils.ConnectivityReceiver;
import com.omorni.utils.ContextWrapper;
import com.omorni.utils.LocaleHelper;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import java.util.Locale;


public class SplashActivity extends BaseActivity {

    private static int SPLASH_TIME_OUT = 10;
    SavePref savePref;
    private SparseIntArray mErrorString;
    private static final int REQUEST_PERMISSIONS = 20;
    VideoView videoview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(SplashActivity.this, R.color.splash_color));
            getWindow().setStatusBarColor(ContextCompat.getColor(SplashActivity.this, R.color.splash_color));
        }
        setTheme(R.style.AppThemeSeller);
        setContentView(R.layout.activity_splash);


        savePref = new SavePref(SplashActivity.this);
//        LocaleHelper.setLocale(getApplicationContext(), savePref.getString(getApplicationContext(), "app_language", "ar"));
//        mErrorString = new SparseIntArray();

        videoview = (VideoView) findViewById(R.id.myvideo);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                try {
                    Uri video = Uri.parse("android.resource://" + getPackageName() + "/"
                            + R.raw.splash_video);

                    videoview.setVideoURI(video);

                    videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        public void onCompletion(MediaPlayer mp) {
                            if (savePref.getUserId().equals("")) {
                                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                if (ConnectivityReceiver.isConnected()) {
                                    if (!Utils.isMyServiceRunning(UpdateLocationService.class, SplashActivity.this)) {
                                        Intent mServiceIntent = new Intent(SplashActivity.this, UpdateLocationService.class);
                                        startService(mServiceIntent);
                                    }
                                }
                                Intent intent1 = new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(intent1);
                            }
                            finish();
                        }

                    });
                    videoview.start();
                } catch (Exception ex) {
//                    jump();
                }


            }
        }, SPLASH_TIME_OUT);

        /*int currentapiVersion = Build.VERSION.SDK_INT;
        // if current version is M or greater than M
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            String[] array = {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
            requestAppPermissions(array, R.string.permission, REQUEST_PERMISSIONS);
        } else {
            onPermissionsGranted(REQUEST_PERMISSIONS);
        }*/
    }

    // check requested permissions are on or off
    public void requestAppPermissions(final String[] requestedPermissions, final int stringId, final int requestCode) {
        mErrorString.put(requestCode, stringId);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        boolean shouldShowRequestPermissionRationale = false;
        for (String permission : requestedPermissions) {
            permissionCheck = permissionCheck + ContextCompat.checkSelfPermission(this, permission);
            shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale) {
                Snackbar snack = Snackbar.make(findViewById(android.R.id.content), stringId, Snackbar.LENGTH_INDEFINITE);
                View view = snack.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snack.setAction("GRANT", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(SplashActivity.this, requestedPermissions, requestCode);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(this, requestedPermissions, requestCode);
            }
        } else {
            onPermissionsGranted(requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int permission : grantResults) {
            permissionCheck = permissionCheck + permission;
        }
        if ((grantResults.length > 0) && permissionCheck == PackageManager.PERMISSION_GRANTED) {
            onPermissionsGranted(requestCode);
        } else {
            // onPermissionsGranted(requestCode);
        }
    }

    // if permissions granted succesfully
    private void onPermissionsGranted(int requestcode) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                try {
                    Uri video = Uri.parse("android.resource://" + getPackageName() + "/"
                            + R.raw.splash_video);

                    videoview.setVideoURI(video);

                    videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        public void onCompletion(MediaPlayer mp) {
                            if (savePref.getUserId().equals("")) {
                                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                if (ConnectivityReceiver.isConnected()) {
                                    if (!Utils.isMyServiceRunning(UpdateLocationService.class, SplashActivity.this)) {
                                        Intent mServiceIntent = new Intent(SplashActivity.this, UpdateLocationService.class);
                                        startService(mServiceIntent);
                                    }
                                }
                                Intent intent1 = new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(intent1);
                            }
                            finish();
                        }

                    });
                    videoview.start();
                } catch (Exception ex) {
//                    jump();
                }


            }
        }, SPLASH_TIME_OUT);
    }


}
