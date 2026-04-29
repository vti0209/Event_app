package org.o7planning.eventmanagementapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AppNotificationDao {
    @Query("SELECT * FROM app_notifications WHERE isArchived = 0 ORDER BY timestamp DESC")
    List<AppNotification> getAllNotifications();

    @Insert
    void insert(AppNotification notification);

    @Update
    void update(AppNotification notification);

    @Delete
    void delete(AppNotification notification);

    @Query("DELETE FROM app_notifications")
    void deleteAll();
}
