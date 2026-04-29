package org.o7planning.eventmanagementapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView rvEventsByDate;
    private List<Event> dayEvents;
    private EventAdapter adapter;
    private EventDao eventDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        rvEventsByDate = view.findViewById(R.id.rvEventsByDate);
        eventDao = AppDatabase.getInstance(getContext()).eventDao();

        dayEvents = new ArrayList<>();
        adapter = new EventAdapter(getContext(), dayEvents, event -> {
            Toast.makeText(getContext(), "Sự kiện: " + event.getName(), Toast.LENGTH_SHORT).show();
        });
        
        rvEventsByDate.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEventsByDate.setAdapter(adapter);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            loadEventsForDate(selectedDate);
        });

        // Load today's events initially
        Calendar c = Calendar.getInstance();
        String today = c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.YEAR);
        loadEventsForDate(today);

        return view;
    }

    private void loadEventsForDate(String date) {
        List<Event> events = eventDao.getEventsByDate(date);
        dayEvents.clear();
        if (events != null) {
            dayEvents.addAll(events);
        }
        adapter.notifyDataSetChanged();
    }
}
