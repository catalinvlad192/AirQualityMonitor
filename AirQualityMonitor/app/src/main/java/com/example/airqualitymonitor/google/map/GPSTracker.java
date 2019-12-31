package com.example.airqualitymonitor.google.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

public class GPSTracker
{
    private Context context_;
    private Location location_;
    private LocationManager locationManager_;
    private LocationListener locationListener_;

    public GPSTracker(Context c)
    {
        context_ = c;
        locationManager_ = (LocationManager) context_.getSystemService(Context.LOCATION_SERVICE);

        locationListener_ = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null)
                {
                    location_ = location;
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) {}
        };

        if (ActivityCompat.checkSelfPermission(context_, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context_,Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        locationManager_.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener_);
        locationManager_.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener_);
    }

    public Location getLocation()
    {
        return location_;
    }
}
