package com.example.dell.relaxreminder.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.example.dell.relaxreminder.MainWindow.DateHandler;
import com.example.dell.relaxreminder.Model.TimeLog;
import com.example.dell.relaxreminder.Model.DateLog;

/**
 * Created by DELL on 3/17/2019.
 */

public class RelaxDataSource {
    private SQLiteDatabase database;
    private RelaxDbHelper dbHelper;

    private String[] allDateColumns = {RelaxDbHelper.COLUMN_ID,
            RelaxDbHelper.COLUMN_RELAX_NEED, RelaxDbHelper.COLUMN_RELAX_DONE , RelaxDbHelper.COLUMN_DATE};

    private String[] allTimeColumns = {RelaxDbHelper.COLUMN_TIME_ID,
            RelaxDbHelper.COLUMN_TIME_DATE, RelaxDbHelper.COLUMN_TIME};

    public RelaxDataSource(Context context) {
        dbHelper = new RelaxDbHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public DateLog createDateLog(int amount,int n,String d) {
        if(! isCurrentDateExist(d)){
            ContentValues values = new ContentValues();
            values.put(RelaxDbHelper.COLUMN_RELAX_DONE, amount);
            values.put(RelaxDbHelper.COLUMN_RELAX_NEED, n);
            values.put(RelaxDbHelper.COLUMN_DATE, d);
            long insertId = database.insert(RelaxDbHelper.Date_TABLE_NAME, null, values);
            Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,
                    allDateColumns, RelaxDbHelper.COLUMN_ID + " = " + insertId, null,
                    null, null, null);
            cursor.moveToFirst();
            DateLog newDateLog = cursorToDataLog(cursor);
            cursor.close();
            return newDateLog;
        }
        return null;
    }
    public static List<String> getDaysBetweenDates(String startDate, String endDate)
    {
        List<String> dates = new ArrayList<String>();
        Calendar calendar = new GregorianCalendar();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date sDate = format.parse(startDate);
            Date eDate = format.parse(endDate);

            calendar.setTime(sDate);

            while (calendar.getTime().before(eDate))
            {
                Date result = calendar.getTime();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String reportDate = df.format(result);
                dates.add(reportDate);
                calendar.add(Calendar.DATE, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dates;
    }
    public String getLastDay() {
        String date = null;
        Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,new String []{
                RelaxDbHelper.COLUMN_DATE } , null, null, null,null, RelaxDbHelper.COLUMN_DATE + " DESC", " 1");
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            date = cursor.getString(cursor.getColumnIndex(RelaxDbHelper.COLUMN_DATE));
        }
        cursor.close();
        return date;
    }
    public void createMissingDateLog(int amount,int n) {

        List<DateLog> dateLog = new ArrayList<DateLog>();
        String day=null;
        ContentValues values = new ContentValues();
        Cursor cursor=null;
        String startDate = getLastDay();
        String endDate = DateHandler.getCurrentDate();
        List days = getDaysBetweenDates(startDate, endDate);
        for (int i = 1; i < days.size(); i++) {
            day = (String) days.get(i);
            System.out.println("days " + day);

            values.put(RelaxDbHelper.COLUMN_RELAX_DONE, amount);
            values.put(RelaxDbHelper.COLUMN_RELAX_NEED, n);
            values.put(RelaxDbHelper.COLUMN_DATE, day);

            long insertId = database.insert(RelaxDbHelper.Date_TABLE_NAME, null, values);
            cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,
                    allDateColumns, RelaxDbHelper.COLUMN_ID + " = " + insertId, null,
                    null, null, null);
        }
        if(day!=null&&!isCurrentDateExist(day)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DateLog newDateLog = cursorToDataLog(cursor);
                dateLog.add(newDateLog);
            }
            cursor.close();

        }

    }

    private boolean isCurrentDateExist(String d) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = fmt.parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        fmt = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr =fmt.format(date);
        Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,new String []{RelaxDbHelper.COLUMN_DATE},
                RelaxDbHelper.COLUMN_TIME_DATE + " like '%"+dateStr+"%' ", null,null,null,null,null);
        if(cursor.getCount()>0)
            return true;
        else
            return false;
    }

    public void close() {
        dbHelper.close();
    }

    public int updateConsumedRelaxInDateLog( int removedAmount ,String date) {
        int TotalRelax = getConsumedAmount(date) + removedAmount;
        if(TotalRelax<0)
            TotalRelax=0;
        System.out.println("total relax "+TotalRelax);
        ContentValues cv = new ContentValues();
        cv.put(RelaxDbHelper.COLUMN_RELAX_DONE, TotalRelax);
        return database.update(RelaxDbHelper.Date_TABLE_NAME, cv, RelaxDbHelper.COLUMN_DATE + " like '%"+date+"%' ",null);
    }

    public boolean updateConsumedRelaxForTodayDateLog( int amount ) {
        int consumed = geConsumedRelaxForToadyDateLog() + amount;
        ContentValues cv = new ContentValues();
        cv.put(RelaxDbHelper.COLUMN_RELAX_DONE, consumed);
        return database.update(RelaxDbHelper.Date_TABLE_NAME, cv, RelaxDbHelper.COLUMN_ID + "= (SELECT  MAX(" + RelaxDbHelper.COLUMN_ID + " ) from "+ RelaxDbHelper.Date_TABLE_NAME+" )", null)>0;
    }

    public boolean updateRelaxNeedForTodayDateLog(int relaxNeed ) {
        System.out.println("relax need" +relaxNeed);

        ContentValues cv = new ContentValues();
        cv.put(RelaxDbHelper.COLUMN_RELAX_NEED, relaxNeed);
        return database.update(RelaxDbHelper.Date_TABLE_NAME, cv, RelaxDbHelper.COLUMN_ID + "= (SELECT  MAX(" + RelaxDbHelper.COLUMN_ID + " ) from "+ RelaxDbHelper.Date_TABLE_NAME+" )", null)>0;
    }
    public int geConsumedRelaxForToadyDateLog() {
        int relaxAmount = 0;
        Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,
                new String[]{RelaxDbHelper.COLUMN_ID,
                        RelaxDbHelper.COLUMN_RELAX_DONE, RelaxDbHelper.COLUMN_RELAX_NEED}, null, null, null, null, RelaxDbHelper.COLUMN_ID + " DESC", " 1");
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            relaxAmount = cursor.getInt(cursor.getColumnIndex(RelaxDbHelper.COLUMN_RELAX_DONE));
        }
        cursor.close();
        return relaxAmount;
    }

    public int getConsumedAmount(String date) {
        int consumed = 0;
        Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,
                new String[]{RelaxDbHelper.COLUMN_ID,
                        RelaxDbHelper.COLUMN_RELAX_DONE, RelaxDbHelper.COLUMN_RELAX_NEED}, RelaxDbHelper.COLUMN_DATE + " like '%"+date+"%' ", null, null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            consumed = cursor.getInt(cursor.getColumnIndex(RelaxDbHelper.COLUMN_RELAX_DONE));
            System.out.println("consumed "+consumed);
        }
        cursor.close();
        return consumed;
    }

    public int getConsumedPercentage() {
        int  relaxNeed=0;
        Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,
                new String[]{RelaxDbHelper.COLUMN_ID,
                        RelaxDbHelper.COLUMN_RELAX_NEED}, null, null, null, null, RelaxDbHelper.COLUMN_ID + " DESC", " 1");
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            relaxNeed = cursor.getInt(cursor.getColumnIndex( RelaxDbHelper.COLUMN_RELAX_NEED));
        }
        cursor.close();
        if(relaxNeed==0)
            return 0;
        int value= geConsumedRelaxForToadyDateLog()*100/relaxNeed;
        if (value>100)
            return 100;
        return value;
    }

    public List<DateLog> getAllDateLogs() {
        List<DateLog> dateLog = new ArrayList<DateLog>();
        Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,
                allDateColumns  , null, null, null, null, RelaxDbHelper.COLUMN_ID+" DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DateLog task = cursorToDataLog(cursor);
            System.out.println("here "+task.getRelaxNeed());
            dateLog.add(task);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return dateLog;
    }

    private DateLog cursorToDataLog(Cursor cursor) {
        DateLog date = new DateLog();
        date.setID(cursor.getLong(0));
        date.setRelaxNeed(cursor.getInt(1));
        date.setRelaxDone(cursor.getInt(2));
        date.setDate(cursor.getString(3));
        return date;
    }

    public TimeLog createTimeLog (int amount,String typ,String date,String time) {
        ContentValues values = new ContentValues();
        values.put(RelaxDbHelper.COLUMN_TIME_DATE, date);
        values.put(RelaxDbHelper.COLUMN_TIME, time);
        long insertId = database.insert(RelaxDbHelper.TIME_TABLE_NAME, null, values);
        Cursor cursor = database.query(RelaxDbHelper.TIME_TABLE_NAME,
                allTimeColumns, RelaxDbHelper.COLUMN_TIME_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        TimeLog newTime = cursorToTimeLog(cursor);
        cursor.close();
        return newTime;
    }
    private TimeLog cursorToTimeLog(Cursor cursor) {
        TimeLog time = new TimeLog();
        time.setID(cursor.getLong(0));
        time.setAmount(cursor.getInt(1));
        time.setContainerTyp(cursor.getString(2));
        time.setDate(cursor.getString(3));
        time.setTime(cursor.getString(4));
        return time;
    }

    public List<TimeLog> getAllTimeLogs(String sortBy,String date) {
        List<TimeLog> timeLog = new ArrayList<TimeLog>();
        Cursor cursor = database.query(RelaxDbHelper.TIME_TABLE_NAME,
                allTimeColumns, RelaxDbHelper.COLUMN_TIME_DATE + " like '%"+date+"%' ", null,null,null,sortBy,null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TimeLog task = cursorToTimeLog(cursor);
            timeLog.add(task);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return timeLog;
    }

    public void deleteTimeLog(TimeLog data) {
        Long id = data.getID();
        database.delete(RelaxDbHelper.TIME_TABLE_NAME,
                RelaxDbHelper.COLUMN_TIME_ID + "=" + id, null);
    }

    public boolean updateTimeLog(int ID, int doneAmount,String containerTyp) {
        ContentValues cv = new ContentValues();
        return database.update(RelaxDbHelper.TIME_TABLE_NAME, cv,
                RelaxDbHelper.COLUMN_TIME_ID + "=" + ID, null) > 0;
    }



    public String getYear() {
        String date = null;
        Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,new String []{
                RelaxDbHelper.COLUMN_DATE },  RelaxDbHelper.COLUMN_DATE +" BETWEEN datetime('now', 'start of year') AND datetime('now', 'localtime')", null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            date = cursor.getString(cursor.getColumnIndex(RelaxDbHelper.COLUMN_DATE));
        }
        cursor.close();
        return date;
    }

    public String getMonth() {
        String date = null;
        Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,new String []{
                RelaxDbHelper.COLUMN_DATE },  RelaxDbHelper.COLUMN_DATE +" BETWEEN datetime('now', 'start of month') AND datetime('now', 'localtime')", null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            date = cursor.getString(cursor.getColumnIndex(RelaxDbHelper.COLUMN_DATE));
        }
        cursor.close();
        return date;

    }
    public String getWeek() {
        String date = null;
        Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,new String []{
                RelaxDbHelper.COLUMN_DATE },  RelaxDbHelper.COLUMN_DATE +" BETWEEN datetime('now', '-7 days') AND datetime('now', 'localtime')", null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            date = cursor.getString(cursor.getColumnIndex(RelaxDbHelper.COLUMN_DATE));
        }
        cursor.close();
        return date;

    }

    public ArrayList<TimeLog>getRelaxByDay(){
        ArrayList<TimeLog> timeLog = new ArrayList<TimeLog>();
        Cursor cursor = database.query(RelaxDbHelper.TIME_TABLE_NAME,
                allTimeColumns , RelaxDbHelper.COLUMN_TIME_DATE + " BETWEEN date('now', 'start of day') AND datetime('now', 'localtime')", null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TimeLog task = cursorToTimeLog(cursor);
            timeLog.add(task);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return timeLog;

    }


    public List<DateLog> getRelaxByWeek() {
        ArrayList<DateLog> dateLog = new ArrayList<DateLog>();
        Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,
                allDateColumns , RelaxDbHelper.COLUMN_DATE + " BETWEEN datetime('now', '-7 days') AND datetime('now', 'localtime')", null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DateLog task = cursorToDataLog(cursor);
            dateLog.add(task);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return dateLog;
    }


    public String getCurrentDay() {
        String date = null;
        Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,new String []{
                RelaxDbHelper.COLUMN_DATE },  RelaxDbHelper.COLUMN_DATE + " BETWEEN datetime('now', 'start of day') AND datetime('now', 'localtime')", null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            date = cursor.getString(cursor.getColumnIndex(RelaxDbHelper.COLUMN_DATE));

        }
        cursor.close();
        return date;
    }


    public List<DateLog> getRelaxByMonth() {
        ArrayList<DateLog> dateLog = new ArrayList<DateLog>();
        Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,
                allDateColumns , RelaxDbHelper.COLUMN_DATE + " BETWEEN datetime('now', 'start of month') AND datetime('now', 'localtime')", null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DateLog task = cursorToDataLog(cursor);
            dateLog.add(task);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return dateLog;

    }

    public List<DateLog> getRelaxByYear() {
        ArrayList<DateLog> dateLog = new ArrayList<DateLog>();
        Cursor cursor = database.query(RelaxDbHelper.Date_TABLE_NAME,new String[]{RelaxDbHelper.COLUMN_ID,
                RelaxDbHelper.COLUMN_RELAX_NEED, "SUM("+ RelaxDbHelper.COLUMN_RELAX_DONE+")"
                , RelaxDbHelper.COLUMN_DATE}, RelaxDbHelper.COLUMN_DATE+ " BETWEEN datetime('now', 'start of year') AND datetime('now', 'localtime')", null, " strftime('%m',"
                + RelaxDbHelper.COLUMN_DATE+")", null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DateLog task = cursorToDataLog(cursor);
            dateLog.add(task);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return dateLog;
    }

    public String  sortByTimeAsc(){return RelaxDbHelper.COLUMN_TIME + " ASC ";}
    public String  sortByTimeDesc(){return RelaxDbHelper.COLUMN_TIME + " DESC ";}
}
