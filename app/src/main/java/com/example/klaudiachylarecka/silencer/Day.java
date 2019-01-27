package com.example.klaudiachylarecka.silencer;

import java.util.ArrayList;

public class Day {

    public int Number;

    public String Name;

    public boolean IsVisible;

    public Day(int number, String name, boolean isVisible) {
        Number = number;
        Name = name;
        IsVisible = isVisible;
    }

    public static final ArrayList<Day> days = new ArrayList<Day>() {{
        add(new Day(1, "Niedziela", false));
        add(new Day(2, "Poniedziałek", true));
        add(new Day(3, "Wtorek", true));
        add(new Day(4, "Środa", true));
        add(new Day(5, "Czwartek", true));
        add(new Day(6, "Piątek", true));
        add(new Day(7, "Sobota", false));
    }};

    public static Day GetDayByNumber(int number){
        for (Day day : days){
            if (day.Number == number)
                return day;
        }

        return null;
    }
}
