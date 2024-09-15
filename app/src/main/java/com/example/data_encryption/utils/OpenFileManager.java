package com.example.data_encryption.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class OpenFileManager {

    private static final int PICK_FILE_REQUEST = 1;

    public static void manageFile(Activity activity){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try{
            activity.startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_FILE_REQUEST);
        }
        catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(activity, "Please install a file manager", Toast.LENGTH_SHORT).show();
        }
    }

    public static void handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data){
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK){
            if (data!=null){
                Uri selectedFileUri = data.getData();
                if (selectedFileUri!=null){
                    String filePath = selectedFileUri.getPath();
                    Toast.makeText(activity.getApplicationContext(), "Selected file: " + filePath, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
