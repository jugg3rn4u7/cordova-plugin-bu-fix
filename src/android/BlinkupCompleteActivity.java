package com.eades.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.TokenStatusCallback;
import com.electricimp.blinkup.TokenAcquireCallback;
import com.electricimp.blinkup.ServerErrorHandler;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BlinkupCompleteActivity extends Activity {
    final private String TAG = "EADES BU PLUGIN";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
    private BlinkupController blinkup;
    private TokenStatusCallback tokenStatusCallback = new TokenStatusCallback() {
        @Override public void onSuccess(JSONObject json) {
            Log.i(TAG, "TokenStatusCallback : SUCCESS : " + json.toString());
            
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(BlinkupCompleteActivity.this);
            SharedPreferences.Editor editor = pref.edit();

            try {
                String dateStr = json.getString("claimed_at");
                dateStr = dateStr.replace("Z", "+0:00");
                Date claimedAt = dateFormat.parse(dateStr);

                String planId = json.getString("plan_id");
                String agentUrl = json.getString("agent_url");

                String impeeId = json.getString("impee_id");
                if (impeeId != null) {
                    impeeId = impeeId.trim();
                }

                editor.putString("planId", planId);
                editor.putString("agentUrl", agentUrl);
                editor.putLong("claimedAt", claimedAt.getTime());
                editor.putString("impeeId", impeeId);
                editor.commit();

                Log.i(TAG, "planId : " + planId);
                Log.i(TAG, "agentUrl : " + agentUrl);
                Log.i(TAG, "impeeId : " + impeeId);

                finish();
            } catch (JSONException e) {
                onError(e.getMessage());
            } catch (ParseException e) {
                onError(e.getMessage());
            }
        }

        @Override public void onError(String errorMsg) {
            Log.e(TAG, "TokenStatusCallback : ERROR : " + errorMsg);
            //Toast.makeText(_context, errorMsg, Toast.LENGTH_SHORT).show();
        }

        @Override public void onTimeout() {
            Log.e(TAG, "TokenStatusCallback : Timed out");
            //Toast.makeText(_context, "Timed out", Toast.LENGTH_SHORT).show();
        }
    };
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blinkup = BlinkupController.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        blinkup.getTokenStatus(tokenStatusCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        blinkup.cancelTokenStatusPolling();
    }
}