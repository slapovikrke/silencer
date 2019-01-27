package com.example.klaudiachylarecka.silencer;

public class Lesson {

    private int _startTimeHour;
    private int _startTimeMinute;
    private int _endTimeHour;
    private int _endTimeMinute;
    private String _day;

    public Lesson(int startTimeH, int startTimeM, int endTimeH, int endTimeM, String day) {
        _startTimeHour = startTimeH;
        _startTimeMinute = startTimeM;
        _endTimeHour = endTimeH;
        _endTimeMinute = endTimeM;
        _day = day;
    }
}
