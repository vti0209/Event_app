package org.o7planning.eventmanagementapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String PREF_NAME = "EventPrefs";
    private static final String KEY_EVENTS = "events_list";

    public static void saveEvents(Context context, List<Event> events) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(events);
        editor.putString(KEY_EVENTS, json);
        editor.apply();
    }

    public static List<Event> loadEvents(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_EVENTS, null);
        
        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Event>>() {}.getType();
        List<Event> events = gson.fromJson(json, type);
        
        return events != null ? events : new ArrayList<>();
    }
}