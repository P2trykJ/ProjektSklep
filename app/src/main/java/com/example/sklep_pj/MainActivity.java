package com.example.sklep_pj;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private ShopDatabaseHelper shopDatabaseHelper;
    private String currentUsername = "demoUser";
    private double totalOrderPrice = 0.0;

    private final String[][] PRODUCTS = {
            {"Komputer Gamingowy", "Komputer o wysokiej wydajności do gier", "3500.00", "drawable/komputer"},
            {"Klawiatura Mechaniczna", "Podświetlana klawiatura mechaniczna", "300.00", "drawable/klawiatura"},
            {"Mysz Gamingowa", "Ergonomiczna mysz z regulacją DPI", "150.00", "drawable/mysz"},
            {"Monitor 4K", "Monitor o rozdzielczości 4K UHD", "1200.00", "drawable/monitor"},
            {"Kamera Internetowa", "Kamera Full HD do wideorozmów", "200.00", "drawable/kamera"}
    };


    private EditText firstNameField, lastNameField, phoneNumberField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            setContentView(R.layout.activity_main);

            Button logoutButton = findViewById(R.id.logoutButton);
            logoutButton.setOnClickListener(v -> logout());

            firstNameField = findViewById(R.id.firstName);
            lastNameField = findViewById(R.id.lastName);
            phoneNumberField = findViewById(R.id.phoneNumber);

            loadUserDetails();

            firstNameField.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) saveUserDetails();
            });

            lastNameField.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) saveUserDetails();
            });

            phoneNumberField.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) saveUserDetails();
            });
        }

        shopDatabaseHelper = new ShopDatabaseHelper(this);

        ListView productListView = findViewById(R.id.product_list);
        Button viewOrdersButton = findViewById(R.id.view_orders);

        ArrayList<String> productNames = new ArrayList<>();
        for (String[] product : PRODUCTS) {
            productNames.add(product[0] + " - " + product[2] + " PLN");
        }

        ProductAdapter adapter = new ProductAdapter(this, PRODUCTS);
        productListView.setAdapter(adapter);


        productListView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String productName = PRODUCTS[position][0];
            double productPrice = Double.parseDouble(PRODUCTS[position][2]);
            String imagePath = PRODUCTS[position][3];  // Get image path for the selected product

            Log.d("baza", "Wybrano produkt: " + productName + ", Cena: " + productPrice);

            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            boolean added = shopDatabaseHelper.addOrder(currentUsername, productName, productPrice, productPrice, currentDate, imagePath);

            if (added) {
                totalOrderPrice += productPrice;
                Toast.makeText(this, getString(R.string.add_to_order) + productName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.order_error_add), Toast.LENGTH_SHORT).show();
            }
        });



        viewOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OrdersActivity.class);
            startActivity(intent);
        });

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void saveUserDetails() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("firstName", firstNameField.getText().toString());
        editor.putString("lastName", lastNameField.getText().toString());
        editor.putString("phoneNumber", phoneNumberField.getText().toString());
        editor.apply();
    }

    private void loadUserDetails() {
        String firstName = sharedPreferences.getString("firstName", "");
        String lastName = sharedPreferences.getString("lastName", "");
        String phoneNumber = sharedPreferences.getString("phoneNumber", "");
        firstNameField.setText(firstName);
        lastNameField.setText(lastName);
        phoneNumberField.setText(phoneNumber);
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_orders:
                startActivity(new Intent(this, OrdersActivity.class));
                return true;

            case R.id.menu_send_sms:
                sendOrderViaSms();
                return true;

            case R.id.menu_author_info:
                showAboutDialog();
                return true;

            case R.id.menu_share_cart:
                shareOrderDetails();
                return true;
            case R.id.menu_save_cart:
                saveOrder();
                return true;
            case R.id.menu_language:
                    showLanguageDialog();
                    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void saveOrder() {
        String firstName = firstNameField.getText().toString().trim();
        String lastName = lastNameField.getText().toString().trim();
        String phoneNumber = phoneNumberField.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, getString(R.string.first_name_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean orderSaved = shopDatabaseHelper.saveOrder(currentUsername);
        if (orderSaved) {
            Toast.makeText(this, getString(R.string.order_save), Toast.LENGTH_SHORT).show();
            totalOrderPrice = 0.0;
            saveUserDetails();

            shopDatabaseHelper.clearCartForUser(currentUsername);
        } else {
            Toast.makeText(this, getString(R.string.order_error), Toast.LENGTH_SHORT).show();
        }
    }
    private void sendOrderViaSms() {
        String firstName = firstNameField.getText().toString().trim();
        String lastName = lastNameField.getText().toString().trim();
        String phoneNumber = phoneNumberField.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, getString(R.string.first_name_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = firstName + " " + lastName;

        Cursor ordersCursor = shopDatabaseHelper.getOrders(currentUsername);
        if (ordersCursor == null || !ordersCursor.moveToFirst()) {
            Toast.makeText(this, getString(R.string.no_orders), Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder smsContent = new StringBuilder();
        smsContent.append(getString(R.string.order_first_name)).append(fullName).append(":\n");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        double totalPrice = 0.0;
        do {
            @SuppressLint("Range") String productName = ordersCursor.getString(ordersCursor.getColumnIndex("product"));
            @SuppressLint("Range") double price = ordersCursor.getDouble(ordersCursor.getColumnIndex("price"));

            if (productName != null && !productName.isEmpty() && price > 0) {
                smsContent.append("- ").append(productName).append(", " + getString(R.string.price)).append(price).append(" PLN\n");
                totalPrice += price;
            }
        } while (ordersCursor.moveToNext());

        smsContent.append(getString(R.string.total_price)).append(totalPrice).append(" PLN\n");
        smsContent.append(getString(R.string.date)).append(currentDate);

        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
        smsIntent.putExtra("sms_body", smsContent.toString());

        try {
            startActivity(smsIntent);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.sms_send_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareOrderDetails() {
        String firstName = firstNameField.getText().toString().trim();
        String lastName = lastNameField.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, getString(R.string.first_name_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = firstName + " " + lastName;

        Cursor ordersCursor = shopDatabaseHelper.getOrders(currentUsername);
        if (ordersCursor == null || !ordersCursor.moveToFirst()) {
            Toast.makeText(this, getString(R.string.no_orders_share), Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder shareContent = new StringBuilder();
        shareContent.append(getString(R.string.order_first_name)).append(fullName).append(":\n");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        double totalPrice = 0.0;
        do {
            @SuppressLint("Range") String productName = ordersCursor.getString(ordersCursor.getColumnIndex("product"));
            @SuppressLint("Range") double price = ordersCursor.getDouble(ordersCursor.getColumnIndex("price"));

            if (productName != null && !productName.isEmpty() && price > 0) {
                shareContent.append("- ").append(productName).append(", " + getString(R.string.price)).append(price).append(" PLN\n");
                totalPrice += price;
            }
        } while (ordersCursor.moveToNext());

        shareContent.append(getString(R.string.total_price)).append(totalPrice).append(" PLN\n");
        shareContent.append(getString(R.string.date)).append(currentDate);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Moje zamówienie w Sklep PJ");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent.toString());

        try {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.share_error), Toast.LENGTH_SHORT).show();
        }
    }


    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about_author));
        builder.setMessage(getString(R.string.app_name_author)+"\n"+ getString(R.string.author)+" Patryk Jarosiewicz\n\n" +
                getString(R.string.function)+"\n- "+getString(R.string.do_order)+"\n- "+getString(R.string.sendViaSms)+"\n- "+getString(R.string.sharing_orders)+"\n" +
                "- " + getString(R.string.orders_history));
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    public void changeLanguage(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        recreate();
    }
    private void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_lang));
        builder.setItems(new String[] {"Polski", "English"}, (dialog, which) -> {
            if (which == 0) {
                changeLanguage("pl");
            } else {
                changeLanguage("en");
            }
        });
        builder.show();
    }
}
