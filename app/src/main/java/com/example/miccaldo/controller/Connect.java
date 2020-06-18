package com.example.miccaldo.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.BluetoothCallback;
import me.aflak.bluetooth.DeviceCallback;
import me.aflak.bluetooth.DiscoveryCallback;

import static android.app.PendingIntent.getActivity;

public class Connect extends AppCompatActivity {

    private static Context context = MyAppContext.getContext();

    static Bluetooth bluetooth = new Bluetooth(context);
    static int indexDevice;
    static ArrayList<BluetoothDevice> devices = new ArrayList<>();
    static ArrayList<BluetoothDevice> devicesPairedList = new ArrayList<>();
    static boolean connected = false;
    static boolean pairedDeviceFlag;

    private ListView bt_devices;
    private ListView lv_pairedDevices;
    private ArrayAdapter<String> bt_adapter;
    private boolean newScanning;
    private int cnt;
    private int connectCnt;

    private static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

    private Handler timerConnected = new Handler();
    private Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {

            if(bluetooth.isConnected()){
                connected = true;
                timerConnected.removeCallbacks(timerRunnable);

                if(!pairedDeviceFlag) writeConnectedDevice(devices);
                else writeConnectedDevice(devicesPairedList);
            }else{
                timerConnected.postDelayed(timerRunnable, 3000);
                connectCnt++;
                if(connectCnt > 5){
                    Toast.makeText(getApplicationContext(),"Błąd ", Toast.LENGTH_LONG).show();
                    timerConnected.removeCallbacks(timerRunnable);
                    connectCnt = 0;
                }else{
                    Toast.makeText(getApplicationContext(),"Łączenie.. ", Toast.LENGTH_LONG) .show();
                }
            }
        }
    };

    private Handler timerDisconnect = new Handler();
    private Runnable timerDisconnectRun = new Runnable() {

        @Override
        public void run() {

            timerConnected.removeCallbacks(timerRunnable);

            connected = false;
            pairedDeviceFlag = false;

            Toast.makeText(getApplicationContext(),"Rozłączenie.. ", Toast.LENGTH_LONG).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MainActivity.bluetoothFlag = true;

        bt_devices = (ListView)findViewById(R.id.bt_list);
        lv_pairedDevices = (ListView)findViewById(R.id.bt_pairedList);


       bluetooth.setBluetoothCallback(new BluetoothCallback() {
            @Override
            public void onBluetoothTurningOn() {
                Toast.makeText(getApplicationContext(),"Bluetooth włączony.. ", Toast.LENGTH_LONG).show();
            }

            @Override public void onBluetoothOn(){ }
            @Override public void onBluetoothTurningOff() {}
            @Override public void onBluetoothOff() { }
            @Override public void onUserDeniedActivation() {
                // when using bluetooth.showEnableDialog()
                // you will also have to call bluetooth.onActivityResult()
            }
        });

        bluetooth.setDiscoveryCallback(new DiscoveryCallback() {

            @Override public void onDiscoveryStarted() {}
            @Override public void onDiscoveryFinished() {
                FloatingActionButton bt_search_button = (FloatingActionButton)findViewById(R.id.bt_search_button);
                FloatingActionButton bt_refresh_button = (FloatingActionButton) findViewById(R.id.bt_refresh_button);

                bt_search_button.setVisibility(View.INVISIBLE);
                bt_refresh_button.setVisibility(View.VISIBLE);

                TextView textView = (TextView)findViewById(R.id.searchTextView);
                textView.setText("Dostępne urządzenia:");

                newScanning = false;

                if(devices.size() == 0){
                    Toast.makeText(getApplicationContext(),"brak urządzeń", Toast.LENGTH_LONG) .show();
                }
            }

            @Override public void onDeviceFound(BluetoothDevice device) {

                devices.add(device);
                setDevices(bt_devices, 0);

            }
            @Override public void onDevicePaired(BluetoothDevice device) {
                bluetooth.connectToDevice(devices.get(indexDevice));
                Toast.makeText(getApplicationContext(),"Łączenie.. ", Toast.LENGTH_LONG).show();
                timerConnected.postDelayed(timerRunnable, 3000);
            }
            @Override public void onDeviceUnpaired(BluetoothDevice device) { }
            @Override public void onError(String message) {}
        });



        bluetooth.setDeviceCallback(new DeviceCallback() {
            @Override public void onDeviceConnected(BluetoothDevice device) {
                //connected = true;
                //Toast.makeText(getApplicationContext(),"Połączono do ", Toast.LENGTH_LONG).show();
            }
            @Override public void onDeviceDisconnected(BluetoothDevice device, String message) {
                Toast.makeText(getApplicationContext(),"Rozłączono", Toast.LENGTH_LONG).show();
            }
            @Override public void onMessage(String message) {}
            @Override public void onError(String message) {

                Toast.makeText(getApplicationContext(),"błąd połączenia..", Toast.LENGTH_LONG) .show();
            }
            @Override public void onConnectError(BluetoothDevice device, String message) {

                //Toast.makeText(getApplicationContext(),"błąd połączenia..", Toast.LENGTH_LONG).show();
            }
        });

        final FloatingActionButton bt_search_button = (FloatingActionButton)findViewById(R.id.bt_search_button);
        final FloatingActionButton bt_refresh_button = (FloatingActionButton) findViewById(R.id.bt_refresh_button);
        bt_refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bt_refresh_button.setVisibility(View.INVISIBLE);
                bt_search_button.setVisibility(View.VISIBLE);

                clearDevices();

                if(bluetooth.isConnected()) bluetooth.disconnect();
                bluetooth.onStart();
                bluetooth.startScanning();
                newScanning = true;

                TextView textView = (TextView)findViewById(R.id.searchTextView);
                textView.setText("Wyszukiwanie urządzeń:");

                Toast.makeText(getApplicationContext(),"Skanowanie.. ", Toast.LENGTH_LONG).show();
            }
        });


        bt_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (!bluetooth.isConnected()) {
                    indexDevice = i;
                    String deviceName = "";
                    boolean pairedFlag = true;

                    if (newScanning) {
                        bluetooth.stopScanning();
                        newScanning = false;
                    }

                    bt_search_button.setVisibility(View.INVISIBLE);
                    bt_refresh_button.setVisibility(View.VISIBLE);

                    for (BluetoothDevice bt : pairedDevices) {

                        deviceName = bt.getAddress();

                        if (deviceName.equals(devices.get(indexDevice).getAddress())) {
                            Toast.makeText(getApplicationContext(),"Łączenie.. ", Toast.LENGTH_LONG).show();
                            pairedFlag = false;
                            bluetooth.connectToDevice(devices.get(indexDevice));
                            timerConnected.postDelayed(timerRunnable, 3000);
                        }

                        if (pairedFlag) bluetooth.pair(devices.get(indexDevice));
                    }
                }else{
                    bluetooth.disconnect();

                    timerDisconnect.postDelayed(timerDisconnectRun, 300);
                }
            }
        });

        lv_pairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (!bluetooth.isConnected()) {
                    BluetoothDevice[] bluetoothDevices = new BluetoothDevice[pairedDevices.size()];
                    indexDevice = i;
                    pairedDeviceFlag = true;

                    if (newScanning) {
                        bluetooth.stopScanning();
                        newScanning = false;
                    }

                    bt_search_button.setVisibility(View.INVISIBLE);
                    bt_refresh_button.setVisibility(View.VISIBLE);

                    pairedDevices.toArray(bluetoothDevices);

                    for (int j = 0; j < bluetoothDevices.length; j++) {
                        devicesPairedList.add(bluetoothDevices[j]);
                    }

                    bluetooth.onStart();

                    Toast.makeText(getApplicationContext(),"Łączenie.. ", Toast.LENGTH_LONG).show();
                    bluetooth.connectToDevice(bluetoothDevices[indexDevice]);


                    timerConnected.postDelayed(timerRunnable, 3000);
                }else{
                    bluetooth.disconnect();

                    timerDisconnect.postDelayed(timerDisconnectRun, 300);
                }
            }
        });
    }

    private void writeConnectedDevice(ArrayList<BluetoothDevice> list){
        Toast.makeText(getApplicationContext(),"Połączono do: " + list.get(indexDevice).getName(), Toast.LENGTH_LONG).show();
    }

    private void setDevices(ListView listView, int value){
        ArrayList<String> devicesName_list = new ArrayList<String>();

        if(value == 0) {
            for (BluetoothDevice device_bt : devices) {
                devicesName_list.add(device_bt.getName());
            }
        }
        else{
            for (BluetoothDevice device_bt : pairedDevices) {
                devicesName_list.add(device_bt.getName());
            }
        }
        bt_adapter = new ArrayAdapter<String>(Connect.this, R.layout.bt_row, devicesName_list);
        listView.setAdapter(bt_adapter);
    }

    private void clearDevices(){
        devices.clear();
        ArrayList<String> devicesName_list = new ArrayList<String>();
        bt_adapter = new ArrayAdapter<String>(Connect.this, R.layout.bt_row, devicesName_list);
        bt_devices.setAdapter(bt_adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        pairedDeviceFlag = false;
        connected = false;

        if(!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
            bluetooth.enable();
            bluetooth.onStart();
        }

        if(devices.size() > 0){
            setDevices(bt_devices, 0);
        }

        for(int i = 0; i < 5; i++){
            setDevices(lv_pairedDevices, 1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(newScanning == true) {
            bluetooth.stopScanning();
            newScanning = false;
        }
    }
}
