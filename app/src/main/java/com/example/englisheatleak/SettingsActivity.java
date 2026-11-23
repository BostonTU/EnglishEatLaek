
package com.example.englisheatleak;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    private DataManager dataManager;
    private SoundManager soundManager;

    private TextView tvSettingsHighScore;
    private SwitchCompat switchSound;
    private Button btnResetHighScoreSettings;
    private Button btnBackToMain;
    private RadioGroup radioGroupTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedTheme(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        dataManager = DataManager.getInstance(this);
        soundManager = SoundManager.getInstance(this);

        tvSettingsHighScore = findViewById(R.id.tvSettingsHighScore);
        switchSound = findViewById(R.id.switchSound);
        btnResetHighScoreSettings = findViewById(R.id.btnResetHighScoreSettings);
        btnBackToMain = findViewById(R.id.btnBackToMain);
        radioGroupTheme = findViewById(R.id.radioGroupTheme);

        loadSettings();
        setupThemeSelector();

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dataManager.setSoundEnabled(isChecked);

            if (isChecked) {
                soundManager.playButtonClickSound();
            }
        });

        btnResetHighScoreSettings.setOnClickListener(v -> {
            soundManager.playButtonClickSound();
            showResetConfirmationDialog();
        });

        btnBackToMain.setOnClickListener(v -> {
            soundManager.playButtonClickSound();
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.settingsLayout), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );
    }

    private void loadSettings() {
        int highScore = dataManager.getHighScore();

        tvSettingsHighScore.setText(
                getString(R.string.start_best_score_format, highScore)
        );

        switchSound.setChecked(dataManager.isSoundEnabled());

        String currentTheme = ThemeManager.getTheme(this);

        switch (currentTheme) {
            case ThemeManager.LIGHT:
                radioGroupTheme.check(R.id.radioThemeLight);
                break;

            case ThemeManager.DARK:
                radioGroupTheme.check(R.id.radioThemeDark);
                break;

            default:
                radioGroupTheme.check(R.id.radioThemeSystem);
                break;
        }
    }

    private void setupThemeSelector() {
        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {

            String selectedTheme;

            if (checkedId == R.id.radioThemeLight) {
                selectedTheme = ThemeManager.LIGHT;

            } else if (checkedId == R.id.radioThemeDark) {
                selectedTheme = ThemeManager.DARK;

            } else {
                selectedTheme = ThemeManager.SYSTEM;
            }

            if (!selectedTheme.equals(ThemeManager.getTheme(this))) {
                soundManager.playButtonClickSound();
                ThemeManager.setTheme(this, selectedTheme);
            }
        });
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_reset_title))
                .setMessage(getString(R.string.confirm_reset_message))
                .setPositiveButton(
                        getString(R.string.confirm_yes),
                        (dialog, which) -> {
                            dataManager.resetHighScore();
                            dataManager.clearSavedGameState();

                            loadSettings();

                            soundManager.playWrongSound();

                            Toast.makeText(
                                    SettingsActivity.this,
                                    getString(R.string.toast_reset_success),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .setNegativeButton(
                        getString(R.string.confirm_cancel),
                        null
                )
                .show();
    }
}
