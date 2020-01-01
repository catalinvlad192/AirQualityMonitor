package com.example.airqualitymonitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.airqualitymonitor.db.model.DummyDbEntry;
import com.example.airqualitymonitor.google.map.GPSTracker;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
{
    private String TAG = "[MainActivity]";

    // UUID for all HC-05 devices
    private UUID myUUID_ = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    // Return value from bluetooth and GPS enabling
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_GPS = 1;

    // Toggle micro controller LED
    private static boolean toggle = false;

    // Choose if notification should be pushed (should not be notified when you read data from dev)
    public static boolean shouldSendNotification = false;

    //Handler
    private Handler handler_;

    // Bluetooth
    private BluetoothSocket socket_ = null;
    private BluetoothAdapter bluetoothAdapter_;

    // Wrappers for Firebase and Realm
    public static RealmWrapper realmWrapper;
    private FirebaseWrapper firebaseWrapper_;

    // For reading current phone location
    private GPSTracker gpsTracker_;

    // List adapter elements
    private HashMap<String, BluetoothDevice> foundDevices_;
    private ArrayList<String> foundDevicesAddresses_;
    private MyAdapter myAdapter_;

    // Palette
    private Button startScanButton_;
    private Button gpsButton_;
    private ListView listView_;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                if(device.getName() != null)
                {
                    if( (foundDevices_.put(device.getAddress(), device)) == null )
                    {
                        foundDevicesAddresses_.add(device.getAddress());
                        ((BaseAdapter) myAdapter_).notifyDataSetChanged();

                        Toast.makeText(MainActivity.this, "Map size: "
                                + foundDevices_.size() + " ArraySize: "
                                + foundDevicesAddresses_.size(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find Palette by ID
        startScanButton_ = findViewById(R.id.scanButton);
        gpsButton_ = findViewById(R.id.gpsButton);
        listView_ = findViewById(R.id.devicesListView);

        // Create tracker and request location permission permission
        gpsTracker_ = new GPSTracker(getApplicationContext());
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, 123);

        // Create wrapper objects
        realmWrapper = new RealmWrapper(this);
        firebaseWrapper_ = new FirebaseWrapper(realmWrapper, getApplicationContext());

        // Devices, Devices Addresses and Handler
        foundDevices_ = new HashMap<String, BluetoothDevice>();
        foundDevicesAddresses_ = new ArrayList<String>();
        handler_ = new Handler();

        //Enable GPS
        LocationManager lm = (LocationManager)getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
        if (lm != null && !lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent1, REQUEST_ENABLE_GPS);
        }

        // Bluetooth adapter and bluetooth enable
        bluetoothAdapter_ = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter_ == null)
        {
            Toast.makeText(this, "This device does not support bluetooth",
                    Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter_.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        // Adapter list
        myAdapter_ = new MyAdapter(this, foundDevicesAddresses_, foundDevices_);
        listView_.setAdapter(myAdapter_);

        // Set item click on ListView
        listView_.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                readFromDeviceViaBluetooth(i);
            }
        });

        // On startScanButton_ click
        startScanButton_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                foundDevicesAddresses_.clear();
                foundDevices_.clear();
                myAdapter_.notifyDataSetChanged();

                // Stops scanning after a pre-defined scan period
                handler_.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothAdapter_.cancelDiscovery();
                        Toast.makeText(MainActivity.this, "Stop scan",
                                Toast.LENGTH_SHORT).show();
                    }
                }, SCAN_PERIOD);
                bluetoothAdapter_.startDiscovery();
                Toast.makeText(MainActivity.this, "Start scan",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // On GPSButton_ click
        gpsButton_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this,
                        GoogleMapActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        realmWrapper.close();
        unregisterReceiver(receiver);
    }

    private void readFromDeviceViaBluetooth(int index)
    {
        BluetoothDevice device = foundDevices_.get(foundDevicesAddresses_.get(index));
        socket_ = null;

        // Trying to connect to bluetooth device
        try
        {
            socket_ = device.createRfcommSocketToServiceRecord(myUUID_);
            socket_.connect();
        }
        // Trying to connect to bluetooth device - fallback
        catch (IOException e)
        {
            Log.d(TAG, "Couldn't connect the first time. Trying fallback");
            try
            {
                socket_ =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket",
                        new Class[] {int.class}).invoke(device,1);
                socket_.connect();
            }
            catch(Exception ex)
            {
                Log.d(TAG, "Could not connect not even the second time. Returning...");
                return;
            }
        }

        bluetoothAdapter_.cancelDiscovery();
        try
        {
            Log.d(TAG, "Connected");

            // Toggle controller LED
            OutputStream outputStream = socket_.getOutputStream();
            if (toggle)
            {
                outputStream.write('1');
                toggle = false;
            }
            else
            {
                outputStream.write('0');
                toggle = true;
            }

            InputStream inputStream = socket_.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            // Read parameters
            String start = bufferedReader.readLine();
            String id = bufferedReader.readLine();
            String deviceName = bufferedReader.readLine();
            String latitude = "0.0";
            String longitude = "0.0";
            String temp = bufferedReader.readLine();
            String humidity = bufferedReader.readLine();
            String pressure = bufferedReader.readLine();
            String co2 = bufferedReader.readLine();
            String altitude = bufferedReader.readLine();
            String lastUpdate = java.text.DateFormat.getDateTimeInstance()
                    .format((Calendar.getInstance().getTime()));

            // Get location
            Location location = gpsTracker_.getLocation();

            if (location != null)
            {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(lon);
            }

            // Create Firebase object with read parameters
            DummyDbEntry entry = new DummyDbEntry();
            entry.id_ = id;
            entry.deviceName_ = deviceName;
            entry.latitude_ = latitude;
            entry.longitude_ = longitude;
            entry.temperature_ = temp;
            entry.humidity_ = humidity;
            entry.pressure_ = pressure;
            entry.co2_ = co2;
            entry.altitude_ = altitude;
            entry.lastUpdate_ = lastUpdate;

            // Should not notify the user
            shouldSendNotification = false;

            // Push object to firebase
            firebaseWrapper_.push(entry);

        } catch (IOException connectException)
        {
            // Unable to connect; close the socket and return.
            try
            {
                socket_.close();
            } catch (IOException closeException)
            {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
        try
        {
            socket_.close();
        } catch (IOException closeException)
        {
            Log.e(TAG, "Could not close the client socket after finishing", closeException);
        }
    }

    // ListAdapter class
    class MyAdapter extends ArrayAdapter<String>
    {
        Context context_;
        ArrayList<String> devsAddress_;
        HashMap<String, BluetoothDevice> devsHashMap_;

        MyAdapter(Context ctx, ArrayList<String> devs, HashMap<String,BluetoothDevice> devMap)
        {
            super(ctx, R.layout.list_view_element_layout, devs);
            context_ = ctx;
            devsAddress_ = devs;
            devsHashMap_ = devMap;
        }

        @NonNull
        @Override
        public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent)
        {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.list_view_element_layout, parent, false);

            TextView title = row.findViewById(R.id.device_name);
            TextView address = row.findViewById(R.id.device_address);

            title.setText(devsHashMap_.get(devsAddress_.get(position)).getName());
            address.setText(devsAddress_.get(position));
            return row;
        }
    }
}
