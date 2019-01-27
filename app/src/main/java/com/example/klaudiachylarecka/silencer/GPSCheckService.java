package com.example.klaudiachylarecka.silencer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class GPSCheckService extends Service {

    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static final double TARGET_LAT = 51.109110;
    private static final double TARGET_LON = 17.060524;
    private static final float RANGE = 20000;


    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public boolean CheckIfInRange() {
            Log.i(TAG, "Checking location, current is: " + mLastLocation != null ? mLastLocation.toString() : "NULL");

            if (mLastLocation != null) {
                float[] dist = new float[1];
                Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(), GPSCheckService.TARGET_LAT, GPSCheckService.TARGET_LON, dist);


                if (dist[0] <= RANGE )
                {
                    Log.i(TAG, "In range!");
                    return true;
                }
            }
            return false;
        }

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    private class LocationCheckTask extends TimerTask {

        public  LocationCheckTask(LocationListener[] locationListeners)
        {
            mLocationListeners = locationListeners;
            am = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        }

        AudioManager am;

        private LocationListener[] mLocationListeners;

        private boolean isSilenced = false;

        private void Silence() {
            if (am != null) {
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }
        }

        private void UnSilence() {
            if (am != null) {
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        }

        @Override
        public void run() {

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    boolean shouldBeSilenced = false;
                    HoursDbHelper mDbHelper = new HoursDbHelper(getApplicationContext());
                    SQLiteDatabase db = mDbHelper.getReadableDatabase();
                    db.beginTransaction();

                    Calendar calendar = Calendar.getInstance();
                    //calendar.set(2019, Calendar.JANUARY, 29);
                    int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

                    if (currentDay >= Calendar.MONDAY && currentDay <= Calendar.FRIDAY) {
                        String query = "SELECT " + HoursContract.Hour.COLUMN_NAME_START_TIME + ", " + HoursContract.Hour.COLUMN_NAME_END_TIME + " FROM " + HoursContract.Hour.TABLE_NAME +
                                " WHERE " + HoursContract.Hour.COLUMN_NAME_DAY + " = '" + Day.GetDayByNumber(currentDay).Name + "'";

                        Cursor c = db.rawQuery(query, null);

                        while (c.moveToNext())
                        {
                            String sTime = c.getString(c.getColumnIndex(HoursContract.Hour.COLUMN_NAME_START_TIME));
                            String eTime = c.getString(c.getColumnIndex(HoursContract.Hour.COLUMN_NAME_END_TIME));

                            Date sTimeDate = new SimpleDateFormat("HH:mm").parse(sTime, new ParsePosition(0));
                            Calendar sTimeCalendar = Calendar.getInstance();
                            sTimeCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), sTimeDate.getHours(), sTimeDate.getMinutes());

                            Date eTimeDate = new SimpleDateFormat("HH:mm").parse(eTime, new ParsePosition(0));
                            Calendar eTimeCalendar = Calendar.getInstance();
                            eTimeCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), eTimeDate.getHours(), eTimeDate.getMinutes());

                            Date currentTime = calendar.getTime();

                            Date sDebug = sTimeCalendar.getTime();
                            Date eDebug = eTimeCalendar.getTime();

                            Log.i(TAG, "Found lesson: Day: " + Day.GetDayByNumber(currentDay).Name + ", StartTime: " + sDebug + ", EndTime: " + eDebug + ", currentTime: " + currentTime);

                            if (currentTime.after(sTimeCalendar.getTime()) && currentTime.before(eTimeCalendar.getTime())) {
                                Log.i(TAG, "Currently during lesson.");

                                for (LocationListener listener : mLocationListeners) {
                                    if (listener.CheckIfInRange())
                                    {
                                        Log.i(TAG, "In range, silence.");
                                        shouldBeSilenced = true;
                                    }
                                }
                            }
                    }
                    }

                    db.setTransactionSuccessful();
                    db.endTransaction();

                    if (shouldBeSilenced && !isSilenced)
                    {
                        Silence();
                        isSilenced = true;
                    }

                    if (!shouldBeSilenced && isSilenced)
                    {
                        UnSilence();
                    }
                }
            });
        }
    }

    public LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        if (mTimer != null) {
            mTimer.cancel();
        }
        else {
            mTimer = new Timer();
        }

        mTimer.scheduleAtFixedRate(new LocationCheckTask(mLocationListeners), 0, 1000);
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
