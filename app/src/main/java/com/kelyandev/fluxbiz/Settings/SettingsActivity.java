package com.kelyandev.fluxbiz.Settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.kelyandev.fluxbiz.Auth.ReauthenticationFragment;
import com.kelyandev.fluxbiz.R;
import com.kelyandev.fluxbiz.Settings.Accessibility.AccessibilitySettingsFragment;
import com.kelyandev.fluxbiz.Settings.Account.AccountSettingsFragment;
import com.kelyandev.fluxbiz.Settings.Account.ChangeEmailFragment;
import com.kelyandev.fluxbiz.Settings.Account.ChangeUsernameFragment;
import com.kelyandev.fluxbiz.Settings.Security.ChangePasswordFragment;
import com.kelyandev.fluxbiz.Settings.Security.SecuritySettingsFragment;

public class SettingsActivity extends AppCompatActivity {
    private static final String CURRENT_FRAGMENT_KEY = "current_fragment";
    private static final String PREFS_NAME = "settings_pref";
    private static final String THEME_PREF_KEY = "theme";

    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        toolbarTitle = findViewById(R.id.toolbar_title);
        ImageButton backArrow  = findViewById(R.id.backArrow);

        backArrow.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new RootSettingsFragment())
                    .commit();
        } else {
            restoreFragmentState(savedInstanceState);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.settings_container);
            if (currentFragment != null) {
                toolbarTitle.setText(getFragmentTitle(currentFragment));
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.settings_container);
        if (currentFragment != null) {
            getSupportFragmentManager().putFragment(outState, CURRENT_FRAGMENT_KEY, currentFragment);
            toolbarTitle.setText(getFragmentTitle(currentFragment));
        }
    }

    /**
     * Function to restore a fragment if the app reloaded while the user was on a fragment
     * @param savedInstanceState The instance saved
     */
    public void restoreFragmentState(@NonNull Bundle savedInstanceState) {
        Fragment restoredFragment = getSupportFragmentManager().getFragment(savedInstanceState, CURRENT_FRAGMENT_KEY);
        if (restoredFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, restoredFragment)
                    .commit();
        }
    }

    /**
     * Function to apply the theme once it has been changed
     */
    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String theme = prefs.getString(THEME_PREF_KEY, "system");

        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    /**
     * Function to get the current fragment title
     * @param currentFragment The current fragment
     * @return The title of the current fragment
     */
    private String getFragmentTitle(Fragment currentFragment) {
        if (currentFragment instanceof AccountSettingsFragment) {
            return getString(R.string.account);
        } else if (currentFragment instanceof SecuritySettingsFragment) {
            return getString(R.string.security);
        } else if (currentFragment instanceof ChangeUsernameFragment) {
            return getString(R.string.change_username);
        } else if (currentFragment instanceof ChangeEmailFragment) {
            return getString(R.string.change_mail);
        } else if (currentFragment instanceof ReauthenticationFragment) {
            return getString(R.string.verif);
        } else if (currentFragment instanceof ChangePasswordFragment) {
            return getString(R.string.change_password);
        } else if (currentFragment instanceof AccessibilitySettingsFragment) {
            return getString(R.string.accessibility);
        } else {
            return getString(R.string.params);
        }
    }
}
