package com.example.data_encryption.MediaViewer;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.data_encryption.R;

public class VideoPlayerDialogFragment extends DialogFragment {

    private static final String ARG_URI = "uri";

    public static VideoPlayerDialogFragment newInstance(Uri uri) {
        VideoPlayerDialogFragment fragment = new VideoPlayerDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        fragment.setArguments(args);
        return fragment;
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
                params.height = WindowManager.LayoutParams.MATCH_PARENT;
                window.setAttributes(params);
            }
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_video_player, container, false);
        VideoView videoView = view.findViewById(R.id.videoView);
        Uri uri = getArguments().getParcelable(ARG_URI);
        videoView.setVideoURI(uri);
        videoView.start();
        return view;
    }
}
