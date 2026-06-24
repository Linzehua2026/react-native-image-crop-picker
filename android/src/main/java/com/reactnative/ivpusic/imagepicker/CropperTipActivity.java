package com.reactnative.ivpusic.imagepicker;

import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yalantis.ucrop.UCropActivity;
import com.yalantis.ucrop.view.UCropView;

public class CropperTipActivity extends UCropActivity {
    // Static refs so release shrinkResources/R8 keep guide drawables (getIdentifier is invisible to the shrinker).
    @SuppressWarnings("unused")
    private static final int[] KEEP_GUIDE_DRAWABLES = {
            R.drawable.body_layer,
            R.drawable.half_body_layer,
    };

    public static final String EXTRA_TIP_TEXT = "com.reactnative.ivpusic.imagepicker.EXTRA_TIP_TEXT";
    public static final String EXTRA_TIP_COLOR = "com.reactnative.ivpusic.imagepicker.EXTRA_TIP_COLOR";
    public static final String EXTRA_SHOW_CROP_GUIDE_LAYER = "com.reactnative.ivpusic.imagepicker.EXTRA_SHOW_CROP_GUIDE_LAYER";
    public static final String EXTRA_CIRCLE_OVERLAY = "com.reactnative.ivpusic.imagepicker.EXTRA_CIRCLE_OVERLAY";

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        final ImageView layerView = new ImageView(this);
        // frame 已按裁切框高度等比计算，用 FIT_XY 避免二次 AspectFit 留白
        layerView.setScaleType(ImageView.ScaleType.FIT_XY);
        layerView.setAdjustViewBounds(false);

        int drawableId = circleOverlay ? R.drawable.half_body_layer : R.drawable.body_layer;
        layerView.setImageResource(drawableId);
        final UCropView ucropView = findViewById(com.yalantis.ucrop.R.id.ucrop);
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
            if (cropRect == null || cropRect.width() <= 0 || cropRect.height() <= 0) {
                return;
            }
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) layerView.getLayoutParams();
            Drawable drawable = layerView.getDrawable();
            int imageWidth = drawable != null ? drawable.getIntrinsicWidth() : 0;
            int imageHeight = drawable != null ? drawable.getIntrinsicHeight() : 0;
            if (imageWidth > 0 && imageHeight > 0) {
                // 高度与裁切框对齐（顶/底贴齐），宽度按图片比例缩放并水平居中
                float scale = cropRect.height() / imageHeight;
                int scaledWidth = Math.round(imageWidth * scale);
                lp.width = scaledWidth;
                lp.height = Math.round(cropRect.height());
                lp.leftMargin = Math.round(cropRect.left + (cropRect.width() - scaledWidth) / 2f);
                lp.topMargin = Math.round(cropRect.top);
            } else {
                lp.width = Math.round(cropRect.width());
                lp.height = Math.round(cropRect.height());
                lp.leftMargin = Math.round(cropRect.left);
                lp.topMargin = Math.round(cropRect.top);
            }
            layerView.setLayoutParams(lp);
        };

        ucropView.post(updateBounds);
        // Some camera flows update crop rect after first layout pass.
        ucropView.postDelayed(updateBounds, 60);
        ucropView.postDelayed(updateBounds, 180);
        ucropView.postDelayed(updateBounds, 360);
        ucropView.postDelayed(updateBounds, 700);

        View overlayView = ucropView.getOverlayView();
        overlayView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> updateBounds.run());
        ucropView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> updateBounds.run());
    }

    private void addTipLabel(String tipText, String tipColor) {
        final ViewGroup rootView = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content);

        final TextView tipLabel = new TextView(this);
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

        // 提示固定在底部，不随裁剪框移动
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        rootView.addView(tipLabel, params);

        final int topGapPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
        final int bottomGapPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());

        Runnable applyTipLayout = () -> {
            // 固定在底部（rootView 已扣除系统栏安全区），上方与底部留固定间距
            params.bottomMargin = bottomGapPx;
            tipLabel.setLayoutParams(params);

            // 预留裁剪区底部空间，使裁剪框不会覆盖到提示文字
            int measureWidth = rootView.getWidth();
            if (measureWidth <= 0) {
                return;
            }
            tipLabel.measure(
                    View.MeasureSpec.makeMeasureSpec(measureWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int tipHeight = tipLabel.getMeasuredHeight();

            View ucropFrame = findViewById(com.yalantis.ucrop.R.id.ucrop_frame);
            if (ucropFrame != null) {
                int reservedBottom = topGapPx + tipHeight + bottomGapPx;
                if (ucropFrame.getPaddingBottom() != reservedBottom) {
                    ucropFrame.setPadding(
                            ucropFrame.getPaddingLeft(),
                            ucropFrame.getPaddingTop(),
                            ucropFrame.getPaddingRight(),
                            reservedBottom);
                    ucropFrame.requestLayout();
                }
            }
        };

        rootView.post(applyTipLayout);
        rootView.postDelayed(applyTipLayout, 120);
        rootView.postDelayed(applyTipLayout, 280);
        rootView.postDelayed(applyTipLayout, 520);
        rootView.postDelayed(applyTipLayout, 800);
    }
}
