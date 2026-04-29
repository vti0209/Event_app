package org.o7planning.eventmanagementapp;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class NotesFragment extends Fragment implements NoteAdapter.OnNoteClickListener {

    private RecyclerView rvNotes;
    private View emptyView;
    private NoteAdapter adapter;
    private NoteDao noteDao;
    private final int[] noteColors = {
            Color.parseColor("#FFF9C4"), // Light Yellow
            Color.parseColor("#E1F5FE"), // Light Blue
            Color.parseColor("#F3E5F5"), // Light Purple
            Color.parseColor("#F1F8E9"), // Light Green
            Color.parseColor("#FFF3E0"), // Light Orange
            Color.parseColor("#FFEBEE")  // Light Red
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        rvNotes = view.findViewById(R.id.rvNotes);
        emptyView = view.findViewById(R.id.layoutEmptyNotes);
        FloatingActionButton fabAddNote = view.findViewById(R.id.fabAddNote);

        noteDao = AppDatabase.getInstance(getContext()).noteDao();

        setupRecyclerView();
        loadNotes();

        fabAddNote.setOnClickListener(v -> showNoteDialog(null));

        return view;
    }

    private void setupRecyclerView() {
        rvNotes.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new NoteAdapter(new ArrayList<>(), this);
        rvNotes.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Note note = adapter.getNoteAt(position);

                new Thread(() -> {
                    noteDao.delete(note);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            adapter.removeAt(position);
                            Toast.makeText(getContext(), "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
                            checkEmpty();
                        });
                    }
                }).start();
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvNotes);
    }

    private void loadNotes() {
        new Thread(() -> {
            List<Note> notes = noteDao.getAllNotes();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (notes == null || notes.isEmpty()) {
                        showEmpty(true);
                    } else {
                        showEmpty(false);
                        adapter.setNotes(notes);
                    }
                });
            }
        }).start();
    }

    private void checkEmpty() {
        if (adapter.getItemCount() == 0) {
            showEmpty(true);
        }
    }

    private void showNoteDialog(@Nullable Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_note, null);
        EditText etNote = view.findViewById(R.id.etNoteContent);

        if (note != null) {
            etNote.setText(note.getContent());
            builder.setTitle("Sửa ghi chú");
        } else {
            builder.setTitle("Ghi chú mới");
        }

        builder.setView(view)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String content = etNote.getText().toString().trim();
                    if (!content.isEmpty()) {
                        if (note == null) {
                            saveNote(content);
                        } else {
                            updateNote(note, content);
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveNote(String content) {
        int randomColor = getRandomDifferentColor();
        Note newNote = new Note(content, randomColor);
        new Thread(() -> {
            noteDao.insert(newNote);
            loadNotes();
        }).start();
    }

    private int getRandomDifferentColor() {
        if (adapter.getItemCount() == 0) {
            return noteColors[new Random().nextInt(noteColors.length)];
        }
        
        // Cố gắng chọn màu khác với màu của ghi chú cuối cùng để tránh trùng lặp liên tiếp
        int lastColor = adapter.getNoteAt(0).getColor();
        List<Integer> availableColors = new ArrayList<>();
        for (int color : noteColors) {
            if (color != lastColor) {
                availableColors.add(color);
            }
        }
        
        if (availableColors.isEmpty()) {
            return noteColors[new Random().nextInt(noteColors.length)];
        }
        
        return availableColors.get(new Random().nextInt(availableColors.size()));
    }

    private void updateNote(Note note, String newContent) {
        note.setContent(newContent);
        new Thread(() -> {
            noteDao.update(note);
            loadNotes();
        }).start();
    }

    @Override
    public void onNoteClick(Note note) {
        showNoteDialog(note);
    }

    private void showEmpty(boolean isEmpty) {
        if (emptyView != null) emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (rvNotes != null) rvNotes.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
