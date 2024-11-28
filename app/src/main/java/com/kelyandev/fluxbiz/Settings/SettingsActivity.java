package com.kelyandev.fluxbiz.Settings;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.kelyandev.fluxbiz.Auth.ReauthenticationFragment;
import com.kelyandev.fluxbiz.R;
import com.kelyandev.fluxbiz.Settings.Account.AccountSettingsFragment;
import com.kelyandev.fluxbiz.Settings.Account.ChangeEmailFragment;
import com.kelyandev.fluxbiz.Settings.Account.ChangeUsernameFragment;
import com.kelyandev.fluxbiz.Settings.Security.ChangePasswordFragment;
import com.kelyandev.fluxbiz.Settings.Security.SecuritySettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new RootSettingsFragment())
                .commit();
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        ImageButton backArrow  = findViewById(R.id.backArrow);

        backArrow.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.settings_container);
            if (currentFragment != null) {
                if (currentFragment instanceof AccountSettingsFragment) {
                    toolbarTitle.setText(R.string.account);
                } else if (currentFragment instanceof SecuritySettingsFragment) {
                    toolbarTitle.setText(R.string.security);
                } else if (currentFragment instanceof ChangeUsernameFragment) {
                    toolbarTitle.setText(R.string.change_username);
                } else if (currentFragment instanceof ChangeEmailFragment) {
                    toolbarTitle.setText(R.string.change_mail);
                } else if (currentFragment instanceof ReauthenticationFragment) {
                    toolbarTitle.setText(R.string.verif);
                } else if (currentFragment instanceof ChangePasswordFragment) {
                    toolbarTitle.setText(R.string.change_password);
                }else {
                    toolbarTitle.setText(R.string.params);
                }
            }
        });



    }
}
