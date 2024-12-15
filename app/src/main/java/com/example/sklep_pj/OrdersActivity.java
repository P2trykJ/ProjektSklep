package com.example.sklep_pj;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class OrdersActivity extends AppCompatActivity {

    private ShopDatabaseHelper dbHelper;
    private ArrayList<String> orders;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        dbHelper = new ShopDatabaseHelper(this);
        ListView ordersList = findViewById(R.id.orders_list);

        String currentUsername = "demoUser";
        orders = new ArrayList<>();

        loadOrders(currentUsername);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orders);
        ordersList.setAdapter(adapter);

        ordersList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedOrder = orders.get(position);
            String[] details = selectedOrder.split("\n");
            String productName = details[1].replace(getString(R.string.product), "").trim();

            boolean removed = dbHelper.deleteOrder(currentUsername, productName);
            if (removed) {
                Toast.makeText(this, getString(R.string.del_order) + productName, Toast.LENGTH_SHORT).show();
                orders.remove(position);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, getString(R.string.del_order_fail), Toast.LENGTH_SHORT).show();
            }

        });

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
    }

    private void loadOrders(String username) {
        Cursor cursor = dbHelper.getOrders(username);
        orders.clear();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String productName = cursor.getString(cursor.getColumnIndex("product"));
                @SuppressLint("Range") double productPrice = cursor.getDouble(cursor.getColumnIndex("price"));
                @SuppressLint("Range") double totalPrice = cursor.getDouble(cursor.getColumnIndex("total_price"));
                @SuppressLint("Range") String orderDate = cursor.getString(cursor.getColumnIndex("order_date"));

                if (totalPrice > 0) {
                    orders.add(getString(R.string.order)+"\n" +
                            productName + "\n" + getString(R.string.price) + productPrice + " PLN\n"+ getString(R.string.total_price) + totalPrice +
                            " PLN\n" + getString(R.string.date) + orderDate);
                } else {
                    orders.add(getString(R.string.product) + productName + "\n" + getString(R.string.price) + productPrice + " PLN\n" + getString(R.string.date) + orderDate);
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) cursor.close();
    }

}
