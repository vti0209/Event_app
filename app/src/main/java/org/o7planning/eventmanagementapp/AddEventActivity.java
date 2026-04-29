package org.o7planning.eventmanagementapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    private EditText etEventName, etDescription, etPreparation;
    private RadioGroup rgPriority;
    private MaterialButton btnPickDate, btnPickTime, btnSave;
    private TextView tvSelectedDateTime;
    private ImageView ivEventImage;
    private Uri selectedImageUri;

    private int mYear, mMonth, mDay, mHour, mMinute;
    private String selectedDate = "";
    private String selectedTime = "";
    private Event editingEvent = null;
    private AppNotificationDao notificationDao;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    ivEventImage.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        initViews();
        notificationDao = AppDatabase.getInstance(this).notificationDao();

        Intent intent = getIntent();
        if (intent.hasExtra("EVENT_DATA")) {
            editingEvent = (Event) intent.getSerializableExtra("EVENT_DATA");
            fillDataForEdit(editingEvent);
            btnSave.setText("CẬP NHẬT THÔNG TIN");
        }

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        setupListeners();
    }

    private void initViews() {
        etEventName = findViewById(R.id.etEventName);
        etDescription = findViewById(R.id.etDescription);
        etPreparation = findViewById(R.id.etPreparation);
        rgPriority = findViewById(R.id.rgPriority);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSave = findViewById(R.id.btnSave);
        tvSelectedDateTime = findViewById(R.id.tvSelectedDateTime);
        ivEventImage = findViewById(R.id.ivEventImage);
    }

    private void setupListeners() {
        findViewById(R.id.cardEventImage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnPickDate.setOnClickListener(v -> new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate = day + "/" + (month + 1) + "/" + year;
            mYear = year; mMonth = month; mDay = day;
            updateDateTimeDisplay();
        }, mYear, mMonth, mDay).show());

        btnPickTime.setOnClickListener(v -> new TimePickerDialog(this, (view, hour, minute) -> {
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            mHour = hour; mMinute = minute;
            updateDateTimeDisplay();
        }, mHour, mMinute, true).show());

        btnSave.setOnClickListener(v -> saveEvent());
    }

    private void fillDataForEdit(Event event) {
        etEventName.setText(event.getName());
        etDescription.setText(event.getDescription());
        etPreparation.setText(event.getPreparation());
        selectedDate = event.getDate();
        selectedTime = event.getTime();
        updateDateTimeDisplay();

        if (event.getImageUri() != null) {
            selectedImageUri = Uri.parse(event.getImageUri());
            ivEventImage.setImageURI(selectedImageUri);
        }

        if ("High".equals(event.getPriority())) ((RadioButton)findViewById(R.id.rbHigh)).setChecked(true);
        else if ("Low".equals(event.getPriority())) ((RadioButton)findViewById(R.id.rbLow)).setChecked(true);
        else ((RadioButton)findViewById(R.id.rbMedium)).setChecked(true);
    }

    private void saveEvent() {
        String name = etEventName.getText().toString().trim();
        String preparation = etPreparation.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên sự kiện", Toast.LENGTH_SHORT).show();
            return;
        }

        if (preparation.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập phần cần chuẩn bị", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        String priority = "Medium";
        int checkedId = rgPriority.getCheckedRadioButtonId();
        if (checkedId == R.id.rbHigh) priority = "High";
        else if (checkedId == R.id.rbLow) priority = "Low";

        Event resultEvent = editingEvent != null ? editingEvent : new Event(name, "", "", "", "", 0, "");
        resultEvent.setName(name);
        resultEvent.setDate(selectedDate);
        resultEvent.setTime(selectedTime);
        resultEvent.setDescription(etDescription.getText().toString());
        resultEvent.setPreparation(preparation);
        resultEvent.setPriority(priority);

        if (selectedImageUri != null) resultEvent.setImageUri(selectedImageUri.toString());

        // 1. Lưu vào Database thông báo
        String notifTitle = (editingEvent != null) ? "Đã cập nhật sự kiện" : "Sự kiện mới";
        String notifMsg = "Sự kiện: " + name + " lúc " + resultEvent.getTime() + " ngày " + resultEvent.getDate();
        notificationDao.insert(new AppNotification(notifTitle, notifMsg));

        // 2. Đẩy thông báo lên hệ thống Android ngay lập tức
        sendInstantNotification(notifTitle, notifMsg);

        // 3. Lên lịch thông báo nhắc nhở trước 10 phút
        scheduleNotification(resultEvent);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("UPDATED_EVENT", resultEvent);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void sendInstantNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "EVENT_ADD_CHANNEL";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Event Management", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void scheduleNotification(Event event) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(mYear, mMonth, mDay, mHour, mMinute);
            calendar.set(Calendar.SECOND, 0);
            
            // Nhắc trước 10 phút
            long triggerTime = calendar.getTimeInMillis() - (10 * 60 * 1000);

            if (triggerTime > System.currentTimeMillis()) {
                Intent intent = new Intent(this, AlarmReceiver.class);
                intent.putExtra("EVENT_NAME", event.getName());
                intent.putExtra("EVENT_TIME", event.getTime());
                
                int requestCode = event.getId().hashCode();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, 
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateDateTimeDisplay() {
        tvSelectedDateTime.setText("Thời gian: " + selectedDate + " " + selectedTime);
    }
}
