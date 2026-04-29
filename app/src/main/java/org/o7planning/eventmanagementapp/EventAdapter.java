package org.o7planning.eventmanagementapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private Context context;
    private List<Event> events;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public EventAdapter(Context context, List<Event> events, OnItemClickListener listener) {
        this.context = context;
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEvent;
        TextView tvName, tvDate, tvTime;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEvent = itemView.findViewById(R.id.ivEvent);
            tvName = itemView.findViewById(R.id.tvName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(final Event event, final OnItemClickListener listener) {
            if (event.getImageUri() != null) {
                ivEvent.setImageURI(Uri.parse(event.getImageUri()));
            } else {
                ivEvent.setImageResource(event.getImageResId() != 0 ? event.getImageResId() : android.R.drawable.ic_menu_gallery);
            }

            tvName.setText(event.getName());
            tvDate.setText("Ngày: " + event.getDate());
            tvTime.setText("Giờ: " + event.getTime());

            itemView.setOnClickListener(v -> listener.onItemClick(event));
        }
    }
}
