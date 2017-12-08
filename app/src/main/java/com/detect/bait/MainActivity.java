package com.detect.bait;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    public final static int REQUEST_CODE = 10101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        boolean loggedIn = sharedPreferences.getBoolean("loggedIn", false);

        if (loggedIn) {
            startService(new Intent(MainActivity.this, PowerButtonService.class));
            finish();
        } else {
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);
        }
    }

    @OnClick(R.id.rlSingIn)
    public void onSignIn(View view) {
        if (checkDrawOverlayPermission()) {

            SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("loggedIn", true);
            editor.commit();

            startService(new Intent(MainActivity.this, PowerButtonService.class));
            finish();
        }
    }


    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (!Settings.canDrawOverlays(this)) {
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            /** request permission via start activity for result */
            startActivityForResult(intent, REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                startService(new Intent(this, PowerButtonService.class));
            }
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (! hasFocus) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }


    ///////////////////////


    //////////////////
    private static final int SCREEN_OFF_TIME_OUT = 13000;
    private int mSystemScreenOffTimeOut;

    private void setScreenOffTimeOut() {
        try {
            mSystemScreenOffTimeOut = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, SCREEN_OFF_TIME_OUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreScreenOffTimeOut() {
        if (mSystemScreenOffTimeOut == 0) return;
        try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, mSystemScreenOffTimeOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
