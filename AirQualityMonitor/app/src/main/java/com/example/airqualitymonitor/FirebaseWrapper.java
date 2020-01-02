package com.example.airqualitymonitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.airqualitymonitor.db.model.DummyDbEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseWrapper
{
    private FirebaseDatabase database_;
    private DatabaseReference myRef_;
    private RealmWrapper realmWrapper_;
    private Context context_;

    public FirebaseWrapper(RealmWrapper realm, final Context context)
    {
        context_ = context;
        database_ = FirebaseDatabase.getInstance();
        myRef_ = database_.getReference();
        realmWrapper_ = realm;

        myRef_.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                for(DataSnapshot x : dataSnapshot.getChildren())
                {
                    DummyDbEntry value = x.getValue(DummyDbEntry.class);
                    if(value != null)
                    {
                        realmWrapper_.createOrUpdateEntry(value.id_, value.deviceName_,
                                value.latitude_, value.longitude_,
                                value.temperature_, value.humidity_,
                                value.pressure_, value.co2_, value.altitude_, value.lastUpdate_);
                        Log.d("[FirebaseWrapper::onDataChange]", "Value is: "
                                + value.toString());

                    } else {
                        Log.d("[FirebaseWrapper::onDataChange]", "Value is NULL");
                    }
                }
                if(MainActivity.shouldSendNotification)
                {
                    // Create notification channel
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        CharSequence name = "AirQualityMonitorNotificationChannel";
                        String description = "AirQualityMonitorNotificationChannel";
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
                        NotificationChannel channel = new NotificationChannel("1200", name,
                                importance);
                        channel.setDescription(description);
                        // Register the channel with the system; you can't change the importance
                        // or other notification behaviors after this
                        NotificationManager notificationManager = context_
                                .getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                    }

                    // Create an explicit intent for an Activity in your app
                    Intent intent = new Intent(context_, GoogleMapActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context_, 0, intent, 0);

                    // Create notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context_, "1200")
                            .setSmallIcon(R.drawable.button_round1_no_bg)
                            .setContentTitle("AirQualityMonitor")
                            .setContentText("Updated information about air quality in your area")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    // Send notification
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context_);

                    // notificationId is a unique int for each notification that you must define
                    notificationManager.notify(1201, builder.build());
                }
                MainActivity.shouldSendNotification = true;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("[MainActivity::onCancelled]", "Failed to read value.", error.toException());
            }
        });
    }

    public void push(DummyDbEntry entry)
    {
        myRef_.child(entry.id_).setValue(entry);
    }
}
