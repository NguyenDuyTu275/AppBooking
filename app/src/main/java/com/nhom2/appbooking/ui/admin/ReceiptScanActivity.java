package com.nhom2.appbooking.ui.admin;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.os.BundleCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.SessionManager;
import com.nhom2.appbooking.data.model.DailyRevenue;
import com.nhom2.appbooking.data.model.ReceiptScanResult;
import com.nhom2.appbooking.data.model.RevenueReceipt;
import com.nhom2.appbooking.databinding.ActivityReceiptScanBinding;
import com.nhom2.appbooking.databinding.DialogCashReceiptBinding;
import com.nhom2.appbooking.databinding.ItemRevenueReceiptBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.BindingAdapter;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.util.Formatters;
import com.nhom2.appbooking.util.MoneyInput;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import okhttp3.MultipartBody;

/**
 * "Quét hóa đơn" của admin: chụp hoặc chọn ảnh hóa đơn thanh toán, backend nhờ Gemini đọc thông tin.
 * Hóa đơn có mã đơn trên hệ thống được ghi ngay vào doanh thu hôm nay; hóa đơn tiền mặt tại khách sạn
 * chưa có trên hệ thống thì admin kiểm tra thông tin rồi xác nhận. Màn hình cũng là báo cáo doanh thu theo ngày.
 */
public class ReceiptScanActivity extends AppCompatActivity {

    private static final String STATE_PHOTO = "photo_uri";
    private static final String STATE_DATE = "report_date";
    private static final String PHOTO_DIR = "receipts";
    private static final String DATE_PICKER = "date_picker";

    private final ActivityResultLauncher<Uri> takePhoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), this::onPhotoTaken);
    private final ActivityResultLauncher<PickVisualMediaRequest> pickPhoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::scan);

    private ActivityReceiptScanBinding binding;
    private BindingAdapter<RevenueReceipt, ItemRevenueReceiptBinding> adapter;
    // Ảnh máy ảnh đang chụp: giữ qua lúc Android hủy màn hình khi app máy ảnh đang mở
    private Uri pendingPhoto;
    private LocalDate reportDate = LocalDate.now();

    public static void start(Context context) {
        context.startActivity(new Intent(context, ReceiptScanActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new SessionManager(this).isAdmin()) {
            finish();
            return;
        }
        if (savedInstanceState != null) {
            pendingPhoto = BundleCompat.getParcelable(savedInstanceState, STATE_PHOTO, Uri.class);
            reportDate = parseDate(savedInstanceState.getString(STATE_DATE), LocalDate.now());
        }

        binding = ActivityReceiptScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setTitle(R.string.admin_receipts_title);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new BindingAdapter<>(ItemRevenueReceiptBinding::inflate, this::bind);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);
        binding.swipe.setColorSchemeResources(R.color.primary);
        binding.swipe.setOnRefreshListener(this::loadReport);

        binding.buttonCamera.setOnClickListener(v -> openCamera());
        binding.buttonGallery.setOnClickListener(v -> pickPhoto.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
        binding.buttonDate.setOnClickListener(v -> pickDate(reportDate, date -> {
            reportDate = date;
            loadReport();
        }));
        loadReport();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_PHOTO, pendingPhoto);
        outState.putString(STATE_DATE, reportDate.toString());
    }

    private void openCamera() {
        File dir = new File(getCacheDir(), PHOTO_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            toast(R.string.admin_receipts_photo_failed);
            return;
        }
        File photo = new File(dir, "receipt_" + System.currentTimeMillis() + ".jpg");
        pendingPhoto = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photo);
        try {
            takePhoto.launch(pendingPhoto);
        } catch (ActivityNotFoundException e) {
            toast(R.string.admin_receipts_no_camera);
        }
    }

    private void onPhotoTaken(Boolean saved) {
        if (Boolean.TRUE.equals(saved) && pendingPhoto != null) {
            scan(pendingPhoto);
        }
    }

    private void scan(Uri photo) {
        if (photo == null) {
            return;
        }
        setScanning(true);
        ReceiptPhotos.read(this, photo, new ReceiptPhotos.Callback() {
            @Override
            public void onReady(MultipartBody.Part part) {
                upload(part);
            }

            @Override
            public void onError() {
                setScanning(false);
                toast(R.string.admin_receipts_photo_failed);
            }
        });
    }

    private void upload(MultipartBody.Part part) {
        ApiClient.get(this).scanReceipt(part).enqueue(new ApiCallback<ReceiptScanResult>(this) {
            @Override
            protected void onSuccess(ReceiptScanResult result) {
                setScanning(false);
                if (result != null) {
                    showResult(result);
                }
            }

            @Override
            protected void onError(String message) {
                setScanning(false);
                super.onError(message);
            }
        });
    }

    private void setScanning(boolean scanning) {
        if (binding == null) {
            return;
        }
        binding.layoutScanning.setVisibility(scanning ? View.VISIBLE : View.GONE);
        binding.buttonCamera.setEnabled(!scanning);
        binding.buttonGallery.setEnabled(!scanning);
    }

    private void showResult(ReceiptScanResult result) {
        String code = bookingCode(result.bookingId);
        RevenueReceipt receipt = result.receipt;
        if (ReceiptScanResult.NEW_CASH_RECEIPT.equals(result.outcome) || receipt == null) {
            openCashForm(result);
            return;
        }
        String date = Formatters.date(receipt.revenueDate);
        if (ReceiptScanResult.ALREADY_RECORDED.equals(result.outcome)) {
            info(R.string.admin_receipts_already_title, getString(R.string.admin_receipts_already_body, code, date));
        } else if (ReceiptScanResult.BOOKING_MARKED_PAID.equals(result.outcome)) {
            info(R.string.admin_receipts_paid_title,
                    getString(R.string.admin_receipts_paid_body, code, Formatters.money(receipt.amount), date));
        } else {
            info(R.string.admin_receipts_added_title,
                    getString(R.string.admin_receipts_added_body, code, Formatters.money(receipt.amount), date));
        }
        reportDate = parseDate(receipt.revenueDate, reportDate);
        loadReport();
    }

    private void info(int title, String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    /** Hóa đơn chưa có trên hệ thống: điền sẵn những gì Gemini đọc được để admin sửa rồi ghi vào doanh thu. */
    private void openCashForm(ReceiptScanResult result) {
        DialogCashReceiptBinding form = DialogCashReceiptBinding.inflate(getLayoutInflater());
        ReceiptScanResult.Scanned scanned = result.scanned == null ? new ReceiptScanResult.Scanned() : result.scanned;
        LocalDate[] date = {parseDate(result.suggestedDate, LocalDate.now())};

        form.editAmount.setText(MoneyInput.plain(scanned.totalAmount));
        form.editDate.setText(Formatters.date(date[0]));
        form.editDate.setOnClickListener(v -> pickDate(date[0], picked -> {
            date[0] = picked;
            form.editDate.setText(Formatters.date(picked));
        }));
        form.editNumber.setText(orEmpty(scanned.receiptNumber));
        form.editCustomer.setText(orEmpty(scanned.customerName));
        form.editHotel.setText(orEmpty(scanned.hotelName));
        form.editDescription.setText(orEmpty(scanned.description));

        AdminDialogs.showForm(this, getString(R.string.admin_receipts_cash_title), form.getRoot(),
                getString(R.string.admin_receipts_save), false, dialog -> {
                    Double amount = MoneyInput.parse(UiUtils.text(form.editAmount));
                    form.layoutAmount.setError(amount == null ? getString(R.string.admin_receipts_amount_invalid) : null);
                    if (amount == null) {
                        return;
                    }
                    saveCash(dialog, new ApiRequests.CashReceipt(amount, date[0].toString(),
                            blankToNull(form.editNumber), blankToNull(form.editCustomer),
                            blankToNull(form.editHotel), blankToNull(form.editDescription)));
                });
    }

    private void saveCash(AlertDialog dialog, ApiRequests.CashReceipt body) {
        AdminDialogs.setBusy(dialog, true);
        ApiClient.get(this).createCashReceipt(body).enqueue(new ApiCallback<RevenueReceipt>(this) {
            @Override
            protected void onSuccess(RevenueReceipt saved) {
                dialog.dismiss();
                if (saved != null) {
                    Toast.makeText(ReceiptScanActivity.this, getString(R.string.admin_receipts_saved,
                            Formatters.money(saved.amount), Formatters.date(saved.revenueDate)), Toast.LENGTH_LONG).show();
                    reportDate = parseDate(saved.revenueDate, reportDate);
                }
                loadReport();
            }

            @Override
            protected void onError(String message) {
                AdminDialogs.setBusy(dialog, false);
                super.onError(message);
            }
        });
    }

    private void loadReport() {
        binding.swipe.setRefreshing(true);
        binding.buttonDate.setText(Formatters.date(reportDate));
        ApiClient.get(this).getDailyRevenue(reportDate.toString()).enqueue(new ApiCallback<DailyRevenue>(this) {
            @Override
            protected void onSuccess(DailyRevenue report) {
                binding.swipe.setRefreshing(false);
                showReport(report);
            }

            @Override
            protected void onError(String message) {
                binding.swipe.setRefreshing(false);
                super.onError(getString(R.string.admin_receipts_load_failed));
            }
        });
    }

    private void showReport(DailyRevenue report) {
        List<RevenueReceipt> receipts = report == null || report.receipts == null
                ? Collections.emptyList() : report.receipts;
        double cash = report == null ? 0 : report.cashTotal;
        double invoices = report == null ? 0 : report.invoiceTotal;
        binding.textTotal.setText(Formatters.money(report == null ? 0.0 : report.total));
        binding.textBreakdown.setText(getString(R.string.admin_receipts_breakdown,
                Formatters.money(cash), Formatters.money(invoices), receipts.size()));
        adapter.submit(receipts);
        binding.textEmpty.setVisibility(receipts.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void bind(ItemRevenueReceiptBinding b, RevenueReceipt receipt) {
        (receipt.isCash() ? Tag.GOLD : Tag.BLUE).applyTo(b.textSource, getString(receipt.isCash()
                ? R.string.admin_receipts_source_cash : R.string.admin_receipts_source_invoice));
        b.textAmount.setText(Formatters.money(receipt.amount));
        showJoined(b.textCustomer, receipt.customerName, receipt.hotelName);
        showJoined(b.textDescription, receipt.description);
        showJoined(b.textDetails,
                receipt.bookingId == null ? null : getString(R.string.admin_receipts_booking, bookingCode(receipt.bookingId)),
                receipt.receiptNumber == null ? null : getString(R.string.admin_receipts_number, receipt.receiptNumber),
                receipt.createdAt == null ? null : Formatters.time(receipt.createdAt),
                receipt.createdBy);
    }

    private void pickDate(LocalDate current, Consumer<LocalDate> onPicked) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setSelection(current.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                .setCalendarConstraints(new CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointBackward.now())
                        .build())
                .build();
        picker.addOnPositiveButtonClickListener(millis ->
                onPicked.accept(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()));
        picker.show(getSupportFragmentManager(), DATE_PICKER);
    }

    /** Ẩn dòng khi không có nội dung; các phần ngăn bởi " · ". */
    private static void showJoined(TextView view, String... parts) {
        List<String> present = new ArrayList<>();
        for (String part : parts) {
            if (part != null && !part.trim().isEmpty()) {
                present.add(part.trim());
            }
        }
        view.setText(String.join(" · ", present));
        view.setVisibility(present.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /** Cùng mã đơn hiển thị trên app và web (Booking.code()). */
    private static String bookingCode(Long bookingId) {
        return bookingId == null ? "" : String.format(Locale.ROOT, "BK%06d", bookingId);
    }

    private static LocalDate parseDate(String isoDate, LocalDate fallback) {
        if (isoDate == null || isoDate.length() < 10) {
            return fallback;
        }
        try {
            return LocalDate.parse(isoDate.substring(0, 10));
        } catch (DateTimeParseException e) {
            return fallback;
        }
    }

    private static String blankToNull(TextView view) {
        String value = UiUtils.text(view);
        return value.isEmpty() ? null : value;
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private void toast(int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
