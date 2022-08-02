package com.example.attend;

import android.util.Log;

import org.apache.commons.net.time.TimeTCPClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyCalendar extends Thread{

    static private String date, month, year, fullDate;
    boolean tryOnceMoreIfFail = true;
    public static String getFullDate() {
        return fullDate;
    }

    public String getDate() {
        return date;
    }

    public String getMonth() {
        return month;
    }

    public String getYear() {
        return year;
    }

//    get real current date year month online
    public void run() {
        TimeTCPClient timeclient = new TimeTCPClient();
        try {
            timeclient.setDefaultTimeout(60000);
            timeclient.connect("time.nist.gov");
            Date now = timeclient.getDate();
            MyCalendar.fullDate = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(now);
            MyCalendar.date = new SimpleDateFormat("d", Locale.ENGLISH).format(now);
            MyCalendar.month = new SimpleDateFormat("M", Locale.ENGLISH).format(now);
            MyCalendar.year = new SimpleDateFormat("yyyy",Locale.ENGLISH).format(now);
        } catch (Exception e) {
            Log.d("Looog", Log.getStackTraceString(e));
            if(tryOnceMoreIfFail)
            {
                tryOnceMoreIfFail = false;
                run();
            }
        }
    }
}
