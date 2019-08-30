package com.lib.pinpoint;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public enum AnalyticsManager {

    INSTANCE;

    private static final String TAG = "AnalyticsManager";
    private static final String[] RMS_GLOBAL_SETTINGS = {
            "SRM_GBM",
            "SRM_deviceType",
            "SRM_region",
            "SRM_regionId",
            "SRM_subsidiary",
            "SRM_subsidiaryId",
            "SRM_country",
            "SRM_channel",
            "SRM_channelId",
            "SRM_subchannel",
            "SRM_subchannelId",
            "SRM_storeId",
            "SRM_shopperId",
            "SRM_deviceId",
    };

    private static final String[] RMS_ANALYTICS_ATTRIBUTES = {
            "gbm",
            "device_type",
            "region",
            "region_id",
            "subsidiary",
            "subsidiary_id",
            "country",
            "channel",
            "channel_id",
            "subchannel",
            "subchannel_id",
            "store_id",
            "shopper_id",
            "device_id",
    };

    private PinpointManager mPinpointManager;
    private WeakReference<Context> mContextRef;

    public void initialize(Context context) {
        Log.i(TAG, "initialize: ");
        // Initialize the AWS Mobile Client
        final AWSConfiguration awsConfig = new AWSConfiguration(context);

        if (IdentityManager.getDefaultIdentityManager() == null) {
            final IdentityManager identityManager = new IdentityManager(context, awsConfig);
            IdentityManager.setDefaultIdentityManager(identityManager);
        }

        try {
            PinpointConfiguration pinpointConfig = new PinpointConfiguration(
                    context,
                    IdentityManager.getDefaultIdentityManager().getCredentialsProvider(),
                    awsConfig);
            mPinpointManager = new PinpointManager(pinpointConfig);
        } catch (final AmazonClientException ex) {
            Log.e(TAG, "initialize: Unable to initialize PinpointManager. " + ex.getMessage(), ex);
        }

        // Enable logging
        java.util.logging.Logger.getLogger("com.amazonaws").setLevel(java.util.logging.Level.ALL);

        mContextRef = new WeakReference<>(context);
    }

    public void start() {
        Log.i(TAG, "start: ");
        if (BuildConfig.ANALYTICS_ENABLED) {
            mPinpointManager.getSessionClient().startSession();
        }
        else {
            Log.d(TAG, "Analytics is not enabled");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void logEvent(String name, String value) {
        Log.i(TAG, "logEvent: name:" + name + ", value:" + value);
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
            final AnalyticsEvent event =mPinpointManager.getAnalyticsClient().createEvent("RetailMLFaceDetectedEvent");
            final Map<String, String> map = createRMSAttributes();
            map.put("event_type", name);
            map.put("extra", value);
            map.forEach((k, v) ->  event.addAttribute(k, v));
            event.addAttribute("device_model", getDeviceModel());

            Log.i(TAG, "logEvent: " + event.toJSONObject().toString());
            if (BuildConfig.ANALYTICS_ENABLED) {
                mPinpointManager.getAnalyticsClient().recordEvent(event);
            }
        }
    }

    public void stop() {
        Log.i(TAG, "stop: ");
        if (BuildConfig.ANALYTICS_ENABLED) {
            mPinpointManager.getSessionClient().stopSession();
            mPinpointManager.getAnalyticsClient().submitEvents();
        }
    }

    public void reset() {
        Log.i(TAG, "reset: ");
    }

    private Map<String, String> createRMSAttributes() {
        final Context context = mContextRef.get();
        Map<String, String> result = new HashMap<>();
        if (context != null) {
            for (int i = 0; i < RMS_GLOBAL_SETTINGS.length; i++) {
                final String val = Settings.Global.getString(context.getContentResolver(), RMS_GLOBAL_SETTINGS[i]);
                result.put(RMS_ANALYTICS_ATTRIBUTES[i], val);
            }
        }
        return result;
    }

    private String getDeviceModel() {
        return android.os.Build.MODEL;
    }
}