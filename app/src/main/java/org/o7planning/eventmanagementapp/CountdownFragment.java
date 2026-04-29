package org.o7planning.eventmanagementapp;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class CountdownFragment extends Fragment {

    private NumberPicker pickerH, pickerM, pickerS;
    private TextView tvDisplay;
    private Button btnStart, btnReset;
    private LinearLayout layoutPickers;

    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private long timeLeftInMillis;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_countdown, container, false);

        initViews(view);
        setupPickers();

        btnStart.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());

        return view;
    }

    private void initViews(View view) {
        pickerH = view.findViewById(R.id.pickerHours);
        pickerM = view.findViewById(R.id.pickerMinutes);
        pickerS = view.findViewById(R.id.pickerSeconds);
        tvDisplay = view.findViewById(R.id.tvCountdownDisplay);
        btnStart = view.findViewById(R.id.btnStartCountdown);
        btnReset = view.findViewById(R.id.btnResetCountdown);
        layoutPickers = view.findViewById(R.id.layoutPickers);
    }

    private void setupPickers() {
        pickerH.setMinValue(0); pickerH.setMaxValue(23);
        pickerM.setMinValue(0); pickerM.setMaxValue(59);
        pickerS.setMinValue(0); pickerS.setMaxValue(59);
    }

    private void startTimer() {
        if (!isRunning && countDownTimer == null) {
            long hours = pickerH.getValue();
            long minutes = pickerM.getValue();
            long seconds = pickerS.getValue();
            timeLeftInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000;

            if (timeLeftInMillis <= 0) {
                Toast.makeText(getContext(), "Vui lòng chọn thời gian", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        layoutPickers.setVisibility(View.GONE);
        tvDisplay.setVisibility(View.VISIBLE);
        btnReset.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                isRunning = false;
                btnStart.setText("Bắt đầu");
                playAlarmSound();
                Toast.makeText(getContext(), "Hết giờ!", Toast.LENGTH_LONG).show();
            }
        }.start();

        isRunning = true;
        btnStart.setText("Tạm dừng");
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        btnStart.setText("Tiếp tục");
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        isRunning = false;
        timeLeftInMillis = 0;
        btnStart.setText("Bắt đầu");
        btnReset.setVisibility(View.GONE);
        tvDisplay.setVisibility(View.GONE);
        layoutPickers.setVisibility(View.VISIBLE);
    }

    private void updateCountDownText() {
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        tvDisplay.setText(timeLeftFormatted);
    }

    private void playAlarmSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (notification == null) {
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
            Ringtone r = RingtoneManager.getRingtone(getContext(), notification);
            r.play();
            
            // Tự động dừng chuông sau 5 giây
            new Handler(Looper.getMainLooper()).postDelayed(r::stop, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
