package com.example.sklep_pj;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);

        EditText usernameField = findViewById(R.id.registerUsername);
        EditText passwordField = findViewById(R.id.registerPassword);
        Button registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.no_all_text), Toast.LENGTH_SHORT).show();
            } else {
                boolean success = db.addUser(username, password);
                if (success) {
                    Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.register_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
