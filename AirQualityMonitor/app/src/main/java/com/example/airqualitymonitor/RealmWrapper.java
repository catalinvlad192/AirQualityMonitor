package com.example.airqualitymonitor;

import android.content.Context;

import com.example.airqualitymonitor.db.model.DbEntry;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class RealmWrapper
{
    private Realm realm;
    private Context context_;

    public RealmWrapper(Context context)
    {
        context_ = context;
        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        realm = Realm.getDefaultInstance();
    }

    public void createOrUpdateEntry(final String id, final String deviceName, final String latitude,
                                    final String longitude, final String temperature,
                                    final String humidity,final String pressure, final String co2,
                                    final String altitude, final String lastUpdate)
    {
        DbEntry entry = realm.where(DbEntry.class)
                .equalTo("id_", id)
                .findFirst();

        realm.beginTransaction();

        if (entry == null)
        {
            DbEntry newEntry = realm.createObject(DbEntry.class);
            newEntry.id_ = id;
            newEntry.deviceName_ = deviceName;
            newEntry.latitude_ = latitude;
            newEntry.longitude_ = longitude;
            newEntry.temperature_ = temperature;
            newEntry.humidity_ = humidity;
            newEntry.pressure_ = pressure;
            newEntry.co2_ = co2;
            newEntry.altitude_ = altitude;
            newEntry.lastUpdate_ = lastUpdate;
        } else {
            entry.deviceName_ = deviceName;
            entry.latitude_ = latitude;
            entry.longitude_ = longitude;
            entry.temperature_ = temperature;
            entry.humidity_ = humidity;
            entry.pressure_ = pressure;
            entry.co2_ = co2;
            entry.altitude_ = altitude;
            entry.lastUpdate_ = lastUpdate;
        }
        realm.commitTransaction();
    }

    public void deleteEntry(final String id)
    {
        DbEntry entry = realm.where(DbEntry.class)
                .equalTo("id_", id)
                .findFirst();

        realm.beginTransaction();

        if (entry != null)
        {
            entry.deleteFromRealm();
        }
        realm.commitTransaction();
    }

    public RealmResults<DbEntry> getEntireDatabase()
    {
        RealmResults<DbEntry> entriesDb = realm.where(DbEntry.class).findAll();
        return entriesDb;
    }

    public void close()
    {
        realm.close();
    }
}
