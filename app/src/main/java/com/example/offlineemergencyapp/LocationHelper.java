package com.example.offlineemergencyapp.helpers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

public class LocationHelper {

    private final Context context;
    private final LocationManager locationManager;
    private final GPSCallback gpsCallback;

    public interface GPSCallback {
        void onGPSStatusChanged(boolean enabled);
    }

    public interface LocationCallback {
        void onLocationReceived(Location location);
    }

    public LocationHelper(Context context, GPSCallback callback) {
        this.context = context;
        this.gpsCallback = callback;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void getCurrentLocation(final LocationCallback callback) {
        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    callback.onLocationReceived(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, android.os.Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {
                    callback.onLocationReceived(null);
                }
            }, null);
        } catch (SecurityException e) {
            callback.onLocationReceived(null);
        }
    }

    public void startLocationUpdates() {
        gpsCallback.onGPSStatusChanged(true);
    }

    public void stopLocationUpdates() {
        gpsCallback.onGPSStatusChanged(false);
    }
}
