package com.evilstan.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataBaseEditor {

    private final DBHelper dbHelper;
    private SQLiteDatabase mDbHelper;
    private final List<String> colNamesList; //list of table column names
    List<Integer> indexesList; //list of all column indexes of table in db
    final String REGEX_SPLIT_UNITS = ListActivity.REGEX_SPLIT_UNITS;
    final String REGEX_SPLIT_ID = ListActivity.REGEX_SPLIT_ID;

    private final Context context;


    public DataBaseEditor(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
        colNamesList = new ArrayList<>();
        indexesList = new ArrayList<>();
        mDbHelper = dbHelper.getWritableDatabase();
        Cursor cursor = mDbHelper.query("waybill_In", null, null, null, null, null, null);

        //fill list with names of columns with data
        for (int i = 0; i < 20; i++) {
            colNamesList.add("item_" + i + "_name");
            colNamesList.add("item_" + i + "_quantity");
        }

        // find column indexes by it's name
        for (String columnName : colNamesList) {
            int colIndex = cursor.getColumnIndex(columnName);
            indexesList.add(colIndex);
        }

        cursor.close();
        mDbHelper.close();
    }

    //pushes item names from array to database. Debugging tool
    public void addItems(List<String> list) {
        //dbHelper = new DBHelper(context);
        ContentValues cv = new ContentValues();
        mDbHelper = dbHelper.getWritableDatabase();

        int i = 0;

        for (String s : list) {
            cv.put("code", i);
            i++;
            cv.put("name", s);
            i++;
        }

        //long rowID = db.insert("items", null, cv);
    }


    public void addWayBill(Map<String, Double> dataMap, int idNumber, Calendar calendar,
        String tableName) {
        //TODO make idNumber String instead of Integer
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String date = sdf.format(calendar.getTime());

        ContentValues cv = new ContentValues();
        mDbHelper = dbHelper.getWritableDatabase();
        int i = 0;
        cv.put("date", date);
        cv.put("idNumber", idNumber);

        for (Map.Entry<String, Double> mapEntry : dataMap.entrySet()) {
            System.out.println(mapEntry.getKey() + " " + mapEntry.getValue());
            cv.put(colNamesList.get(i), mapEntry.getKey());
            i++;
            cv.put(colNamesList.get(i), mapEntry.getValue());
            i++;
        }

        long rowID = mDbHelper.insert(tableName, null, cv);
        mDbHelper.close();
    }


    //calculates sum of every item in DB
    public Map<String, Double> showBalance(String tableName) {
        mDbHelper = dbHelper.getReadableDatabase();
        Cursor cursor = mDbHelper.query(tableName, null, null, null, null, null, null);
        Map<String, Double> dataMap = new LinkedHashMap<>();

        if (cursor.moveToFirst()) {
            do {

                for (int i = 0; i < indexesList.size(); i += 2) {
                    String itemName = cursor.getString(i + 3);
                    double itemValue = cursor.getDouble(i + 4);

                    if (itemName == null) {
                        break;
                    }

                    dataMap.merge(itemName, itemValue, Double::sum);
                }

            } while (cursor.moveToNext());
        } else {
            System.out.println("Database is empty");
        }

        mDbHelper.close();
        cursor.close();
        return dataMap;
    }

    // returns all records ID's for start screen
    public List<Integer> getRecordIdList(String tableName) {
        List<Integer> mResult = new ArrayList<>();
        mDbHelper = dbHelper.getReadableDatabase();
        Cursor cursor = mDbHelper.query(tableName, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                mResult.add(id);
            } while (cursor.moveToNext());
        } else {
            mResult.add(0);
            System.out.println("Database is empty");
        }

        mDbHelper.close();
        cursor.close();
        return mResult;
    }


    // returns all records for start screen
    public List<String> getData(String tableName) {
        List<String> dataArray = new ArrayList<>();
        mDbHelper = dbHelper.getReadableDatabase();
        Cursor cursor = mDbHelper.query(tableName, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String d = context.getString(R.string.date);
                String date = d + " " + cursor.getString(cursor.getColumnIndex("date")) + "\n";

                String k = context.getString(R.string.id_number);
                String idNumber = k + " " + cursor.getInt(cursor.getColumnIndex("idNumber")) + "\n";
                String buf = date + idNumber;

                for (int i = 0; i < indexesList.size(); i += 2) {
                    String itemName = cursor.getString(i + 3);
                    double itemValue = cursor.getDouble(i + 4);

                    if (itemName == null) {
                        break;
                    }

                    String unit;
                    try {
                        unit = itemName.split(REGEX_SPLIT_UNITS)[1];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        unit = "шт";
                    }
                    itemName = itemName.split(REGEX_SPLIT_UNITS)[0];
                    buf += itemName + " = " + itemValue + " " + unit + "; ";
                }

                buf += "\n";
                dataArray.add(buf);
            } while (cursor.moveToNext());
        } else {
            System.out.println("Database is empty");
            String temp = context.getString(R.string.database_empty);
            dataArray.add(temp);
        }

        mDbHelper.close();
        cursor.close();
        return dataArray;
    }


    //returns one record by it's ID
    public Map<String, Double> getRecord(int id, String tableName) {
        Map<String, Double> dataMap = new LinkedHashMap<>();
        mDbHelper = dbHelper.getReadableDatabase();
        Cursor mCursor = mDbHelper.query(tableName, null, null, null, null, null, null);

        if (mCursor.moveToFirst()) {

            String name;
            double value;

            do {
                if (mCursor.getInt(0) == id) {
                    String date = mCursor.getString(mCursor.getColumnIndex("date"));
                    double idNumber = mCursor.getInt(mCursor.getColumnIndex("idNumber"));
                    dataMap.put(date, idNumber);

                    int mIdSize = indexesList.size();
                    for (int k = 0; k < mIdSize; k += 2) {
                        name = mCursor.getString(indexesList.get(k));
                        value = mCursor.getDouble(indexesList.get(k + 1));

                        if (name == null) {
                            break;
                        }

                        dataMap.put(name, value);
                    }
                }
            } while (mCursor.moveToNext());
        } else {
            String temp = context.getString(R.string.database_empty);
            //dataMap.put(temp, 0.0);
            System.out.println("Database is empty");
        }

        mDbHelper.close();
        mCursor.close();
        return dataMap;
    }


    public void deleteRecord(int id, String tableName) {
        mDbHelper = dbHelper.getReadableDatabase();
        mDbHelper.delete(tableName, "_id=" + id, null);
        mDbHelper.close();
    }

    static class DBHelper extends SQLiteOpenHelper {

        final String CREATE_TABLE_ITEMS = "CREATE TABLE IF NOT EXISTS items"
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "code INTEGER NOT NULL, "
            + "name TEXT NOT NULL)";

        final String CREATE_TABLE_BALANCE = "CREATE TABLE IF NOT EXISTS balance"
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "code INTEGER NOT NULL, "
            + "name TEXT NOT NULL)";

        private String waybillInTableQuery = "CREATE TABLE IF NOT EXISTS waybill_In"
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "date TEXT NOT NULL, "
            + "idNumber REAL NOT NULL,";

        private String waybillOutTableQuery = "CREATE TABLE IF NOT EXISTS waybill_Out"
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "date TEXT NOT NULL, "
            + "idNumber REAL NOT NULL,";


        public DBHelper(Context context) {

            super(context, "Doka.db", null, 1);

            int waybillMaxSize = AddWaybillActivity.WAYBILL_MAX_SIZE;
            String s = ",";
            String buf1 = "";

            for (int i = 0; i < waybillMaxSize; i++) {

                if (i == waybillMaxSize - 1) {
                    s = ");";
                }

                buf1 += "item_" + i + "_name TEXT,item_" + i + "_quantity REAL" + s;
            }

            waybillInTableQuery += buf1;
            waybillOutTableQuery += buf1;

            System.out.println(waybillInTableQuery);
            System.out.println(waybillOutTableQuery);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
/*          db.execSQL("DROP TABLE IF EXISTS waybill_In");
            db.execSQL("DROP TABLE IF EXISTS items");
            db.execSQL("DROP TABLE IF EXISTS balance");*/
            db.execSQL("PRAGMA foreign_keys=ON;");
            db.execSQL(waybillInTableQuery);
            db.execSQL(waybillOutTableQuery);
            db.execSQL(CREATE_TABLE_ITEMS);
            db.execSQL(CREATE_TABLE_BALANCE);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            System.out.println("upgraded");
        }
    }
}
