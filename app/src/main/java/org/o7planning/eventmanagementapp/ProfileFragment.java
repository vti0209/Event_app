package org.o7planning.eventmanagementapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileBio;
    private ImageView ivProfilePic;
    private View btnEditProfile, btnLogout;
    private SharedPreferences sharedPreferences;
    private Uri tempImageUri;
    private ImageView ivDialogProfile;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    tempImageUri = result.getData().getData();
                    if (getContext() != null && tempImageUri != null) {
                        getContext().getContentResolver().takePersistableUriPermission(tempImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    if (ivDialogProfile != null) {
                        ivDialogProfile.setImageURI(tempImageUri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        sharedPreferences = getContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        
        loadProfileData();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileBio = view.findViewById(R.id.tvProfileBio);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> showEditDialog());
        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void handleLogout() {
        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply();
        startActivity(new Intent(getContext(), LoginActivity.class));
        if (getActivity() != null) getActivity().finish();
    }

    private void loadProfileData() {
        String savedName = sharedPreferences.getString("name", "Người dùng ẩn danh");
        String savedBio = sharedPreferences.getString("bio", "Chưa có giới thiệu");
        String imageUriString = sharedPreferences.getString("profileImage", null);
        
        tvProfileName.setText(savedName);
        tvProfileBio.setText(savedBio);
        if (imageUriString != null) {
            ivProfilePic.setImageURI(Uri.parse(imageUriString));
        } else {
            ivProfilePic.setImageResource(android.R.drawable.ic_menu_myplaces);
        }
    }

    private void showEditDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        ivDialogProfile = dialogView.findViewById(R.id.ivEditProfilePic);
        EditText etName = dialogView.findViewById(R.id.etProfileName);
        EditText etBio = dialogView.findViewById(R.id.etProfileBio);
        EditText etPhone = dialogView.findViewById(R.id.etProfilePhone);
        EditText etPass = dialogView.findViewById(R.id.etProfilePassword);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveProfile);

        String currentImage = sharedPreferences.getString("profileImage", null);
        if (currentImage != null) {
            ivDialogProfile.setImageURI(Uri.parse(currentImage));
        }

        etName.setText(sharedPreferences.getString("name", ""));
        etBio.setText(sharedPreferences.getString("bio", ""));
        etPhone.setText(sharedPreferences.getString("phone", ""));

        ivDialogProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newBio = etBio.getText().toString().trim();
            if (newName.isEmpty()) {
                etName.setError("Vui lòng nhập tên");
                return;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("name", newName);
            editor.putString("bio", newBio);
            editor.putString("phone", etPhone.getText().toString().trim());
            String pass = etPass.getText().toString().trim();
            if (!pass.isEmpty()) editor.putString("password", pass);
            if (tempImageUri != null) {
                editor.putString("profileImage", tempImageUri.toString());
                ivProfilePic.setImageURI(tempImageUri);
            }
            editor.apply();

            tvProfileName.setText(newName);
            tvProfileBio.setText(newBio.isEmpty() ? "Chưa có giới thiệu" : newBio);
            
            NotificationHelper.sendNotification(getContext(), "Cập nhật tài khoản", 
                    "Thông tin cá nhân của bạn đã được cập nhật.");

            Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
