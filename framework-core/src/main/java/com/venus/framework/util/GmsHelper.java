package com.venus.framework.util;

import android.content.Context;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

/**
 * Created by ywu on 15/10/28.
 */
public class GmsHelper {

    public static boolean handleGooglePlayException(Context ctx, Throwable e) {
        boolean handled = true;
        if (e instanceof GooglePlayServicesNotAvailableException) {
            handlePlaySvcUnavailable(ctx,
                    GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(ctx), e);
        } else if (e instanceof GooglePlayServicesRepairableException) {
            handlePlaySvcUnavailable(ctx,
                    ((GooglePlayServicesRepairableException) e).getConnectionStatusCode(), e);
        } else {
            handled = false;
        }

        return handled;
    }

    private static void handlePlaySvcUnavailable(Context ctx, int status, Throwable e) {
        // Prompt the user to install/update/enable Google Play services.
        GoogleApiAvailability.getInstance().showErrorNotification(ctx, status);
        L.e("GooglePlayService unavailable, status is " + status, e);
    }
}
