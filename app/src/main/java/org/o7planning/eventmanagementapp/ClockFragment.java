package org.o7planning.eventmanagementapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ClockFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clock, container, false);

        tabLayout = view.findViewById(R.id.clockTabLayout);
        viewPager = view.findViewById(R.id.clockViewPager);

        setupViewPager();

        return view;
    }

    private void setupViewPager() {
        // Sử dụng getChildFragmentManager() thay vì 'this' để quản lý fragment con tốt hơn trong ViewPager2
        viewPager.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return new AlarmFragment();
                    case 1: return new CountdownFragment();
                    case 2: return new StopwatchFragment();
                    default: return new AlarmFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText(getString(R.string.tab_alarm)); break;
                case 1: tab.setText(getString(R.string.tab_countdown)); break;
                case 2: tab.setText(getString(R.string.tab_stopwatch)); break;
            }
        }).attach();
    }
}
