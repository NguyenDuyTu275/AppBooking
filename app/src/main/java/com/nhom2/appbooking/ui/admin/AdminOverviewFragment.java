package com.nhom2.appbooking.ui.admin;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.model.AdminOverview;
import com.nhom2.appbooking.data.model.Booking;
import com.nhom2.appbooking.data.model.RevenueReceipt;
import com.nhom2.appbooking.databinding.FragmentAdminOverviewBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.DownloadSaver;
import com.nhom2.appbooking.util.Formatters;
import com.nhom2.appbooking.util.MonthlyRevenue;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

/** Mục "Tổng quan": số liệu, biểu đồ doanh thu 6 tháng, tỉ lệ đơn, xuất báo cáo Excel (như AdminOverview trên web). */
public class AdminOverviewFragment extends Fragment {

    private static final int CHART_MONTHS = 6;
    private static final String XLSX_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // Admin chọn nơi lưu file qua hộp thoại của hệ thống, app không cần quyền bộ nhớ
    private final ActivityResultLauncher<String> createReport =
            registerForActivityResult(new ActivityResultContracts.CreateDocument(XLSX_TYPE), this::exportTo);

    private FragmentAdminOverviewBinding binding;
    private int pendingRequests;
    // Biểu đồ vẽ lại khi có thêm dữ liệu: đơn đã thanh toán + hóa đơn tiền mặt quét tại khách sạn
    private List<Booking> chartBookings;
    private List<RevenueReceipt> chartReceipts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminOverviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.swipe.setColorSchemeResources(R.color.primary);
        binding.swipe.setOnRefreshListener(this::load);
        binding.buttonExport.setOnClickListener(v -> createReport.launch(
                getString(R.string.admin_overview_report_file, LocalDate.now().toString())));
        binding.buttonScan.setOnClickListener(v -> ReceiptScanActivity.start(requireContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        binding.swipe.setRefreshing(true);
        pendingRequests = 3;
        ApiClient.get(requireContext()).getAdminOverview().enqueue(new ApiCallback<AdminOverview>(this) {
            @Override
            protected void onSuccess(AdminOverview overview) {
                if (overview != null) {
                    showStats(overview);
                }
                requestDone();
            }

            @Override
            protected void onError(String message) {
                requestDone();
                super.onError(getString(R.string.admin_overview_load_failed));
            }
        });
        ApiClient.get(requireContext()).getAdminBookings(null).enqueue(new ApiCallback<List<Booking>>(this) {
            @Override
            protected void onSuccess(List<Booking> bookings) {
                chartBookings = bookings;
                showRevenueChart();
                requestDone();
            }

            @Override
            protected void onError(String message) {
                requestDone();
            }
        });
        String chartStart = YearMonth.now().minusMonths(CHART_MONTHS - 1).atDay(1).toString();
        ApiClient.get(requireContext()).getRevenueReceipts(chartStart, null)
                .enqueue(new ApiCallback<List<RevenueReceipt>>(this) {
                    @Override
                    protected void onSuccess(List<RevenueReceipt> receipts) {
                        chartReceipts = receipts;
                        showRevenueChart();
                        requestDone();
                    }

                    @Override
                    protected void onError(String message) {
                        // Biểu đồ vẫn hiện doanh thu từ đơn đặt phòng
                        requestDone();
                    }
                });
    }

    private void requestDone() {
        pendingRequests--;
        if (pendingRequests <= 0) {
            binding.swipe.setRefreshing(false);
        }
    }

    private void showStats(AdminOverview overview) {
        binding.textRevenue.setText(Formatters.money(overview.paidRevenue == null ? 0.0 : overview.paidRevenue));
        binding.textTotalBookings.setText(String.valueOf(overview.totalBookings));
        binding.textPendingBookings.setText(String.valueOf(overview.pendingBookings));
        binding.textPendingRequests.setText(String.valueOf(overview.pendingRequests));

        Context context = requireContext();
        int[] colors = {
                ContextCompat.getColor(context, R.color.stat_pending),
                ContextCompat.getColor(context, R.color.chart_confirmed),
                ContextCompat.getColor(context, R.color.chart_closed),
        };
        long[] values = {overview.pendingBookings, overview.confirmedBookings, overview.closedBookings()};
        int[] labels = {R.string.admin_overview_pending, R.string.admin_overview_confirmed, R.string.admin_overview_closed};
        binding.chartRatio.setSegments(values, colors);

        binding.layoutLegend.removeAllViews();
        for (int i = 0; i < values.length; i++) {
            binding.layoutLegend.addView(legendRow(context, colors[i],
                    getString(R.string.admin_overview_legend, getString(labels[i]), (int) values[i])));
        }
    }

    private static TextView legendRow(Context context, int color, String text) {
        SpannableString line = new SpannableString("●  " + text);
        line.setSpan(new ForegroundColorSpan(color), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView row = new TextView(context);
        row.setText(line);
        row.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        row.setTextSize(14);
        row.setPadding(0, 2, 0, 2);
        return row;
    }

    private void showRevenueChart() {
        List<MonthlyRevenue> months =
                MonthlyRevenue.lastMonths(chartBookings, chartReceipts, YearMonth.now(), CHART_MONTHS);
        List<String> labels = new ArrayList<>();
        for (MonthlyRevenue month : months) {
            labels.add(getString(R.string.admin_overview_month_short, month.month.getMonthValue()));
        }
        binding.chartRevenue.setData(months, labels);
    }

    private void exportTo(Uri target) {
        if (target == null) {
            return;
        }
        binding.buttonExport.setEnabled(false);
        ApiClient.get(requireContext()).exportBookingsExcel().enqueue(new ApiCallback<ResponseBody>(this) {
            @Override
            protected void onSuccess(ResponseBody body) {
                DownloadSaver.save(requireContext(), body, target, AdminOverviewFragment.this::exportFinished);
            }

            @Override
            protected void onError(String message) {
                exportFinished(false);
            }
        });
    }

    private void exportFinished(boolean saved) {
        if (binding == null) {
            return;
        }
        binding.buttonExport.setEnabled(true);
        Toast.makeText(requireContext(),
                saved ? R.string.admin_overview_export_success : R.string.admin_overview_export_failed,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
