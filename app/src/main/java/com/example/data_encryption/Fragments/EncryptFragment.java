package com.example.data_encryption.Fragments;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.data_encryption.R;
import com.example.data_encryption.utils.OpenFileManager;

public class EncryptFragment extends Fragment {

    private static final int PICK_FILE_REQUEST = 1; // Ensure this matches OpenFileManager's request code
    private CardView cardUpload;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_encrypt, container, false);

        cardUpload = view.findViewById(R.id.cardView);

        cardUpload.setOnClickListener(v -> {
            triggerVibration();
            // Call the file manager
            OpenFileManager.manageFile(this);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST) {
            OpenFileManager.handleActivityResult(getActivity(), requestCode, resultCode, data, isSuccess -> {
                if (isSuccess) {
                    Toast.makeText(getContext(), "File encrypted successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "File encryption failed or was canceled.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Trigger vibration
    private void triggerVibration() {
        Context context = getContext();  // Retrieve the fragment's context
        if (context != null) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(50);
                }
            }
        }
    }

}
