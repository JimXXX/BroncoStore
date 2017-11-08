package com.example.jimshire.broncostore;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.estimote.coresdk.common.config.EstimoteSDK;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.jimshire.broncostore.MainActivity.*;

public class MyApplication extends Application {

    protected String beaconStatus = null;
    private static final Map<String, List<String>> PLACES_BY_BEACONS;

    static {
        Map<String, List<String>> placesByBeacons = new HashMap<>();
        placesByBeacons.put("10735:22436", new ArrayList<String>() {{
            add("Minty");
        }});
        placesByBeacons.put("30857:62045)", new ArrayList<String>() {{
            add("Blueberry");
        }});
        placesByBeacons.put("59744:9419)", new ArrayList<String>() {{
            add("Icey");
        }});

        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    private List<String> placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return Collections.emptyList();
    }
    private BeaconManager beaconManager;
    private BeaconRegion region;

    @Override
    public void onCreate() {
        super.onCreate();

        EstimoteSDK.initialize(getApplicationContext(), "bronco-ke4", "cb29b32da4832a1393f186b184508efc");

        beaconManager = new BeaconManager(this);

        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
            @Override
            public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> beacons) {
                Log.d("Bronco", "enter region.....");
                showNotification("Welcome Broncos", "You see the light!");
                updateBeaconStatus("Beacon in range");
                beaconManager.startRanging(region);
            }

            @Override
            public void onExitedRegion(BeaconRegion beaconRegion) {
                Log.d("Bronco", "exit region.....");
                showNotification("Broncos", "Confirm payment");
                updateBeaconStatus("Beacon out of range");
                beaconManager.stopRanging(region);

            }
        });

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> beacons) {
                if (!beacons.isEmpty()) {
                    Beacon nearestBeacon = beacons.get(0);
                    List<String> places = placesNearBeacon(nearestBeacon);
                    Log.d("Bronco", "Nearest places: " + places);
                }

            }
        });

        region = new BeaconRegion("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
                beaconManager.startMonitoring(region);
            }
        });
    }




    public void cancelNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setContentIntent(pendingIntent);

        notification.setDefaults(Notification.DEFAULT_SOUND);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification.build());
    }

    public void updateBeaconStatus(String bStatus){
        Intent intent = new Intent("beaconStatus");
        intent.putExtra("beaconStatus", bStatus);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("Bronco:", "Brocasting...");
    }

}
