package org.o7planning.eventmanagementapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<Alarm> alarmList;
    private OnAlarmChangeListener listener;

    public interface OnAlarmChangeListener {
        void onToggle(Alarm alarm, boolean isEnabled);
        void onDelete(Alarm alarm);
        void onClick(Alarm alarm);
    }

    public AlarmAdapter(List<Alarm> alarmList, OnAlarmChangeListener listener) {
        this.alarmList = alarmList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarmList.get(position);
        holder.tvTime.setText(alarm.getTimeFormatted());
        holder.tvLabel.setText(alarm.getLabel());
        
        // Tránh trigger listener khi bind lại data
        holder.switchAlarm.setOnCheckedChangeListener(null);
        holder.switchAlarm.setChecked(alarm.isEnabled());

        holder.switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggle(alarm, isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(alarm);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDelete(alarm);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public void updateList(List<Alarm> newList) {
        this.alarmList = newList;
        notifyDataSetChanged();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvLabel;
        SwitchMaterial switchAlarm;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvAlarmTime);
            tvLabel = itemView.findViewById(R.id.tvAlarmLabel);
            switchAlarm = itemView.findViewById(R.id.switchAlarm);
        }
    }
}
