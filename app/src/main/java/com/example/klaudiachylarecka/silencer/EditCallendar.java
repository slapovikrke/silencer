package com.example.klaudiachylarecka.silencer;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import com.example.klaudiachylarecka.silencer.Lesson;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static java.security.AccessController.getContext;

public class EditCallendar extends AppCompatActivity implements View.OnClickListener {

    private Map<String, LinearLayout> _containers = new HashMap<String, LinearLayout>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_callendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       Button back = (Button) findViewById(R.id.button2);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditCallendar.this, MainActivity.class);
                EditCallendar.this.startActivity(intent);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        LinearLayout master = (LinearLayout) findViewById(R.id.mainLayout);
        int index = 0;

        for (Day day : Day.days){

            if (day.IsVisible){
                LinearLayout parent = new LinearLayout(this);
                parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                parent.setOrientation(LinearLayout.VERTICAL);

                LinearLayout child1 = new LinearLayout(this);
                child1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                child1.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout child2 = new LinearLayout(this);
                child2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                child2.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                );

                _containers.put(day.Name, child2);

                TextView textView = new TextView(this);
                textView.setText(day.Name);
                textView.setLayoutParams(param);
                textView.setGravity(Gravity.LEFT | Gravity.BOTTOM);

                FloatingActionButton button = new FloatingActionButton(this);
                button.setTag(day.Name);
                button.setLayoutParams(param);
                button.setOnClickListener(this);

                child1.addView(textView);
                child1.addView(button);

                parent.addView(child1);
                parent.addView(child2);
                master.addView(parent);
            }

        }
    }

    @Override
    public void onClick(View v) {
        final String day = (String) v.getTag();

        Calendar mcurrentTime = Calendar.getInstance();
        final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        final int minute = mcurrentTime.get(Calendar.MINUTE);
        final EditCallendar ctx = this;

        TimePickerDialog dialog = new TimePickerDialog(ctx, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                final int fromTimeHour = selectedHour;
                final int fromTimeMinute = selectedMinute;

                TimePickerDialog dialog2 = new TimePickerDialog(ctx, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        TextView label1 = new TextView(ctx);
                        label1.setText("Od:");
                        TextView label2 = new TextView(ctx);
                        label2.setText("Do:");
                        TextView timeFrom = new TextView(ctx);
                        timeFrom.setText(fromTimeHour + ":" + fromTimeMinute);

                        TextView timeTo = new TextView(ctx);
                        timeTo.setText(selectedHour + ":" + selectedMinute);

                        LinearLayout container = new LinearLayout(ctx);
                        container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        container.setOrientation(LinearLayout.HORIZONTAL);

                        container.addView(label1);
                        container.addView(timeFrom);

                        TextView spaces = new TextView(ctx);
                        spaces.setText("   ");
                        container.addView(spaces);

                        container.addView(label2);
                        container.addView(timeTo);

                        Lesson lesson = new Lesson(fromTimeHour, fromTimeMinute, selectedHour, selectedMinute, day);

                        HoursDbHelper mDbHelper = new HoursDbHelper(getApplicationContext());
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put(HoursContract.Hour.COLUMN_NAME_START_TIME, fromTimeHour + ":" + fromTimeMinute);
                        values.put(HoursContract.Hour.COLUMN_NAME_END_TIME, selectedHour + ":" + selectedMinute);
                        values.put(HoursContract.Hour.COLUMN_NAME_DAY, day);

                        long newRowId = db.insert(HoursContract.Hour.TABLE_NAME, null, values);

                        LinearLayout master = _containers.get(day);
                        master.addView(container);
                    }
                }, hour, minute, true);
                dialog2.setTitle("Do:");
                dialog2.show();
            }
        }, hour, minute, true);//Yes 24 hour time
        dialog.setTitle("Od:");
        dialog.show();
    }
}
