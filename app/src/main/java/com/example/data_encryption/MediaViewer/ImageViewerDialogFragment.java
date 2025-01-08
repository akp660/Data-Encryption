package com.example.data_encryption.MediaViewer;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.Glide;
import com.example.data_encryption.R;

public class ImageViewerDialogFragment extends DialogFragment {

    private static final String ARG_URI = "uri";

    public static ImageViewerDialogFragment newInstance(Uri uri) {
        ImageViewerDialogFragment fragment = new ImageViewerDialogFragment();
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
        View view = inflater.inflate(R.layout.dialog_image_viewer, container, false);
        ImageView imageView = view.findViewById(R.id.imageView);
        Uri uri = getArguments().getParcelable(ARG_URI);

        // Add logging
        Log.d("ImageViewerDialog", "URI: " + uri);

        Glide.with(this).load(uri).into(imageView);
        return view;
    }

}
