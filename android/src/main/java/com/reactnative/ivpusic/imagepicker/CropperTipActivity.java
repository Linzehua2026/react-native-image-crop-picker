package com.reactnative.ivpusic.imagepicker;

import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yalantis.ucrop.UCropActivity;
import com.yalantis.ucrop.view.UCropView;

public class CropperTipActivity extends UCropActivity {
    public static final String EXTRA_TIP_TEXT = "com.reactnative.ivpusic.imagepicker.EXTRA_TIP_TEXT";
    public static final String EXTRA_TIP_COLOR = "com.reactnative.ivpusic.imagepicker.EXTRA_TIP_COLOR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String tipText = getIntent().getStringExtra(EXTRA_TIP_TEXT);
        String tipColor = getIntent().getStringExtra(EXTRA_TIP_COLOR);
        if (tipText != null && !tipText.isEmpty()) {
            addTipLabel(tipText, tipColor);
        }
    }

    private void addTipLabel(String tipText, String tipColor) {
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content);

        TextView tipLabel = new TextView(this);
        tipLabel.setText(tipText);
        int color = Color.WHITE;
        if (tipColor != null && !tipColor.isEmpty()) {
            try { color = Color.parseColor(tipColor); } catch (Exception ignored) {}
        }
        tipLabel.setTextColor(color);
        tipLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        float fontPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 13, getResources().getDisplayMetrics());
        float lineHeightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 19, getResources().getDisplayMetrics());
        tipLabel.setLineSpacing(lineHeightPx - fontPx, 1);
        tipLabel.setGravity(Gravity.CENTER);

        int horizontalPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        tipLabel.setPadding(horizontalPadding, 0, horizontalPadding, 0);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        rootView.addView(tipLabel, params);

        rootView.post(() -> {
            UCropView ucropView = findViewById(com.yalantis.ucrop.R.id.ucrop);
            if (ucropView != null) {
                RectF cropRect = ucropView.getOverlayView().getCropViewRect();
                int topMargin = (int) cropRect.bottom + (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
                params.topMargin = topMargin;
                tipLabel.setLayoutParams(params);
            }
        });
    }
}
