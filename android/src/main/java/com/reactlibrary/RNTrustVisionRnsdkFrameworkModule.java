
package com.reactlibrary;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Base64;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.trustingsocial.apisdk.TVApi;
import com.trustingsocial.apisdk.data.TVApiError;
import com.trustingsocial.apisdk.data.TVCallback;
import com.trustingsocial.apisdk.data.TVEmptyException;
import com.trustingsocial.tvcoresdk.external.SelfieImage;
import com.trustingsocial.tvcoresdk.external.TVCapturingCallBack;
import com.trustingsocial.tvcoresdk.external.TVCardType;
import com.trustingsocial.tvcoresdk.external.TVDetectionError;
import com.trustingsocial.tvcoresdk.external.TVDetectionResult;
import com.trustingsocial.tvcoresdk.external.TVIDConfiguration;
import com.trustingsocial.tvcoresdk.external.TVImageClass;
import com.trustingsocial.tvcoresdk.external.TVSDKCallback;
import com.trustingsocial.tvcoresdk.external.TVSDKConfiguration;
import com.trustingsocial.tvcoresdk.external.TVSelfieConfiguration;
import com.trustingsocial.tvcoresdk.external.TVTransactionData;
import com.trustingsocial.tvsdk.TrustVisionSDK;

import java.util.List;

import static com.reactlibrary.RNTrustVisionUtils.convertToBase64;

public class RNTrustVisionRnsdkFrameworkModule extends ReactContextBaseJavaModule {
    private static String INTERNAL_ERROR = "internal_error";
    private static String SDK_CANCELED = "sdk_canceled";
    private static String SDK_CANCELED_MESSAGE = "sdk is canceled by user";

    private final ReactApplicationContext reactContext;

    public RNTrustVisionRnsdkFrameworkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNTrustVisionRnsdkFramework";
    }

    @ReactMethod
    public void initialize(String accessKeyId, String accessKeySecret, String endpoint, String xRequestId, Boolean isForce, final Promise promise) {
        Activity activity = getCurrentActivity();
        TrustVisionSDK.TVInitializeListener listener = new TrustVisionSDK.TVInitializeListener() {
            @Override
            public void onInitSuccess() {
                promise.resolve("initSuccess");
            }

            @Override
            public void onInitError(TVDetectionError tvDetectionError) {
                promise.reject(String.valueOf(tvDetectionError.getDetailErrorCode()), tvDetectionError.getErrorDescription());
            }
        };

        try {
            TrustVisionSDK.init(activity, endpoint, accessKeyId, accessKeySecret, "vi", xRequestId, listener, null);
        } catch (TVEmptyException e) {
            e.printStackTrace();
            promise.reject(String.valueOf(TVDetectionError.DETECTION_ERROR_AUTHENTICATION_MISSING), e.getMessage());
        }
    }

    @ReactMethod
    public void initialize(String accessKeyId, String accessKeySecret, String endpoint, String xRequestId, final Promise promise) {
        Activity activity = getCurrentActivity();
        TrustVisionSDK.TVInitializeListener listener = new TrustVisionSDK.TVInitializeListener() {
            @Override
            public void onInitSuccess() {
                promise.resolve("initSuccess");
            }

            @Override
            public void onInitError(TVDetectionError tvDetectionError) {
                promise.reject(String.valueOf(tvDetectionError.getDetailErrorCode()), tvDetectionError.getErrorDescription());
            }
        };

        try {
            if (TextUtils.isEmpty(accessKeyId) || TextUtils.isEmpty(accessKeySecret)) {
                TrustVisionSDK.init(activity, endpoint, "vi", xRequestId, listener, null);
            } else {
                TrustVisionSDK.init(activity, endpoint, accessKeyId, accessKeySecret, "vi", xRequestId, listener, null);
            }
        } catch (TVEmptyException e) {
            promise.reject(String.valueOf(TVDetectionError.DETECTION_ERROR_AUTHENTICATION_MISSING), e.getMessage());
        }


    }

    @ReactMethod
    public void getCardTypes(Promise promise) {
        List<TVCardType> cardTypes = TrustVisionSDK.getCardTypes();
        try {
            WritableArray array = RNTrustVisionUtils.toWritableArray(cardTypes);
            promise.resolve(array);
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject(INTERNAL_ERROR, "get cardTypes error");
        }
    }

    @ReactMethod
    public void getSelfieCameraMode(Promise promise) {
        try {
            promise.resolve(TrustVisionSDK.getCameraOption().toString());
        } catch (Exception ex) {
            promise.reject(INTERNAL_ERROR, "getSelfieCameraMode error");
        }
    }

    @ReactMethod
    public void getLivenessOption(Promise promise) {
        try {
            promise.resolve(RNTrustVisionUtils.toWritableArrayObject(TrustVisionSDK.getLivenessOptions()));
        } catch (Exception ex) {
            promise.reject(INTERNAL_ERROR, ex.getMessage());
        }
    }

    @ReactMethod
    public void startTransaction(String referenceID, final Promise promise) {
        try {
            TrustVisionSDK.startTransaction(referenceID, new TVSDKCallback<TVTransactionData>() {
                @Override
                public void onSuccess(TVTransactionData data) {
                    promise.resolve(data);

                }

                @Override
                public void onError(TVDetectionError tvDetectionError) {
                    promise.reject(tvDetectionError.getDetailErrorCode(), RNTrustVisionUtils.convertErrorString(tvDetectionError));
                }
            });
        } catch (Exception ex) {
            promise.reject(INTERNAL_ERROR, ex.getMessage());
        }
    }

    @ReactMethod
    public void endTransaction(final Promise promise) {
        try {
            TrustVisionSDK.endTransaction(getCurrentActivity(), new TVSDKCallback<String>() {
                @Override
                public void onSuccess(String transactionId) {
                    WritableMap data = new WritableNativeMap();
                    data.putString("transactionId", transactionId);
                    promise.resolve(data);
                }

                @Override
                public void onError(TVDetectionError tvDetectionError) {
                    promise.reject(tvDetectionError.getDetailErrorCode(), RNTrustVisionUtils.convertErrorString(tvDetectionError));
                }
            });
        } catch (Exception ex) {
            promise.reject(INTERNAL_ERROR, ex.getMessage());
        }
    }

    @ReactMethod
    public void startFlow(ReadableMap config, final Promise promise) {
        try {
            TVSDKConfiguration configuration = RNTrustVisionUtils.convertConfigFromMap(config);
            TrustVisionSDK.startTrustVisionSDK(getCurrentActivity(), configuration, new TVCapturingCallBack() {
                @Override
                public void onError(TVDetectionError tvDetectionError) {
                    promise.reject(tvDetectionError.getDetailErrorCode(), RNTrustVisionUtils.convertErrorString(tvDetectionError));
                }

                @Override
                public void onSuccess(TVDetectionResult tvDetectionResult) {
                    try {
                        promise.resolve(convertResult(tvDetectionResult));
                    } catch (Exception e) {
                        promise.reject(INTERNAL_ERROR, "Parse result error");
                    }
                }

                @Override
                public void onCanceled() {
                    promise.reject(SDK_CANCELED, SDK_CANCELED_MESSAGE);
                }
            });
        } catch (Exception ex) {
            promise.reject(INTERNAL_ERROR, ex.getMessage());
        }
    }

    @ReactMethod
    public void startIdCapturing(ReadableMap config, final Promise promise) {
        try {
            TVIDConfiguration configuration = RNTrustVisionUtils.convertIdConfigFromMap(config);
            TrustVisionSDK.startIDCapturing(getCurrentActivity(), configuration, new TVCapturingCallBack() {
                @Override
                public void onError(TVDetectionError tvDetectionError) {
                    promise.reject(tvDetectionError.getDetailErrorCode(), RNTrustVisionUtils.convertErrorString(tvDetectionError));
                }

                @Override
                public void onSuccess(TVDetectionResult tvDetectionResult) {
                    try {
                        promise.resolve(convertResult(tvDetectionResult));
                    } catch (Exception e) {
                        promise.reject(INTERNAL_ERROR, "Parse result error");
                    }
                }

                @Override
                public void onCanceled() {
                    promise.reject(SDK_CANCELED, SDK_CANCELED_MESSAGE);
                }
            });
        } catch (Exception ex) {
            promise.reject(INTERNAL_ERROR, ex.getMessage());
        }
    }

    @ReactMethod
    public void startSelfieCapturing(ReadableMap config, final Promise promise) {
        try {
            TVSelfieConfiguration configuration = RNTrustVisionUtils.convertSelfieConfigFromMap(config);
            TrustVisionSDK.startSelfieCapturing(getCurrentActivity(), configuration, new TVCapturingCallBack() {
                @Override
                public void onError(TVDetectionError tvDetectionError) {
                    promise.reject(tvDetectionError.getDetailErrorCode(), RNTrustVisionUtils.convertErrorString(tvDetectionError));
                }

                @Override
                public void onSuccess(TVDetectionResult tvDetectionResult) {
                    try {
                        promise.resolve(convertResult(tvDetectionResult));
                    } catch (Exception e) {
                        promise.reject(INTERNAL_ERROR, "Parse result error");
                    }
                }

                @Override
                public void onCanceled() {
                    promise.reject(SDK_CANCELED, SDK_CANCELED_MESSAGE);
                }
            });
        } catch (Exception ex) {
            promise.reject(INTERNAL_ERROR, ex.getMessage());
        }
    }

    @ReactMethod
    public void matchFace(String imageId1, String imageId2, final Promise promise) {
        try {
            TrustVisionSDK.faceMatching(imageId1, imageId2, new TVCapturingCallBack() {
                @Override
                public void onError(TVDetectionError tvDetectionError) {
                    promise.reject(tvDetectionError.getDetailErrorCode(), RNTrustVisionUtils.convertErrorString(tvDetectionError));
                }

                @Override
                public void onSuccess(TVDetectionResult tvDetectionResult) {
                    try {
                        promise.resolve(convertResult(tvDetectionResult));
                    } catch (Exception e) {
                        promise.reject(INTERNAL_ERROR, "Parse result error");
                    }

                }

                @Override
                public void onCanceled() {
                    promise.reject(SDK_CANCELED, SDK_CANCELED_MESSAGE);
                }
            });
        } catch (Exception ex) {
            promise.reject(INTERNAL_ERROR, ex.getMessage());
        }
    }

    @ReactMethod
    public void downloadImage(String imageId, final Promise promise) {
        try {
            TVApi.getInstance().downloadImage(imageId, new TVCallback<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    promise.resolve(Base64.encodeToString(bytes, Base64.NO_WRAP));
                }

                @Override
                public void onError(List<TVApiError> list) {
                    TVDetectionError error = new TVDetectionError(list.get(0));
                    promise.reject(error.getDetailErrorCode(), RNTrustVisionUtils.convertErrorString(error));
                }
            });
        } catch (Exception ex) {
            promise.reject(INTERNAL_ERROR, ex.getMessage());
        }
    }

    private WritableMap convertResult(TVDetectionResult tvDetectionResult) throws Exception {
        WritableMap result = RNTrustVisionUtils.objectToMap(tvDetectionResult);
        if (tvDetectionResult.getSelfieImages() != null && !tvDetectionResult.getSelfieImages().isEmpty()) {
            WritableArray selfieArray = new WritableNativeArray();
            for (SelfieImage selfieImage: tvDetectionResult.getSelfieImages()) {
                selfieArray.pushMap(convertSelfieImage(selfieImage));
            }
            result.putArray("selfieImages", selfieArray);
        }

        if (tvDetectionResult.getFrontCardImage() != null) {
            result.putMap("idFrontImage", convertTVImageClass(tvDetectionResult.getFrontCardImage()));
        }

        if (tvDetectionResult.getBackCardImage() != null) {
            result.putMap("idBackImage", convertTVImageClass(tvDetectionResult.getBackCardImage()));
        }
        return result;
    }

    private WritableMap convertSelfieImage(SelfieImage selfieImage) {
        WritableMap selfieMap = new WritableNativeMap();
        selfieMap.putString("gesture_type", selfieImage.getGestureType().toGestureType().toString().toLowerCase());

        TVImageClass frontalImage = selfieImage.getFrontalImage();
        if (frontalImage != null) {
            selfieMap.putMap("frontal_image", convertTVImageClass(frontalImage));
        }

        TVImageClass gestureImage = selfieImage.getGestureImage();
        if (gestureImage != null) {
            selfieMap.putMap("gesture_image", convertTVImageClass(gestureImage));
        }

        return selfieMap;
    }

    private WritableMap convertTVImageClass(TVImageClass tvImageClass) {
        WritableMap map = new WritableNativeMap();
        map.putString("raw_image_base64", convertToBase64(tvImageClass.getImage()));
        map.putString("encrypted_image_hex_string", tvImageClass.getEncryptedHexString());
        map.putString("image_id", tvImageClass.getImageId());
        return map;
    }
}