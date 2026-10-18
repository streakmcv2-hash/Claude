package com.calc.simple;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/** Ukryte menu: wlacznik mocy, suwak intensywnosci i wybor trybu. */
public class SecretActivity extends Activity {

    private static final int MODE_CONSTANT = 0;
    private static final int MODE_PULSE = 1;
    private static final int MODE_WAVE = 2;
    private static final String[] MODE_NAMES = {"Stały", "Pulsacja", "Fala"};

    private static final int COLOR_ON = Color.rgb(255, 45, 85);
    private static final int COLOR_OFF = Color.rgb(60, 60, 60);

    private Vibrator vibrator;
    private boolean running = false;
    private int intensity = 50; // 1..100
    private int mode = MODE_CONSTANT;

    private Button powerButton;
    private TextView intensityLabel;
    private final Button[] modeButtons = new Button[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // brak podgladu w ostatnich aplikacjach i zrzutow ekranu
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        buildUi();
        refreshUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.rgb(15, 15, 18));
        root.setPadding(dp(24), dp(48), dp(24), dp(24));

        TextView title = new TextView(this);
        title.setText("Menu");
        title.setTextColor(Color.WHITE);
        title.setTextSize(26);
        root.addView(title, wrap());

        powerButton = new Button(this);
        powerButton.setAllCaps(false);
        powerButton.setTextSize(30);
        powerButton.setTextColor(Color.WHITE);
        powerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRunning(!running);
            }
        });
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(dp(200), dp(200));
        plp.topMargin = dp(40);
        plp.bottomMargin = dp(40);
        root.addView(powerButton, plp);

        intensityLabel = new TextView(this);
        intensityLabel.setTextColor(Color.WHITE);
        intensityLabel.setTextSize(18);
        root.addView(intensityLabel, wrap());

        SeekBar seek = new SeekBar(this);
        seek.setMax(99); // 0..99 -> 1..100%
        seek.setProgress(intensity - 1);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                intensity = progress + 1;
                refreshUi();
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar s) {
                if (running) {
                    startVibration();
                }
            }
        });
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = dp(8);
        slp.bottomMargin = dp(32);
        root.addView(seek, slp);

        LinearLayout modes = new LinearLayout(this);
        modes.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < MODE_NAMES.length; i++) {
            final int m = i;
            Button b = new Button(this);
            b.setAllCaps(false);
            b.setText(MODE_NAMES[i]);
            b.setTextColor(Color.WHITE);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mode = m;
                    refreshUi();
                    if (running) {
                        startVibration();
                    }
                }
            });
            modeButtons[i] = b;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(52), 1f);
            lp.leftMargin = dp(4);
            lp.rightMargin = dp(4);
            modes.addView(b, lp);
        }
        root.addView(modes, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    private void setRunning(boolean on) {
        running = on;
        if (on) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            startVibration();
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            vibrator.cancel();
        }
        refreshUi();
    }

    private void startVibration() {
        if (!vibrator.hasVibrator()) {
            return;
        }
        int amp = Math.max(1, Math.round(intensity * 255f / 100f));
        boolean ampControl = vibrator.hasAmplitudeControl();
        long[] timings;
        int[] amps;

        switch (mode) {
            case MODE_PULSE:
                timings = new long[]{450, 300};
                amps = new int[]{ampControl ? amp : VibrationEffect.DEFAULT_AMPLITUDE, 0};
                break;
            case MODE_WAVE: {
                int steps = 10;
                timings = new long[steps * 2];
                amps = new int[steps * 2];
                for (int i = 0; i < steps; i++) {
                    int up = Math.max(1, amp * (i + 1) / steps);
                    int down = Math.max(1, amp * (steps - i) / steps);
                    timings[i] = 120;
                    timings[steps + i] = 120;
                    amps[i] = ampControl ? up : VibrationEffect.DEFAULT_AMPLITUDE;
                    amps[steps + i] = ampControl ? down : VibrationEffect.DEFAULT_AMPLITUDE;
                }
                if (!ampControl) {
                    // bez regulacji amplitudy: faluj dlugoscia impulsow
                    for (int i = 0; i < steps; i++) {
                        timings[i] = 20 + 100L * (i + 1) / steps * intensity / 100;
                        timings[steps + i] = 20 + 100L * (steps - i) / steps * intensity / 100;
                        amps[i] = (i % 2 == 0) ? VibrationEffect.DEFAULT_AMPLITUDE : 0;
                        amps[steps + i] = (i % 2 == 0) ? VibrationEffect.DEFAULT_AMPLITUDE : 0;
                    }
                }
                break;
            }
            default: // MODE_CONSTANT
                if (ampControl) {
                    timings = new long[]{1000};
                    amps = new int[]{amp};
                } else {
                    // bez regulacji amplitudy: moc przez wypelnienie impulsow
                    long on = Math.max(10, intensity);
                    long off = 100 - on;
                    if (off <= 0) {
                        timings = new long[]{1000};
                        amps = new int[]{VibrationEffect.DEFAULT_AMPLITUDE};
                    } else {
                        timings = new long[]{on, off};
                        amps = new int[]{VibrationEffect.DEFAULT_AMPLITUDE, 0};
                    }
                }
        }
        vibrator.cancel();
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amps, 0));
    }

    private void refreshUi() {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(running ? COLOR_ON : COLOR_OFF);
        powerButton.setBackground(circle);
        powerButton.setText(running ? "WŁĄCZONE" : "WYŁĄCZONE");
        intensityLabel.setText("Moc: " + intensity + "%");
        for (int i = 0; i < modeButtons.length; i++) {
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(dp(12));
            shape.setColor(i == mode ? COLOR_ON : COLOR_OFF);
            modeButtons[i].setBackground(shape);
        }
    }

    @Override
    protected void onStop() {
        // wyjscie z menu zawsze wylacza wibracje
        super.onStop();
        if (running) {
            setRunning(false);
        } else {
            vibrator.cancel();
        }
        if (!isChangingConfigurations()) {
            finish();
        }
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
