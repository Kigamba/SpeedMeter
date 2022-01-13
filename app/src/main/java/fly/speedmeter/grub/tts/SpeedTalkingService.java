package fly.speedmeter.grub.tts;

import android.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import fly.speedmeter.grub.Data;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SpeedTalkingService extends IntentService implements GpsStatus.Listener, LocationListener {

    private int NOTIFY_ID = 1337;
    private int FOREGROUND_ID = 1338;
    private int seconds = 15;
    private Speaker speaker = null;

    private BroadcastReceiver speakSpeedRequestReceiver;
    private Data.OnGpsServiceUpdate onGpsServiceUpdate;
    private Data data;

    private LocationManager mLocationManager;
    private double lastSpeed = 0d;
    private double lastSpeakSpeed = 0d;

    public SpeedTalkingService() {
        super("SpeedTalkingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Do nothing for now
    }


    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (speaker == null) {
            speaker = new Speaker(this);
        }

        startForeground(
                FOREGROUND_ID,
                buildForegroundNotification()
        );

        speakSpeedRequestReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    int currentSpeed = intent.getIntExtra("SPEED", 0);

                    if (currentSpeed != 0) {
                        speaker.speak(String.valueOf(currentSpeed));
                    }
                }
            }
        };


        onGpsServiceUpdate = new Data.OnGpsServiceUpdate() {
            @Override
            public void update() {
                double currentSpeed = data.getCurSpeed();

                /*if (lastSpeed == 0) {
                    lastSpeed = currentSpeed;
                } else {*/
                    /*if (Math.abs(lastSpeed - currentSpeed) >= 3) {//10) {
                        speaker.speak(String.valueOf((int) currentSpeed));
                    }

                    lastSpeed = currentSpeed;*/
                //}

            }
        };

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        setupGps();


        // Register broadcast receiver
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(speakSpeedRequestReceiver, new IntentFilter("SPEED-SPEAK"));

        /*new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(seconds * 1000L);

                        // Request for the latest speed
                        speaker.speak(String.valueOf((int) lastSpeed));

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();*/

        return Service.START_STICKY;
    }

    private void setupGps() {

        if (data == null) {
            data = new Data(onGpsServiceUpdate);
            data.setRunning(true);
        } else {
            data.setOnGpsServiceUpdate(onGpsServiceUpdate);
            data.setRunning(true);
        }

        if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        } else {
            Log.w("MainActivity", "No GPS location provider found. GPS data display will not be available.");
        }

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //showGpsDisabledDialog();
        }

        mLocationManager.addGpsStatusListener(this);
    }


    private Notification buildForegroundNotification() {
        if (Build.VERSION.SDK_INT < 26) {
            //NotificationChannel notificationChannel = createNotificationChannel()

            NotificationCompat.Builder b = new NotificationCompat.Builder(this);
            b.setOngoing(true)
                    .setContentTitle(getString(fly.speedmeter.grub.R.string.riding_aid_active))
                    .setContentText(getString(fly.speedmeter.grub.R.string.mirror_checking_aid_running))
                    .setSmallIcon(R.drawable.stat_sys_download);
            return b.build();
        } else {
            //NotificationChannel notificationChannel = createNotificationChannel()

            NotificationCompat.Builder b = new NotificationCompat.Builder(this);
            b.setOngoing(true)
                    .setContentTitle(getString(fly.speedmeter.grub.R.string.riding_aid_active))
                    .setContentText(getString(fly.speedmeter.grub.R.string.mirror_checking_aid_running))
                    .setSmallIcon(R.drawable.stat_sys_download);
            return b.build();
        }
    }

    @Override
    public void onDestroy() {
        if (speaker != null) {
            speaker.stop();
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(speakSpeedRequestReceiver);
        super.onDestroy();
    }

    @Override
    public void onGpsStatusChanged(int event) {
        /*switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                Iterable<GpsSatellite> sats = gpsStatus.getSatellites();
                for (GpsSatellite sat : sats) {
                    satsInView++;
                    if (sat.usedInFix()) {
                        satsUsed++;
                    }
                }
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    //showGpsDisabledDialog();
                }
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
        }*/
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.hasSpeed()) {
            double currentSpeed = location.getSpeed() * 3.6;

                /*if (lastSpeed == 0) {
                    lastSpeed = currentSpeed;
                } else {*/
            if (Math.abs(lastSpeakSpeed - currentSpeed) >= 3) {//10) {
                lastSpeakSpeed = currentSpeed;
                speaker.speak(String.valueOf((int) currentSpeed));
            }

            lastSpeed = currentSpeed;
        }

        if (data.isRunning()) {
            if (location.hasSpeed()) {
                data.setCurSpeed(location.getSpeed() * 3.6);

            }
            data.update();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    /*
    private fun createNotificationChannel(): NotificationChannel {
        val notificationChannel = NotificationChannel(
                "riding-aid",
                "Riding Aid",
                NotificationManager.IMPORTANCE_DEFAULT
        )

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        return notificationChannel
    }*/
}
