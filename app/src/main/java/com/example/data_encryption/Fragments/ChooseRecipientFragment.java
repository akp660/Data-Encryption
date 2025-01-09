package com.example.data_encryption.Fragments;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.data_encryption.R;

import java.util.ArrayList;
import java.util.List;

public class ChooseRecipientFragment extends DialogFragment {

    private EditText searchBar;
    private ListView recipientsListView;
    private ArrayAdapter<Recipient> adapter;
    private List<Recipient> recipients = new ArrayList<>();
    private List<Recipient> filteredRecipients = new ArrayList<>();

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
                    textView.setText(recipient.getUsername());
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
        // Mock data
        recipients.add(new Recipient("User1", "https://example.com/user1.jpg", false));
        recipients.add(new Recipient("User2", "https://example.com/user2.jpg", false));
        // Add more recipients

        filteredRecipients.addAll(recipients);
        adapter.notifyDataSetChanged();
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

    private class Recipient {
        private String username;
        private String imageUrl;
        private boolean inviteNeeded;

        public Recipient(String username, String imageUrl, boolean inviteNeeded) {
            this.username = username;
            this.imageUrl = imageUrl;
            this.inviteNeeded = inviteNeeded;
        }

        public String getUsername() {
            return username;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public boolean isInviteNeeded() {
            return inviteNeeded;
        }
    }
}
