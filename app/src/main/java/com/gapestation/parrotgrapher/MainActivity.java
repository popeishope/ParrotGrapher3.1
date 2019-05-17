package com.gapestation.parrotgrapher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ARDiscoveryService mArdiscoveryService;
    private ServiceConnection mArdiscoveryServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ARSDK.loadSDKLibs();
        initDiscoveryService();

    }

    private void initDiscoveryService() {
        if (mArdiscoveryServiceConnection == null) {
            mArdiscoveryServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    mArdiscoveryService = ((ARDiscoveryService.LocalBinder) iBinder).getService();
                    startDiscovery();
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    mArdiscoveryService = null;
                }
            };
        }
        if (mArdiscoveryService == null) {
            Intent i = new Intent(getApplicationContext(), ARDiscoveryService.class);
            getApplicationContext().bindService(i, mArdiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            startDiscovery();
        }
    }

    private void startDiscovery() {
        if (mArdiscoveryService != null) {
            mArdiscoveryService.start();
        }
    }

    private void registerReceivers() {
        ARDiscoveryServicesDevicesListUpdatedReceiver receiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(mDiscoveryDelegate);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.registerReceiver(receiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }

    private final ARDiscoveryServicesDevicesListUpdatedReceiverDelegate mDiscoveryDelegate = new ARDiscoveryServicesDevicesListUpdatedReceiverDelegate() {
        @Override
        public void onServicesDevicesListUpdated() {
            if (mArdiscoveryService != null) {
                List<ARDiscoveryDeviceService> deviceList = mArdiscoveryService.getDeviceServicesArray();
                for (ARDiscoveryDeviceService device : deviceList) {
                    Log.i("Device found", device.getName());
                }
                // Java program for slope of line
 java.io.;

                class GFG {
                     float slope(float x1, float y1,
                                       float x2, float y2)
                    {
                        return (y2 - y1) / (x2 - x1);
                    }
                    public void main(String[] args)
                    {
                        float x1 = 4, y1 = 2;
                        float x2 = 2, y2 = 5;
                        System.out.println("Slope is " +
                                slope(x1, y1, x2, y2));
                    }
                }



            }
        }
    };

}
