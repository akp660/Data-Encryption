package com.example.data_encryption.MediaViewer;

import android.app.Dialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.data_encryption.R;
import java.io.IOException;

public class AudioPlayerDialogFragment extends DialogFragment {

    private static final String ARG_URI = "uri";
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Button playPauseButton;

    public static AudioPlayerDialogFragment newInstance(Uri uri) {
        AudioPlayerDialogFragment fragment = new AudioPlayerDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_audio_player, container, false);
        seekBar = view.findViewById(R.id.seekBar);
        playPauseButton = view.findViewById(R.id.playPauseButton);
        Uri uri = getArguments().getParcelable(ARG_URI);

        if (uri != null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(getContext(), uri);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> {
                    seekBar.setMax(mediaPlayer.getDuration());
                    mediaPlayer.start();
                    playPauseButton.setText("Pause");
                    updateSeekBar();
                });

                mediaPlayer.setOnCompletionListener(mp -> playPauseButton.setText("Play"));

                playPauseButton.setOnClickListener(v -> {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        playPauseButton.setText("Play");
                    } else {
                        mediaPlayer.start();
                        playPauseButton.setText("Pause");
                    }
                });

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mediaPlayer.seekTo(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error loading audio", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "URI is null", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void updateSeekBar() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            Runnable updater = this::updateSeekBar;
            seekBar.postDelayed(updater, 1000);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(params);
            }
        }
    }
}

