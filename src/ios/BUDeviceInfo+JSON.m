#import <BlinkUp/BlinkUp.h>
#import "BUDeviceInfo+JSON.h"
#import "BlinkUpPlugin.h"

NSString * const DEVICE_ID_KEY = @"deviceId";
NSString * const PLAN_ID_KEY = @"planId";
NSString * const AGENT_URL_KEY = @"agentURL";
NSString * const VERIFICATION_DATE_KEY = @"verificationDate";

@implementation BUDeviceInfo (JSON)

-(NSDictionary*)toDictionary {
    NSMutableDictionary *deviceInfo = [[NSMutableDictionary alloc] init];
    [deviceInfo setValue:self.planId forKey:PLAN_ID_KEY];
    [deviceInfo setValue:self.deviceId forKey:DEVICE_ID_KEY];
    [deviceInfo setValue:self.agentURL.description forKey:AGENT_URL_KEY];
    [deviceInfo setValue:self.verificationDate.description forKey:VERIFICATION_DATE_KEY];
    return deviceInfo;
}

@end
