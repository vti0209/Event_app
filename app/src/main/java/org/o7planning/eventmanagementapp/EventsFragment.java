package org.o7planning.eventmanagementapp;

import android.graphics.Color;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class EventsFragment extends Fragment {

    private static final int REQUEST_CODE_ADD = 100;
    private static final int REQUEST_CODE_EDIT = 101;

    private RecyclerView rvEvents;
    private TextView tvResultsHeader, tvEmptyMessage;
    private View layoutEmptyResults;
    private List<Event> eventList;
    private List<Event> filteredList;
    private EventAdapter adapter;
    private EventDao eventDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        // Bỏ setHasOptionsMenu(true) vì MainActivity đã quản lý Menu chung cho toàn app
        // setHasOptionsMenu(true);

        rvEvents = view.findViewById(R.id.rvEvents);
        tvResultsHeader = view.findViewById(R.id.tvResultsHeader);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        layoutEmptyResults = view.findViewById(R.id.layoutEmptyResults);
        
        eventDao = AppDatabase.getInstance(getContext()).eventDao();

        loadDataFromDatabase();

        adapter = new EventAdapter(getContext(), filteredList, this::showEventDetail);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEvents.setAdapter(adapter);

        updateHeaderText("Tất cả sự kiện");

        return view;
    }

    private void loadDataFromDatabase() {
        eventList = eventDao.getAllEvents();
        if (eventList == null) eventList = new ArrayList<>();
        filteredList = new ArrayList<>(eventList);
    }

    private void updateHeaderText(String text) {
        if (tvResultsHeader != null) {
            tvResultsHeader.setText(text);
        }
        
        if (filteredList.isEmpty()) {
            layoutEmptyResults.setVisibility(View.VISIBLE);
            rvEvents.setVisibility(View.GONE);
            if (tvEmptyMessage != null) {
                if (text.contains("Tìm kiếm")) {
                    tvEmptyMessage.setText(String.format("Không tìm thấy kết quả nào cho \"%s\"", text.substring(text.indexOf("\"") + 1, text.lastIndexOf("\""))));
                } else if (text.contains("Tháng")) {
                    tvEmptyMessage.setText("Không có sự kiện nào trong tháng này");
                } else {
                    tvEmptyMessage.setText("Chưa có sự kiện nào được tạo");
                }
            }
        } else {
            layoutEmptyResults.setVisibility(View.GONE);
            rvEvents.setVisibility(View.VISIBLE);
        }
    }

    private void showEventDetail(Event event) {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_event_detail, null);
        builder.setView(dialogView);

        ImageView ivDetail = dialogView.findViewById(R.id.ivDetail);
        TextView tvNameDetail = dialogView.findViewById(R.id.tvNameDetail);
        TextView tvDateTimeDetail = dialogView.findViewById(R.id.tvDateTimeDetail);
        TextView tvDescriptionDetail = dialogView.findViewById(R.id.tvDescriptionDetail);
        TextView tvPreparationDetail = dialogView.findViewById(R.id.tvPreparationDetail);

        Button btnEdit = dialogView.findViewById(R.id.btnEdit);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        Button btnBack = dialogView.findViewById(R.id.btnBack);

        if (event.getImageUri() != null) {
            ivDetail.setImageURI(Uri.parse(event.getImageUri()));
        } else {
            ivDetail.setImageResource(event.getImageResId() != 0 ? event.getImageResId() : android.R.drawable.ic_menu_gallery);
        }

        tvNameDetail.setText(event.getName());
        tvDateTimeDetail.setText(String.format("%s | %s", event.getDate(), event.getTime()));
        tvDescriptionDetail.setText(event.getDescription());
        tvPreparationDetail.setText(event.getPreparation());

        AlertDialog dialog = builder.create();

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddEventActivity.class);
            intent.putExtra("EVENT_DATA", event);
            startActivityForResult(intent, REQUEST_CODE_EDIT);
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            eventDao.deleteEvent(event);
            refreshUI();
            Toast.makeText(getContext(), "Đã xóa sự kiện", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnBack.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Event eventResult = (Event) data.getSerializableExtra("UPDATED_EVENT");
            if (requestCode == REQUEST_CODE_ADD) eventDao.insertEvent(eventResult);
            else if (requestCode == REQUEST_CODE_EDIT) eventDao.updateEvent(eventResult);
            refreshUI();
        }
    }

    private void refreshUI() {
        eventList = eventDao.getAllEvents();
        filteredList.clear();
        filteredList.addAll(eventList);
        adapter.notifyDataSetChanged();
        updateHeaderText("Tất cả sự kiện");
    }

    // Gỡ bỏ onCreateOptionsMenu và onOptionsItemSelected từ đây 
    // vì MainActivity sẽ gửi tín hiệu qua listener hoặc interface nếu cần.
    // Tuy nhiên, để đơn giản và nhanh, MainActivity sẽ tự xử lý hoặc gọi method này.

    public void handleSearch(String query) {
        filterByName(query);
    }

    public void handleAddEvent() {
        startActivityForResult(new Intent(getContext(), AddEventActivity.class), REQUEST_CODE_ADD);
    }

    public void handleFilter() {
        showFilterDialog();
    }

    private void filterByName(String text) {
        filteredList.clear();
        if (text == null || text.isEmpty()) {
            filteredList.addAll(eventList);
            updateHeaderText("Tất cả sự kiện");
        } else {
            for (Event event : eventList) {
                if (event.getName().toLowerCase().contains(text.toLowerCase())) filteredList.add(event);
            }
            updateHeaderText(String.format("Kết quả tìm kiếm cho \"%s\"", text));
        }
        adapter.notifyDataSetChanged();
    }

    private void showFilterDialog() {
        if (getContext() == null) return;
        String[] months = {"Tất cả", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                          "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        new AlertDialog.Builder(getContext())
            .setTitle("Lọc theo tháng")
            .setItems(months, (dialog, which) -> {
                if (which == 0) {
                    refreshUI();
                } else {
                    filteredList.clear();
                    for (Event event : eventList) {
                        if (event.getMonth() == which) filteredList.add(event);
                    }
                    adapter.notifyDataSetChanged();
                    updateHeaderText(String.format("Sự kiện trong %s", months[which]));
                }
            }).show();
    }
}
