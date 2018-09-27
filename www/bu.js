/*global cordova, module*/

module.exports = {
    startBu: function (apiKey, timeoutMs, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BlinkUpPlugin", "startBu", [apiKey, timeoutMs]);
    }
};
