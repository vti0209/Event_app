package org.o7planning.eventmanagementapp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Entity(tableName = "app_notifications")
public class AppNotification {
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String message;
    private String timestamp;
    private boolean isArchived = false;

    public AppNotification(String title, String message) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.message = message;
        this.timestamp = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }
}
