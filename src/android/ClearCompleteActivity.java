package com.eades.plugin;

import android.app.Activity;
import android.os.Bundle;

/*****************************************************
 * When the clearing BlinkUpPlugin process completes, it
 * executes the BlinkUpClearIntent set in BlinkUpPlugin.java,
 * starting this activity, which tells the callback
 * that clearing is complete, then dismisses
 ******************************************************/
public class ClearCompleteActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // send callback that we've cleared device
        BlinkUpPluginResult clearResult = new BlinkUpPluginResult();
        clearResult.setState(BlinkUpPluginResult.STATE_COMPLETED);

        // set the status code depending if we just cleared the cache
        if (BlinkUpPlugin.getClearCache()) {
            clearResult.setStatusCode(BlinkUpPlugin.STATUS_CLEAR_WIFI_AND_CACHE_COMPLETE);
            BlinkUpPlugin.setClearCache(false);
        }
        else {
            clearResult.setStatusCode(BlinkUpPlugin.STATUS_CLEAR_WIFI_COMPLETE);
        }

        clearResult.sendResultsToCallback();

        finish();
    }
}
