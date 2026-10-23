package com.nhom2.appbooking.ui.admin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.nhom2.appbooking.R;

import java.util.Locale;

/** Biểu đồ vành khuyên tỉ lệ đơn, giống PieChart (innerRadius 60, outerRadius 100) ở trang Tổng quan của web. */
public class DonutChartView extends View {

    private static final float INNER_RATIO = 0.6f;
    private static final float PADDING_ANGLE = 5f;
    private static final float FULL_CIRCLE = 360f;
    private static final float START_ANGLE = -90f;
    private static final float LABEL_SP = 13f;

    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();

    private long[] values = new long[0];
    private int[] colors = new int[0];

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        segmentPaint.setStyle(Paint.Style.STROKE);
        emptyPaint.setStyle(Paint.Style.STROKE);
        emptyPaint.setColor(ContextCompat.getColor(context, R.color.chart_grid));
        labelPaint.setColor(ContextCompat.getColor(context, R.color.white));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setFakeBoldText(true);
        labelPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, LABEL_SP,
                getResources().getDisplayMetrics()));
    }

    public void setSegments(long[] segmentValues, int[] segmentColors) {
        values = segmentValues.clone();
        colors = segmentColors.clone();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth() - getPaddingLeft() - getPaddingRight(),
                getHeight() - getPaddingTop() - getPaddingBottom());
        float outer = size / 2;
        float ring = outer * (1 - INNER_RATIO);
        float cx = getPaddingLeft() + (getWidth() - getPaddingLeft() - getPaddingRight()) / 2f;
        float cy = getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom()) / 2f;
        float middle = outer - ring / 2;
        bounds.set(cx - middle, cy - middle, cx + middle, cy + middle);
        segmentPaint.setStrokeWidth(ring);
        emptyPaint.setStrokeWidth(ring);

        long total = 0;
        int nonZero = 0;
        for (long value : values) {
            total += value;
            if (value > 0) {
                nonZero++;
            }
        }
        if (total == 0) {
            canvas.drawArc(bounds, 0, FULL_CIRCLE, false, emptyPaint);
            return;
        }

        float padding = nonZero > 1 ? PADDING_ANGLE : 0f;
        float available = FULL_CIRCLE - padding * nonZero;
        float angle = START_ANGLE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] <= 0) {
                continue;
            }
            float sweep = available * values[i] / total;
            segmentPaint.setColor(colors[i % colors.length]);
            canvas.drawArc(bounds, angle, sweep, false, segmentPaint);

            double mid = Math.toRadians(angle + sweep / 2);
            float lx = (float) (cx + middle * Math.cos(mid));
            float ly = (float) (cy + middle * Math.sin(mid) + labelPaint.getTextSize() / 3);
            String percent = String.format(Locale.ROOT, "%d%%", Math.round(100.0 * values[i] / total));
            canvas.drawText(percent, lx, ly, labelPaint);
            angle += sweep + padding;
        }
    }
}
