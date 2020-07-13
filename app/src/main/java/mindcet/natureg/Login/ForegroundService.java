package mindcet.natureg.Login;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import mindcet.natureg.R;

import static mindcet.natureg.Login.ForegroundDriver.locationListenerGPS;
import static mindcet.natureg.Login.ForegroundDriver.locationManager;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    Handler mainHandler;

    @Override
    public void onCreate() {
        mainHandler = new Handler(this.getMainLooper());
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, ForegroundDriver.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.foreground_service))
                .setContentText(getString(R.string.foreground_service_example))
                .setSmallIcon(R.drawable.ic_stat_name)
                //.setContentIntent(pendingIntent)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        startForeground(1, notification);


        //do heavy work on a background thread


        mainHandler.post(new Runnable() {
            @Override
            public void run() {
/*
                Toast.makeText(getBaseContext(), "I'm a toast!", Toast.LENGTH_SHORT).show();
*/
                if (ActivityCompat.checkSelfPermission(ForegroundService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(ForegroundService.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        3000,
                        0, locationListenerGPS);
                // Do your stuff here related to UI, e.g. show toast
            }
        });

        //stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locationListenerGPS);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


}
