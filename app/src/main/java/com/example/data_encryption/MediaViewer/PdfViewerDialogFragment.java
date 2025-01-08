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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.example.data_encryption.R;

public class PdfViewerDialogFragment extends DialogFragment {

    private static final String ARG_URI = "uri";

    public static PdfViewerDialogFragment newInstance(Uri uri) {
        PdfViewerDialogFragment fragment = new PdfViewerDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_pdf_viewer, container, false);
        PDFView pdfView = view.findViewById(R.id.pdfView);
        Uri uri = getArguments().getParcelable(ARG_URI);

        if (uri != null) {
            pdfView.fromUri(uri)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .enableAnnotationRendering(true)
                    .password(null)
                    .scrollHandle(null)
                    .enableAntialiasing(true)
                    .onError(t -> Log.e("PdfViewerDialog", "Error loading PDF", t))
                    .onLoad(nbPages -> Log.d("PdfViewerDialog", "PDF loaded successfully with " + nbPages + " pages"))
                    .load();
        } else {
            Log.e("PdfViewerDialog", "URI is null");
        }

        return view;
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
}
