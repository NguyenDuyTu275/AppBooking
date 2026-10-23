package com.nhom2.appbooking.ui.admin;

import android.view.View;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.model.BookingRequest;
import com.nhom2.appbooking.databinding.ItemAdminRequestBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.BindingAdapter;
import com.nhom2.appbooking.util.Formatters;

import java.util.List;

import retrofit2.Call;

/** Mục "Yêu cầu Đặt phòng": yêu cầu hủy / đổi ngày của khách, duyệt hoặc từ chối kèm phản hồi (như web). */
public class AdminRequestsFragment extends AdminListFragment {

    private static final String NO_RESPONSE = "—";
    private static final int[] FILTER_LABELS = {
            R.string.admin_filter_all,
            R.string.admin_requests_status_pending,
            R.string.admin_status_confirmed,
            R.string.admin_status_rejected,
    };
    private static final String[] FILTER_VALUES = {
            null,
            BookingRequest.STATUS_PENDING,
            BookingRequest.STATUS_APPROVED,
            BookingRequest.STATUS_REJECTED,
    };

    private BindingAdapter<BookingRequest, ItemAdminRequestBinding> adapter;
    private String statusFilter;

    @Override
    protected int titleRes() {
        return R.string.admin_menu_requests;
    }

    @Override
    protected void setUp() {
        adapter = new BindingAdapter<>(ItemAdminRequestBinding::inflate, this::bind);
        binding.recycler.setAdapter(adapter);
        showFilters(FILTER_LABELS, FILTER_VALUES, status -> {
            statusFilter = status;
            load();
        });
    }

    @Override
    protected void load() {
        setLoading(true);
        ApiClient.get(requireContext()).getBookingRequests(statusFilter)
                .enqueue(new ApiCallback<List<BookingRequest>>(this) {
                    @Override
                    protected void onSuccess(List<BookingRequest> requests) {
                        setLoading(false);
                        adapter.submit(requests);
                        showEmpty(adapter.isEmpty(), R.string.admin_requests_empty);
                    }

                    @Override
                    protected void onError(String message) {
                        setLoading(false);
                        super.onError(getString(R.string.admin_requests_load_failed));
                    }
                });
    }

    private void bind(ItemAdminRequestBinding b, BookingRequest request) {
        b.textBooking.setText(request.booking == null ? NO_RESPONSE
                : getString(R.string.admin_requests_booking_code, (int) request.booking.id));
        AdminStatusUi.requestType(b.textType, request.type);
        AdminStatusUi.requestStatus(b.textStatus, request.status);

        b.textNewDates.setVisibility(request.isChangeDate() ? View.VISIBLE : View.GONE);
        b.textNewDates.setText(getString(R.string.admin_requests_new_dates,
                Formatters.date(request.newCheckIn), Formatters.date(request.newCheckOut)));
        boolean hasReason = request.reason != null && !request.reason.trim().isEmpty();
        b.textReason.setText(getString(R.string.admin_requests_reason,
                hasReason ? request.reason : getString(R.string.admin_requests_no_reason)));
        boolean hasResponse = request.adminResponse != null && !request.adminResponse.trim().isEmpty();
        b.textResponse.setText(getString(R.string.admin_requests_admin_response,
                hasResponse ? request.adminResponse : NO_RESPONSE));

        b.layoutActions.setVisibility(request.isPending() ? View.VISIBLE : View.GONE);
        b.buttonApprove.setOnClickListener(v -> new AdminDialogs.TextDialog(requireContext())
                .title(getString(R.string.admin_requests_approve_title))
                .prompt(getString(R.string.admin_requests_response_prompt))
                .placeholder(getString(R.string.admin_requests_approve_placeholder))
                .positive(getString(R.string.admin_confirm_approve), false)
                .show((dialog, response) -> process(dialog, ApiClient.get(requireContext())
                        .approveBookingRequest(request.id, new ApiRequests.ProcessRequest(emptyToNull(response))),
                        R.string.admin_requests_approved)));
        b.buttonReject.setOnClickListener(v -> new AdminDialogs.TextDialog(requireContext())
                .title(getString(R.string.admin_requests_reject_title))
                .prompt(getString(R.string.admin_requests_response_prompt))
                .placeholder(getString(R.string.admin_requests_reject_placeholder))
                .required(R.string.admin_reason_required)
                .positive(getString(R.string.admin_confirm_reject), true)
                .show((dialog, response) -> process(dialog, ApiClient.get(requireContext())
                        .rejectBookingRequest(request.id, new ApiRequests.ProcessRequest(response)),
                        R.string.admin_requests_rejected)));
    }

    private static String emptyToNull(String text) {
        return text.isEmpty() ? null : text;
    }

    private void process(AlertDialog dialog, Call<BookingRequest> call, @StringRes int success) {
        AdminDialogs.setBusy(dialog, true);
        call.enqueue(new ApiCallback<BookingRequest>(this) {
            @Override
            protected void onSuccess(BookingRequest body) {
                dialog.dismiss();
                toast(success);
                load();
            }

            @Override
            protected void onError(String message) {
                AdminDialogs.setBusy(dialog, false);
                super.onError(message);
            }
        });
    }
}
