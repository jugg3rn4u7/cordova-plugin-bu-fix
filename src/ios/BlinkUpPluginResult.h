// see README.md for format of JSON string to be sent to callback

#import <Cordova/CDV.h>

@class BUDeviceInfo;

@interface BlinkUpPluginResult : NSObject

typedef NS_ENUM(NSInteger, BlinkUpPluginState) {
    Started,
    Completed,
    Error
};
typedef NS_ENUM(NSInteger, BlinkUpErrorType) {
    BlinkUpSDKError,
    PluginError
};

//*************************************
// Public methods
//*************************************
- (void) setBlinkUpError:(NSError *)error;
- (void) setPluginError:(NSInteger)errorCode;
- (NSString *)getResultsAsJsonString;
- (CDVCommandStatus) getCordovaStatus;
- (BOOL) getKeepCallback;

//=====================================
// BlinkUp result
//=====================================
@property BlinkUpPluginState state;
@property NSInteger statusCode;
@property BlinkUpErrorType errorType;
@property NSInteger errorCode;
@property NSString *errorMsg;
@property BUDeviceInfo *deviceInfo;

@end
