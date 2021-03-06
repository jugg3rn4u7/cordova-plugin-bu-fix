package com.eades.plugin;

import android.text.TextUtils;
import android.util.Log;

import com.eades.plugin.util.DebugUtils;

import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

public class BlinkUpPluginResult {
    private static final String TAG = "EADES ----> BlinkUpPluginResult";

    // the JSON keys are from the Android BlinkUp SDK, documented at:
    // https://electricimp.com/docs/manufacturing/sdkdocs/android/callbacks/
    private static final String SDK_IMPEE_ID_KEY = "impee_id";
    static final String SDK_PLAN_ID_KEY = "plan_id";
    private static final String SDK_AGENT_URL_KEY = "agent_url";
    private static final String SDK_CLAIMED_AT_KEY = "claimed_at";

    // possible states
    static final String STATE_STARTED = "started";
    static final String STATE_COMPLETED = "completed";
    static final String STATE_ERROR = "error";

    // possible error types
    private static final String ERROR_TYPE_BLINK_UP_SDK_ERROR = "blinkup";
    private static final String ERROR_TYPE_PLUGIN_ERROR = "plugin";

    //=====================================
    // JSON keys for results
    //=====================================
    private enum ResultKeys {
        STATE("state"),
        STATUS_CODE("statusCode"),

        ERROR("error"),
        ERROR_TYPE("errorType"),
        ERROR_CODE("errorCode"),
        ERROR_MSG("errorMsg"),

        DEVICE_INFO("deviceInfo"),
        DEVICE_ID("deviceId"),
        PLAN_ID("planId"),
        AGENT_URL("agentURL"),
        VERIFICATION_DATE("verificationDate");

        private final String key;
        ResultKeys(String key) { this.key = key; }
        public String getKey() { return this.key; }
    }

    //====================================
    // BlinkUp Results
    //====================================
    private String mState;
    private int mStatusCode;
    private String mErrorType;
    private int mErrorCode;
    private String mErrorMsg;

    private String mDeviceId;
    private String mPlanId;
    private String mAgentURL;
    private String mVerificationDate;
    private boolean mHasDeviceInfo = false;

    /*************************************
     * Setters for our Results
     *************************************/
    public void setState(String state) {
        DebugUtils.checkAssert(TextUtils.equals(state, STATE_COMPLETED)
                || TextUtils.equals(state, STATE_ERROR)
                || TextUtils.equals(state, STATE_STARTED));
        mState = state;
    }
    public void setStatusCode(int statusCode) {
        mStatusCode = statusCode;
    }
    public void setPluginError(int errorCode) {
        mState = STATE_ERROR;
        mErrorType = ERROR_TYPE_PLUGIN_ERROR;
        mErrorCode = errorCode;
    }
    public void setBlinkUpError(String errorMsg) {
        mState = STATE_ERROR;
        mErrorType = ERROR_TYPE_BLINK_UP_SDK_ERROR;
        mErrorCode = 1; // set generic error code
        mErrorMsg = errorMsg;
    }
    public void setDeviceInfoFromJson(JSONObject deviceInfo) {
        try {
            Log.i(TAG, "inside setDeviceInfoFromJson");
            mDeviceId = (deviceInfo.getString(SDK_IMPEE_ID_KEY) != null) ? deviceInfo.getString(SDK_IMPEE_ID_KEY).trim() : null;
            mPlanId = deviceInfo.getString(SDK_PLAN_ID_KEY);
            mAgentURL = deviceInfo.getString(SDK_AGENT_URL_KEY);
            mVerificationDate = deviceInfo.getString(SDK_CLAIMED_AT_KEY).replace("Z", "+0:00"); // match date format to iOS
            mHasDeviceInfo = true;
            Log.i(TAG, "inside setDeviceInfoFromJson : after assignments");
        } catch (JSONException e) {
            Log.i(TAG, "inside setDeviceInfoFromJson : JSONException");
            mState = STATE_ERROR;
            setPluginError(BlinkUpPlugin.ERROR_JSON_ERROR);
            sendResultsToCallback();
        }
    }

    static void sendPluginErrorToCallback(int error) {
        BlinkUpPluginResult argErrorResult = new BlinkUpPluginResult();
        argErrorResult.setState(STATE_ERROR);
        argErrorResult.setPluginError(error);
        argErrorResult.sendResultsToCallback();
    }

    /*************************************
     * Generates JSON of our plugin results
     * and sends back to the callback
     *************************************/
    public void sendResultsToCallback() {
        Log.i(TAG, "inside sendResultsToCallback");
        JSONObject resultJSON = new JSONObject();

        // set result status
        PluginResult.Status cordovaResultStatus;
        if (TextUtils.equals(mState, STATE_ERROR)) {
            cordovaResultStatus = PluginResult.Status.ERROR;
        }
        else {
            cordovaResultStatus = PluginResult.Status.OK;
        }

        try {
            resultJSON.put(ResultKeys.STATE.getKey(), mState);

            if (TextUtils.equals(mState, STATE_ERROR)) {
                resultJSON.put(ResultKeys.ERROR.getKey(), generateErrorJson());
            }
            else {
                resultJSON.put(ResultKeys.STATUS_CODE.getKey(), ("" + mStatusCode));
                if (mHasDeviceInfo) {
                    resultJSON.put(ResultKeys.DEVICE_INFO.getKey(), generateDeviceInfoJson());
                }
            }
        } catch (JSONException e) {
            // don't want endless loop calling ourselves so just log error (don't send to callback)
            Log.i(TAG, "inside sendResultsToCallback: JSONException");
        }

        Log.i(TAG, "inside sendResultsToCallback: before sendPluginResult");
        PluginResult pluginResult = new PluginResult(cordovaResultStatus, resultJSON.toString());
        pluginResult.setKeepCallback(true); // uses same BlinkUpPlugin object across calls, so need to keep callback
        BlinkUpPlugin.getCallbackContext().sendPluginResult(pluginResult);
        Log.i(TAG, "inside sendResultsToCallback: after sendPluginResult");
    }

    /*************************************
     * Returns JSON containing error
     *************************************/
    private JSONObject generateErrorJson() {
        Log.i(TAG, "inside generateErrorJson");
        JSONObject errorJson = new JSONObject();

        try {
            errorJson.put(ResultKeys.ERROR_TYPE.getKey(), mErrorType);
            errorJson.put(ResultKeys.ERROR_CODE.getKey(), "" + mErrorCode);

            if (TextUtils.equals(mErrorType, ERROR_TYPE_BLINK_UP_SDK_ERROR)) {
                errorJson.put(ResultKeys.ERROR_MSG.getKey(), mErrorMsg);
            }
        } catch (JSONException e) {
            mState = STATE_ERROR;
            setPluginError(BlinkUpPlugin.ERROR_JSON_ERROR);
            sendResultsToCallback();
        }

        return errorJson;
    }

    /*************************************
     * Returns deviceInfo in JSON
     *************************************/
    private JSONObject generateDeviceInfoJson() {
        Log.i(TAG, "inside generateDeviceInfoJson");
        JSONObject deviceInfoJson = new JSONObject();

        try {
            deviceInfoJson.put(ResultKeys.DEVICE_ID.getKey(), mDeviceId);
            deviceInfoJson.put(ResultKeys.PLAN_ID.getKey(), mPlanId);
            deviceInfoJson.put(ResultKeys.AGENT_URL.getKey(), mAgentURL);
            deviceInfoJson.put(ResultKeys.VERIFICATION_DATE.getKey(), mVerificationDate);
            Log.i(TAG, "inside generateDeviceInfoJson: after deviceInfoJson");
        } catch (JSONException e) {
            Log.i(TAG, "inside generateDeviceInfoJson: JSONException");
            mState = STATE_ERROR;
            setPluginError(BlinkUpPlugin.ERROR_JSON_ERROR);
            sendResultsToCallback();
        }

        return deviceInfoJson;
    }
}
