package com.example.airqualitymonitor;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import javax.annotation.Nullable;

public class NotificationService extends IntentService
{
    public static final String TAG = "[NotificationService]";
    private boolean isRunning_ = true;
    private Context context_;

    public NotificationService()
    {
        super("NotificationService");
        Log.d(TAG, "constructor");
        setIntentRedelivery(true);

    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG, "onCreate");
        context_ = getApplicationContext();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        while(isRunning_)
        {
            // Create notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "AirQualityMonitorNotificationChannel";
                String description = "News!";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel("1300", name,
                        importance);
                channel.setDescription(description);

                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context_
                        .getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            // Create an explicit intent for an Activity in your app
            Intent mainActIntent = new Intent(context_, MainActivity.class);
            mainActIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context_, 0, mainActIntent, 0);

            // Create notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context_, "1300")
                    .setSmallIcon(R.drawable.button_round2_no_bg)
                    .setContentTitle("AirQualityMonitor")
                    .setContentText("New air information might be available. Check it out!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            // Send notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context_);

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(1301, builder.build());

            SystemClock.sleep(1000*60*60);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
