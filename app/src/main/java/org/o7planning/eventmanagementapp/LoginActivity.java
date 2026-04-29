package org.o7planning.eventmanagementapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etLoginPhone, etLoginPass;
    private MaterialButton btnLogin;
    private TextView tvGoToRegister;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        
        // Nếu đã đăng nhập trước đó, vào thẳng MainActivity
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etLoginPhone = findViewById(R.id.etLoginPhone);
        etLoginPass = findViewById(R.id.etLoginPass);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String phone = etLoginPhone.getText().toString().trim();
        String pass = etLoginPass.getText().toString().trim();

        String savedPhone = sharedPreferences.getString("phone", "");
        String savedPass = sharedPreferences.getString("password", "");

        if (phone.equals(savedPhone) && pass.equals(savedPass) && !phone.isEmpty()) {
            sharedPreferences.edit().putBoolean("isLoggedIn", true).apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Số điện thoại hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
        }
    }
}
