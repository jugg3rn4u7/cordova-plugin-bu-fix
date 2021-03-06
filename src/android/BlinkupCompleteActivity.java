package com.eades.plugin;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.TokenStatusCallback;
import com.eades.plugin.util.PreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class BlinkUpCompleteActivity extends Activity {

    private static final String TAG = "EADES ----> BlinkUpCompleteActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "inside onCreate");

        int timeoutMs = getIntent().getIntExtra(Extras.EXTRA_TIMEOUT_MS, 30000);
        Log.i(TAG, "before getDeviceInfo");
        getDeviceInfo(timeoutMs);
        Log.i(TAG, "after getDeviceInfo");

        BlinkUpPluginResult pluginResult = new BlinkUpPluginResult();
        pluginResult.setState(BlinkUpPluginResult.STATE_STARTED);
        pluginResult.setStatusCode(BlinkUpPlugin.STATUS_GATHERING_INFO);
        pluginResult.sendResultsToCallback();

        Log.i(TAG, "before pluginResult.sendResultsToCallback");

        finish();
    }

    private void getDeviceInfo(int timeoutMs) {
        Log.i(TAG, "inside getDeviceInfo");

        final TokenStatusCallback tokenStatusCallback = new TokenStatusCallback() {

            //---------------------------------
            // give connection info to Cordova
            //---------------------------------
            @Override public void onSuccess(JSONObject json) {
                Log.i(TAG, "inside tokenStatusCallback: onSuccess");

                BlinkUpPluginResult successResult = new BlinkUpPluginResult();
                successResult.setState(BlinkUpPluginResult.STATE_COMPLETED);
                successResult.setStatusCode(BlinkUpPlugin.STATUS_DEVICE_CONNECTED);
                successResult.setDeviceInfoFromJson(json);
                successResult.sendResultsToCallback();

                // cache planID if not development ID (see electricimp.com/docs/manufacturing/planids/)
                try {
                    String planId = json.getString(BlinkUpPluginResult.SDK_PLAN_ID_KEY);
                    PreferencesHelper.setPlanId(BlinkUpCompleteActivity.this, planId);
                    Log.i(TAG, "inside tokenStatusCallback: onSuccess: after PreferencesHelper.setPlanId");
                } catch (JSONException e) {
                    BlinkUpPluginResult.sendPluginErrorToCallback(BlinkUpPlugin.ERROR_JSON_ERROR);
                }
            }

            //---------------------------------
            // give error msg to Cordova
            //---------------------------------
            @Override public void onError(String errorMsg) {
                // can't use "sendPluginErrorToCallback" since this is an SDK error
                Log.i(TAG, "inside tokenStatusCallback: onError");

                BlinkUpPluginResult errorResult = new BlinkUpPluginResult();
                errorResult.setState(BlinkUpPluginResult.STATE_ERROR);
                errorResult.setBlinkUpError(errorMsg);
                errorResult.sendResultsToCallback();

                Log.i(TAG, "inside tokenStatusCallback: onError: errorResult");
            }

            //---------------------------------
            // give timeout message to Cordova
            //---------------------------------
            @Override public void onTimeout() {
                BlinkUpPluginResult.sendPluginErrorToCallback(BlinkUpPlugin.ERROR_PROCESS_TIMED_OUT);
            }
        };
        
        Log.i(TAG, "before getTokenStatus");
        // request the device info from the server
        BlinkupController.getInstance().getTokenStatus(tokenStatusCallback, timeoutMs);
        Log.i(TAG, "after getTokenStatus");
    }
}
