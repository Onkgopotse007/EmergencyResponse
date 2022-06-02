package com.example.emergencyresponse;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.VolumeProvider;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

@SuppressWarnings("ConstantConditions")
public class AutoStartService extends Service {
    private static final String TAG = "Myservice";
    private MediaSession mediaSession;
    int cVolume = 50;
    long totalTime = 0L;
    Timer timer = new Timer();
    int keyPressed = 0;
    Context mContext;
    LocationManager locationManager;
    String userCurrLocation;
    double longitude;
    double latitude;
    String msg="";
    int phoneIndex = 0;
    static Boolean isAlertRunning;

    //String [] message;
    DatabaseHelper myDB;

    public void funcGetLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            System.out.println("Location permissions  given");
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        msg = "I am under attack here's the location: " + "https://maps.google.com/?q=" + latitude + "," + longitude;
                        System.out.println(msg);
                    }
                    if (location == null) {
                        System.out.println("Location is empty");
                    }

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {
                    System.out.println("Provider enabled");
                }

                @Override
                public void onProviderDisabled(String provider) {
                    System.out.println("Provider disabled");
                }
            };
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,0,0,locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        myDB = new DatabaseHelper(this);

    }

    public void sendSms(String contactNumber, String mess) {

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(contactNumber, null, mess, null, null);
            System.out.println("Sending message");


            //System.out.println("following exception happens: "+ErrVar);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {

        ArrayList<String> phoneNo = new ArrayList<>();
        Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStart");
        mediaSession = new MediaSession(this, "PlayerService");
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        PlaybackState state = new PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0, 0) //you simulate a player which plays something.
                .build();
        mediaSession.setPlaybackState(state);
        mediaSession.setCallback(new MediaSession.Callback() {
            /*@Override
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                //System.out.println("Working");
                return super.onMediaButtonEvent(mediaButtonIntent);
            }*/

        });
        //this will only work on Lollipop and up, see https://code.google.com/p/android/issues/detail?id=224134
        VolumeProvider myVolumeProvider =
                new VolumeProvider(VolumeProvider.VOLUME_CONTROL_RELATIVE, /*max volume*/100, /*initial volume level*/cVolume) {
                    @Override
                    public void onAdjustVolume(int direction) {
                        super.onAdjustVolume(direction);
                /*
                -1 -- volume down
                1 -- volume up
                0 -- volume button released
                 */

                        if (direction == 1 && !timer.stopWatchRunning) {
                            System.out.println("Volume +1");
                            cVolume += 6;
                            setCurrentVolume(getCurrentVolume() + 6);

                            timer.start();
                            keyPressed++;

                        } else if (direction == 1 && timer.stopWatchRunning) {
                            cVolume += 6;
                            setCurrentVolume(getCurrentVolume() + 6);
                            timer.stop();
                            keyPressed++;
                            totalTime += timer.getElapsedMilliseconds();
                            System.out.println("Key pressed " + keyPressed + " number of times for " + totalTime + " milliseconds");
                            timer.start();
                        } else if (direction != 1 && direction != 0 || keyPressed > 3) {
                            //When a different button is pressed
                            cVolume -= 6;
                            setCurrentVolume(getCurrentVolume() - 6);
                            timer.stop();
                            keyPressed = 0;
                            totalTime = 0;
                        }
                        //if volume button is clicked 3 times print to console or put the time logic/if statement needed//ideal time between clicks is 300-500ms
                        if (keyPressed == 3 && totalTime >= 300 && totalTime <= 550) {
                            timer.stop();
                            totalTime += timer.getElapsedMilliseconds();
                            final Handler handler = new Handler();
                            System.out.println("State of thread: "+isAlertRunning);

                                funcGetLocation();funcGetLocation();
                                System.out.println(msg);
                                if (!msg.isEmpty()) {
                                    Cursor res = myDB.getData();
                                    if (res.getCount() == 0) {
                                        Toast.makeText(AutoStartService.this, "No Data exists", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    while (res.moveToNext()) {
                                        phoneNo.add(res.getString(1));
                                        phoneIndex++;
                                    }
                                    for (int p = 0; p < phoneNo.size(); p++) {
                                        sendSms(phoneNo.get(p), msg);
                                    }

                                } else {
                                    Toast.makeText(AutoStartService.this, "Message is empty", Toast.LENGTH_SHORT).show();
                                }
                                startActivity(new Intent(AutoStartService.this, StopAlert.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP).setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER));
                                final Runnable r = new Runnable() {

                                    @Override
                                    public void run() {
                                        funcGetLocation();funcGetLocation();
                                        System.out.println(msg);
                                        if (!msg.isEmpty()) {
                                            Cursor res = myDB.getData();
                                            if (res.getCount() == 0) {
                                                Toast.makeText(AutoStartService.this, "No Data exists", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            while (res.moveToNext()) {
                                                phoneNo.add(res.getString(1));
                                                phoneIndex++;
                                            }
                                            for (int p = 0; p < phoneNo.size(); p++) {
                                                sendSms(phoneNo.get(p), msg);
                                            }
                                        } else {
                                            System.out.println("Could not generate location");
                                        }
                                    }
                                };
                                handler.postDelayed(r, 300000);


                            //funcGetLocation();
                            System.out.println("Volume button pressed 3 times in " + totalTime + "milliseconds");
                            keyPressed = 0;
                            totalTime = 0;
                        }


                    }

                    @Override
                    public void onSetVolumeTo(int volume) {
                        super.onSetVolumeTo(volume);
                        System.out.println("onSetVolumeTo " + volume);
                    }
                };

        mediaSession.setPlaybackToRemote(myVolumeProvider);
        mediaSession.setActive(true);
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            mediaSession = new MediaSession(this, "PlayerService");
            mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                    MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
            PlaybackState state= new PlaybackState.Builder()
                    .setState(PlaybackState.STATE_PLAYING, 0, 0) //you simulate a player which plays something.
                    .build();
            mediaSession.setPlaybackState(state);

            //this will only work on Lollipop and up, see https://code.google.com/p/android/issues/detail?id=224134
            VolumeProvider myVolumeProvider =
                    new VolumeProvider(VolumeProvider.VOLUME_CONTROL_RELATIVE, /*max volume*//*100, /*initial volume level*//*50) {
                    @Override
                    public void onAdjustVolume(int direction) {
                        super.onAdjustVolume(direction);
                /*
                -1 -- volume down
                1 -- volume up
                0 -- volume button released
                 */
                        /*if(direction>0){
                            System.out.println("Volume +1");
                        }if(direction<0){
                            System.out.println("Volume -1");
                        }


                    }
                };

        mediaSession.setPlaybackToRemote(myVolumeProvider);
        mediaSession.setActive(true);
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return Service.START_STICKY;
    }*/

    @Override
    public void onDestroy() {

        super.onDestroy();
        //mediaSession.release();
        Toast.makeText(this, "Service Destroy", Toast.LENGTH_LONG).show();
    }

}
