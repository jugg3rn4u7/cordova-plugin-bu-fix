package com.eades.plugin;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.TokenStatusCallback;
import com.electricimp.blinkup.TokenAcquireCallback;
import com.electricimp.blinkup.ServerErrorHandler;
import com.eades.plugin.util.PreferencesHelper;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class BlinkUpPlugin extends CordovaPlugin {
    private static final String TAG = "EADES ----> BlinkUpPlugin";

    private static final String START_BLINKUP = "startBu";

    private static CallbackContext sCallbackContext;
    private static boolean sClearCache = false;

    // only needed in this class
    private String mApiKey;

    static final int STATUS_DEVICE_CONNECTED = 0;
    static final int STATUS_GATHERING_INFO = 200;
    static final int STATUS_CLEAR_WIFI_COMPLETE = 201;
    static final int STATUS_CLEAR_WIFI_AND_CACHE_COMPLETE = 202;

    static final int ERROR_INVALID_ARGUMENTS = 100;
    static final int ERROR_PROCESS_TIMED_OUT = 101;
    static final int ERROR_CANCELLED_BY_USER = 102;
    static final int ERROR_INVALID_API_KEY = 103;
    static final int ERROR_VERIFY_API_KEY_FAIL = 301; // android only
    static final int ERROR_JSON_ERROR = 302;          // android only

    private static final int INVOKE_BLINKUP_ARG_API_KEY = 0;
    private static final int INVOKE_BLINKUP_ARG_DEVELOPER_PLAN_ID = 1;
    private static final int INVOKE_BLINKUP_ARG_TIMEOUT_MS = 2;
    private static final int INVOKE_BLINKUP_ARG_GENERATE_PLAN_ID = 3;

    private static final int START_BLINKUP_ARG_API_KEY = 0;
    private static final int START_BLINKUP_ARG_TIMEOUT_MS = 3;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        sCallbackContext = callbackContext;
        final Activity activity = cordova.getActivity();
        final BlinkupController controller = BlinkupController.getInstance();

        if (START_BLINKUP.equalsIgnoreCase(action)) {
            return startBlinkUp(activity, controller, data);
        } 
        return false;
    }

    private boolean startBlinkUp(final Activity activity, final BlinkupController controller, JSONArray data) {
        int timeoutMs;
        try {
            mApiKey = data.getString(START_BLINKUP_ARG_API_KEY);
            timeoutMs = data.getInt(START_BLINKUP_ARG_TIMEOUT_MS);
        } catch (JSONException exc) {
            BlinkUpPluginResult.sendPluginErrorToCallback(ERROR_INVALID_ARGUMENTS);
            return false;
        }

        // if api key not valid, send error message and quit
        if (!apiKeyFormatValid()) {
            BlinkUpPluginResult.sendPluginErrorToCallback(ERROR_INVALID_API_KEY);
            return false;
        } 

        controller.intentBlinkupComplete = createBlinkUpCompleteIntent(activity, timeoutMs);

        // default is to run on WebCore thread, we have UI so need UI thread
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                presentBlinkUp(activity, controller);
            }
        });
        return true;
    }

    private Intent createBlinkUpCompleteIntent(Activity activity, int timeoutMs) {
        Intent blinkupCompleteIntent = new Intent(activity, BlinkUpCompleteActivity.class);
        blinkupCompleteIntent.putExtra(Extras.EXTRA_TIMEOUT_MS, timeoutMs);
        return blinkupCompleteIntent;
    }

    private void presentBlinkUp(Activity activity, BlinkupController controller) {
        
        // show toast if can't acquire token
        final TokenAcquireCallback tokenAcquireCallback = new TokenAcquireCallback() {
            @Override
            public void onSuccess(String planId, String id) { }

            @Override
            public void onError(String s) {
                Log.e(TAG, s);
            }
        };

        // send back error if connectivity issue
        ServerErrorHandler serverErrorHandler = new ServerErrorHandler() {
            @Override
            public void onError(String s) {
                Log.e(TAG, s);
                BlinkUpPluginResult.sendPluginErrorToCallback(ERROR_VERIFY_API_KEY_FAIL);
            }
        };

        controller.acquireSetupToken(activity, mApiKey, tokenAcquireCallback);
        controller.selectWifiAndSetupDevice(activity, mApiKey, serverErrorHandler);
    }

    private boolean apiKeyFormatValid() {
        if (TextUtils.isEmpty(mApiKey) || TextUtils.getTrimmedLength(mApiKey) != 32) {
            return false;
        }

        String isAlphaNumericPattern = "^[a-zA-Z0-9]*$";
        return mApiKey.matches(isAlphaNumericPattern);
    }

    static boolean getClearCache() {
        return sClearCache;
    }

    static void setClearCache(boolean val) {
        sClearCache = val;
    }

    static CallbackContext getCallbackContext() {
        return sCallbackContext;
    }
}
