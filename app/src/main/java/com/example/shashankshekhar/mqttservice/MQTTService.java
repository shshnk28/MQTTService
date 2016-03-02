package com.example.shashankshekhar.mqttservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

public class MQTTService extends Service {
    private final String SAMPLE_TEST_DATA =",13.0198,77.5660,airtel,40445,Airtel,18";
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final ODMqtt odMqtt = new ODMqtt(getApplicationContext(),randomString());
         odMqtt.connectToMqttBroker();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Long time = System.currentTimeMillis();
                    time = time/1000;
                    odMqtt.publishMessge("Shashank,"+ time.toString()+SAMPLE_TEST_DATA);
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return Service.START_NOT_STICKY;
    }
    private String randomString() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid;
    }

}
