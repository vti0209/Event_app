package org.o7planning.eventmanagementapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationsFragment extends Fragment {
    
    private RecyclerView rvNotifications;
    private View emptyView;
    private AppNotificationDao notificationDao;
    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        
        rvNotifications = view.findViewById(R.id.rvNotifications);
        emptyView = view.findViewById(R.id.layoutEmptyNotif);
        
        notificationDao = AppDatabase.getInstance(getContext()).notificationDao();
        
        setupRecyclerView();
        loadNotifications();
        
        return view;
    }

    private void setupRecyclerView() {
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                AppNotification notification = adapter.getNotificationAt(position);

                if (direction == ItemTouchHelper.LEFT) {
                    // Xóa (Swipe Left)
                    new Thread(() -> {
                        notificationDao.delete(notification);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                adapter.removeAt(position);
                                Toast.makeText(getContext(), "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
                                checkEmpty();
                            });
                        }
                    }).start();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Lưu trữ và ẩn (Swipe Right)
                    new Thread(() -> {
                        notification.setArchived(true);
                        notificationDao.update(notification);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                adapter.removeAt(position);
                                Toast.makeText(getContext(), "Đã lưu trữ thông báo", Toast.LENGTH_SHORT).show();
                                checkEmpty();
                            });
                        }
                    }).start();
                }
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvNotifications);
    }

    private void loadNotifications() {
        new Thread(() -> {
            List<AppNotification> notifications = notificationDao.getAllNotifications();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (notifications == null || notifications.isEmpty()) {
                        showEmpty(true);
                    } else {
                        showEmpty(false);
                        adapter = new NotificationAdapter(notifications);
                        rvNotifications.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }

    private void checkEmpty() {
        if (adapter.getItemCount() == 0) {
            showEmpty(true);
        }
    }

    private void showEmpty(boolean isEmpty) {
        if (emptyView != null) emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (rvNotifications != null) rvNotifications.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
