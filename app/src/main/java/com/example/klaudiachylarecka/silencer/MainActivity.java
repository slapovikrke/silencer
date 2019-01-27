package com.example.klaudiachylarecka.silencer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button _goToCallendarButton;


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    initApp();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Log.e("MAIN_ACTIVITY", "Premissions not granted.");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    protected void initApp() {
        Intent i= new Intent(this, GPSCheckService.class);
        this.startService(i);

        _goToCallendarButton = (Button) findViewById(R.id.button);

        _goToCallendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditCallendar.class);
                MainActivity.this.startActivity(intent);
            }
        });

        TextView amount = (TextView) findViewById(R.id.textView2);

        HoursDbHelper mDbHelper = new HoursDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();


        long count = DatabaseUtils.queryNumEntries(db, HoursContract.Hour.TABLE_NAME,
                "", null
        );

        amount.setText(Long.toString(count));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                initApp();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            initApp();
        }
    }
}
