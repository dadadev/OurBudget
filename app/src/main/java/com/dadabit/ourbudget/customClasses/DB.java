package com.dadabit.ourbudget.customClasses;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.dadabit.ourbudget.R;
import com.dadabit.ourbudget.fragments.main.InputFragment;
import com.dadabit.ourbudget.fragments.payments.PaymentsInputFragment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class DB {

    private static final String DB_NAME = "db_transactions";
    private static final int DB_VERSION = 1;

    public static final String COLUMN_ID = "_id";

    public static final String TABLE_NAME_CATEGORIES = "categories";
    public static final String COLUMN_CATEGORY_NAME = "category_name";
    public static final String COLUMN_CATEGORY_USE_COUNTER = "category_use_counter";

    public static final String TABLE_NAME_MY_TRANSACTIONS = "transactions_my";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_CATEGORY_ID = "category_id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_IS_DELETED = "is_deleted";

    public static final String TABLE_NAME_BUDGETS = "budgets";
    public static final String COLUMN_BUDGET_NAME = "budget_name";
    public static final String COLUMN_BUDGET_AMOUNT = "budget_amount";
    public static final String COLUMN_BUDGET_TYPE = "budget_type";
    public static final String COLUMN_BUDGET_CATEGORY = "budget_category";

    public static final String TABLE_NAME_PERSON2_TRANSACTIONS = "transactions_person2";

    public static final String TABLE_NAME_PAYMENTS = "payments";
    public static final String COLUMN_PAYMENT_NAME = "payment_name";
    public static final String COLUMN_PAYMENT_AMOUNT = "payment_amount";
    public static final String COLUMN_PAYMENT_AMOUNT_TYPE = "payment_amount_type";
    public static final String COLUMN_PAYMENT_TYPE = "payment_type";
    public static final String COLUMN_PAYMENT_CATEGORY = "payment_category";
    public static final String COLUMN_PAYMENT_DATE = "payment_date";

    private static final String DB_CREATE_CATEGORIES =
            "create table " + TABLE_NAME_CATEGORIES + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_CATEGORY_NAME + " TEXT, " +
                    COLUMN_CATEGORY_USE_COUNTER + " INTEGER);";

    private static final String DB_CREATE_MY_TRANSACTIONS =
            "create table " + TABLE_NAME_MY_TRANSACTIONS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_AMOUNT + " INTEGER, " +
                    COLUMN_TYPE + " INTEGER, " +
                    COLUMN_DATE + " INTEGER, " +
                    COLUMN_CATEGORY_ID + " INTEGER, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_IS_DELETED + " INTEGER);";

    private static final String DB_CREATE_PERSON2_TRANSACTIONS =
            "create table " + TABLE_NAME_PERSON2_TRANSACTIONS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_AMOUNT + " INTEGER, " +
                    COLUMN_TYPE + " INTEGER, " +
                    COLUMN_DATE + " INTEGER, " +
                    COLUMN_CATEGORY_ID + " INTEGER, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_IS_DELETED + " INTEGER);";

    private static final String DB_CREATE_MY_BUDGETS =
            "create table " + TABLE_NAME_BUDGETS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_BUDGET_NAME + " TEXT, " +
                    COLUMN_BUDGET_AMOUNT + " INTEGER, " +
                    COLUMN_BUDGET_TYPE + " INTEGER, " +
                    COLUMN_BUDGET_CATEGORY + " INTEGER, " +
                    COLUMN_IS_DELETED + " INTEGER);";

    private static final String DB_CREATE_PAYMENTS =
            "create table " + TABLE_NAME_PAYMENTS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_PAYMENT_NAME + " TEXT, " +
                    COLUMN_PAYMENT_AMOUNT + " INTEGER, " +
                    COLUMN_PAYMENT_AMOUNT_TYPE + " INTEGER, " +
                    COLUMN_PAYMENT_TYPE + " INTEGER, " +
                    COLUMN_PAYMENT_CATEGORY + " INTEGER, " +
                    COLUMN_PAYMENT_DATE + " INTEGER);";

    private DatabaseOpenHelper databaseHelper = null;
    private SQLiteDatabase database = null;
    private Context context;

    public DB(Context context) {
        this.context = context;
        databaseHelper = new DatabaseOpenHelper(context, DB_NAME, null, DB_VERSION);
    }

    public void open() throws SQLException {
        if(database == null) {
            try {
                database = databaseHelper.getWritableDatabase();
            } catch (NullPointerException e){
                Log.d("Database", e.getClass().getSimpleName() + " " + e.getLocalizedMessage());
            }
        }
    }

    public void close(){
        if (database!=null)
            database.close();
    }

    /**
     *  >>>>>>>>>>>>>>>CATEGORIES<<<<<<<<<<<<<<<<<<
     */

    public int[] getCategories(){
        open();

        Cursor cursor = database.query(
                TABLE_NAME_CATEGORIES,
                null,
                null,
                null,
                null,
                null,
                null);
        int[] ids = null;

        if (cursor.getCount() != 0){
            cursor.moveToFirst();
            ids = new int[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                ids[i]=cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            }
        }
        cursor.close();
        close();

        return ids;
    }

    public int[] getSortedCategoriesIds(){
        open();

        Cursor cursor = database.query(
                TABLE_NAME_CATEGORIES,
                new  String[] {COLUMN_ID},
                null,
                null,
                null,
                null,
                COLUMN_CATEGORY_USE_COUNTER +" DESC");

        int[] ids=null;

        if (cursor.getCount()!=0){
            cursor.moveToFirst();
            ids = new int[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                ids[i]=cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            }
        }
        cursor.close();
        close();

        return ids;
    }



    /**
     *  >>>>>>>>>>>>>>>TRANSACTIONS<<<<<<<<<<<<<<<<<<
     */

    public void addTransaction(int amount,
                               int type,
                               long date,
                               long category,
                               String description,
                               int isDeleted){

        ContentValues newTransaction = new ContentValues();
        newTransaction.put(COLUMN_AMOUNT, amount);
        newTransaction.put(COLUMN_TYPE, type);
        newTransaction.put(COLUMN_DATE, date);
        newTransaction.put(COLUMN_CATEGORY_ID, category);
        newTransaction.put(COLUMN_DESCRIPTION, description);
        newTransaction.put(COLUMN_IS_DELETED, isDeleted);

        open();
        database.insert(TABLE_NAME_MY_TRANSACTIONS, null, newTransaction);
        database.execSQL("UPDATE " + TABLE_NAME_CATEGORIES +
                        " SET " + COLUMN_CATEGORY_USE_COUNTER +
                        " = " + COLUMN_CATEGORY_USE_COUNTER + " + 1 " +
                        " WHERE " + COLUMN_ID + "=?",
                new String[] { String.valueOf(category) } );
//        close();
    }

    public void addTransactionWithId(long id,
                                     int amount,
                                     int type,
                                     long date,
                                     long category,
                                     String description,
                                     int isDeleted){

        open();

        ContentValues newTransaction = new ContentValues();
        newTransaction.put(COLUMN_ID, id);
        newTransaction.put(COLUMN_AMOUNT, amount);
        newTransaction.put(COLUMN_TYPE, type);
        newTransaction.put(COLUMN_DATE, date);
        newTransaction.put(COLUMN_CATEGORY_ID, category);
        newTransaction.put(COLUMN_DESCRIPTION, description);
        newTransaction.put(COLUMN_IS_DELETED, isDeleted);

        database.insert(TABLE_NAME_MY_TRANSACTIONS, null, newTransaction);
//        close();
    }

    public void changeTransaction(long id,
                                  int amount,
                                  int type,
                                  long date,
                                  long category,
                                  String description){
        ContentValues update = new ContentValues();
        update.put(COLUMN_AMOUNT, amount);
        update.put(COLUMN_TYPE, type);
        update.put(COLUMN_DATE, date);
        update.put(COLUMN_CATEGORY_ID, category);
        update.put(COLUMN_DESCRIPTION, description);

        open();
        database.update(TABLE_NAME_MY_TRANSACTIONS, update, COLUMN_ID+" = "+id, null);
        database.execSQL("UPDATE " + TABLE_NAME_CATEGORIES +
                        " SET " + COLUMN_CATEGORY_USE_COUNTER +
                        " = " + COLUMN_CATEGORY_USE_COUNTER + " + 1 " +
                        " WHERE " + COLUMN_ID + "=?",
                new String[] { String.valueOf(category) } );
        close();
    }

    public void deleteTransaction(long id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_IS_DELETED, 1);
        open();
        database.update(TABLE_NAME_MY_TRANSACTIONS, contentValues, COLUMN_ID+" = "+id, null);
        close();
    }

    public Cursor getMyTransactions(){
        open();
        return database.query(TABLE_NAME_MY_TRANSACTIONS,null,COLUMN_IS_DELETED+" = 0",null,null,null,COLUMN_DATE+" DESC");
    }

    public ArrayList<HashMap<Integer, Integer>> getStatisticData(Calendar currentDate, int chartType){
        ArrayList<HashMap<Integer, Integer>> amountsArray = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            amountsArray.add(new HashMap<Integer, Integer>());
        }

        open();

        Cursor cursor = database.query(
                TABLE_NAME_MY_TRANSACTIONS,
                null,
                COLUMN_IS_DELETED+" = 0",
                null,
                null,
                null,
                COLUMN_DATE+" ASC");

        Cursor cursor2 = database.query(
                TABLE_NAME_PERSON2_TRANSACTIONS,
                null,
                COLUMN_IS_DELETED+" = 0",
                null,
                null,
                null,
                COLUMN_DATE+" ASC");

        if (cursor.getCount()!=0){

            Calendar currentCalendar;

            cursor.moveToFirst();
            while (!cursor.isAfterLast()){

                currentCalendar = getDate(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)));

                if (currentCalendar.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)
                        && cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE))==chartType){

                    if (amountsArray.get(currentCalendar.get(Calendar.MONTH))
                            .containsKey(cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY_ID)))){

                        amountsArray.get(currentCalendar.get(Calendar.MONTH))
                                .put(
                                        cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY_ID)),
                                        amountsArray.get(currentCalendar.get(Calendar.MONTH))
                                                .get(cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY_ID)))
                                                + cursor.getInt(cursor.getColumnIndex(COLUMN_AMOUNT)));

                    } else {
                        amountsArray.get(currentCalendar.get(Calendar.MONTH))
                                .put(cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY_ID)),
                                        cursor.getInt(cursor.getColumnIndex(COLUMN_AMOUNT)));
                    }
                }
                cursor.moveToNext();
            }
        }

        if (cursor2.getCount()!=0){

            Calendar currentCalendar;

            if (amountsArray.get(0)==null){
                for (int i = 0; i < currentDate.getActualMaximum(Calendar.MONTH); i++) {
                    amountsArray.add(new HashMap<Integer, Integer>());
                }
            }
            cursor2.moveToFirst();
            while (!cursor2.isAfterLast()){

                currentCalendar = getDate(cursor2.getLong(cursor2.getColumnIndex(COLUMN_DATE)));

                if (currentCalendar.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)
                        && cursor2.getInt(cursor2.getColumnIndex(COLUMN_TYPE)) == chartType){

                    if (amountsArray.get(currentCalendar.get(Calendar.MONTH))
                            .containsKey(cursor2.getInt(cursor2.getColumnIndex(COLUMN_CATEGORY_ID)))){

                        amountsArray.get(currentCalendar.get(Calendar.MONTH))
                                .put(
                                        cursor2.getInt(cursor2.getColumnIndex(COLUMN_CATEGORY_ID)),
                                        amountsArray.get(currentCalendar.get(Calendar.MONTH))
                                                .get(cursor2.getInt(cursor2.getColumnIndex(COLUMN_CATEGORY_ID)))
                                                + cursor2.getInt(cursor2.getColumnIndex(COLUMN_AMOUNT)));

                    } else {
                        amountsArray.get(currentCalendar.get(Calendar.MONTH))
                                .put(cursor2.getInt(cursor2.getColumnIndex(COLUMN_CATEGORY_ID)),
                                        cursor2.getInt(cursor2.getColumnIndex(COLUMN_AMOUNT)));
                    }
                }
                cursor2.moveToNext();
            }
        }



        cursor.close();
        cursor2.close();
        close();
        return amountsArray;
    }



    private long getLastMyTransactionID(){
        open();
        Cursor cursor = database.query(TABLE_NAME_MY_TRANSACTIONS, new  String[] {COLUMN_ID}, null, null, null, null, null);
        long lastId;
        if (cursor.getCount()>0){
            cursor.moveToLast();
            lastId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
        } else {
            lastId=0;
        }
        cursor.close();
        return lastId;
    }

    public void addPerson2Transaction(long id,
                                      int amount,
                                      int type,
                                      long date,
                                      long category,
                                      String description,
                                      int isDeleted){

        ContentValues newTransaction = new ContentValues();
        newTransaction.put(COLUMN_ID, id);
        newTransaction.put(COLUMN_AMOUNT, amount);
        newTransaction.put(COLUMN_TYPE, type);
        newTransaction.put(COLUMN_DATE, date);
        newTransaction.put(COLUMN_CATEGORY_ID, category);
        newTransaction.put(COLUMN_DESCRIPTION, description);
        newTransaction.put(COLUMN_IS_DELETED, isDeleted);

        open();
        try {
            database.insert(TABLE_NAME_PERSON2_TRANSACTIONS, null, newTransaction);
        } catch (Exception e){
//            e.getCause().toString();
        }
//        close();
    }

    public void changePerson2Transaction(long id,
                                         int amount,
                                         int type,
                                         long date,
                                         long category,
                                         String description){
        ContentValues update = new ContentValues();
        update.put(COLUMN_AMOUNT, amount);
        update.put(COLUMN_TYPE, type);
        update.put(COLUMN_DATE, date);
        update.put(COLUMN_CATEGORY_ID, category);
        update.put(COLUMN_DESCRIPTION, description);

        open();
        database.update(TABLE_NAME_PERSON2_TRANSACTIONS, update, COLUMN_ID+" = "+id, null);
        close();
    }

    public void deletePerson2Transaction(long id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_IS_DELETED, 1);
        open();
        database.update(TABLE_NAME_PERSON2_TRANSACTIONS, contentValues, COLUMN_ID+" = "+id, null);
//        close();
    }

    public Cursor getPerson2Transactions(){
        open();
        return database.query(TABLE_NAME_PERSON2_TRANSACTIONS,null,COLUMN_IS_DELETED+" = 0",null,null,null,COLUMN_DATE+" DESC");
    }

    public Cursor getAllPerson2Transactions(){
        open();
        return database.query(TABLE_NAME_PERSON2_TRANSACTIONS,null,null,null,null,null,null);
    }

    public PostTransaction getLastTransaction(){
        open();
        Cursor cursor = database.query(
                TABLE_NAME_MY_TRANSACTIONS,
                null,
                COLUMN_IS_DELETED+" = 0",
                null,
                null,
                null,
                null);

        PostTransaction postTransaction = null;

        if(cursor.getCount()!=0){
            cursor.moveToLast();
            postTransaction = new PostTransaction(
                    cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID)),
                    cursor.getInt(cursor.getColumnIndex(DB.COLUMN_AMOUNT)),
                    cursor.getInt(cursor.getColumnIndex(DB.COLUMN_TYPE)),
                    cursor.getLong(cursor.getColumnIndex(DB.COLUMN_DATE)),
                    cursor.getInt(cursor.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                    cursor.getString(cursor.getColumnIndex(DB.COLUMN_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndex(DB.COLUMN_IS_DELETED)));
        }
        cursor.close();
        close();

        return postTransaction;
    }


    public ArrayList<ArrayList<Transaction>> getMonthTransactionsArrays (){

        ArrayList<ArrayList<Transaction>> groups = null;
        int month;

        open();
        Cursor myTransactions = database.query(TABLE_NAME_MY_TRANSACTIONS,null,COLUMN_IS_DELETED+" = 0",null,null,null,COLUMN_DATE+" DESC");
        Cursor person2Transactions = database.query(TABLE_NAME_PERSON2_TRANSACTIONS,null,COLUMN_IS_DELETED+" = 0",null,null,null,COLUMN_DATE+" DESC");

        if (myTransactions.getCount() != 0 && person2Transactions.getCount() !=0)
        {
            groups = new ArrayList<>();
            ArrayList<Transaction> child = new ArrayList<>();
            myTransactions.moveToFirst();
            person2Transactions.moveToFirst();

            if (person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))
                    >= myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) {
                month = getMonth(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE)));
            } else {
                month = getMonth(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
            }

            while (!myTransactions.isAfterLast() || !person2Transactions.isAfterLast())
            {
                if (person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))
                        >= myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))
                        && getMonth(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == month) {
                    child.add(new Transaction(
                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,2));
                    person2Transactions.moveToNext();

                    if (person2Transactions.isAfterLast()){
                        while (!myTransactions.isAfterLast())
                        {
                            if (myTransactions.isLast()){
                                if (getMonth(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) == month){
                                    child.add(new Transaction(
                                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                            ,1));
                                    groups.add(child);
                                    myTransactions.close();
                                    person2Transactions.close();
                                    close();
                                    return groups;
                                } else {
                                    groups.add(child);
                                    child = new ArrayList<>();
                                    child.add(new Transaction(
                                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                            ,1));
                                    groups.add(child);
                                    myTransactions.close();
                                    person2Transactions.close();
                                    close();
                                    return groups;
                                }
                            }
                            if (getMonth(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) == month){
                                child.add(new Transaction(
                                        myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                        getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                        myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                        ,1));
                                myTransactions.moveToNext();
                            } else {
                                groups.add(child);
                                child = new ArrayList<>();
                                month = getMonth(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
                                child.add(new Transaction(
                                        myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                        getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                        myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                        ,1));
                                myTransactions.moveToNext();
                            }
                        }
                    }

                }
                if (myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))
                        >= person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))
                        && getMonth(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) == month) {
                    child.add(new Transaction(
                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,1));
                    myTransactions.moveToNext();
                    if (myTransactions.isAfterLast()){
                        while (!person2Transactions.isAfterLast()){
                            if (person2Transactions.isLast()){
                                if (getMonth(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == month){
                                    child.add(new Transaction(
                                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                            ,2));
                                    groups.add(child);
                                    myTransactions.close();
                                    person2Transactions.close();
                                    close();
                                    return groups;
                                } else {
                                    groups.add(child);
                                    child = new ArrayList<>();
                                    child.add(new Transaction(
                                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                            ,2));
                                    groups.add(child);
                                    myTransactions.close();
                                    person2Transactions.close();
                                    close();
                                    return groups;
                                }
                            }
                            if (getMonth(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == month){
                                child.add(new Transaction(
                                        person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                        getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                        person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                        ,2));
                                person2Transactions.moveToNext();
                            } else {
                                groups.add(child);
                                child = new ArrayList<>();
                                month = getMonth(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE)));
                                child.add(new Transaction(
                                        person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                        getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                        person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                        ,2));
                                person2Transactions.moveToNext();
                            }
                        }
                    }
                }
                if (getMonth(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) != month
                        && getMonth(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) != month) {
                    groups.add(child);
                    child = new ArrayList<>();
                    if (person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))
                            >= myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) {
                        month = getMonth(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE)));
                    } else {
                        month = getMonth(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
                    }
                }
            }
        }

        if (myTransactions.getCount()!=0){
            ArrayList<Transaction> child = new ArrayList<>();
            groups = new ArrayList<>();
            myTransactions.moveToFirst();
            month = getMonth(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
            while (!myTransactions.isAfterLast()){
                if (myTransactions.isLast()){
                    if (getMonth(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)))==month){
                        Transaction transaction = new Transaction(
                                myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                ,1);
                        child.add(transaction);
                        groups.add(child);
                        myTransactions.close();
                        person2Transactions.close();
                        close();
                        return groups;
                    } else {
                        groups.add(child);
                        child = new ArrayList<>();
                        Transaction transaction = new Transaction(
                                myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                ,1);
                        child.add(transaction);
                        groups.add(child);
                        myTransactions.close();
                        person2Transactions.close();
                        close();
                        return groups;
                    }
                }
                if (getMonth(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)))==month){
                    Transaction transaction = new Transaction(
                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,1);
                    child.add(transaction);
                    myTransactions.moveToNext();
                } else {
                    groups.add(child);
                    child = new ArrayList<>();
                    month = getMonth(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
                    Transaction transaction = new Transaction(
                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,1);
                    child.add(transaction);
                    myTransactions.moveToNext();
                }
            }
        }

        if (person2Transactions.getCount()!=0){
            groups = new ArrayList<>();
            ArrayList<Transaction> child = new ArrayList<>();
            person2Transactions.moveToFirst();
            month = getMonth(person2Transactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
            while (!person2Transactions.isAfterLast()){
                if (person2Transactions.isLast()){
                    if (getMonth(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == month){
                        child.add(new Transaction(
                                person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                ,2));
                        groups.add(child);
                        myTransactions.close();
                        person2Transactions.close();
                        close();
                        return groups;
                    } else {
                        groups.add(child);
                        child = new ArrayList<>();
                        child.add(new Transaction(
                                person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                ,2));
                        groups.add(child);
                        myTransactions.close();
                        person2Transactions.close();
                        close();
                        return groups;
                    }
                }
                if (getMonth(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == month){
                    child.add(new Transaction(
                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,2));
                    person2Transactions.moveToNext();
                } else {
                    groups.add(child);
                    child = new ArrayList<>();
                    month = getMonth(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE)));
                    child.add(new Transaction(
                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,2));
                    person2Transactions.moveToNext();
                }
            }
        }
        myTransactions.close();
        person2Transactions.close();
        close();
        return groups;
    }

    public ArrayList<ArrayList<Transaction>> getWeekTransactionsArrays (){

        ArrayList<ArrayList<Transaction>> groups = null;
        int week;

        open();
        Cursor myTransactions = database.query(TABLE_NAME_MY_TRANSACTIONS,null,COLUMN_IS_DELETED+" = 0",null,null,null,COLUMN_DATE+" DESC");
        Cursor person2Transactions = database.query(TABLE_NAME_PERSON2_TRANSACTIONS,null,COLUMN_IS_DELETED+" = 0",null,null,null,COLUMN_DATE+" DESC");


        if (myTransactions.getCount()!=0 && person2Transactions.getCount() !=0)
        {
            groups = new ArrayList<>();
            ArrayList<Transaction> child = new ArrayList<>();
            myTransactions.moveToFirst();
            person2Transactions.moveToFirst();

            if (person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))
                    >= myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) {
                week = getWeek(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE)));
            } else {
                week = getWeek(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
            }

            while (!myTransactions.isAfterLast() || !person2Transactions.isAfterLast())
            {
                if (person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))
                        >= myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))
                        && getWeek(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == week) {
                    child.add(new Transaction(
                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,2));
                    person2Transactions.moveToNext();

                    if (person2Transactions.isAfterLast()){
                        while (!myTransactions.isAfterLast())
                        {
                            if (myTransactions.isLast()){
                                if (getWeek(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) == week){
                                    child.add(new Transaction(
                                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                            ,1));
                                    groups.add(child);
                                    myTransactions.close();
                                    person2Transactions.close();
                                    close();
                                    return groups;
                                } else {
                                    groups.add(child);
                                    child = new ArrayList<>();
                                    child.add(new Transaction(
                                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                            ,1));
                                    groups.add(child);
                                    myTransactions.close();
                                    person2Transactions.close();
                                    close();
                                    return groups;
                                }
                            }
                            if (getWeek(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) == week){
                                child.add(new Transaction(
                                        myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                        getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                        myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                        ,1));
                                myTransactions.moveToNext();
                            } else {
                                groups.add(child);
                                child = new ArrayList<>();
                                week = getWeek(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
                                child.add(new Transaction(
                                        myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                        getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                        myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                        ,1));
                                myTransactions.moveToNext();
                            }
                        }
                    }

                }
                if (myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))
                        >= person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))
                        && getWeek(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) == week) {
                    child.add(new Transaction(
                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,1));
                    myTransactions.moveToNext();
                    if (myTransactions.isAfterLast()){
                        while (!person2Transactions.isAfterLast()){
                            if (person2Transactions.isLast()){
                                if (getWeek(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == week){
                                    child.add(new Transaction(
                                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                            ,2));
                                    groups.add(child);
                                    myTransactions.close();
                                    person2Transactions.close();
                                    close();
                                    return groups;
                                } else {
                                    groups.add(child);
                                    child = new ArrayList<>();
                                    child.add(new Transaction(
                                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                            ,2));
                                    groups.add(child);
                                    myTransactions.close();
                                    person2Transactions.close();
                                    close();
                                    return groups;
                                }
                            }
                            if (getWeek(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == week){
                                child.add(new Transaction(
                                        person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                        getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                        person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                        ,2));
                                person2Transactions.moveToNext();
                            } else {
                                groups.add(child);
                                child = new ArrayList<>();
                                week = getWeek(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE)));
                                child.add(new Transaction(
                                        person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                        getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                        person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                        ,2));
                                person2Transactions.moveToNext();
                            }
                        }
                    }
                }
                if (getWeek(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) != week
                        && getWeek(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) != week) {
                    groups.add(child);
                    child = new ArrayList<>();
                    if (person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))
                            >= myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) {
                        week = getWeek(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE)));
                    } else {
                        week = getWeek(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
                    }
                }
            }
        }


        if (myTransactions.getCount()!=0){
            groups = new ArrayList<>();
            ArrayList<Transaction> child = new ArrayList<>();
            myTransactions.moveToFirst();
            week = getWeek(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
            while (!myTransactions.isAfterLast()){
                if (myTransactions.isLast()){
                    if (getWeek(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)))==week){
                        Transaction transaction = new Transaction(
                                myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                ,1);
                        child.add(transaction);
                        groups.add(child);
                        myTransactions.close();
                        person2Transactions.close();
                        close();
                        return groups;
                    } else {
                        groups.add(child);
                        child = new ArrayList<>();
                        Transaction transaction = new Transaction(
                                myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                ,1);
                        child.add(transaction);
                        groups.add(child);
                        myTransactions.close();
                        person2Transactions.close();
                        close();
                        return groups;
                    }
                }
                if (getWeek(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)))==week){
                    Transaction transaction = new Transaction(
                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,1);
                    child.add(transaction);
                    myTransactions.moveToNext();
                } else {
                    groups.add(child);
                    child = new ArrayList<>();
                    week = getWeek(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
                    Transaction transaction = new Transaction(
                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,1);
                    child.add(transaction);
                    myTransactions.moveToNext();
                }
            }
        }

        if (person2Transactions.getCount()!=0){
            groups = new ArrayList<>();
            ArrayList<Transaction> child = new ArrayList<>();
            person2Transactions.moveToFirst();
            week = getWeek(person2Transactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
            while (!person2Transactions.isAfterLast()){
                if (person2Transactions.isLast()){
                    if (getWeek(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == week){
                        child.add(new Transaction(
                                person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                ,2));
                        groups.add(child);
                        myTransactions.close();
                        person2Transactions.close();
                        close();
                        return groups;
                    } else {
                        groups.add(child);
                        child = new ArrayList<>();
                        child.add(new Transaction(
                                person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                ,2));
                        groups.add(child);
                        myTransactions.close();
                        person2Transactions.close();
                        close();
                        return groups;
                    }
                }
                if (getWeek(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == week){
                    child.add(new Transaction(
                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,2));
                    person2Transactions.moveToNext();
                } else {
                    groups.add(child);
                    child = new ArrayList<>();
                    week = getWeek(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE)));
                    child.add(new Transaction(
                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,2));
                    person2Transactions.moveToNext();
                }
            }
        }
        myTransactions.close();
        person2Transactions.close();
        close();
        return groups;
    }

    public ArrayList<ArrayList<Transaction>> getDayTransactionsArrays (){
        ArrayList<ArrayList<Transaction>> groups = null;
        int day;

        open();
        Cursor myTransactions = database.query(TABLE_NAME_MY_TRANSACTIONS,null,COLUMN_IS_DELETED+" = 0",null,null,null,COLUMN_DATE+" DESC");
        Cursor person2Transactions = database.query(TABLE_NAME_PERSON2_TRANSACTIONS,null,COLUMN_IS_DELETED+" = 0",null,null,null,COLUMN_DATE+" DESC");


        if (myTransactions.getCount()!=0 && person2Transactions.getCount() !=0)
        {
            groups = new ArrayList<>();
            ArrayList<Transaction> child = new ArrayList<>();
            myTransactions.moveToFirst();
            person2Transactions.moveToFirst();

            if (person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))
                    >= myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) {
                day = getDay(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE)));
            } else {
                day = getDay(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
            }

            while (!myTransactions.isAfterLast() || !person2Transactions.isAfterLast())
            {
                if (person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))
                        >= myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))
                        && getDay(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == day) {
                    child.add(new Transaction(
                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,2));
                    person2Transactions.moveToNext();

                    if (person2Transactions.isAfterLast()){
                        while (!myTransactions.isAfterLast())
                        {
                            if (myTransactions.isLast()){
                                if (getDay(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) == day){
                                    child.add(new Transaction(
                                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                            ,1));
                                    groups.add(child);
                                    myTransactions.close();
                                    person2Transactions.close();
                                    close();
                                    return groups;
                                } else {
                                    groups.add(child);
                                    child = new ArrayList<>();
                                    child.add(new Transaction(
                                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                            ,1));
                                    groups.add(child);
                                    myTransactions.close();
                                    person2Transactions.close();
                                    close();
                                    return groups;
                                }
                            }
                            if (getDay(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) == day){
                                child.add(new Transaction(
                                        myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                        getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                        myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                        ,1));
                                myTransactions.moveToNext();
                            } else {
                                groups.add(child);
                                child = new ArrayList<>();
                                day = getDay(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
                                child.add(new Transaction(
                                        myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                        getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                        myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                        myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                        ,1));
                                myTransactions.moveToNext();
                            }
                        }
                    }

                }
                if (myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))
                        >= person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))
                        && getDay(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) == day) {
                    child.add(new Transaction(
                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,1));
                    myTransactions.moveToNext();
                    if (myTransactions.isAfterLast()){
                        while (!person2Transactions.isAfterLast()){
                            if (person2Transactions.isLast()){
                                if (getDay(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == day){
                                    child.add(new Transaction(
                                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                            ,2));
                                    groups.add(child);
                                    myTransactions.close();
                                    person2Transactions.close();
                                    close();
                                    return groups;
                                } else {
                                    groups.add(child);
                                    child = new ArrayList<>();
                                    child.add(new Transaction(
                                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                            ,2));
                                    groups.add(child);
                                    myTransactions.close();
                                    person2Transactions.close();
                                    close();
                                    return groups;
                                }
                            }
                            if (getDay(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == day){
                                child.add(new Transaction(
                                        person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                        getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                        person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                        ,2));
                                person2Transactions.moveToNext();
                            } else {
                                groups.add(child);
                                child = new ArrayList<>();
                                day = getDay(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE)));
                                child.add(new Transaction(
                                        person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                        getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                        person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                        person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                        ,2));
                                person2Transactions.moveToNext();
                            }
                        }
                    }
                }
                if (getDay(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) != day
                        && getDay(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) != day) {
                    groups.add(child);
                    child = new ArrayList<>();
                    if (person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))
                            >= myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))) {
                        day = getDay(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE)));
                    } else {
                        day = getDay(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
                    }
                }
            }
        }


        if (myTransactions.getCount()!=0){
            groups = new ArrayList<>();
            ArrayList<Transaction> child = new ArrayList<>();
            myTransactions.moveToFirst();
            day = getDay(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
            while (!myTransactions.isAfterLast()){
                if (myTransactions.isLast()){
                    if (getDay(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)))==day){
                        Transaction transaction = new Transaction(
                                myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                ,1);
                        child.add(transaction);
                        groups.add(child);
                        myTransactions.close();
                        person2Transactions.close();
                        close();
                        return groups;
                    } else {
                        groups.add(child);
                        child = new ArrayList<>();
                        Transaction transaction = new Transaction(
                                myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                                getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                                myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                ,1);
                        child.add(transaction);
                        groups.add(child);
                        myTransactions.close();
                        person2Transactions.close();
                        close();
                        return groups;
                    }
                }
                if (getDay(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)))==day){
                    Transaction transaction = new Transaction(
                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,1);
                    child.add(transaction);
                    myTransactions.moveToNext();
                } else {
                    groups.add(child);
                    child = new ArrayList<>();
                    day = getDay(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
                    Transaction transaction = new Transaction(
                            myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_ID)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(myTransactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE))),
                            myTransactions.getInt(myTransactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            myTransactions.getString(myTransactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,1);
                    child.add(transaction);
                    myTransactions.moveToNext();
                }
            }
        }

        if (person2Transactions.getCount()!=0){
            groups = new ArrayList<>();
            ArrayList<Transaction> child = new ArrayList<>();
            person2Transactions.moveToFirst();
            day = getDay(person2Transactions.getLong(myTransactions.getColumnIndex(DB.COLUMN_DATE)));
            while (!person2Transactions.isAfterLast()){
                if (person2Transactions.isLast()){
                    if (getDay(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == day){
                        child.add(new Transaction(
                                person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                ,2));
                        groups.add(child);
                        myTransactions.close();
                        person2Transactions.close();
                        close();
                        return groups;
                    } else {
                        groups.add(child);
                        child = new ArrayList<>();
                        child.add(new Transaction(
                                person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                                getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                                person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                                ,2));
                        groups.add(child);
                        myTransactions.close();
                        person2Transactions.close();
                        close();
                        return groups;
                    }
                }
                if (getDay(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))) == day){
                    child.add(new Transaction(
                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,2));
                    person2Transactions.moveToNext();
                } else {
                    groups.add(child);
                    child = new ArrayList<>();
                    day = getDay(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE)));
                    child.add(new Transaction(
                            person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_ID)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_AMOUNT)),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_TYPE)),
                            getDate(person2Transactions.getLong(person2Transactions.getColumnIndex(DB.COLUMN_DATE))),
                            person2Transactions.getInt(person2Transactions.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                            person2Transactions.getString(person2Transactions.getColumnIndex(DB.COLUMN_DESCRIPTION))
                            ,2));
                    person2Transactions.moveToNext();
                }
            }
        }
        myTransactions.close();
        person2Transactions.close();
        close();
        return groups;
    }

    private Calendar getDate(long date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar;
    }

    private int getMonth(long date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.MONTH);
    }

    private int getWeek(long date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    private int getDay(long date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.DAY_OF_YEAR);
    }


    /**
     *  >>>>>>>>>>>>>>>BUDGETS<<<<<<<<<<<<<<<<<<
     */

    public void addBudget(String name,
                          int amount,
                          int type,
                          long category){

        ContentValues newTransaction = new ContentValues();
        newTransaction.put(COLUMN_BUDGET_NAME, name);
        newTransaction.put(COLUMN_BUDGET_AMOUNT, amount);
        newTransaction.put(COLUMN_BUDGET_TYPE, type);
        newTransaction.put(COLUMN_BUDGET_CATEGORY, category);
        newTransaction.put(COLUMN_IS_DELETED, 0);

        open();
        database.insert(TABLE_NAME_BUDGETS, null, newTransaction);
        close();
    }


    public void deleteBudget(long id){
        ContentValues update = new ContentValues();
        update.put(COLUMN_IS_DELETED, 1);

        open();
        database.update(TABLE_NAME_BUDGETS, update, COLUMN_ID+" = "+id, null);
        close();
    }

    public ArrayList<Budget> getBudgetArrayList(){

        ArrayList<Budget> budgetsArrayList = new ArrayList<>();
        ArrayList<Transaction> currentTransactionsArray;
        int currentSpendSum;


        try {
            open();
            Cursor myBudgets = database.query(TABLE_NAME_BUDGETS,null,COLUMN_IS_DELETED+" = 0",null,null,null,null);

            if (myBudgets.getCount()!=0){
                ArrayList<ArrayList<Transaction>> transactionArrays = getMonthTransactionsArrays();
                myBudgets.moveToFirst();
                while (!myBudgets.isAfterLast()){

                    if (myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_TYPE))
                            == Budget.TYPE_BY_WEEK)
                    {
                        currentTransactionsArray = getWeekTransactions(transactionArrays);
                        if (currentTransactionsArray!=null && currentTransactionsArray.size()>0){

                            currentSpendSum = getSpendSum(currentTransactionsArray,
                                    myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_CATEGORY)));

                            Budget budget = new Budget(
                                    myBudgets.getLong(myBudgets.getColumnIndex(DB.COLUMN_ID)),
                                    myBudgets.getLong(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_CATEGORY)),
                                    myBudgets.getString(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_NAME)),
                                    myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_AMOUNT)),
                                    myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_TYPE)),
                                    currentSpendSum,
                                    getOverBalance(
                                            currentSpendSum,
                                            myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_AMOUNT))),
                                    getDayBalanceByWeek(getOverBalance(
                                            currentSpendSum,
                                            myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_AMOUNT)))));
                            budgetsArrayList.add(budget);
                        }
                    }
                    if (myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_TYPE))
                            == Budget.TYPE_BY_MONTH)
                    {
                        currentTransactionsArray = getMonthTransactions(transactionArrays);
                        if (currentTransactionsArray!=null){

                            currentSpendSum = getSpendSum(currentTransactionsArray,
                                    myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_CATEGORY)));

                            Budget budget = new Budget(myBudgets.getLong(myBudgets.getColumnIndex(DB.COLUMN_ID)),
                                    myBudgets.getLong(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_CATEGORY)),
                                    myBudgets.getString(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_NAME)),
                                    myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_AMOUNT)),
                                    myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_TYPE)),
                                    currentSpendSum,
                                    getOverBalance(currentSpendSum,
                                            myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_AMOUNT))),
                                    getDayBalanceByMonth(getOverBalance(currentSpendSum,
                                            myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_AMOUNT)))));
                            budgetsArrayList.add(budget);
                        }
                    }

                    if (myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_TYPE))
                            == Budget.TYPE_BY_YEAR)
                    {
                        currentTransactionsArray = getYearTransactions(transactionArrays);
                        if (currentTransactionsArray!=null && currentTransactionsArray.size()>0){

                            currentSpendSum = getSpendSum(currentTransactionsArray,
                                    myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_CATEGORY)));

                            Budget budget = new Budget(myBudgets.getLong(myBudgets.getColumnIndex(DB.COLUMN_ID)),
                                    myBudgets.getLong(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_CATEGORY)),
                                    myBudgets.getString(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_NAME)),
                                    myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_AMOUNT)),
                                    myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_TYPE)),
                                    currentSpendSum,
                                    getOverBalance(currentSpendSum,
                                            myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_AMOUNT))),
                                    getDayBalanceByYear(getOverBalance(currentSpendSum,
                                            myBudgets.getInt(myBudgets.getColumnIndex(DB.COLUMN_BUDGET_AMOUNT)))));
                            budgetsArrayList.add(budget);
                        }
                    }
                    myBudgets.moveToNext();
                }
            }
            myBudgets.close();
            close();
        } catch (NullPointerException e){
            Log.d("Database", e.getClass().getSimpleName() + " " + e.getLocalizedMessage());
        }
        return budgetsArrayList;
    }


    private ArrayList<Transaction> getWeekTransactions(ArrayList<ArrayList<Transaction>> transactions){
        Calendar calendar = Calendar.getInstance();
        ArrayList<Transaction> weekTransactions = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            for (int j = 0; j < transactions.get(i).size(); j++) {
                if (transactions.get(i).get(j).getDate().get(Calendar.WEEK_OF_YEAR)
                        == calendar.get(Calendar.WEEK_OF_YEAR)){
                    weekTransactions.add(transactions.get(i).get(j));
                }
            }
        }
        return weekTransactions;
    }

    private ArrayList<Transaction> getMonthTransactions(ArrayList<ArrayList<Transaction>> transactions){
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).get(0).getDate().get(Calendar.MONTH)==calendar.get(Calendar.MONTH))
                return transactions.get(i);
        }
        return null;
    }

    private ArrayList<Transaction> getYearTransactions(ArrayList<ArrayList<Transaction>> transactions){
        Calendar calendar = Calendar.getInstance();
        ArrayList<Transaction> yearTransactions = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            for (int j = 0; j < transactions.get(i).size(); j++) {
                if (transactions.get(i).get(j).getDate().get(Calendar.YEAR)
                        == calendar.get(Calendar.YEAR)){
                    yearTransactions.add(transactions.get(i).get(j));
                }
            }
        }

        return yearTransactions;
    }

    private int getSpendSum(ArrayList<Transaction> transactions, int category){
        int sum=0;
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getCategoryID()==category
                    &&
                    transactions.get(i).getAmountType()
                            == InputFragment.TRANSACTION_EXPENSE){
                sum+=transactions.get(i).getAmount();
            }
        }
        return sum;
    }

    private int getOverBalance(int spendSum, int totalSum){
        return totalSum-spendSum;
    }

    private int getDayBalanceByWeek(int balance){
        Calendar calendar = Calendar.getInstance();
        int days = 7-calendar.get(Calendar.DAY_OF_WEEK);
        if (days==0)
            days=1;
        return balance/days;
    }

    private int getDayBalanceByMonth(int balance){
        Calendar calendar = Calendar.getInstance();
        int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)-calendar.get(Calendar.DAY_OF_MONTH);
        if (days==0)
            days=1;
        return balance/days;
    }

    private int getDayBalanceByYear(int balance){
        Calendar calendar = Calendar.getInstance();
        int days = calendar.getActualMaximum(Calendar.DAY_OF_YEAR)-calendar.get(Calendar.DAY_OF_YEAR);
        if (days<1)
            days=1;
        return balance/days;
    }


    /**
     *  >>>>>>>>>>>>>>>PAYMENTS<<<<<<<<<<<<<<<<<<
     */

    public void addPayment(String name,
                           int amount,
                           int amountType,
                           int type,
                           long category,
                           long date){

        ContentValues newTransaction = new ContentValues();
        newTransaction.put(COLUMN_PAYMENT_NAME, name);
        newTransaction.put(COLUMN_PAYMENT_AMOUNT, amount);
        newTransaction.put(COLUMN_PAYMENT_AMOUNT_TYPE, amountType);
        newTransaction.put(COLUMN_PAYMENT_TYPE, type);
        newTransaction.put(COLUMN_PAYMENT_CATEGORY, category);
        newTransaction.put(COLUMN_PAYMENT_DATE, date);

        open();
        database.insert(TABLE_NAME_PAYMENTS, null, newTransaction);
        close();
    }

    public ArrayList<RegularPayment> getPayments(){
        open();
        Cursor cursor = database.query(TABLE_NAME_PAYMENTS,null,null,null,null,null,COLUMN_ID +" DESC");
        ArrayList<RegularPayment> payments = new ArrayList<>();
        if (cursor.getCount()!=0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                payments.add(new RegularPayment(
                        cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_PAYMENT_NAME)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_PAYMENT_AMOUNT)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_PAYMENT_AMOUNT_TYPE)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_PAYMENT_TYPE)),
                        cursor.getLong(cursor.getColumnIndex(COLUMN_PAYMENT_CATEGORY)),
                        cursor.getLong(cursor.getColumnIndex(COLUMN_PAYMENT_DATE))));
                cursor.moveToNext();
            }
        }
        cursor.close();
        close();
        return payments;
    }


    private void updatePayment(long id,
                               long date){
        ContentValues update = new ContentValues();
        update.put(COLUMN_PAYMENT_DATE, date);

        open();
        database.update(TABLE_NAME_PAYMENTS, update, COLUMN_ID+" = "+id, null);
    }

    public void deletePayment(long id){
        open();
        database.delete(TABLE_NAME_PAYMENTS, COLUMN_ID+" = "+id, null);
        close();
    }


    public ArrayList<PostTransaction> getNewTransactions (){
        ArrayList<PostTransaction> newTransactions = new ArrayList<>();

        open();
        Cursor cursor = database.query(TABLE_NAME_PAYMENTS,null,null,null,null,null,null);

        if (cursor.getCount()!=0){
            long lastID = getLastMyTransactionID();
            int counter = 0;
            Calendar currentDate = Calendar.getInstance();
            Calendar transactionDate = Calendar.getInstance();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){

                transactionDate.setTimeInMillis(
                        cursor.getLong(cursor.getColumnIndex(DB.COLUMN_PAYMENT_DATE)));

                if (cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_TYPE))
                        == PaymentsInputFragment.PAYMENT_TYPE_DAY)
                {
                    if (currentDate.get(Calendar.DAY_OF_YEAR)
                            > transactionDate.get(Calendar.DAY_OF_YEAR)){
                        int missedDays =
                                currentDate.get(Calendar.DAY_OF_YEAR)
                                        -transactionDate.get(Calendar.DAY_OF_YEAR);
                        String description = cursor.getString(cursor.getColumnIndex(DB.COLUMN_PAYMENT_NAME))
                                + context.getString(R.string.payment_day_description);
                        for (int i = 0; i < missedDays; i++) {
                            transactionDate.add(Calendar.DAY_OF_YEAR, 1);
                            counter++;
                            addTransaction(
                                    cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT)),
                                    cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT_TYPE)),
                                    transactionDate.getTimeInMillis(),
                                    cursor.getLong(cursor.getColumnIndex(DB.COLUMN_PAYMENT_CATEGORY)),
                                    description,0);

                            PostTransaction postTransaction = new PostTransaction(
                                    lastID+counter,
                                    cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT)),
                                    cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT_TYPE)),
                                    transactionDate.getTimeInMillis(),
                                    cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_CATEGORY)),
                                    description,0);

                            newTransactions.add(postTransaction);
                        }
                        updatePayment(
                                cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID)),
                                currentDate.getTimeInMillis());
                    } else {
                        if (currentDate.get(Calendar.YEAR)>transactionDate.get(Calendar.YEAR)){
                            int missedDays = transactionDate.getMaximum(Calendar.DAY_OF_YEAR)
                                    -transactionDate.get(Calendar.DAY_OF_YEAR)
                                    +currentDate.get(Calendar.DAY_OF_YEAR);
                            String description = cursor.getString(cursor.getColumnIndex(DB.COLUMN_PAYMENT_NAME))
                                    + context.getString(R.string.payment_day_description);
                            for (int i = 0; i < missedDays; i++) {
                                transactionDate.add(Calendar.DAY_OF_YEAR, 1);
                                counter++;
                                addTransactionWithId(
                                        lastID+counter,
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT_TYPE)),
                                        transactionDate.getTimeInMillis(),
                                        cursor.getLong(cursor.getColumnIndex(DB.COLUMN_PAYMENT_CATEGORY)),
                                        description,0);

                                PostTransaction postTransaction = new PostTransaction(
                                        lastID+counter,
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT_TYPE)),
                                        transactionDate.getTimeInMillis(),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_CATEGORY)),
                                        description,0);

                                newTransactions.add(postTransaction);
                            }
                            updatePayment(
                                    cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID)),
                                    transactionDate.getTimeInMillis());
                        }
                    }
                }
                if (cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_TYPE))
                        == PaymentsInputFragment.PAYMENT_TYPE_WEEK )
                {
                    if (currentDate.get(Calendar.WEEK_OF_YEAR)
                            >transactionDate.get(Calendar.WEEK_OF_YEAR))
                    {
                        int missedWeeks =
                                currentDate.get(Calendar.WEEK_OF_YEAR)
                                        -transactionDate.get(Calendar.WEEK_OF_YEAR);
                        String description = cursor.getString(cursor.getColumnIndex(DB.COLUMN_PAYMENT_NAME))
                                + context.getString(R.string.payments_week_description);
                        for (int i = 0; i < missedWeeks; i++) {
                            transactionDate.add(Calendar.WEEK_OF_YEAR, 1);
                            if (transactionDate.get(Calendar.WEEK_OF_YEAR)
                                    == currentDate.get(Calendar.WEEK_OF_YEAR)
                                    &&
                                    transactionDate.get(Calendar.DAY_OF_WEEK)
                                            > currentDate.get(Calendar.DAY_OF_WEEK)){
                                transactionDate.add(Calendar.WEEK_OF_YEAR, -1);
                            } else {
                                counter++;
                                addTransactionWithId(
                                        lastID+counter,
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT_TYPE)),
                                        transactionDate.getTimeInMillis(),
                                        cursor.getLong(cursor.getColumnIndex(DB.COLUMN_PAYMENT_CATEGORY)),
                                        description,0);

                                PostTransaction postTransaction = new PostTransaction(
                                        lastID+counter,
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT_TYPE)),
                                        transactionDate.getTimeInMillis(),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_CATEGORY)),
                                        description,0);

                                newTransactions.add(postTransaction);
                            }
                        }
                        updatePayment(
                                cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID)),
                                transactionDate.getTimeInMillis());
                    } else {
                        if (currentDate.get(Calendar.YEAR)>transactionDate.get(Calendar.YEAR)){
                            int missedWeeks = currentDate.getActualMaximum(Calendar.WEEK_OF_YEAR)
                                    -transactionDate.get(Calendar.WEEK_OF_YEAR)
                                    +currentDate.get(Calendar.WEEK_OF_YEAR);
                            String description = cursor.getString(cursor.getColumnIndex(DB.COLUMN_PAYMENT_NAME))
                                    + context.getString(R.string.payments_week_description);
                            for (int i = 0; i < missedWeeks; i++) {
                                transactionDate.add(Calendar.WEEK_OF_YEAR, 1);
                                counter++;
                                addTransactionWithId(
                                        lastID+counter,
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT_TYPE)),
                                        transactionDate.getTimeInMillis(),
                                        cursor.getLong(cursor.getColumnIndex(DB.COLUMN_PAYMENT_CATEGORY)),
                                        description,0);

                                PostTransaction postTransaction = new PostTransaction(
                                        lastID+counter,
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT_TYPE)),
                                        transactionDate.getTimeInMillis(),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_CATEGORY)),
                                        description,0);

                                newTransactions.add(postTransaction);
                            }
                            updatePayment(
                                    cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID)),
                                    transactionDate.getTimeInMillis());
                        }
                    }
                }
                if (cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_TYPE))
                        == PaymentsInputFragment.PAYMENT_TYPE_MONTH){
                    if (currentDate.get(Calendar.MONTH)>transactionDate.get(Calendar.MONTH)){
                        int missedMonths =
                                currentDate.get(Calendar.MONTH)
                                        - transactionDate.get(Calendar.MONTH);
                        String description = cursor.getString(cursor.getColumnIndex(DB.COLUMN_PAYMENT_NAME))
                                + context.getString(R.string.payment_month_description);
                        for (int i = 0; i < missedMonths; i++) {
                            transactionDate.add(Calendar.MONTH, 1);
                            if (transactionDate.get(Calendar.MONTH)
                                    == currentDate.get(Calendar.MONTH)
                                    &&
                                    transactionDate.get(Calendar.DAY_OF_MONTH)
                                            > currentDate.get(Calendar.DAY_OF_MONTH)){
                                transactionDate.add(Calendar.MONTH, -1);
                            } else {
                                counter++;
                                addTransactionWithId(
                                        lastID+counter,
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT_TYPE)),
                                        transactionDate.getTimeInMillis(),
                                        cursor.getLong(cursor.getColumnIndex(DB.COLUMN_PAYMENT_CATEGORY)),
                                        description,0);

                                PostTransaction postTransaction = new PostTransaction(
                                        lastID+counter,
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT_TYPE)),
                                        transactionDate.getTimeInMillis(),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_CATEGORY)),
                                        description,0);

                                newTransactions.add(postTransaction);
                            }
                        }
                        updatePayment(
                                cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID)),
                                transactionDate.getTimeInMillis());
                    } else {
                        if (currentDate.get(Calendar.YEAR)>transactionDate.get(Calendar.YEAR)){
                            int missedMonths = currentDate.getActualMaximum(Calendar.MONTH)
                                    -transactionDate.get(Calendar.MONTH)
                                    +currentDate.get(Calendar.MONTH);
                            String description = cursor.getString(cursor.getColumnIndex(DB.COLUMN_PAYMENT_NAME))
                                    + context.getString(R.string.payment_month_description);
                            for (int i = 0; i < missedMonths; i++) {
                                transactionDate.add(Calendar.MONTH, 1);
                                counter++;
                                addTransactionWithId(
                                        lastID+counter,
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT_TYPE)),
                                        transactionDate.getTimeInMillis(),
                                        cursor.getLong(cursor.getColumnIndex(DB.COLUMN_PAYMENT_CATEGORY)),
                                        description,0);

                                PostTransaction postTransaction = new PostTransaction(
                                        lastID+counter,
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_AMOUNT_TYPE)),
                                        transactionDate.getTimeInMillis(),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_PAYMENT_CATEGORY)),
                                        description,0);

                                newTransactions.add(postTransaction);
                            }
                            updatePayment(
                                    cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID)),
                                    currentDate.getTimeInMillis());
                        }
                    }
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        close();

        return newTransactions;
    }




    /**
     * Inner Class SQLiteOpenHelper
     */

    private class DatabaseOpenHelper extends SQLiteOpenHelper {

        DatabaseOpenHelper(Context context, String name,
                           SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_CATEGORIES);
            db.execSQL(DB_CREATE_MY_TRANSACTIONS);
            db.execSQL(DB_CREATE_PERSON2_TRANSACTIONS);
            db.execSQL(DB_CREATE_MY_BUDGETS);
            db.execSQL(DB_CREATE_PAYMENTS);

            insertCategories(db);


        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        private void insertCategories(SQLiteDatabase db){
            String[] categoryNames = context.getResources().getStringArray(R.array.category_names);

            for (String categoryName : categoryNames) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_CATEGORY_NAME, categoryName);
                contentValues.put(COLUMN_CATEGORY_USE_COUNTER, 0);

                db.insert(TABLE_NAME_CATEGORIES, null, contentValues);
            }
        }




    }
}
