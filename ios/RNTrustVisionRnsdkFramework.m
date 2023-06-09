
#import <objc/runtime.h>
#import "RNTrustVisionRnsdkFramework.h"
#import <React/RCTConvert.h>
@import TrustVisionSDK;
@import TrustVisionCoreSDK;
@import TrustVisionAPI;

@implementation RNTrustVisionRnsdkFramework

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(getLogFilePathsWithResolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSArray *logFilePaths = [[TrustVisionSdk shared] getLogFilePaths];
    NSLog(@"Log files %@", logFilePaths);
    resolve(logFilePaths);
}

RCT_EXPORT_METHOD(initialize:(NSString *)accessKeyId
                  accessKeySecret:(NSString *)accessKeySecret
                  endpoint:(NSString *)endpoint
                  xRequestId:(NSString *)xRequestId
                  isForce:(BOOL)isForce
//                  enableDebuggingLog:(BOOL)enableDebuggingLog
                  withResolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    
    [[TrustVisionSdk shared] initializeWithAccessKeyId:accessKeyId
                                       accessKeySecret:accessKeySecret
                                               baseUrl:endpoint
                                     localizationFiles:NULL
                                          languageCode:@"vi"
                                            xRequestId:xRequestId
                                              isForced:isForce
//                                    enableDebuggingLog:enableDebuggingLog
                                    enableDebuggingLog:false
                                               success:^{
        resolve(@"Initialize TV framework done.");
    }
                                               failure:^(TVError *error) {
        [self rejectWithRejecter:reject TvError:error];
    }
                                               onEvent:^(TVTrackingEvent * event) {
        // do nothing for nows
    }];
}

// MARK: - Get settings
RCT_EXPORT_METHOD(getCardTypes:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSArray<TVCardType *> *cardTypes = [[TrustVisionSdk shared] getCardTypes];
    NSMutableArray<NSDictionary *> *cardTypeDicts = [[NSMutableArray alloc] init];
    for (TVCardType *cardType in cardTypes) {
        NSDictionary *cardTypeDict = [cardType toDictionary];
        [cardTypeDicts addObject:cardTypeDict];
    }
    resolve(cardTypeDicts);
}

RCT_EXPORT_METHOD(getSelfieCameraMode:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    TVCameraOption cameraMode = [[TrustVisionSdk shared] getSelfieCameraMode];
    resolve(@(cameraMode));
}

RCT_EXPORT_METHOD(getLivenessOption:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSArray<NSString *> *livenessOptions = [[TrustVisionSdk shared] getLivenessOptions];
    resolve(livenessOptions);
}

RCT_EXPORT_METHOD(getIdCardSanityCheckingEnable:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    BOOL idCardSanityChecking = [[TrustVisionSdk shared] getIdCardSanityCheckingEnable];
    resolve(@(idCardSanityChecking));
}

RCT_EXPORT_METHOD(getSelfieSanityCheckingEnable:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    BOOL selfieSanityChecking = [[TrustVisionSdk shared] getSelfieSanityCheckingEnable];
    resolve(@(selfieSanityChecking));
}

// MARK: - Flows
RCT_EXPORT_METHOD(startTransaction:(NSString *)referenceId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [[TrustVisionSdk shared] startTransactionWithReferenceId:referenceId success:^(NSString * transactionId) {
        resolve(transactionId);
    } failure:^(TVError * error) {
        [self rejectWithRejecter:reject TvError: error];
    }];
}

RCT_EXPORT_METHOD(endTransactionWithResolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [[TrustVisionSdk shared] endTransactionWithSuccess:^{
        resolve(@{});
    } failure:^(TVError * error) {
        [self rejectWithRejecter:reject TvError: error];
    }];
}

RCT_EXPORT_METHOD(startFlow:(NSDictionary *)configDict
                  withResolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    TVSDKConfig *config = [TVSDKConfig dictToObjWithDict: configDict];

    dispatch_async(dispatch_get_main_queue(), ^{
        
        UIViewController *mainVc = [[TrustVisionSdk shared] startFullFlowWithConfig:config
                                                                          success:^(TVDetectionResult * result) {
            resolve([result toDictionary]);
        }
                                                                          failure:^(TVError * error) {
            [self rejectWithRejecter:reject TvError: error];
        }
                                                                     cancellation:^{
            [self rejectWithCancelationErrorWithRejecter:reject];
        }];
        
        [self presentViewController:mainVc];
    });
}

RCT_EXPORT_METHOD(startIdCapturing:(NSDictionary *)configDict
                  withResolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    TVIdCardConfiguration *config = [TVIdCardConfiguration dictToObjWithDict: configDict];

    dispatch_async(dispatch_get_main_queue(), ^{

        UIViewController *mainVc = [[TrustVisionSdk shared] startIdCapturingWithConfiguration:config
                                                           success:^(TVDetectionResult * result) {
            resolve([result toDictionary]);
        }
                                                           failure:^(TVError * error) {
            [self rejectWithRejecter:reject TvError: error];
        }
                                                      cancellation:^{
            [self rejectWithCancelationErrorWithRejecter:reject];
        }];
        
        [self presentViewController:mainVc];
    });
}

RCT_EXPORT_METHOD(startSelfieCapturing:(NSDictionary *)configDict
                  withResolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    TVSelfieConfiguration *config = [TVSelfieConfiguration dictToObjWithDict: configDict];

    dispatch_async(dispatch_get_main_queue(), ^{

        UIViewController *mainVc = [[TrustVisionSdk shared] startSelfieCapturingWithConfiguration:config
                                                                                 success:^(TVDetectionResult * result) {
            resolve([result toDictionary]);
        }
                                                                                 failure:^(TVError * error) {
            [self rejectWithRejecter:reject TvError: error];
        }
                                                                            cancellation:^{
            [self rejectWithCancelationErrorWithRejecter:reject];
        }];

        [self presentViewController:mainVc];
    });
}

RCT_EXPORT_METHOD(matchFace:(NSString *)image1Id
                  image2Id:(NSString *)image2Id
                  withResolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [[TrustVisionSdk shared] matchFaceWithImage1Id:image1Id image2Id:image2Id success:^(TVCompareFacesResult * result) {
        resolve([result toDictionary]);
    } failure:^(TVError * error) {
        [self rejectWithRejecter:reject TvError: error];
    }];
}

RCT_EXPORT_METHOD(downloadImage:(NSString *)imageId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [[TrustVisionSdk shared] downloadImageWithImageId:imageId success:^(UIImage * image) {
        NSString *base64 = [self getBase64StringFromImage: image];
        resolve(base64);
    } failure:^(TVError * error) {
        [self rejectWithRejecter:reject TvError: error];
    }];
}

// MARK: - Helpers
- (void) rejectWithCancelationErrorWithRejecter: (RCTPromiseRejectBlock)reject {
    reject(@"sdk_canceled", @"sdk is canceled by user", NULL);
}

- (void) presentViewController: (UIViewController *)vc {
    [[[[[UIApplication sharedApplication] delegate] window] rootViewController] presentViewController:vc animated:NO completion:nil];
}
         
- (void) rejectWithRejecter: (RCTPromiseRejectBlock)reject TvError: (TVError *)tvError {
    NSError *e = [NSError errorWithDomain:@"TrustVisionSDKError" code:[[tvError errorCode] integerValue] userInfo: nil];
    reject([tvError errorCode], [tvError description], e);
}

- (NSString *) getBase64StringFromImage: (UIImage *)image {
    NSData *imageData = UIImagePNGRepresentation(image);
    NSString *imageBase64 = [imageData base64EncodedStringWithOptions:NSDataBase64EncodingEndLineWithLineFeed];
    return imageBase64;
}

- (NSDictionary *)toDictionary: (NSObject *)obj {
    unsigned int count = 0;
    
    NSMutableDictionary *dictionary = [NSMutableDictionary new];
    objc_property_t *properties = class_copyPropertyList([obj class], &count);
    
    for (int i = 0; i < count; i++) {
        
        NSString *key = [NSString stringWithUTF8String:property_getName(properties[i])];
        id value = [obj valueForKey:key];
        
        if (value == nil) {
            // nothing todo
        }
        else if ([value isKindOfClass:[NSNumber class]]
                 || [value isKindOfClass:[NSString class]]
                 || [value isKindOfClass:[NSDictionary class]]
                 || [value isKindOfClass:[NSData class]]) {
            // TODO: extend to other types
            [dictionary setObject:value forKey:key];
        }
        else if ([value isKindOfClass:[UIImage class]]) {
            NSString *base64 = [self getBase64StringFromImage: value];
            [dictionary setObject:base64 forKey:key];
        }
        else if ([value isKindOfClass:[NSObject class]]) {
            [dictionary setObject:[self toDictionary: value] forKey:key];
        }
        else {
            NSLog(@"Invalid type for %@ (%@)", NSStringFromClass([obj class]), key);
        }
    }
    
    return dictionary;
}

@end
