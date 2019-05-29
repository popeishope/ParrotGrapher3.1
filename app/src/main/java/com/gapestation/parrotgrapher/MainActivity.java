package com.gapestation.parrotgrapher;

import android.content.BroadcastReceiver;
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
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARFeatureARDrone3;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ARDeviceControllerListener, ARDiscoveryServicesDevicesListUpdatedReceiverDelegate {

    private ARDiscoveryService mArdiscoveryService;
    private ServiceConnection mArdiscoveryServiceConnection;
    private ARDiscoveryServicesDevicesListUpdatedReceiver mArdiscoveryServicesDevicesListUpdatedReceiver;
    private ARDeviceController deviceController;
    private ARDiscoveryDevice device;
    private Button button1;
    private Button emergencyLand;
    private Button land;
    private String TAG = getClass().getSimpleName();
    private Integer batValue;
    ProgressBar progressBar;

    static {
        ARSDK.loadSDKLibs();
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ARSDK.loadSDKLibs();
        Log.i(TAG, "Initializing Discovery Service.");
        initDiscoveryService();
        registerReceivers();
        button1 = findViewById(R.id.button);
        emergencyLand = findViewById(R.id.emergency_land);
        land = findViewById(R.id.land);
        progressBar = findViewById(R.id.progressBar);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Going Crazy!");
                if (deviceController != null) {
                    Log.i(TAG, "Takeoff command sent.");
                    testFlight();
                    //deviceController.getFeatureARDrone3().sendPilotingTakeOff();
                }
            }
        });
        emergencyLand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emergencyLand();
            }
        });
        land.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                land();
            }
        });
    }


    private void initDiscoveryService() {
        if (mArdiscoveryServiceConnection == null) {
            Log.i(TAG, "Starting new service connection.");
            mArdiscoveryServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    mArdiscoveryService = ((ARDiscoveryService.LocalBinder) iBinder).getService();
                    Log.i(TAG, "Starting Discovery.");
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
            Log.i(TAG, "Starting Discovery.");
            startDiscovery();
        }
    }

    private void startDiscovery() {
        if (mArdiscoveryService != null) {

            Log.i(TAG, "Discovery Service Started.");

            mArdiscoveryService.start();
        }
       /* else{
            initDiscoveryService();
        }*/
    }

    private void registerReceivers() {
        mArdiscoveryServicesDevicesListUpdatedReceiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(this);
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.registerReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }

    @Override
    public void onServicesDevicesListUpdated() {
        if (mArdiscoveryService != null) {
            List<ARDiscoveryDeviceService> deviceList = mArdiscoveryService.getDeviceServicesArray();
            Log.i("Devices", deviceList.toString());
            device = createDiscoveryDevice(deviceList.get(0));
            try {
                deviceController = new ARDeviceController(device);
                Log.i(TAG, "Device Controller Created.");
                deviceController.addListener(MainActivity.this);
                Toast toast = Toast.makeText(this, deviceList.get(0).getName() + " connected.", Toast.LENGTH_SHORT);
                toast.show();
                unregisterReceivers();
                closeServices();
                deviceController.start();
            } catch (ARControllerException e) {
                e.printStackTrace();
            }
            // Java program for slope of line


               /* class GFG {
                    float slope(float x1, float y1,
                                float x2, float y2) {
                        return (y2 - y1) / (x2 - x1);
                    }

                    public void main(String[] args) {
                        float x1 = 4, y1 = 2;
                        float x2 = 2, y2 = 5;
                        System.out.println("Slope is " +
                                slope(x1, y1, x2, y2));
                    }
                }*/


        }
    }

    private ARDiscoveryDevice createDiscoveryDevice(ARDiscoveryDeviceService service) {
        ARDiscoveryDevice mdevice = null;
        if ((service != null) &&
                (ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_ARDRONE.equals(ARDiscoveryService.getProductFromProductID(service.getProductID())))) {
            try {
                mdevice = new ARDiscoveryDevice();

                ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) service.getDevice();

                mdevice.initWifi(ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_ARDRONE, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
                Log.i("TAG", "WiFi connection initialized. Device: " + netDeviceService.getName() + " IP: " + netDeviceService.getIp());
            } catch (ARDiscoveryException e) {
                e.printStackTrace();
                Log.e(getClass().getSimpleName(), "Error: " + e.getError());
            }
        }
        Log.i(TAG, "Discovery Device Created.");
        return mdevice;
    }

    @Override
    public void onStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error) {
        switch (newState) {
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                break;
            case ARCONTROLLER_DEVICE_STATE_STARTING:
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPING:
                break;

            default:
                break;
        }
    }

    @Override
    public void onExtensionStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error) {

    }

    @Override
    public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary) {
        if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED) && (elementDictionary != null)) {
            ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
            if (args != null) {
                ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue((Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE));
                if (state == ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING) {
                    deviceController.getFeatureARDrone3().sendPilotingMoveBy(20, 0, 0, 0);
                }
            }
        }
        if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND) && (elementDictionary != null)) {
            ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
            if (args != null) {
                float dX = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_DX)).doubleValue();
                float dY = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_DY)).doubleValue();
                float dZ = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_DZ)).doubleValue();
                float dPsi = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_DPSI)).doubleValue();
                ARCOMMANDS_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR_ENUM error = ARCOMMANDS_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR_ENUM.getFromValue((Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR));
                land();
            }
        }

        if (elementDictionary != null) {
            // if the command received is a battery state changed
            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED) {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);

                if (args != null) {
                    batValue = (Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT);
                    progressBar.setProgress(batValue);
                    Log.i(TAG, "Battery level " + batValue + "%");
                }
            }
        } else {
            Log.e("isnull", "elementDictionary is null");
        }
    }

    private ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM getPilotingState() {
        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.eARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_UNKNOWN_ENUM_VALUE;
        if (deviceController != null) {
            try {
                ARControllerDictionary dict = deviceController.getCommandElements(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED);
                if (dict != null) {
                    ARControllerArgumentDictionary<Object> args = dict.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                    if (args != null) {
                        Integer flyingStateInt = (Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE);
                        flyingState = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(flyingStateInt);
                    }
                }
            } catch (ARControllerException e) {
                e.printStackTrace();
                Log.e(TAG, e.getError().name());
            }


        }
        return flyingState;
    }

    private void takeoff() {
        if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED.equals(getPilotingState())) {
            if (batValue != null) {
                if (batValue > 25) {
                    ARCONTROLLER_ERROR_ENUM error = deviceController.getFeatureARDrone3().sendPilotingTakeOff();

                    if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK)) {
                        Log.e(TAG, "Error while sending take off: " + error);
                    } else {
                        Toast toast = Toast.makeText(this, "Battery too low to take off: " + batValue + "%", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        }
    }

    private void unregisterReceivers() {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());

        localBroadcastMgr.unregisterReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver);
    }

    private void emergencyLand() {
        deviceController.getFeatureARDrone3().sendPilotingEmergency();
    }

    private void land() {
        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = getPilotingState();
        if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING.equals(flyingState) ||
                ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING.equals(flyingState)) {
            ARCONTROLLER_ERROR_ENUM error = deviceController.getFeatureARDrone3().sendPilotingLanding();

            if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK)) {
                Log.e(TAG, "Error while sending land: " + error);
            }
        }
    }

    private void testFlight() {
        takeoff();

    }

    private void closeServices() {
        Log.d(TAG, "closeServices ...");

        if (mArdiscoveryService != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mArdiscoveryService.stop();

                    getApplicationContext().unbindService(mArdiscoveryServiceConnection);
                    mArdiscoveryService = null;
                }
            }).start();
        }
    }
}

