package com.reactnative.ivpusic.imagepicker;

import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yalantis.ucrop.UCropActivity;
import com.yalantis.ucrop.view.UCropView;

public class CropperTipActivity extends UCropActivity {
    public static final String EXTRA_TIP_TEXT = "com.reactnative.ivpusic.imagepicker.EXTRA_TIP_TEXT";
    public static final String EXTRA_TIP_COLOR = "com.reactnative.ivpusic.imagepicker.EXTRA_TIP_COLOR";
    public static final String EXTRA_SHOW_CROP_GUIDE_LAYER = "com.reactnative.ivpusic.imagepicker.EXTRA_SHOW_CROP_GUIDE_LAYER";
    public static final String EXTRA_CIRCLE_OVERLAY = "com.reactnative.ivpusic.imagepicker.EXTRA_CIRCLE_OVERLAY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String tipText = getIntent().getStringExtra(EXTRA_TIP_TEXT);
        String tipColor = getIntent().getStringExtra(EXTRA_TIP_COLOR);
        boolean showLayer = getIntent().getBooleanExtra(EXTRA_SHOW_CROP_GUIDE_LAYER, false);
        boolean circleOverlay = getIntent().getBooleanExtra(EXTRA_CIRCLE_OVERLAY, false);

        if (showLayer) {
            addLayerImage(circleOverlay);
        }

        if (tipText != null && !tipText.isEmpty()) {
            addTipLabel(tipText, tipColor);
        }
    }

    private void addLayerImage(boolean circleOverlay) {
        ImageView layerView = new ImageView(this);
        layerView.setScaleType(ImageView.ScaleType.FIT_XY);
        layerView.setAdjustViewBounds(false);

        String drawableName = circleOverlay ? "half_body_layer" : "body_layer";
        int drawableId = getResources().getIdentifier(drawableName, "drawable", getPackageName());
        if (drawableId == 0) {
            return;
        }

        layerView.setImageResource(drawableId);
        UCropView ucropView = findViewById(com.yalantis.ucrop.R.id.ucrop);
        if (ucropView == null) {
            return;
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, 0);
        params.gravity = Gravity.TOP | Gravity.START;
        layerView.setLayoutParams(params);

        // Put guide layer under OverlayView so it is only visible inside crop window.
        int overlayIndex = ucropView.indexOfChild(ucropView.getOverlayView());
        if (overlayIndex >= 0) {
            ucropView.addView(layerView, overlayIndex, params);
        } else {
            ucropView.addView(layerView, params);
        }

        Runnable updateBounds = () -> {
            RectF cropRect = ucropView.getOverlayView().getCropViewRect();
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) layerView.getLayoutParams();
            lp.width = Math.round(cropRect.width());
            lp.height = Math.round(cropRect.height());
            lp.leftMargin = Math.round(cropRect.left);
            lp.topMargin = Math.round(cropRect.top);
            layerView.setLayoutParams(lp);
        };

        ucropView.post(updateBounds);
        // Some devices/layout paths report an empty rect on first frame.
        ucropView.postDelayed(updateBounds, 60);
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
