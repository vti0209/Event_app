package org.o7planning.eventmanagementapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.AppBarLayout;

public class MainActivity extends AppCompatActivity {

    private int currentNavId = R.id.nav_home;
    private Fragment currentFragment;
    private AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appBarLayout = findViewById(R.id.appBarLayout);
        setupToolbar();
        setupNavigation(savedInstanceState);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }
                @Override
                public boolean onQueryTextChange(String newText) {
                    if (currentFragment instanceof EventsFragment) {
                        ((EventsFragment) currentFragment).handleSearch(newText);
                    }
                    return true;
                }
            });
        }
        
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        MenuItem filterItem = menu.findItem(R.id.menu_filter);
        MenuItem addItem = menu.findItem(R.id.menu_add);
        MenuItem notificationItem = menu.findItem(R.id.menu_notification);

        boolean isEventsTab = (currentNavId == R.id.nav_events);
        boolean isProfileTab = (currentNavId == R.id.nav_profile);
        
        if (searchItem != null) searchItem.setVisible(isEventsTab);
        if (filterItem != null) filterItem.setVisible(isEventsTab);
        if (addItem != null) addItem.setVisible(isEventsTab);
        
        // Ẩn icon thông báo ở trang Profile
        if (notificationItem != null) notificationItem.setVisible(!isProfileTab);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_notification) {
            navigateToFragment(new NotificationsFragment());
            return true;
        } else if (id == R.id.menu_add) {
            if (currentFragment instanceof EventsFragment) {
                ((EventsFragment) currentFragment).handleAddEvent();
            }
            return true;
        } else if (id == R.id.menu_filter) {
            if (currentFragment instanceof EventsFragment) {
                ((EventsFragment) currentFragment).handleFilter();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupNavigation(Bundle savedInstanceState) {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            currentNavId = R.id.nav_home;
            currentFragment = new HomeFragment();
            loadFragment(currentFragment);
            bottomNav.setSelectedItemId(R.id.nav_home);
            updateToolbarVisibility(currentNavId);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            currentNavId = item.getItemId();

            if (currentNavId == R.id.nav_home) currentFragment = new HomeFragment();
            else if (currentNavId == R.id.nav_events) currentFragment = new EventsFragment();
            else if (currentNavId == R.id.nav_notes) currentFragment = new NotesFragment();
            else if (currentNavId == R.id.nav_clock) currentFragment = new ClockFragment();
            else if (currentNavId == R.id.nav_profile) currentFragment = new ProfileFragment();

            if (currentFragment != null) {
                loadFragment(currentFragment);
                updateToolbarVisibility(currentNavId);
                invalidateOptionsMenu();
                return true;
            }
            return false;
        });
    }

    private void updateToolbarVisibility(int navId) {
        if (appBarLayout != null) {
            if (navId == R.id.nav_profile) {
                appBarLayout.setVisibility(View.GONE);
            } else {
                appBarLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void navigateToFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
