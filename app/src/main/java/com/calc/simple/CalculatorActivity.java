package com.calc.simple;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Zwykly kalkulator. Kod w CODE + "=" otwiera ukryte menu. */
public class CalculatorActivity extends Activity {

    private static final String CODE = "10101357";
    private static final int MAX_DIGITS = 15;

    private static final int COLOR_DIGIT = Color.rgb(51, 51, 51);
    private static final int COLOR_FUNC = Color.rgb(165, 165, 165);
    private static final int COLOR_OP = Color.rgb(255, 159, 10);

    private TextView display;
    private TextView expression;

    private String current = "0";
    private BigDecimal acc = null;
    private char op = 0;
    private boolean fresh = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        refresh();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        int pad = dp(12);
        root.setPadding(pad, pad, pad, pad);

        expression = new TextView(this);
        expression.setTextColor(Color.rgb(150, 150, 150));
        expression.setTextSize(20);
        expression.setGravity(Gravity.END | Gravity.BOTTOM);
        expression.setSingleLine(true);
        root.addView(expression, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        display = new TextView(this);
        display.setTextColor(Color.WHITE);
        display.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        display.setGravity(Gravity.END | Gravity.BOTTOM);
        display.setSingleLine(true);
        display.setAutoSizeTextTypeUniformWithConfiguration(28, 80, 2, android.util.TypedValue.COMPLEX_UNIT_SP);
        display.setPadding(0, 0, 0, dp(8));
        root.addView(display, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 2f));

        String[][] rows = {
                {"C", "±", "%", "÷"},
                {"7", "8", "9", "×"},
                {"4", "5", "6", "−"},
                {"1", "2", "3", "+"},
                {"0", ",", "="},
        };
        for (String[] row : rows) {
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            for (String label : row) {
                float weight = label.equals("0") ? 2f : 1f;
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT, weight);
                lp.setMargins(dp(4), dp(4), dp(4), dp(4));
                line.addView(makeButton(label), lp);
            }
            root.addView(line, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        }
        setContentView(root);
    }

    private Button makeButton(final String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(28);
        b.setTextColor(Color.WHITE);
        int bg;
        if (label.equals("C") || label.equals("±") || label.equals("%")) {
            bg = COLOR_FUNC;
            b.setTextColor(Color.BLACK);
        } else if ("÷×−+=".contains(label)) {
            bg = COLOR_OP;
        } else {
            bg = COLOR_DIGIT;
        }
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(bg);
        shape.setCornerRadius(dp(40));
        b.setBackground(shape);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKey(label);
            }
        });
        return b;
    }

    private void onKey(String key) {
        char c = key.charAt(0);
        if (c >= '0' && c <= '9') {
            if (fresh || current.equals("0")) {
                current = key;
                fresh = false;
            } else if (digitCount(current) < MAX_DIGITS) {
                current += key;
            }
        } else if (key.equals(",")) {
            if (fresh) {
                current = "0.";
                fresh = false;
            } else if (!current.contains(".")) {
                current += ".";
            }
        } else if (key.equals("C")) {
            current = "0";
            acc = null;
            op = 0;
            fresh = true;
        } else if (key.equals("±")) {
            if (!current.equals("0") && !current.equals("Błąd")) {
                current = current.startsWith("-") ? current.substring(1) : "-" + current;
            }
        } else if (key.equals("%")) {
            BigDecimal v = parse();
            if (v != null) {
                current = format(v.divide(BigDecimal.valueOf(100), MathContext.DECIMAL64));
                fresh = true;
            }
        } else if (key.equals("=")) {
            if (op == 0 && current.equals(CODE)) {
                onKey("C");
                refresh();
                startActivity(new Intent(this, SecretActivity.class));
                return;
            }
            if (op != 0) {
                calculate();
                op = 0;
                acc = null;
                fresh = true;
            }
        } else { // operator
            if (op != 0 && !fresh) {
                calculate();
            }
            acc = parse();
            op = c;
            fresh = true;
        }
        refresh();
    }

    private void calculate() {
        BigDecimal b = parse();
        if (acc == null || b == null) {
            return;
        }
        BigDecimal r;
        switch (op) {
            case '+': r = acc.add(b); break;
            case '−': r = acc.subtract(b); break;
            case '×': r = acc.multiply(b); break;
            case '÷':
                if (b.signum() == 0) {
                    current = "Błąd";
                    acc = null;
                    return;
                }
                r = acc.divide(b, MathContext.DECIMAL64);
                break;
            default: return;
        }
        current = format(r);
        acc = parse();
    }

    private BigDecimal parse() {
        try {
            return new BigDecimal(current);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String format(BigDecimal v) {
        v = v.round(new MathContext(12, RoundingMode.HALF_UP));
        if (v.signum() == 0) {
            return "0";
        }
        return v.stripTrailingZeros().toPlainString();
    }

    private static int digitCount(String s) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) {
                n++;
            }
        }
        return n;
    }

    private void refresh() {
        display.setText(current.replace('.', ','));
        if (op != 0 && acc != null) {
            expression.setText(format(acc).replace('.', ',') + " " + op);
        } else {
            expression.setText("");
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
