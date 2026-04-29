package org.o7planning.eventmanagementapp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.UUID;

@Entity(tableName = "events")
public class Event implements Serializable {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String date;
    private String time;
    private String description;
    private String preparation;
    private int imageResId;
    private String imageUri;
    private String priority;

    public Event(String name, String date, String time, String description, String preparation, int imageResId, String priority) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.date = date;
        this.time = time;
        this.description = description;
        this.preparation = preparation;
        this.imageResId = imageResId;
        this.priority = priority;
    }

    public void setId(@NonNull String id) { this.id = id; }
    @NonNull public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPreparation() { return preparation; }
    public void setPreparation(String preparation) { this.preparation = preparation; }
    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public int getMonth() {
        try {
            String[] parts = date.split("/");
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return -1;
        }
    }
}
