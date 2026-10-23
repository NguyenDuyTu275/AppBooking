package com.nhom2.appbooking.ui.admin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.util.MonthlyRevenue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Biểu đồ vùng doanh thu theo tháng, giống AreaChart ở trang Tổng quan của web (trục tung theo triệu đồng). */
public class RevenueChartView extends View {

    private static final double MILLION = 1_000_000d;
    private static final int GRID_LINES = 4;
    private static final int FILL_TOP_ALPHA = 77; // ~30% như stopOpacity 0.3 trên web
    private static final float LINE_WIDTH_DP = 3f;
    private static final float POINT_RADIUS_DP = 4f;
    private static final float LABEL_SP = 11f;
    private static final float GAP_DP = 6f;

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valueTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();
    private final int lineColor;
    private final float density;

    private List<MonthlyRevenue> data = new ArrayList<>();
    private List<String> labels = new ArrayList<>();

    // Bố cục đã tính sẵn trong computeLayout()
    private float[] xs;
    private float[] ys;
    private String[] valueLabels;
    private String[] gridLabels;
    private float left;
    private float right;
    private float chartTop;
    private float bottom;
    private float gap;
    private float labelBaseline;

    public RevenueChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        float labelSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, LABEL_SP,
                getResources().getDisplayMetrics());
        lineColor = ContextCompat.getColor(context, R.color.chart_line);

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(LINE_WIDTH_DP * density);
        linePaint.setColor(lineColor);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint.setStyle(Paint.Style.FILL);

        pointPaint.setColor(lineColor);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(density);
        gridPaint.setColor(ContextCompat.getColor(context, R.color.chart_grid));
        gridPaint.setPathEffect(new DashPathEffect(new float[]{3 * density, 3 * density}, 0));

        axisTextPaint.setColor(ContextCompat.getColor(context, R.color.text_muted));
        axisTextPaint.setTextSize(labelSize);

        valueTextPaint.setColor(ContextCompat.getColor(context, R.color.text_secondary));
        valueTextPaint.setTextSize(labelSize);
        valueTextPaint.setTextAlign(Paint.Align.CENTER);
        valueTextPaint.setFakeBoldText(true);
    }

    /** @param monthLabels nhãn trục hoành, cùng thứ tự với {@code revenue}. */
    public void setData(List<MonthlyRevenue> revenue, List<String> monthLabels) {
        data = new ArrayList<>(revenue);
        labels = new ArrayList<>(monthLabels);
        computeLayout();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeLayout();
    }

    /** Tính sẵn toạ độ, nhãn và màu chuyển sắc khi dữ liệu hoặc kích thước đổi, để onDraw không phải tạo đối tượng. */
    private void computeLayout() {
        int count = data.size();
        if (count == 0 || getWidth() == 0 || getHeight() == 0) {
            return;
        }
        double top = niceTop(maxRevenue());
        gap = GAP_DP * density;
        float textHeight = axisTextPaint.getFontSpacing();
        gridLabels = new String[GRID_LINES + 1];
        for (int i = 0; i <= GRID_LINES; i++) {
            gridLabels[i] = millions(top * i / GRID_LINES);
        }
        left = getPaddingLeft() + axisTextPaint.measureText(gridLabels[GRID_LINES]) + gap;
        right = getWidth() - getPaddingRight();
        chartTop = getPaddingTop() + textHeight + gap;
        bottom = getHeight() - getPaddingBottom() - textHeight - gap;
        labelBaseline = getHeight() - getPaddingBottom() - axisTextPaint.descent();

        float step = (right - left) / count;
        xs = new float[count];
        ys = new float[count];
        valueLabels = new String[count];
        for (int i = 0; i < count; i++) {
            double revenue = data.get(i).revenue;
            xs[i] = left + step * (i + 0.5f);
            ys[i] = (float) (bottom - (bottom - chartTop) * (revenue / top));
            // Màn hình cảm ứng không có tooltip khi rê chuột như web: ghi luôn số tiền trên điểm
            valueLabels[i] = revenue > 0 ? millions(revenue) : null;
        }

        linePath.reset();
        fillPath.reset();
        for (int i = 0; i < count; i++) {
            if (i == 0) {
                linePath.moveTo(xs[i], ys[i]);
                fillPath.moveTo(xs[i], bottom);
                fillPath.lineTo(xs[i], ys[i]);
            } else {
                float midX = (xs[i - 1] + xs[i]) / 2;
                linePath.cubicTo(midX, ys[i - 1], midX, ys[i], xs[i], ys[i]);
                fillPath.cubicTo(midX, ys[i - 1], midX, ys[i], xs[i], ys[i]);
            }
        }
        fillPath.lineTo(xs[count - 1], bottom);
        fillPath.close();
        fillPaint.setShader(new LinearGradient(0, chartTop, 0, bottom,
                ColorUtils.setAlphaComponent(lineColor, FILL_TOP_ALPHA),
                ColorUtils.setAlphaComponent(lineColor, 0), Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (xs == null || xs.length != data.size() || data.isEmpty()) {
            return;
        }
        axisTextPaint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i <= GRID_LINES; i++) {
            float y = bottom - (bottom - chartTop) * i / GRID_LINES;
            canvas.drawLine(left, y, right, y, gridPaint);
            canvas.drawText(gridLabels[i], left - gap, y + axisTextPaint.getTextSize() / 3, axisTextPaint);
        }

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        axisTextPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < xs.length; i++) {
            canvas.drawCircle(xs[i], ys[i], POINT_RADIUS_DP * density, pointPaint);
            if (valueLabels[i] != null) {
                canvas.drawText(valueLabels[i], xs[i], ys[i] - gap, valueTextPaint);
            }
            if (i < labels.size()) {
                canvas.drawText(labels.get(i), xs[i], labelBaseline, axisTextPaint);
            }
        }
    }

    private double maxRevenue() {
        double max = 0;
        for (MonthlyRevenue month : data) {
            max = Math.max(max, month.revenue);
        }
        return max;
    }

    /** Mốc trên cùng tròn số (1, 2, 5 × 10^n) để các vạch chia đẹp; chưa có doanh thu thì lấy 1 triệu. */
    private static double niceTop(double max) {
        if (max <= 0) {
            return MILLION;
        }
        double rough = max / GRID_LINES;
        double magnitude = Math.pow(10, Math.floor(Math.log10(rough)));
        double[] steps = {1, 2, 5, 10};
        for (double s : steps) {
            if (s * magnitude >= rough) {
                return s * magnitude * GRID_LINES;
            }
        }
        return 10 * magnitude * GRID_LINES;
    }

    /** 12_500_000 -> "12.5M", 3_000_000 -> "3M" (như tickFormatter value / 1000000 + "M" trên web). */
    private static String millions(double value) {
        double m = value / MILLION;
        if (Math.abs(m - Math.rint(m)) < 0.05) {
            return String.format(Locale.ROOT, "%dM", Math.round(m));
        }
        return String.format(Locale.ROOT, "%.1fM", m);
    }
}
