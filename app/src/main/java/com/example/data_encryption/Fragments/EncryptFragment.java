package com.example.data_encryption.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import com.example.data_encryption.R;
import com.example.data_encryption.utils.OpenFileManager;

public class EncryptFragment extends Fragment {

    private static final int PICK_FILE_REQUEST = 1;
    private CardView cardUpload;
    private CardView recentFileCard;
    Button shareBtn;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_encrypt, container, false);
        cardUpload = view.findViewById(R.id.cardView);
        recentFileCard = view.findViewById(R.id.recentFile);
        recentFileCard.setOnClickListener(v -> {
            ChooseRecipientFragment dialog = new ChooseRecipientFragment();
            dialog.show(getChildFragmentManager(), "ChooseRecipientFragment");
        });
        shareBtn=view.findViewById(R.id.shareButton);
        shareBtn.setOnClickListener(v -> {
            ChooseRecipientFragment dialog = new ChooseRecipientFragment();
            dialog.show(getChildFragmentManager(), "ChooseRecipientFragment");
        });

        cardUpload.setOnClickListener(v -> {
            triggerVibration();
            OpenFileManager.manageFile(this);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST) {
            OpenFileManager.handleActivityResult(getActivity(), requestCode, resultCode, data, isSuccess -> {
                if (isSuccess && data != null && data.getData() != null) {
                    String filename = OpenFileManager.getFileName(getContext(), data.getData());
                    Toast.makeText(getContext(), "File encrypted successfully!", Toast.LENGTH_SHORT).show();
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString("FILE_NAME_KEY", filename).apply();

                    recentFileCard.setVisibility(View.VISIBLE); // Make recentFile card visible
                    showTapTargetPrompt(filename);
                } else {
                    Toast.makeText(getContext(), "File encryption failed or was canceled.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void showTapTargetPrompt(String fileName) {
        TextView fileNameTextView = getView().findViewById(R.id.fileName);
        fileNameTextView.setText(fileName);

        new MaterialTapTargetPrompt.Builder(getActivity())
                .setTarget(R.id.recentFile)
                .setPrimaryText("Share your file")
                .setSecondaryText("Tap the share button to share your file.")
                .show();
    }

    private void triggerVibration() {
        Context context = getContext();
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
