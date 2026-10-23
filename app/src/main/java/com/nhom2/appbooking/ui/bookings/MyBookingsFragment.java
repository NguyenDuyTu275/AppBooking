package com.nhom2.appbooking.ui.bookings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.model.Booking;
import com.nhom2.appbooking.databinding.FragmentMyBookingsBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.DownloadSaver;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.ui.payment.PaymentActivity;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

/** Tab "Đơn của tôi": đơn mới nhất lên đầu, thanh toán hoặc hủy đơn chưa trả tiền. */
public class MyBookingsFragment extends Fragment {

    private static final String PDF_TYPE = "application/pdf";

    // Khách chọn nơi lưu qua hộp thoại của hệ thống, app không cần quyền bộ nhớ
    private final ActivityResultLauncher<String> saveInvoice =
            registerForActivityResult(new ActivityResultContracts.CreateDocument(PDF_TYPE), this::downloadInvoice);

    private FragmentMyBookingsBinding binding;
    private BookingAdapter adapter;
    private Long pendingInvoiceId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMyBookingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new BookingAdapter(new BookingAdapter.Listener() {
            @Override
            public void onPay(Booking booking) {
                PaymentActivity.start(requireContext(), booking.id);
            }

            @Override
            public void onCancel(Booking booking) {
                confirmCancel(booking);
            }

            @Override
            public void onInvoice(Booking booking) {
                pendingInvoiceId = booking.id;
                saveInvoice.launch(getString(R.string.invoice_file, (int) booking.id));
            }
        });
        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);
        binding.swipe.setColorSchemeResources(R.color.primary);
        binding.swipe.setOnRefreshListener(this::load);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại mỗi lần quay về tab, vd sau khi vừa thanh toán
        load();
    }

    private void load() {
        binding.swipe.setRefreshing(true);
        ApiClient.get(requireContext()).getMyBookings().enqueue(new ApiCallback<List<Booking>>(this) {
            @Override
            protected void onSuccess(List<Booking> bookings) {
                binding.swipe.setRefreshing(false);
                List<Booking> sorted = bookings == null ? new ArrayList<>() : new ArrayList<>(bookings);
                sorted.sort((a, b) -> Long.compare(b.id, a.id));
                adapter.submit(sorted);
                UiUtils.animateListIn(binding.recycler);
                binding.textEmpty.setVisibility(sorted.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            protected void onError(String message) {
                binding.swipe.setRefreshing(false);
                super.onError(message);
            }
        });
    }

    private void confirmCancel(Booking booking) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cancel_confirm_title)
                .setMessage(getString(R.string.cancel_confirm_msg, booking.code()))
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.cancel_booking, (dialog, which) -> cancel(booking))
                .show();
    }

    private void cancel(Booking booking) {
        ApiClient.get(requireContext()).cancelBooking(booking.id).enqueue(new ApiCallback<Booking>(this) {
            @Override
            protected void onSuccess(Booking body) {
                Toast.makeText(requireContext(), R.string.cancelled, Toast.LENGTH_SHORT).show();
                load();
            }
        });
    }

    private void downloadInvoice(Uri target) {
        Long bookingId = pendingInvoiceId;
        pendingInvoiceId = null;
        if (target == null || bookingId == null) {
            return;
        }
        ApiClient.get(requireContext()).downloadInvoice(bookingId).enqueue(new ApiCallback<ResponseBody>(this) {
            @Override
            protected void onSuccess(ResponseBody body) {
                DownloadSaver.save(requireContext(), body, target, saved -> invoiceSaved(saved, target));
            }

            @Override
            protected void onError(String message) {
                super.onError(getString(R.string.invoice_failed));
            }
        });
    }

    private void invoiceSaved(boolean saved, Uri file) {
        if (binding == null) {
            return;
        }
        if (!saved) {
            Toast.makeText(requireContext(), R.string.invoice_failed, Toast.LENGTH_LONG).show();
            return;
        }
        Snackbar.make(binding.getRoot(), R.string.invoice_saved, Snackbar.LENGTH_LONG)
                .setAction(R.string.invoice_open, v -> openPdf(file))
                .show();
    }

    private void openPdf(Uri file) {
        Intent view = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(file, PDF_TYPE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(view);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), R.string.no_pdf_viewer, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
