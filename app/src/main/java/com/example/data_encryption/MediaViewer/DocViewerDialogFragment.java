package com.example.data_encryption.MediaViewer;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
//import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
//import org.apache.poi.xwpf.usermodel.XWPFDocument;
import com.example.data_encryption.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DocViewerDialogFragment extends DialogFragment {

    private static final String ARG_URI = "uri";

    public static DocViewerDialogFragment newInstance(Uri uri) {
        DocViewerDialogFragment fragment = new DocViewerDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_doc_viewer, container, false);
        TextView textView = view.findViewById(R.id.textView);
        Uri uri = getArguments().getParcelable(ARG_URI);
//        try (FileInputStream fis = new FileInputStream(new File(uri.getPath()))) {
//            XWPFDocument document = new XWPFDocument(fis);
//            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
//            String docText = extractor.getText();
//            textView.setText(docText);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return view;
    }
}
