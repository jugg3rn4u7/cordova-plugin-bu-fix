/*global cordova, module*/

module.exports = {
    startBu: function (config, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Bu", "startBu", [config]);
    }
};
