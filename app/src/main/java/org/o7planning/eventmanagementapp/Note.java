package org.o7planning.eventmanagementapp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey
    @NonNull
    private String id;
    private String content;
    private String timestamp;
    private int color;

    public Note(String content, int color) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.color = color;
        this.timestamp = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
}
