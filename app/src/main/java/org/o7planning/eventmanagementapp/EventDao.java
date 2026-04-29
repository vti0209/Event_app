package org.o7planning.eventmanagementapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EventDao {
    @Query("SELECT * FROM events")
    List<Event> getAllEvents();

    @Insert
    void insertEvent(Event event);

    @Update
    void updateEvent(Event event);

    @Delete
    void deleteEvent(Event event);

    @Query("SELECT * FROM events WHERE name LIKE :searchQuery")
    List<Event> searchEvents(String searchQuery);

    @Query("SELECT * FROM events WHERE date = :date")
    List<Event> getEventsByDate(String date);
}