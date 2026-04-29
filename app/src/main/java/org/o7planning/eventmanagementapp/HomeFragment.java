package org.o7planning.eventmanagementapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvUpcomingCount, tvPastCount, tvTotalCount;
    private ImageView ivHomeProfilePic;
    private View btnCalendar;
    private RecyclerView rvUpcomingEvents;
    private EventAdapter upcomingAdapter;
    private final List<Event> upcomingEventsList = new ArrayList<>();
    
    private EventDao eventDao;
    private SharedPreferences sharedPreferences;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        
        eventDao = AppDatabase.getInstance(getContext()).eventDao();
        if (getContext() != null) {
            sharedPreferences = getContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        }

        setupRecyclerView();
        loadProfileInfo();
        updateHomeData();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvTotalCount = view.findViewById(R.id.tvTotalCount);
        tvUpcomingCount = view.findViewById(R.id.tvUpcomingCount);
        tvPastCount = view.findViewById(R.id.tvPastCount);
        ivHomeProfilePic = view.findViewById(R.id.ivHomeProfilePic);
        btnCalendar = view.findViewById(R.id.btnCalendarWidget);
        rvUpcomingEvents = view.findViewById(R.id.rvUpcomingEvents);
    }

    private void setupRecyclerView() {
        upcomingAdapter = new EventAdapter(getContext(), upcomingEventsList, event -> 
            Toast.makeText(getContext(), "Sự kiện: " + event.getName(), Toast.LENGTH_SHORT).show()
        );
        rvUpcomingEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUpcomingEvents.setAdapter(upcomingAdapter);
    }

    private void setupListeners() {
        if (btnCalendar != null) {
            btnCalendar.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToFragment(new CalendarFragment());
                }
            });
        }
    }

    private void loadProfileInfo() {
        if (sharedPreferences == null) return;
        String name = sharedPreferences.getString("name", "Bạn");
        String imageUriString = sharedPreferences.getString("profileImage", null);

        tvGreeting.setText(String.format("Xin chào, %s!", name));
        if (ivHomeProfilePic != null) {
            if (imageUriString != null) {
                ivHomeProfilePic.setImageURI(Uri.parse(imageUriString));
            } else {
                ivHomeProfilePic.setImageResource(android.R.drawable.ic_menu_myplaces);
            }
        }
    }

    private void updateHomeData() {
        List<Event> allEvents = eventDao.getAllEvents();
        long upcomingCount = 0;
        long pastCount = 0;
        
        upcomingEventsList.clear();
        Date now = new Date();
        
        // Tính mốc 24h tới
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.HOUR_OF_DAY, 24);
        Date next24Hours = cal.getTime();

        for (Event event : allEvents) {
            try {
                Date eventDate = dateTimeFormat.parse(event.getDate() + " " + event.getTime());
                if (eventDate != null) {
                    if (eventDate.before(now)) {
                        pastCount++;
                    } else {
                        upcomingCount++;
                        // Chỉ thêm vào danh sách hiển thị nếu diễn ra trong 24h tới
                        if (eventDate.before(next24Hours)) {
                            upcomingEventsList.add(event);
                        }
                    }
                }
            } catch (Exception e) {
                // Nếu không parse được, mặc định tính là sắp tới nhưng không hiện ở list 24h
                upcomingCount++;
            }
        }

        if (tvTotalCount != null) {
            tvTotalCount.setText(String.valueOf(allEvents.size()));
        }
        if (tvUpcomingCount != null) {
            tvUpcomingCount.setText(String.valueOf(upcomingCount));
        }
        if (tvPastCount != null) {
            tvPastCount.setText(String.valueOf(pastCount));
        }
        
        upcomingEventsList.sort((e1, e2) -> {
            try {
                Date d1 = dateTimeFormat.parse(e1.getDate() + " " + e1.getTime());
                Date d2 = dateTimeFormat.parse(e2.getDate() + " " + e2.getTime());
                if (d1 != null && d2 != null) {
                    return d1.compareTo(d2);
                }
            } catch (Exception ignored) {}
            return 0;
        });

        upcomingAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileInfo();
        updateHomeData();
    }
}
