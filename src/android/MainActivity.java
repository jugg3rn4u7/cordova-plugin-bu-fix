package com.eades.plugin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.util.Log;

import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.ServerErrorHandler;

import java.util.Date;

public class MainActivity extends Activity {
    private static final String API_KEY = "fe829115961cea360f00c5dc61cc43a3";
    final private String TAG = "EADES BU PLUGIN";

    private BlinkupController blinkup;
    private ServerErrorHandler errorHandler = new ServerErrorHandler() {
        @Override
        public void onError(String errorMsg) {
            Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        blinkup = BlinkupController.getInstance();
        blinkup.intentBlinkupComplete = new Intent(this, MainActivity.class);
        blinkup.selectWifiAndSetupDevice(this, API_KEY, errorHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "MainActivity -> requestCode : " + Integer.toString(requestCode));
        Log.i(TAG, "MainActivity -> resultCode : " + Integer.toString(resultCode));
        //Log.i(TAG, "MainActivity -> data : " + data.toString());
        blinkup.handleActivityResult(this, requestCode, resultCode, data);
    }
}