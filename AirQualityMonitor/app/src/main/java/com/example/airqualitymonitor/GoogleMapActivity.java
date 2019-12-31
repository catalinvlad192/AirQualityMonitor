package com.example.airqualitymonitor;

import androidx.fragment.app.FragmentActivity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.example.airqualitymonitor.db.model.DbEntry;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import io.realm.RealmResults;

public class GoogleMapActivity extends FragmentActivity implements OnMapReadyCallback
{
    private GoogleMap map;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dialog = new Dialog(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        RealmResults<DbEntry> entries =  MainActivity.realmWrapper.getEntireDatabase();

        for(DbEntry entry : entries)
        {
            LatLng latLng = new LatLng(Double.parseDouble(entry.latitude_),
                    Double.parseDouble(entry.longitude_));
            googleMap.addMarker(new MarkerOptions().position(latLng)
                    .title(entry.id_ + " - " + entry.deviceName_));
        }

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                RealmResults<DbEntry> entries =  MainActivity.realmWrapper.getEntireDatabase();

                for(DbEntry entry : entries)
                {
                    if (marker.getTitle().equals(entry.id_ + " - " + entry.deviceName_))
                    {
                        TextView close;
                        TextView device;
                        TextView humidity;
                        TextView pressure;
                        TextView temperature;
                        TextView gas;
                        TextView altitude;
                        TextView lastTimeUpdated;
                        TextView iaq;

                        dialog.setContentView(R.layout.custompopup);

                        close = dialog.findViewById(R.id.close);
                        device = dialog.findViewById(R.id.device);
                        humidity = dialog.findViewById(R.id.humidity);
                        pressure = dialog.findViewById(R.id.pressure);
                        temperature = dialog.findViewById(R.id.temperature);
                        gas = dialog.findViewById(R.id.gas);
                        altitude = dialog.findViewById(R.id.altitude);
                        lastTimeUpdated = dialog.findViewById(R.id.lastUpdate);
                        iaq = dialog.findViewById(R.id.iaq);

                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });

                        device.setText(entry.deviceName_);
                        humidity.setText(entry.humidity_ + " %");
                        pressure.setText(entry.pressure_ + " mmHg");
                        temperature.setText(entry.temperature_  + " *C");
                        gas.setText(entry.co2_ + "KOhms");
                        altitude.setText(entry.altitude_ + " m");
                        lastTimeUpdated.setText(entry.lastUpdate_);

                        String iaqScore = IAQProcessor.calculateIAQ(
                                Double.parseDouble(entry.humidity_),
                                Double.parseDouble(entry.co2_));

                        iaq.setText(iaqScore);

                        dialog.show();
                    }
                }
                return false;
            }
        });
    }
}
