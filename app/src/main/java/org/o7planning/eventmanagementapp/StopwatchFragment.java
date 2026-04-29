package org.o7planning.eventmanagementapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class StopwatchFragment extends Fragment {

    private TextView tvDisplay;
    private Button btnStart, btnReset;
    
    private int milliseconds = 0;
    private boolean running = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        
        tvDisplay = view.findViewById(R.id.tvStopwatchDisplay);
        btnStart = view.findViewById(R.id.btnStartStopwatch);
        btnReset = view.findViewById(R.id.btnResetStopwatch);
        
        btnStart.setOnClickListener(v -> {
            running = !running;
            btnStart.setText(running ? "Tạm dừng" : "Bắt đầu");
        });
        
        btnReset.setOnClickListener(v -> {
            running = false;
            milliseconds = 0;
            btnStart.setText("Bắt đầu");
            updateDisplay();
        });
        
        runTimer();
        
        return view;
    }

    private void runTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (running) {
                    milliseconds += 10;
                    updateDisplay();
                }
                handler.postDelayed(this, 10);
            }
        });
    }

    private void updateDisplay() {
        int secs = milliseconds / 1000;
        int mins = secs / 60;
        int hours = mins / 60;
        int ms = (milliseconds % 1000) / 10;

        String time = String.format(Locale.getDefault(), 
                "%02d:%02d:%02d.%02d", hours, mins % 60, secs % 60, ms);
        tvDisplay.setText(time);
    }
}
