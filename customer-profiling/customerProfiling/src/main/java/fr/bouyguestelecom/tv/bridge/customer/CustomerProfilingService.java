/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2015-2016 InnovationLab BboxLab
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bouyguestelecom.tv.bridge.customer;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import fr.bmartel.android.iotf.handler.AppHandler;
import fr.bmartel.android.iotf.listener.IMessageCallback;
import fr.bouyguestelecom.tv.bridge.IBboxBridge;
import okhttp3.Request;
import tv.bouyguestelecom.fr.bboxapilibrary.Bbox;
import tv.bouyguestelecom.fr.bboxapilibrary.MyBbox;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxApplication;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetCurrentChannel;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxMedia;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxRegisterApp;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSubscribe;
import tv.bouyguestelecom.fr.bboxapilibrary.model.ApplicationResource;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Channel;
import tv.bouyguestelecom.fr.bboxapilibrary.model.MediaResource;

/**
 * @author Bertrand Martel
 */

public class CustomerProfilingService extends Service {

    private static final String TAG = CustomerProfilingService.class.getSimpleName();

    //public IAuthCallback authenticationCallback;

    private AppHandler mHandler;

    private IMessageCallback mIotCallback;

    private boolean exit = false;

    private IBboxBridge.Stub bboxIotService = new IBboxBridge.Stub() {
    };

    private RandomString randomId = new RandomString(30);

    private int eventType;
    private JSONObject lastEvent = new JSONObject();
    private JSONObject lastEventMedia = new JSONObject();

    private int MEDIA = 0;
    private int APPLICATION = 1;
    private DateFormat df;
    private MyBbox bbox;
    private Handler handler = new Handler();
    private Runnable runnable;
    private int mStandby = 0;

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();
        //BboxHolder.getInstance().bboxSearch(CustomerProfilingService.this);
        BboxHolder.getInstance().setCustomBbox("127.0.0.1");
        try {
            bbox = BboxHolder.getInstance().getBbox();
        } catch (MyBboxNotFoundException e) {
            e.printStackTrace();
        }
        mHandler = new AppHandler(this, BuildConfig.BLUEMIX_IOT_ORG, Build.SERIAL, BuildConfig.BLUEMIX_API_KEY, BuildConfig.BLUEMIX_API_TOKEN);
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Log.i(TAG, "CustomerProfilingService started");

        mIotCallback = new IMessageCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection lost");
                if (cause != null) {
                    Log.e(TAG, "connection lost : " + cause.getMessage());
                }
                if (!exit) {
                    Log.i(TAG, "trying to reconnect");
                    BboxHolder.getInstance().setCustomBbox("127.0.0.1");
                    mHandler.connect();
                } else {
                    Log.i(TAG, "not trying to reconnect");
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                //Log.i(TAG, "messageArrived : " + topic + " : " + new String(mqttMessage.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken messageToken) {
                /*try {
                    Log.i(TAG, "deliveryComplete : " + new String(messageToken.getMessage().getPayload()));
                } catch (MqttException e) {
                    e.printStackTrace();
                }*/
            }

            @Override
            public void onConnectionSuccess(IMqttToken iMqttToken) {
                Log.i(TAG, "subscribe to device events ...");
                mHandler.subscribeDeviceEvents("+", "+", "+");
            }


            @Override
            public void onConnectionFailure(IMqttToken iMqttToken, Throwable throwable) {

            }

            @Override
            public void onDisconnectionSuccess(IMqttToken iMqttToken) {
                // disconnection successfull
                if (exit) {
                    mHandler.removeCallback(mIotCallback);
                }
            }

            @Override
            public void onDisconnectionFailure(IMqttToken iMqttToken, Throwable throwable) {

            }

        };

        mHandler.addIotCallback(mIotCallback);
        mHandler.setSSL(true);




                /*subscribe event*/

        System.out.println("ip delay = " + bbox.getIp());

        String appName = getString(R.string.app_name);
        Bbox.getInstance().registerApp(bbox.getIp(),
                BuildConfig.BBOXAPI_APP_ID,
                BuildConfig.BBOXAPI_APP_SECRET,
                appName,
                new IBboxRegisterApp() {
                    @Override
                    public void onResponse(final String registerApp) {
                        if (registerApp != null && !registerApp.isEmpty()) {
                            // Subscribe ressource application
                            Bbox.getInstance().subscribeNotification(bbox.getIp(),
                                    BuildConfig.BBOXAPI_APP_ID,
                                    BuildConfig.BBOXAPI_APP_SECRET,
                                    registerApp,
                                    "Application",
                                    new IBboxSubscribe() {
                                        @Override
                                        public void onSubscribe() {
                                            Bbox.getInstance().addListener(bbox.getIp(),
                                                    registerApp,
                                                    new IBboxApplication() {
                                                        @Override
                                                        public void onNewApplication(final ApplicationResource application) {
                                                            Log.i("notif", "Application " + application.toString());
                                                            try {
                                                                lastEvent = new JSONObject(application.toString());
                                                                if (lastEvent.get("packageName").equals("com.ifeelsmart.smartui")) {
                                                                    Bbox.getInstance().getCurrentChannel(bbox.getIp(),
                                                                            getResources().getString(fr.bouyguestelecom.bboxapi.R.string.APP_ID),
                                                                            getResources().getString(fr.bouyguestelecom.bboxapi.R.string.APP_SECRET),
                                                                            new IBboxGetCurrentChannel() {
                                                                                @Override
                                                                                public void onResponse(final Channel channel) {
                                                                                    if (channel.getMediaState().equals("play")) {
                                                                                        System.out.println("status = " + channel.getMediaState());
                                                                                        System.out.println("pos = " + channel.getPositionId());
                                                                                        System.out.println("name = " + channel.getName());
                                                                                        System.out.println("title = " + channel.getMediaTitle());
                                                                                        lastEvent = lastEventMedia;
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onFailure(Request request, int errorCode) {
                                                                                    Log.i("notif", "Get current channel failed");
                                                                                }
                                                                            });
                                                                }
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onFailure(Request request, int errorCode) {
                                            Log.i("notif", "Notification failed");
                                        }
                                    });
                            // end application
                        }
                    }

                    @Override
                    public void onFailure(Request request, int errorCode) {
                        Log.i("notif", "Notification failed");
                    }
                });

        Bbox.getInstance().registerApp(bbox.getIp(),
                BuildConfig.BBOXAPI_APP_ID,
                BuildConfig.BBOXAPI_APP_SECRET,
                appName,
                new IBboxRegisterApp() {
                    @Override
                    public void onResponse(final String registerApp) {
                        if (registerApp != null && !registerApp.isEmpty()) {
                            // Subscribe ressource media
                            Bbox.getInstance().subscribeNotification(bbox.getIp(),
                                    BuildConfig.BBOXAPI_APP_ID,
                                    BuildConfig.BBOXAPI_APP_SECRET,
                                    registerApp,
                                    "Media",
                                    new IBboxSubscribe() {
                                        @Override
                                        public void onSubscribe() {
                                            Bbox.getInstance().addListener(bbox.getIp(),
                                                    registerApp,
                                                    new IBboxMedia() {
                                                        @Override
                                                        public void onNewMedia(final MediaResource mediaResource) {
                                                            Log.i("notif", "MEDIA " + mediaResource.toString());
                                                            try {
                                                                lastEvent = new JSONObject(mediaResource.toString());
                                                                lastEventMedia = lastEvent;
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onFailure(Request request, int errorCode) {
                                            Log.i("notif", "Notification failed");
                                        }
                                    });
                            // end media
                        }
                    }

                    @Override
                    public void onFailure(Request request, int errorCode) {
                        Log.i("notif", "Notification failed");
                    }
                });
        Log.d(TAG, "connecting");
        mHandler.connect();


        //thread qui s'occupe de l'envoi des message mqtt
        runnable = new Runnable() {
            @Override
            public void run() {
                JSONObject body = new JSONObject();
                try {
                    PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                    //check if bbox is wake up
                    if (pm.isInteractive()) {

                        lastEvent.put("macaddress", BboxHolder.getInstance().getBbox().getMacAddress());
                        lastEvent.put("serialnumber", BboxHolder.getInstance().getBbox().getImei());
                        //on est plus en veille
                        if (mStandby == 1)
                            mStandby = 0;

                        lastEvent.put("standby", mStandby);
                        String date = df.format(Calendar.getInstance().getTime());
                        lastEvent.put("timestamp", date);

                        if (!lastEvent.isNull("positionId") && lastEvent.getInt("positionId") != -1) {
                            lastEvent.put("packageName", "com.ifeelsmart.smartui");
                            lastEvent.put("positionId", Integer.parseInt(String.valueOf(lastEvent.get("positionId"))));
                            body.put("channel", lastEvent);
                            System.out.println("tmp = " + body.toString());
                            mHandler.publishDeviceEvents(BuildConfig.BLUEMIX_IOT_DEVICE_TYPE, BuildConfig.BLUEMIX_IOT_DEVICEID, "MEDIA", body.toString());
                        } else {
                            lastEvent.put("positionId", -1);
                            lastEvent.remove("state");
                            body.put("channel", lastEvent);
                            System.out.println("tmp = " + body.toString());
                            mHandler.publishDeviceEvents(BuildConfig.BLUEMIX_IOT_DEVICE_TYPE, BuildConfig.BLUEMIX_IOT_DEVICEID, "MEDIA", body.toString());
                        }
                    }
                    // si la box est en vielle envoi 1 seul message
                    else if (mStandby == 0) {
                        mStandby = 1;
                        lastEvent.put("positionId", -1);
                        lastEvent.remove("state");
                        lastEvent.put("standby", mStandby);
                        body.put("channel", lastEvent);
                        System.out.println("tmp = " + body.toString());
                        mHandler.publishDeviceEvents(BuildConfig.BLUEMIX_IOT_DEVICE_TYPE, BuildConfig.BLUEMIX_IOT_DEVICEID, "MEDIA", body.toString());

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                } catch (MyBboxNotFoundException e) {
                    e.printStackTrace();
                }
                // Repeat every 5 min
                handler.postDelayed(runnable, 300000);
            }
        };

        // bloc pour lancer des message au meme moment
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        date.set(2017, Calendar.FEBRUARY, 8, 14 - 1, 33, 0);
        date.set(Calendar.MILLISECOND, 0);

        // Start the Runnable immediately
        System.out.println("date  = " + date.getTimeInMillis());
        System.out.println("date1  = " + System.currentTimeMillis());
        long time = (date.getTimeInMillis() - System.currentTimeMillis());
        System.out.println("dif  = " + time);


        handler.postDelayed(runnable, time);

        System.out.println("enddd");

        //    handler.post(runnable);


    }

    @Override
    public void onDestroy() {
        exit = true;
        Log.d(TAG, "disconnecting");
        mHandler.disconnect();

        super.onDestroy();
        Log.i(TAG, "service CustomerProfilingService destroyed");

        handler.removeCallbacks(runnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return bboxIotService;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Called when a client activity is unbinding from Service
     *
     * @param intent service intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}
