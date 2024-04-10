package com.atechticketing.TS_Mobile_RN_3;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class AtechBluetoothModule extends ReactContextBaseJavaModule {
    private static final String TAG = "AtechBluetoothModule";
    public BluetoothAdapter bluetoothAdapter = null;
    public static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static int sleepTime = 50; // milliseconds
    public OutputStream outputStream = null;

    public BluetoothSocket bluetoothSocket = null;
    public boolean ConnectionState;

    public AtechBluetoothModule(ReactApplicationContext reactContext) {
        super(reactContext);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public String getName() {
        return "AtechBluetoothModule";
    }


    @ReactMethod
    public void isBluetoothEnabled(Promise promise) {
        promise.resolve( bluetoothAdapter != null && bluetoothAdapter.isEnabled());
    }
    @ReactMethod
    public void enableBluetooth(Promise promise) {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            promise.resolve(true);
       }
    }
    @ReactMethod
        public void disableBluetooth(Promise promise) {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            promise.resolve(true);
        }
    }
    @ReactMethod
    public void getPairedDevices(Promise promise) {
        WritableArray app_list = new WritableNativeArray();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            promise.reject("BT NOT ENABLED");
        } else {
            try {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                List<String> deviceName = new ArrayList<String>();
                List<String> deviceAddress = new ArrayList<String>();
                List<Integer> ble = new ArrayList<Integer>();


                for (BluetoothDevice bt : pairedDevices) {
                    BluetoothClass bluetoothClass = bt.getBluetoothClass(); // get class of bluetooth device
                    WritableMap info = new WritableNativeMap();
                    info.putString("address", bt.getAddress());
                    info.putDouble("class", bluetoothClass.getDeviceClass()); //1664
                    info.putString("name", bt.getName());
                    info.putString("type", "paired");
                    app_list.pushMap(info);
                }
            } catch (Exception e) {
                promise.reject("Error", e);
            }
        }
       promise.resolve(app_list);
    }
    @ReactMethod
    public void connectToDevice(String macAddress, Promise promise) {
    // this is used only to test a connection
        // once established, the connection is immediately closed
        Log.d(TAG, "in connectToDevice");
        Log.d(TAG, bluetoothAdapter.getName());
        macAddress = macAddress.toUpperCase();
        if (macAddress.matches("^([0-9A-F]{12})")) {
            macAddress = macAddress.substring(0, 2) + ":" + macAddress.substring(2, 4) + ":" + macAddress.substring(4, 6) + ":" + macAddress.substring(6, 8) + ":" +
                    macAddress.substring(8, 10) + ":" + macAddress.substring(10, 12);
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
        if (device != null) {
            try {
                bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
                Log.d(TAG, "device connected");
                closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "device connection error");
                closeConnection();
                promise.reject("Error", "Could not connect to device");
            }
        }
       promise.resolve("device connected");
    }
    @ReactMethod
    public void print(String macAddress, String printString, Promise promise) throws IOException {
         try {
        byte[] msgBuffer = printString.getBytes();
        if (macAddress.matches("^([0-9A-F]{12})")) {
            macAddress = macAddress.substring(0, 2) + ":" + macAddress.substring(2, 4) + ":" + macAddress.substring(4, 6) + ":" + macAddress.substring(6, 8) + ":" +
                    macAddress.substring(8, 10) + ":" + macAddress.substring(10, 12);
        }
        if (!macAddress.matches("^(([0-9A-F]{2}[:]){5}[0-9A-F]{2})")) {
            Log.e(TAG, "Invalid BDA. Try again...");
            promise.reject("error", "invalid device address, try again");
        } else {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
            try {
                bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                promise.reject("error creating socket", e);
            }
            bluetoothAdapter.cancelDiscovery();
            try {
                ConnectionState = true;
                bluetoothSocket.connect();
            } catch (IOException e) {
                try {
                    ConnectionState = false;
                    bluetoothSocket.close();
                    Log.e(TAG, "Connection Attempt Failed...");
                    promise.reject("Connection Attempt Failed...", e);

                } catch (IOException e2) {
                    promise.reject("Connection error...", e2);
                }
            }
            if (ConnectionState) {
                try {
                    outputStream = bluetoothSocket.getOutputStream();
                    outputStream.write(msgBuffer);
                    outputStream.flush();
                    Thread.sleep(sleepTime);
                    outputStream.close();
                    ConnectionState = false;
                    bluetoothSocket.close();
                    promise.resolve(true);
                } catch (IOException | InterruptedException e) {
                    promise.reject("Error writing to device...", e);
                }
            }
        }
         } catch (Exception e) {
            promise.reject("Unhandled error in bluetooth module", e);
        }
    }

    public void closeConnection() {
        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }

            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
