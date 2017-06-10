package com.sowmya.securechat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.client.Firebase;

/**
 * Created by sowmya on 5/26/17.
 */

public class GPS_Service extends Service{

    private LocationListener listener;
    private LocationManager locationManager;
    private static final Firebase firebase = new Firebase("https://securechat-96a99.firebaseio.com/");
    private static final String TAG = "GPS_SERVICE";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        SharedPreferences sharedPreferences = getSharedPreferences("SecureChat",0);
        String id = sharedPreferences.getString("user_id","");

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e(TAG,"Gps service called");
                Intent i = new Intent("location_update");
                i.putExtra("coordinates",location.getLatitude()+" "+location.getLongitude());
                Log.e(TAG,"location values "+location.getLatitude()+" "+location.getLongitude());

                firebase.child("location").child(id).setValue(location.getLatitude()+" "+location.getLongitude());

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                /*Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);*/

                /*Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
                intent.putExtra("enabled", true);
                sendBroadcast(intent);*/
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,listener);
        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,3000,0,listener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager!=null){
            locationManager.removeUpdates(listener);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

}
