package com.nhom2.appbooking.ui.payment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nhom2.appbooking.BuildConfig;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.model.BankApp;
import com.nhom2.appbooking.data.model.Booking;
import com.nhom2.appbooking.data.model.PaymentQr;
import com.nhom2.appbooking.databinding.ActivityPaymentBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.AssistantFab;
import com.nhom2.appbooking.ui.ImageLoader;
import com.nhom2.appbooking.ui.MainActivity;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.util.BankAppLinks;
import com.nhom2.appbooking.util.Formatters;

import java.util.List;

/**
 * Thanh toán chuyển khoản: quét mã QR hoặc mở app ngân hàng với thông tin điền sẵn.
 * Sepay báo tiền về cho backend; màn hình này kiểm tra lại vài giây một lần và ngay khi khách quay lại app.
 */
public class PaymentActivity extends AppCompatActivity {

    private static final String EXTRA_BOOKING_ID = "booking_id";
    private static final long POLL_INTERVAL_MS = 3000;

    private ActivityPaymentBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long bookingId;
    private PaymentQr payment;
    private boolean paid;

    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            checkPayment();
            handler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    public static void start(Context context, long bookingId) {
        context.startActivity(new Intent(context, PaymentActivity.class).putExtra(EXTRA_BOOKING_ID, bookingId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AssistantFab.attach(this, findViewById(android.R.id.content));
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1L);

        binding.toggleMethod.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                boolean bankApp = checkedId == R.id.button_method_bank;
                binding.layoutQr.setVisibility(bankApp ? View.GONE : View.VISIBLE);
                binding.layoutBankApp.setVisibility(bankApp ? View.VISIBLE : View.GONE);
            }
        });
        binding.buttonChooseBank.setOnClickListener(v -> BankAppDialog.show(this, this::openBankApp));
        binding.buttonCopyAccount.setOnClickListener(v ->
                UiUtils.copy(this, getString(R.string.account_number), payment == null ? null : payment.accountNumber));
        binding.buttonCopyContent.setOnClickListener(v ->
                UiUtils.copy(this, getString(R.string.transfer_content), payment == null ? null : payment.transferContent));
        binding.buttonPayLater.setOnClickListener(v -> openMyBookings());

        loadPayment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Quay lại từ app ngân hàng: kiểm tra ngay, sau đó vài giây một lần
        if (!paid) {
            handler.removeCallbacks(poll);
            handler.post(poll);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(poll);
    }

    private void loadPayment() {
        ApiClient.get(this).getPaymentQr(bookingId).enqueue(new ApiCallback<PaymentQr>(this) {
            @Override
            protected void onSuccess(PaymentQr body) {
                if (body == null) {
                    onError(getString(R.string.error_generic));
                    return;
                }
                payment = body;
                bindPayment(body);
            }
        });
    }

    private void bindPayment(PaymentQr p) {
        ImageLoader.loadFit(binding.imageQr, p.qrCodeUrl);
        binding.textBank.setText(p.bankName);
        binding.textAccount.setText(p.accountNumber);
        binding.textAccountName.setText(p.accountName);
        binding.textContent.setText(p.transferContent);
        binding.textAmount.setText(Formatters.money(p.amount));
        // Backend cũ chưa trả deeplink: chỉ dùng được mã QR
        binding.buttonChooseBank.setEnabled(p.bankAppDeeplink != null);
    }

    private void openBankApp(BankApp app) {
        if (payment == null || payment.bankAppDeeplink == null) {
            return;
        }
        String returnUrl = BuildConfig.WEB_BASE_URL + "/booking/payment?bookingId=" + bookingId;
        String link = BankAppLinks.build(payment.bankAppDeeplink, app.appId, returnUrl);

        if (!app.isAutofill()) {
            // App chỉ được mở lên: chép sẵn nội dung "DH <mã>" để khách dán (Sepay dựa vào nội dung này)
            UiUtils.copy(this, getString(R.string.transfer_content), payment.transferContent);
            Toast.makeText(this, getString(R.string.manual_fill, payment.transferContent, payment.accountNumber),
                    Toast.LENGTH_LONG).show();
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_app_to_open, Toast.LENGTH_LONG).show();
        }
    }

    private void checkPayment() {
        ApiClient.get(this).getMyBookings().enqueue(new ApiCallback<List<Booking>>(this) {
            @Override
            protected void onSuccess(List<Booking> bookings) {
                if (bookings == null) {
                    return;
                }
                for (Booking booking : bookings) {
                    if (booking.id == bookingId && booking.isPaid()) {
                        onPaid(booking);
                        return;
                    }
                }
            }

            @Override
            protected void onError(String message) {
                // Lỗi mạng thoáng qua: thử lại ở lần kiểm tra sau
            }
        });
    }

    private void onPaid(Booking booking) {
        if (paid) {
            return;
        }
        paid = true;
        handler.removeCallbacks(poll);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.payment_success_title)
                .setMessage(getString(R.string.payment_success_msg, booking.code()))
                .setCancelable(false)
                .setPositiveButton(R.string.ok, (dialog, which) -> openMyBookings())
                .show();
    }

    private void openMyBookings() {
        MainActivity.openBookings(this);
        finish();
    }
}
