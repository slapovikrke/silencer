package com.example.klaudiachylarecka.silencer;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
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
        if(hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        {
            initApp();
        }
    }

    private void RefreshAmount() {
        TextView amount = (TextView) findViewById(R.id.textView2);

        HoursDbHelper mDbHelper = new HoursDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();


        long count = DatabaseUtils.queryNumEntries(db, HoursContract.Hour.TABLE_NAME,
                "", null
        );

        amount.setText(Long.toString(count));
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

        Button deleteDataButton = (Button) findViewById(R.id.button3);

        deleteDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HoursDbHelper mDbHelper = new HoursDbHelper(getApplicationContext());
                SQLiteDatabase db = mDbHelper.getReadableDatabase();

                String query = "DELETE FROM " + HoursContract.Hour.TABLE_NAME;
                db.execSQL(query);

                Log.i("Sometag", "Data deleted.");
                RefreshAmount();
            }
        });

        RefreshAmount();
    }

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_NOTIFICATION_POLICY
    };

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }

        NotificationManager n = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!n.isNotificationPolicyAccessGranted()){
            return false;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            MainActivity.this.startActivityForResult(intent, 2137);
        }
        else
        {
            initApp();
        }
    }
}
