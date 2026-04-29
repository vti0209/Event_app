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
    private TextView tvGoToRegister, tvClearData;
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
        tvClearData = findViewById(R.id.tvClearData); // Thêm nút xóa sạch dữ liệu nếu cần
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        
        if (tvClearData != null) {
            tvClearData.setOnClickListener(v -> clearAllData());
        }
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

    private void clearAllData() {
        // 1. Xóa SharedPreferences (Thông tin User)
        sharedPreferences.edit().clear().apply();

        // 2. Xóa Database (Sự kiện và Thông báo)
        new Thread(() -> {
            AppDatabase.getInstance(this).clearAllTables();
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã dọn sạch toàn bộ dữ liệu dự án!", Toast.LENGTH_LONG).show();
                // Khởi động lại ứng dụng
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }).start();
    }
}
