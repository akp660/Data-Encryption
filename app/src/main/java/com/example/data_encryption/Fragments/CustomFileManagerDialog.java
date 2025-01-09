package com.example.data_encryption.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import com.example.data_encryption.R;
import com.example.data_encryption.MediaViewer.AudioPlayerDialogFragment;
import com.example.data_encryption.MediaViewer.DocViewerDialogFragment;
import com.example.data_encryption.MediaViewer.ImageViewerDialogFragment;
import com.example.data_encryption.MediaViewer.PdfViewerDialogFragment;
import com.example.data_encryption.MediaViewer.VideoPlayerDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class CustomFileManagerDialog extends DialogFragment {
    private ListView listView;
    private TextView currentPathText;
    private File currentDirectory;
    private FileSelectedListener listener;

    public interface FileSelectedListener {
        void onFileSelected(File file);
    }

    public void setFileSelectedListener(FileSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomDialog);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Reload directory after permissions granted
            loadDirectory(currentDirectory);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_file_manager, container, false);

        listView = view.findViewById(R.id.file_list);
        currentPathText = view.findViewById(R.id.current_path);
        ImageButton closeButton = view.findViewById(R.id.close_button);

        closeButton.setOnClickListener(v -> dismiss());

        // Start at external storage
        currentDirectory = Environment.getExternalStorageDirectory();
        loadDirectory(currentDirectory);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // Set specific dimensions for dialog
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.8);
                params.height = (int)(getResources().getDisplayMetrics().heightPixels * 0.6);
                window.setAttributes(params);

                // Set dialog position (centered by default)
                window.setGravity(android.view.Gravity.CENTER);
            }
        }
    }

    private void loadDirectory(File directory) {
        currentPathText.setText(directory.getPath());

        File[] files = directory.listFiles();
        ArrayList<FileItem> fileItems = new ArrayList<>();

        // Add "up" directory except at root
        if (directory.getParent() != null) {
            fileItems.add(new FileItem(new File(directory, ".."), true));
        }

        if (files != null) {
            Arrays.sort(files);
            for (File file : files) {
                if (!file.isHidden()) {
                    fileItems.add(new FileItem(file, file.isDirectory()));
                }
            }
        }

        // Pass reference of CustomFileManagerDialog to the adapter
        FileListAdapter adapter = new FileListAdapter(requireContext(), fileItems, this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            FileItem item = fileItems.get(position);
            File selectedFile = item.file;

            if (item.isDirectory) {
                if (item.file.getName().equals("..")) {
                    currentDirectory = currentDirectory.getParentFile();
                } else {
                    currentDirectory = selectedFile;
                }
                loadDirectory(currentDirectory);
            } else {
                openFile(selectedFile);
            }
        });
    }

    private void openFile(File file) {
        try {
            String mimeType = getMimeType(file.getName());
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getApplicationContext().getPackageName() + ".provider",
                    file);

            // Log file info
            Log.d("FileManager", "File path: " + file.getAbsolutePath());
            Log.d("FileManager", "URI: " + uri.toString());
            Log.d("FileManager", "Mime type: " + mimeType);

            if (mimeType.startsWith("image/")) {
                showDialogFragment(ImageViewerDialogFragment.newInstance(uri));
            } else if (mimeType.equals("application/pdf")) {
                showDialogFragment(PdfViewerDialogFragment.newInstance(uri));
            } else if (mimeType.equals("application/msword") || mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                showDialogFragment(DocViewerDialogFragment.newInstance(uri));
            } else if (mimeType.startsWith("audio/")) {
                showDialogFragment(AudioPlayerDialogFragment.newInstance(uri));
            } else if (mimeType.startsWith("video/")) {
                showDialogFragment(VideoPlayerDialogFragment.newInstance(uri));
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, mimeType);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e("FileManager", "Error opening file: ", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDialogFragment(DialogFragment dialogFragment) {
        dialogFragment.show(getChildFragmentManager(), dialogFragment.getClass().getSimpleName());
    }

    private String getMimeType(String fileName) {
        // Handle PDF files explicitly
        if (fileName.toLowerCase().endsWith(".pdf")) {
            return "application/pdf";
        }

        String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
        if (extension != null) {
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            return mimeType != null ? mimeType : "*/*";
        }
        return "*/*";
    }

    private void shareFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getApplicationContext().getPackageName() + ".provider",
                    file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(getMimeType(file.getName()));
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share File"));
        } catch (Exception e) {
            Log.e("FileManager", "Error sharing file: ", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static class FileItem {
        File file;
        boolean isDirectory;

        FileItem(File file, boolean isDirectory) {
            this.file = file;
            this.isDirectory = isDirectory;
        }
    }

    private static class FileListAdapter extends ArrayAdapter<FileItem> {
        private final CustomFileManagerDialog dialog;

        FileListAdapter(Context context, ArrayList<FileItem> files, CustomFileManagerDialog dialog) {
            super(context, R.layout.file_item, files);
            this.dialog = dialog;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.file_item, parent, false);
            }

            FileItem item = getItem(position);
            if (item != null) {
                ImageView icon = convertView.findViewById(R.id.file_icon);
                TextView name = convertView.findViewById(R.id.file_name);
                ImageButton shareButton = convertView.findViewById(R.id.share_button);

                name.setText(item.file.getName().equals("..") ? ".." : item.file.getName());
                icon.setImageResource(item.isDirectory ?
                        R.drawable.folder : R.drawable.file);

                shareButton.setVisibility(item.isDirectory ? View.GONE : View.VISIBLE);
                shareButton.setOnClickListener(v -> {
                    dialog.shareFile(item.file);
                });
            }

            return convertView;
        }
    }
}
