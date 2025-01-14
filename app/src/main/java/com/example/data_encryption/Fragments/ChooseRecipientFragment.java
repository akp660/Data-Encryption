package com.example.data_encryption.Fragments;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.android.volley.VolleyError;
import com.example.data_encryption.ApiDirectory.ApiRequestManager;
import com.example.data_encryption.ApiDirectory.ApiResponseListener;
import com.example.data_encryption.R;
import com.example.data_encryption.utils.CryptoKeyGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseRecipientFragment extends DialogFragment {

    private EditText searchBar;
    private ListView recipientsListView;
    private ArrayAdapter<Recipient> adapter;
    private List<Recipient> recipients = new ArrayList<>();
    private List<Recipient> filteredRecipients = new ArrayList<>();
    private ApiRequestManager apiRequestManager;
    private static final int RSA_KEY_LENGTH = 256; // Length of encrypted AES key (for RSA 2048-bit)

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        apiRequestManager = new ApiRequestManager(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipient_dialog, container, false);

        searchBar = view.findViewById(R.id.search_bar);
        recipientsListView = view.findViewById(R.id.recipients_list_view);

        setupSearchBar();
        fetchUsernames();

        adapter = new ArrayAdapter<Recipient>(getContext(), R.layout.list_item_recipient, filteredRecipients) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_recipient, parent, false);
                }

                TextView textView = convertView.findViewById(R.id.recipient_name);
                Button inviteButton = convertView.findViewById(R.id.invite_button);

                Recipient recipient = getItem(position);
                if (recipient != null) {
                    textView.setText(recipient.getUsername()); // Displaying email here
                    if (recipient.isInviteNeeded()) {
                        inviteButton.setVisibility(View.VISIBLE);
                        inviteButton.setOnClickListener(v -> inviteUser(recipient.getUsername()));
                    } else {
                        inviteButton.setVisibility(View.GONE);
                    }
                }

                return convertView;
            }
        };
        recipientsListView.setAdapter(adapter);
        recipientsListView.setOnItemClickListener((parent, view1, position, id) -> {
            Recipient selectedRecipient = filteredRecipients.get(position);
            String publicKey = selectedRecipient.getRSAPublicKey();
            Log.d("RSA1", publicKey);
//            Toast.makeText(getContext(), "Selected RSA Public Key: " + publicKey, Toast.LENGTH_SHORT).show();
            dismiss();
            furtherEncryptFile(selectedRecipient);
        });
        return view;
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString().toLowerCase();
                filterUsernames(query);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void fetchUsernames() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        apiRequestManager.fetchUsers(new ApiResponseListener<List<Recipient>>() {
            @Override
            public void onSuccess(List<Recipient> recipientList) {
                recipients.clear();
                recipients.addAll(recipientList);
                filteredRecipients.clear();
                filteredRecipients.addAll(recipients);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(getContext(), "Error fetching users: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, token);
    }

    private void filterUsernames(String query) {
        filteredRecipients.clear();
        boolean userFound = false;

        for (Recipient recipient : recipients) {
            if (recipient.getUsername().toLowerCase().contains(query)) {
                filteredRecipients.add(recipient);
                userFound = true;
            }
        }

        if (!userFound) {
            // Add a new item with invite button if user not found
            filteredRecipients.add(new Recipient(query, "", true));
        }

        adapter.notifyDataSetChanged();
    }

    private void inviteUser(String username) {
        Toast.makeText(getContext(), "Inviting " + username, Toast.LENGTH_SHORT).show();
        // Implement invite functionality here
    }
    private void furtherEncryptFile(Recipient recipient) {
        try {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String fileName = sharedPreferences.getString("FILE_NAME_KEY", null);
            if (fileName == null) {
                Toast.makeText(getContext(), "No encrypted file found", Toast.LENGTH_LONG).show();
                return;
            }

            File appDir = getContext().getExternalFilesDir(null);
            File combinedFile = new File(appDir, fileName);
            if (!combinedFile.exists()) {
                Toast.makeText(getContext(), "Combined file not found", Toast.LENGTH_LONG).show();
                return;
            }

            FileInputStream fis = new FileInputStream(combinedFile);
            byte[] combinedData = new byte[(int) combinedFile.length()];
            fis.read(combinedData);
            fis.close();

            int aesKeyLength = 32; // Assuming 256-bit AES key
            byte[] encryptedData = new byte[combinedData.length - aesKeyLength];
            byte[] aesKey = new byte[aesKeyLength];
            System.arraycopy(combinedData, 0, encryptedData, 0, encryptedData.length);
            System.arraycopy(combinedData, encryptedData.length, aesKey, 0, aesKeyLength);

            Log.d("FurtherEncrypt", "Combined data size: " + combinedData.length);
            Log.d("FurtherEncrypt", "Encrypted data size: " + encryptedData.length);
            Log.d("FurtherEncrypt", "AES key size: " + aesKey.length);

            PublicKey rsaPublicKey = getRSAPublicKeyFromString(recipient.getRSAPublicKey());
            byte[] furtherEncryptedAESKey = CryptoKeyGenerator.encryptAESKeyWithRSA(rsaPublicKey, new javax.crypto.spec.SecretKeySpec(aesKey, "AES"));
            Log.d("FurtherEncrypt", "Encrypted AES key size: " + furtherEncryptedAESKey.length);

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File finalEncryptedFile = new File(downloadsDir, "encrypted_" + fileName);
            FileOutputStream fos = new FileOutputStream(finalEncryptedFile);
            fos.write(encryptedData);
            fos.write(furtherEncryptedAESKey);
            fos.close();
            Toast.makeText(getContext(), "File encrypted and saved successfully", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("FurtherEncrypting", e.getMessage());
            Toast.makeText(getContext(), "Error further encrypting file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static PublicKey getRSAPublicKeyFromString (String base64PublicKey) throws Exception {
        try {
            byte[] decodedKey = Base64.decode(base64PublicKey, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            Log.e("getRSAPublicKey", "Error loading public key: " + e.getMessage());
            throw e;
        }
    }




    public static class Recipient {
        private String username;
        private String RSAPublicKey;
        private boolean inviteNeeded;
        public Recipient(String username, String RSAPublicKey, boolean inviteNeeded) {
            this.username = username;
            this.RSAPublicKey = RSAPublicKey;
            this.inviteNeeded = inviteNeeded;
        }
        public String getUsername() {
            return username;
        }

        public String getRSAPublicKey() {
            return RSAPublicKey;
        }

        public boolean isInviteNeeded() {
            return inviteNeeded;
        }
    }
}

