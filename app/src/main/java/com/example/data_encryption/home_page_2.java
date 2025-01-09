package com.example.data_encryption;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.data_encryption.Fragments.ChooseRecipientFragment;
import com.example.data_encryption.Fragments.CustomFileManagerDialog;
import com.example.data_encryption.Fragments.DecryptFragment;
import com.example.data_encryption.Fragments.EncryptFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class home_page_2 extends AppCompatActivity {

    CardView i_btn,fileManager_btn;
    TextView user_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page2);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar)); // Replace 'status_bar' with your desired color resource


        // Initialize TabLayout and ViewPager2 from the layout
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        i_btn = findViewById(R.id.i_btn);
        fileManager_btn = findViewById(R.id.fileManager_btn);
        user_name = findViewById(R.id.user_name);


        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "User");

        user_name.setText(userName);



// info_btn OnClickListener
        i_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an Intent to start the info_page
                Intent intent = new Intent(home_page_2.this, info_page.class);
                startActivity(intent); // Use startActivity() for a single activity
// for animation
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                triggerVibration();
            }
        });

        fileManager_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the DialogFragment
                CustomFileManagerDialog dialog = new CustomFileManagerDialog();
                dialog.show(getSupportFragmentManager(), "CustomFileManagerDialog");

//                ChooseRecipientFragment dialog = new ChooseRecipientFragment();
//                dialog.show(getSupportFragmentManager(), "ChooseRecipientFragment");
            }
        });



// Tab Layout.
        viewPager.setAdapter(new ViewPagerAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    // Set tab titles dynamically based on position
                    tab.setText(position == 0 ? "Encryption" : "Decryption");
                }
        ).attach();

        // Handling system insets for edge-to-edge design
        // This ensures content is not obscured by system bars (status/navigation bars)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }




    // Inner class: Adapter for managing fragments within the ViewPager2
    // This adapter provides the necessary fragments for each tab
    private static class ViewPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull AppCompatActivity fragmentActivity) {
            super(fragmentActivity); // Pass the activity context to the adapter
        }

        @NonNull
        @Override
        public androidx.fragment.app.Fragment createFragment(int position) {
            return position == 0 ? new EncryptFragment() : new DecryptFragment();
        }

        @Override
        public int getItemCount() {
            return 2; // Total number of tabs
        }
    }

// Trigger vibration
    private void triggerVibration() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
    }

}
