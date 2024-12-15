package com.example.sklep_pj;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShopDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shop.db"; // baza danych dla zamówień
    private static final int DATABASE_VERSION = 3; // Zwiększymy wersję bazy danych

    private static final String TABLE_ORDERS = "orders";
    private static final String COLUMN_ORDER_ID = "order_id";
    private static final String COLUMN_USER = "username";
    private static final String COLUMN_PRODUCT = "product";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_TOTAL_PRICE = "total_price";
    private static final String COLUMN_DATE = "order_date";

    public ShopDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tworzymy tabelę zamówień
        String createOrdersTable = "CREATE TABLE " + TABLE_ORDERS + " (" +
                COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER + " TEXT, " +
                COLUMN_PRODUCT + " TEXT, " +
                COLUMN_PRICE + " REAL, " +
                COLUMN_TOTAL_PRICE + " REAL, " +
                COLUMN_DATE + " TEXT," +
                "image_path TEXT)";
        db.execSQL(createOrdersTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        onCreate(db);
    }

    public boolean addOrder(String username, String product, double price, double totalPrice, String orderDate, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        Log.d("baza", "Dodawanie do bazy danych: Produkt: " + product + ", Cena: " + price);

        values.put(COLUMN_USER, username);
        values.put(COLUMN_PRODUCT, product);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_TOTAL_PRICE, totalPrice);
        values.put(COLUMN_DATE, orderDate);
        values.put("image_path", imagePath);  // Store image path in the database

        long result = db.insert(TABLE_ORDERS, null, values);
        db.close();

        Log.d("DEBUG", "Wynik dodania: " + result);

        return result != -1;
    }




    public Cursor getOrders(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " WHERE " + COLUMN_USER + " = ?", new String[]{username});

        if (cursor != null && cursor.moveToFirst()) {
            Log.d("baza", "Znaleziono zamówienia: " + cursor.getCount());
        }

        return cursor;
    }


    public boolean deleteOrder(String username, String productName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_ORDERS, "username = ? AND product = ?", new String[]{username, productName});
        return rowsDeleted > 0;
    }
    public void clearCartForUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ORDERS, "username = ? AND total_price=0", new String[]{username});
        db.close();
    }
    public boolean saveOrder(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USER, username);
        values.put(COLUMN_TOTAL_PRICE, 0.0);
        values.put(COLUMN_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        long result = db.insert(TABLE_ORDERS, null, values);
        db.close();

        return result != -1;
    }
}

