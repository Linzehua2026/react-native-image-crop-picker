package com.reactnative.ivpusic.imagepicker;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yalantis.ucrop.UCropActivity;

public class CropperTipActivity extends UCropActivity {
    public static final String EXTRA_TIP_TEXT = "com.reactnative.ivpusic.imagepicker.EXTRA_TIP_TEXT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String tipText = getIntent().getStringExtra(EXTRA_TIP_TEXT);
        if (tipText != null && !tipText.isEmpty()) {
            addTipLabel(tipText);
        }
    }

    private void addTipLabel(String tipText) {
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content);

        TextView tipLabel = new TextView(this);
        tipLabel.setText(tipText);
        tipLabel.setTextColor(Color.WHITE);
        tipLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        tipLabel.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        int bottomMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        params.bottomMargin = bottomMargin;
        int horizontalPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        tipLabel.setPadding(horizontalPadding, 0, horizontalPadding, 0);

        rootView.addView(tipLabel, params);
    }
}
