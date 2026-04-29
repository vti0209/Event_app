package org.o7planning.eventmanagementapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;

public class AlarmFragment extends Fragment {

    private RecyclerView rvAlarms;
    private AlarmAdapter adapter;
    private AlarmDao alarmDao;
    private List<Alarm> alarmList;
    private AlarmManager alarmManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        alarmDao = AppDatabase.getInstance(getContext()).alarmDao();
        alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        
        rvAlarms = view.findViewById(R.id.rvAlarms);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddAlarm);

        setupRecyclerView();

        fabAdd.setOnClickListener(v -> showTimePicker(null));

        return view;
    }

    private void setupRecyclerView() {
        alarmList = alarmDao.getAllAlarms();
        adapter = new AlarmAdapter(alarmList, new AlarmAdapter.OnAlarmChangeListener() {
            @Override
            public void onToggle(Alarm alarm, boolean isEnabled) {
                alarm.setEnabled(isEnabled);
                alarmDao.update(alarm);
                if (isEnabled) {
                    scheduleAlarm(alarm);
                    Toast.makeText(getContext(), "Đã bật báo thức " + alarm.getTimeFormatted(), Toast.LENGTH_SHORT).show();
                } else {
                    cancelAlarm(alarm);
                    Toast.makeText(getContext(), "Đã tắt báo thức", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDelete(Alarm alarm) {
                showDeleteConfirmDialog(alarm);
            }

            @Override
            public void onClick(Alarm alarm) {
                showTimePicker(alarm);
            }
        });
        rvAlarms.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAlarms.setAdapter(adapter);
    }

    private void showTimePicker(@Nullable Alarm existingAlarm) {
        Calendar c = Calendar.getInstance();
        int hour = existingAlarm != null ? existingAlarm.getHour() : c.get(Calendar.HOUR_OF_DAY);
        int minute = existingAlarm != null ? existingAlarm.getMinute() : c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minuteOfHour) -> {
                    if (existingAlarm != null) {
                        // Sửa báo thức
                        cancelAlarm(existingAlarm);
                        existingAlarm.setHour(hourOfDay);
                        existingAlarm.setMinute(minuteOfHour);
                        existingAlarm.setEnabled(true);
                        alarmDao.update(existingAlarm);
                        scheduleAlarm(existingAlarm);
                        Toast.makeText(getContext(), "Đã cập nhật báo thức", Toast.LENGTH_SHORT).show();
                    } else {
                        // Thêm mới
                        Alarm newAlarm = new Alarm(hourOfDay, minuteOfHour, true, "Báo thức");
                        long id = alarmDao.insert(newAlarm);
                        newAlarm.setId((int) id);
                        scheduleAlarm(newAlarm);
                        Toast.makeText(getContext(), "Đã thêm báo thức lúc " + newAlarm.getTimeFormatted(), Toast.LENGTH_SHORT).show();
                    }
                    refreshAlarms();
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void showDeleteConfirmDialog(Alarm alarm) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa báo thức")
                .setMessage("Bạn có chắc chắn muốn xóa báo thức này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    cancelAlarm(alarm);
                    alarmDao.delete(alarm);
                    refreshAlarms();
                    Toast.makeText(getContext(), "Đã xóa báo thức", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void scheduleAlarm(Alarm alarm) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(getContext(), ClockAlarmReceiver.class);
        intent.putExtra("ALARM_LABEL", alarm.getLabel());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), alarm.getId(), intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void cancelAlarm(Alarm alarm) {
        Intent intent = new Intent(getContext(), ClockAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), alarm.getId(), intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    private void refreshAlarms() {
        alarmList = alarmDao.getAllAlarms();
        adapter.updateList(alarmList);
    }
}
