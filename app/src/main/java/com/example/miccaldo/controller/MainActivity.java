package com.example.miccaldo.controller;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Instant;
import java.util.ArrayList;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import me.aflak.bluetooth.Bluetooth;

public class MainActivity extends AppCompatActivity {

    static boolean bluetoothFlag;
    boolean noConnect = false;
    //private BluetoothDevice bluetoothDevice;
    private ArrayList<BluetoothDevice> bluetoothDevice = new ArrayList<>();

    String wsp = " ";

    private void setText(boolean value){

        TextView textView = (TextView)findViewById(R.id.connect_text);
        String text = String.valueOf(value);
        textView.setText(text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {

            if(Connect.connected){
                wsp = "X: " + joystick.getNormalizedX() + ", " + "Y:" + joystick.getNormalizedY() + "\n\r";
                Connect.bluetooth.send(wsp);
            }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_connect) {
            Intent intent = new Intent(MainActivity.this, Connect.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            noConnect = true;
            startActivity(intent);
            //finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noConnect = false;

        writeConnectedDevice();
        setDevice();

        if(bluetoothDevice.size() > 0) Toast.makeText(getApplicationContext()," "+bluetoothDevice.get(0).getName(), Toast.LENGTH_LONG).show();
    }

    private void setDevice(){
        if(Connect.connected){
            if(Connect.pairedDeviceFlag) bluetoothDevice.add(Connect.devicesPairedList.get(Connect.indexDevice));
            else bluetoothDevice.add(Connect.devices.get(Connect.indexDevice));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(bluetoothFlag && !noConnect) {

            if(Connect.bluetooth.isConnected()){
                Connect.bluetooth.disconnect();
                Connect.connected = false;
            }
        }
    }

    private void writeConnectedDevice(){

        TextView textView = (TextView)findViewById(R.id.connect_text);
        String deviceName = " ";

        if(Connect.connected){
            if(Connect.pairedDeviceFlag) deviceName = Connect.devicesPairedList.get(Connect.indexDevice).getName();
            else deviceName = Connect.devices.get(Connect.indexDevice).getName();
            textView.setText("Połączenie: " + deviceName);
        }else{
            textView.setText("Połączenie: brak");
        }
    }
    public void getUp_click(View view) {
        if (Connect.connected) {
            Connect.bluetooth.send("getUP");
        }
    }
    public void turnOff_click(View view) {
        if (Connect.connected) {
            Connect.bluetooth.send("off");
        }
    }

}
