package com.example.tirowka.timer;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final long INTERVAL = 1000*2;
    private static final long FASTEST_INTERVAL = 1000;
    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;
    Location currentLocation, start, end;

    static double distance = 0;

    private final IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        return binder;
    }

    private void createLocationRequest(){
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest, (LocationListener) this);
        }catch (SecurityException e){

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Timer.progressDialog.dismiss();
        currentLocation = location;
        if (start == null){
            start = end = currentLocation;
        }else
            end = currentLocation;

        updateUI();
    }

    private void updateUI(){
        if (Timer.p == 0){
            distance = distance + (start.distanceTo(end)/1000.00);
            Timer.finishTime = System.currentTimeMillis();
            long diff = Timer.finishTime - Timer.startTime;
            diff = TimeUnit.MILLISECONDS.toMinutes(diff);

            Timer.tv_time.setText("Total time : " + diff + " minutes");

            Timer.tv_distance.setText(new DecimalFormat("#.###").format(distance) + " Km/s.");

            start = end;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopLocationUpdate();
        if (googleApiClient.isConnected())
            googleApiClient.disconnect();
        start = end = null;
        distance = 0;
        return super.onUnbind(intent);
    }

    private void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        distance = 0;
    }

    public class LocalBinder extends Binder {

        public LocationService getService(){
            return LocationService.this;
        }
    }
}
