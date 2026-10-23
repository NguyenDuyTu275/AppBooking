package com.nhom2.appbooking.ui.admin;

import android.graphics.Typeface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.model.Review;
import com.nhom2.appbooking.data.model.ReviewPage;
import com.nhom2.appbooking.databinding.ItemAdminReviewBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.BindingAdapter;
import com.nhom2.appbooking.util.Formatters;

import java.util.Collections;
import java.util.List;

/** Mục "Đánh giá": admin trả lời / sửa / xóa phản hồi thay khách sạn (như AdminReviews trên web). */
public class AdminReviewsFragment extends AdminListFragment {

    private static final int PAGE_SIZE = 20;
    /** Bằng ReviewService.MAX_REPLY_LENGTH của backend. */
    private static final int MAX_REPLY_LENGTH = 2000;
    private static final int COLLAPSED_LINES = 3;
    private static final String NO_VALUE = "—";

    private BindingAdapter<Review, ItemAdminReviewBinding> adapter;
    private boolean onlyUnanswered = true;
    private int loadedPage = -1;
    private boolean hasMore;
    private boolean loadingPage;

    @Override
    protected int titleRes() {
        return R.string.admin_reviews_title;
    }

    @Override
    protected void setUp() {
        adapter = new BindingAdapter<>(ItemAdminReviewBinding::inflate, this::bind);
        binding.recycler.setAdapter(adapter);
        showSwitch(R.string.admin_reviews_only_unanswered, onlyUnanswered, checked -> {
            onlyUnanswered = checked;
            load();
        });
        // Tải trang tiếp theo khi cuộn tới cuối (thay cho thanh phân trang của bảng trên web)
        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && !recyclerView.canScrollVertically(1) && hasMore && !loadingPage) {
                    loadPage(loadedPage + 1);
                }
            }
        });
    }

    @Override
    protected void load() {
        loadPage(0);
    }

    private void loadPage(int page) {
        loadingPage = true;
        setLoading(true);
        ApiClient.get(requireContext()).getAdminReviews(page, PAGE_SIZE, onlyUnanswered)
                .enqueue(new ApiCallback<ReviewPage>(this) {
                    @Override
                    protected void onSuccess(ReviewPage result) {
                        loadingPage = false;
                        setLoading(false);
                        List<Review> reviews = result == null || result.content == null
                                ? Collections.emptyList() : result.content;
                        if (page == 0) {
                            adapter.submit(reviews);
                        } else {
                            adapter.append(reviews);
                        }
                        loadedPage = page;
                        hasMore = result != null && result.hasMore();
                        showEmpty(adapter.isEmpty(), R.string.admin_reviews_empty);
                    }

                    @Override
                    protected void onError(String message) {
                        loadingPage = false;
                        setLoading(false);
                        super.onError(getString(R.string.admin_reviews_load_failed));
                    }
                });
    }

    private void bind(ItemAdminReviewBinding b, Review review) {
        b.textHotel.setText(review.hotelName == null ? NO_VALUE : review.hotelName);
        AdminStatusUi.reviewScore(b.textScore, review.rating);
        b.textMeta.setText(getString(R.string.admin_reviews_meta,
                review.username == null ? NO_VALUE : review.username, Formatters.date(review.createdAt)));

        boolean hasComment = review.comment != null && !review.comment.trim().isEmpty();
        b.textComment.setText(hasComment ? review.comment : getString(R.string.admin_reviews_no_comment));
        b.textComment.setTypeface(null, hasComment ? Typeface.NORMAL : Typeface.ITALIC);
        b.textComment.setMaxLines(COLLAPSED_LINES);
        // Chạm để xem hết nhận xét dài (như "Xem thêm" trên web)
        b.textComment.setOnClickListener(v -> b.textComment.setMaxLines(
                b.textComment.getMaxLines() == COLLAPSED_LINES ? Integer.MAX_VALUE : COLLAPSED_LINES));

        boolean replied = review.hasReply();
        b.textReply.setVisibility(replied ? View.VISIBLE : View.GONE);
        b.textReply.setText(replied ? getString(R.string.admin_reviews_reply, review.hotelReply) : null);
        b.buttonReply.setText(replied ? R.string.admin_edit : R.string.admin_reviews_answer);
        b.buttonRemoveReply.setVisibility(replied ? View.VISIBLE : View.GONE);

        b.buttonReply.setOnClickListener(v -> new AdminDialogs.TextDialog(requireContext())
                .title(getString(R.string.admin_reviews_reply_title, review.username == null ? "" : review.username))
                .quoted(getString(R.string.admin_reviews_meta,
                        getString(R.string.admin_reviews_score, review.rating == null ? 0 : review.rating),
                        hasComment ? review.comment : getString(R.string.admin_reviews_no_comment)))
                .placeholder(getString(R.string.admin_reviews_reply_placeholder))
                .initial(review.hotelReply)
                .maxLength(MAX_REPLY_LENGTH)
                .required(R.string.admin_reviews_reply_required)
                .positive(getString(R.string.admin_reviews_send), false)
                .show((dialog, reply) -> reply(dialog, review, reply)));
        b.buttonRemoveReply.setOnClickListener(v -> AdminDialogs.confirmDelete(requireContext(),
                R.string.admin_reviews_remove_title, R.string.admin_reviews_remove_body, () -> removeReply(review)));
    }

    private void reply(AlertDialog dialog, Review review, String reply) {
        AdminDialogs.setBusy(dialog, true);
        ApiClient.get(requireContext()).replyReview(review.id, new ApiRequests.ReviewReply(reply))
                .enqueue(new ApiCallback<Review>(this) {
                    @Override
                    protected void onSuccess(Review body) {
                        dialog.dismiss();
                        toast(R.string.admin_reviews_replied);
                        load();
                    }

                    @Override
                    protected void onError(String message) {
                        AdminDialogs.setBusy(dialog, false);
                        super.onError(message);
                    }
                });
    }

    private void removeReply(Review review) {
        ApiClient.get(requireContext()).removeReviewReply(review.id).enqueue(new ApiCallback<Review>(this) {
            @Override
            protected void onSuccess(Review body) {
                toast(R.string.admin_reviews_removed);
                load();
            }
        });
    }
}
