/*global cordova, module*/

module.exports = {
    startBu: function (apiKey, timeoutMs, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "cordova-blinkup-plugin", "startBu", [apiKey, timeoutMs]);
    }
};
