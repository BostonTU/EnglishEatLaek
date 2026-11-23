package com.example.englisheatlaek;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String PREFS = "game_settings";
    private static final String THEME_KEY = "theme_mode";
    private static final String SYSTEM = "system";
    private static final String LIGHT = "light";
    private static final String DARK = "dark";

    private String screen = "home";
    private int score = 10;
    private int bullets = 3;
    private boolean paused = false;

    private int bg, panel, panelAlt, text, muted, yellow, green, orange, red, blue;
    private int screenWidthDp;
    private boolean tablet;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setNavigationBarColor(Color.BLACK);
        applyThemeColors();
        showHome();
    }

    private String getThemeMode() {
        return getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(THEME_KEY, SYSTEM);
    }

    private boolean isDark() {
        String mode = getThemeMode();
        if (DARK.equals(mode)) return true;
        if (LIGHT.equals(mode)) return false;
        int night = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return night == Configuration.UI_MODE_NIGHT_YES;
    }

    private void applyThemeColors() {
        screenWidthDp = getResources().getConfiguration().screenWidthDp;
        tablet = screenWidthDp >= 600;
        boolean dark = isDark();

        bg = dark ? Color.rgb(7, 24, 32) : Color.rgb(239, 245, 247);
        panel = dark ? Color.rgb(16, 43, 56) : Color.WHITE;
        panelAlt = dark ? Color.rgb(12, 48, 66) : Color.rgb(218, 234, 241);
        text = dark ? Color.WHITE : Color.rgb(16, 42, 54);
        muted = dark ? Color.rgb(177, 199, 208) : Color.rgb(83, 105, 115);
        yellow = Color.rgb(255, 214, 41);
        green = Color.rgb(22, 198, 83);
        orange = Color.rgb(255, 150, 56);
        red = Color.rgb(232, 75, 75);
        blue = Color.rgb(57, 189, 235);

        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setSystemUiVisibility(
                dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private GradientDrawable outline(int fill, int stroke, int radius) {
        GradientDrawable d = shape(fill, radius);
        d.setStroke(dp(2), stroke);
        return d;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER_HORIZONTAL);
        l.setPadding(dp(tablet ? 36 : 22), dp(20), dp(tablet ? 36 : 22), dp(18));
        l.setBackgroundColor(bg);
        return l;
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setIncludeFontPadding(true);
        return t;
    }

    private void gap(LinearLayout parent, int h) {
        View v = new View(this);
        parent.addView(v, new LinearLayout.LayoutParams(1, dp(h)));
    }

    private Button button(String title, int color) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextSize(tablet ? 18 : 15);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackground(shape(color, 14));
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setPadding(dp(12), dp(6), dp(12), dp(6));
        return b;
    }

    private void addButton(LinearLayout parent, String title, int color,
                           int height, Runnable action) {
        Button b = button(title, color);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(height));
        p.setMargins(0, dp(5), 0, dp(5));
        parent.addView(b, p);
        b.setOnClickListener(v -> action.run());
    }

    private TextView artwork(String title, int heightWeight) {
        TextView a = label(title, tablet ? 20 : 15, muted, true);
        a.setBackground(outline(panelAlt, blue, 18));
        return a;
    }

    private void install(LinearLayout page) {
        setContentView(page);
    }

    private void showHome() {
        screen = "home";
        LinearLayout page = column();

        TextView title = label("EnglishEatLaek", tablet ? 40 : 30, yellow, true);
        page.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(tablet ? 62 : 48)));
        page.addView(label("กินเอาตาย", tablet ? 19 : 15, text, true));

        gap(page, tablet ? 22 : 14);
        TextView hero = artwork("CHARACTER ARTWORK\\n\\nใส่ภาพตัวละครจาก Figma ตรงนี้", 1);
        LinearLayout.LayoutParams heroP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        heroP.setMargins(0, 0, 0, dp(18));
        page.addView(hero, heroP);

        LinearLayout.LayoutParams startP = new LinearLayout.LayoutParams(
                tablet ? dp(420) : LinearLayout.LayoutParams.MATCH_PARENT, dp(tablet ? 68 : 58));
        Button start = button("START", green);
        start.setTextSize(tablet ? 21 : 18);
        page.addView(start, startP);
        start.setOnClickListener(v -> showGameplay());

        gap(page, 12);
        LinearLayout menu = new LinearLayout(this);
        menu.setGravity(Gravity.CENTER);
        String[] icons = {"?", "123", "⚡", "♫", "⚙"};
        for (String icon : icons) {
            TextView item = label(icon, tablet ? 24 : 19, Color.rgb(30, 42, 45), true);
            item.setBackground(shape(yellow, 100));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    0, dp(tablet ? 64 : 52), 1f);
            p.setMargins(dp(4), 0, dp(4), 0);
            menu.addView(item, p);
            item.setOnClickListener(v -> {
                if ("⚙".equals(icon)) showThemePicker();
                else Toast.makeText(this, icon, Toast.LENGTH_SHORT).show();
            });
        }
        page.addView(menu);
        gap(page, 12);
        page.addView(label("VERSION 1.0", 11, muted, false));
        install(page);
    }

    private void showGameplay() {
        screen = "game";
        LinearLayout page = column();

        LinearLayout hud = new LinearLayout(this);
        hud.setGravity(Gravity.CENTER);
        hud.setBackground(shape(green, 14));
        TextView points = label("★  " + score, tablet ? 22 : 18, Color.WHITE, true);
        TextView ammo = label("●  " + bullets, tablet ? 22 : 18, Color.WHITE, true);
        TextView pause = label("Ⅱ", tablet ? 24 : 21, yellow, true);
        hud.addView(points, new LinearLayout.LayoutParams(0, dp(58), 1));
        hud.addView(ammo, new LinearLayout.LayoutParams(0, dp(58), 1));
        hud.addView(pause, new LinearLayout.LayoutParams(0, dp(58), 1));
        pause.setOnClickListener(v -> showPauseDialog());
        page.addView(hud);

        gap(page, 12);
        TextView arena = artwork("GAMEPLAY ARTWORK\\n\\nตัวละคร / ศัตรู / ฉากเกม", 1);
        page.addView(arena, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        gap(page, 12);
        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        String[] names = {"MOVE", "CONTINUE", "SHOOT"};
        int[] colors = {blue, orange, yellow};
        for (int i = 0; i < names.length; i++) {
            final int index = i;
            Button b = button(names[i], colors[i]);
            b.setTextColor(Color.rgb(25, 35, 40));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    0, dp(tablet ? 64 : 54), index == 1 ? 1.25f : 1f);
            p.setMargins(dp(4), 0, dp(4), 0);
            controls.addView(b, p);
            b.setOnClickListener(v -> {
                if (index == 0) Toast.makeText(this, "MOVE", Toast.LENGTH_SHORT).show();
                else if (index == 1) showConfirmation();
                else shoot();
            });
        }
        page.addView(controls);
        install(page);
    }

    private void shoot() {
        if (bullets <= 0) {
            Toast.makeText(this, "No bullets left!", Toast.LENGTH_SHORT).show();
            return;
        }
        bullets--;
        score += 10;
        showGameplay();
    }

    private void showConfirmation() {
        screen = "confirm";
        LinearLayout page = column();
        TextView backdrop = artwork("GAMEPLAY BACKGROUND", 1);
        page.addView(backdrop, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        gap(page, 16);
        LinearLayout dialog = new LinearLayout(this);
        dialog.setOrientation(LinearLayout.VERTICAL);
        dialog.setGravity(Gravity.CENTER);
        dialog.setPadding(dp(22), dp(20), dp(22), dp(20));
        dialog.setBackground(outline(panel, yellow, 18));
        dialog.addView(label("CONSIDER", tablet ? 30 : 25, yellow, true));
        gap(dialog, 10);
        dialog.addView(label("Do you want to use 3 bullets to continue?",
                tablet ? 20 : 16, text, false));
        gap(dialog, 18);

        LinearLayout choices = new LinearLayout(this);
        Button yes = button("YES", green);
        Button no = button("NO", red);
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, dp(54), 1);
        half.setMargins(dp(5), 0, dp(5), 0);
        choices.addView(yes, half);
        choices.addView(no, half);
        dialog.addView(choices);
        yes.setOnClickListener(v -> {
            if (bullets >= 3) {
                bullets -= 3;
                showGameplay();
            } else {
                Toast.makeText(this, "Not enough bullets!", Toast.LENGTH_SHORT).show();
                showGameplay();
            }
        });
        no.setOnClickListener(v -> showGameplay());

        LinearLayout.LayoutParams dialogP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(tablet ? 250 : 220));
        dialogP.setMargins(0, dp(12), 0, dp(8));
        page.addView(dialog, dialogP);
        install(page);
    }

    private void showPauseDialog() {
        new AlertDialog.Builder(this)
                .setTitle("PAUSED")
                .setItems(new String[]{"Resume", "Home", "Theme settings"}, (d, which) -> {
                    if (which == 0) showGameplay();
                    else if (which == 1) showHome();
                    else showThemePicker();
                }).show();
    }

    private void showThemePicker() {
        String[] choices = {"System default", "Light", "Dark"};
        String current = getThemeMode();
        int checked = SYSTEM.equals(current) ? 0 : LIGHT.equals(current) ? 1 : 2;
        new AlertDialog.Builder(this)
                .setTitle("Appearance")
                .setSingleChoiceItems(choices, checked, (dialog, which) -> {
                    String mode = which == 0 ? SYSTEM : which == 1 ? LIGHT : DARK;
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                            .putString(THEME_KEY, mode).apply();
                    dialog.dismiss();
                    applyThemeColors();
                    if ("game".equals(screen)) showGameplay();
                    else if ("confirm".equals(screen)) showConfirmation();
                    else showHome();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override public void onBackPressed() {
        if (!"home".equals(screen)) showHome();
        else super.onBackPressed();
    }
}
