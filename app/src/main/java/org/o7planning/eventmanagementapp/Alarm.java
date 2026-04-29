package org.o7planning.eventmanagementapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms")
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int hour;
    private int minute;
    private boolean isEnabled;
    private String label;

    public Alarm(int hour, int minute, boolean isEnabled, String label) {
        this.hour = hour;
        this.minute = minute;
        this.isEnabled = isEnabled;
        this.label = label;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }
    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getTimeFormatted() {
        return String.format("%02d:%02d", hour, minute);
    }
}
