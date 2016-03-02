package com.example.shashankshekhar.mqttservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by shashankshekhar on 29/02/16.
 */
public class ODMqtt implements MqttCallback {
    private final int QoS = 1;
    private final String BROKER_ADDRESS = "tcp://smartx.cds.iisc.ac.in:1883";
    private final String USERNAME = "ODAppUser";
    private final String PASSWORD = "ODApp@Smartx";
    private final String TAG = "Open-Day";
    private final String TOPIC_NAME = "iisc/smartx/crowd/network/ODRSSI";
    private final String TEST_TOPIC_NAME = "iisc/smartx/mobile/water/data";
    private static final String SMART_CAMPUS_FOLDER_NAME = "SmartCampus";
    private static final String SMART_CAMPUS_LOG_FILE_NAME = "SmartCampusLog.txt";
    /*
    sample data format
    UID,TimeStamp,Latitude,Longitude,OperatorName,AreaCode,NetworkName,GSMSignalStrength
     */
    //smartx.cloudapp.net
    //smartx.cds.iisc.ac.in

    private MqttConnectOptions connectOptions = null;
    private MqttAsyncClient mqttClient = null;

    private String clientId;
    private Context applicationContext;

    public ODMqtt(Context appContext, String clientId) {
        this.applicationContext = appContext;
        this.clientId = clientId;
        NetworkConnectivityChangeReceiver.initMqttObj(this);
    }

    private void setConnectionOptions() {
        connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setUserName(USERNAME);
        connectOptions.setPassword(PASSWORD.toCharArray());
        connectOptions.setConnectionTimeout(20);
        connectOptions.setKeepAliveInterval(300); // keep alive is 300Seconds-5 minutes
        connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            mqttClient = new MqttAsyncClient(BROKER_ADDRESS, clientId, persistence);
        } catch (MqttException e) {
            e.printStackTrace();
            printLog("exception, could not instantiate mqttclient");
        }
    }

    public void connectToMqttBroker() {
        setConnectionOptions();
        mqttClient.setCallback(this);
        IMqttToken token;
        Integer keepAliveTime = connectOptions.getKeepAliveInterval();
        printLog("keepAliveTime: "+keepAliveTime);
        try {
            token = mqttClient.connect(connectOptions);
        } catch (MqttException e) {
            e.printStackTrace();
            printLog("exception, could not connect to mqtt");
        }
    }

    public void disconnectMqtt() {
        if (mqttClient.isConnected() == false) {
            return;
        }
        try {
            mqttClient.disconnect();
            printLog("successfully disconnected");
        } catch (MqttException e) {
            printLog("Error.Could not disconnect mqtt. Trying force");
            try {
                mqttClient.disconnectForcibly();
                printLog("disconnection successful with force");
            } catch (MqttException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public void publishMessge(String dataString) {
        IMqttDeliveryToken deliveryToken;
        MqttMessage message1 = new MqttMessage(dataString.getBytes());
        message1.setQos(QoS);
        if (mqttClient.isConnected() == false) {
            printLog("error in publishing,mqtt is not connected");
            return;
        }
        try {
            deliveryToken = mqttClient.publish(TOPIC_NAME, message1);
        } catch (MqttException e) {
            e.printStackTrace();
            printLog("exception in publishing");
            writeDataToLogFile("MQTTService","exception in publishing");

        }
    }

    public boolean isMqttConnected() {
        return mqttClient.isConnected();
    }

    @Override
    public void connectionLost(Throwable cause) {
        printLog("connection lost in receiver!!");
        printLog("cause: " + cause.getCause());
        writeDataToLogFile("MQTTService", "connection Lost-" + cause.getCause());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken tk) {
        printLog("delivery complete for token: " + tk);
        writeDataToLogFile("MQTTService","delivery complete");
    }

    @Override
    public void messageArrived(String topic, MqttMessage msg) {
        printLog("message arrived");
    }

    private void printLog(String string) {
        Log.i(TAG, string);
    }

    public void writeDataToLogFile(String appName, String text) {
        File smartCampusDirectory = new File(Environment.getExternalStorageDirectory(), SMART_CAMPUS_FOLDER_NAME);
        if (smartCampusDirectory.exists() == false) {
            if (!smartCampusDirectory.mkdirs()) {
                return;
            }
        }
        File logFile = new File(smartCampusDirectory, SMART_CAMPUS_LOG_FILE_NAME);
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(currentTime() + ":" + appName + " : " + text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            return;
        }
    }
    public String currentTime () {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = simpleDateFormat.format(new Date());
            return currentTime;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
