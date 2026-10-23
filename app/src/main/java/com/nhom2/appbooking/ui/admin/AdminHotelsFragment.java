package com.nhom2.appbooking.ui.admin;

import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.model.Hotel;
import com.nhom2.appbooking.databinding.DialogHotelFormBinding;
import com.nhom2.appbooking.databinding.ItemAdminHotelBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.BindingAdapter;
import com.nhom2.appbooking.ui.ImageLoader;
import com.nhom2.appbooking.ui.ImageParts;
import com.nhom2.appbooking.ui.UiUtils;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

/** Mục "Quản lý Khách sạn": thêm, sửa, xóa, tải ảnh, quản lý phòng (như AdminHotels trên web). */
public class AdminHotelsFragment extends AdminListFragment {

    private static final String IMAGE_MIME = "image/*";

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    private BindingAdapter<Hotel, ItemAdminHotelBinding> adapter;
    private Long pendingImageHotelId;

    @Override
    protected int titleRes() {
        return R.string.admin_menu_hotels;
    }

    @Override
    protected void setUp() {
        adapter = new BindingAdapter<>(ItemAdminHotelBinding::inflate, this::bind);
        binding.recycler.setAdapter(adapter);
        showAddButton(R.string.admin_hotels_add, v -> openForm(null));
    }

    @Override
    protected void load() {
        setLoading(true);
        ApiClient.get(requireContext()).getHotels().enqueue(new ApiCallback<List<Hotel>>(this) {
            @Override
            protected void onSuccess(List<Hotel> hotels) {
                setLoading(false);
                adapter.submit(hotels);
                showEmpty(adapter.isEmpty(), R.string.admin_hotels_empty);
            }

            @Override
            protected void onError(String message) {
                setLoading(false);
                super.onError(getString(R.string.admin_hotels_load_failed));
            }
        });
    }

    private void bind(ItemAdminHotelBinding b, Hotel hotel) {
        ImageLoader.load(b.imageHotel, hotel.coverImage());
        b.textName.setText(hotel.name);
        b.textCity.setText(hotel.city);
        AdminStatusUi.hotelRating(b.textRating, hotel.ratingAvg);
        b.textId.setText(getString(R.string.admin_id, (int) hotel.id));

        b.buttonRooms.setOnClickListener(v -> AdminRoomsActivity.start(requireContext(), hotel.id, hotel.name));
        b.buttonImage.setOnClickListener(v -> {
            pendingImageHotelId = hotel.id;
            pickImage.launch(IMAGE_MIME);
        });
        b.buttonEdit.setOnClickListener(v -> openForm(hotel));
        b.buttonDelete.setOnClickListener(v -> AdminDialogs.confirmDelete(requireContext(),
                R.string.admin_hotels_delete_title, R.string.admin_hotels_delete_body, () -> delete(hotel)));
    }

    private void delete(Hotel hotel) {
        ApiClient.get(requireContext()).deleteHotel(hotel.id).enqueue(new ApiCallback<String>(this) {
            @Override
            protected void onSuccess(String body) {
                toast(R.string.admin_hotels_deleted);
                load();
            }
        });
    }

    private void onImagePicked(Uri uri) {
        Long hotelId = pendingImageHotelId;
        pendingImageHotelId = null;
        if (uri == null || hotelId == null) {
            return;
        }
        ImageParts.read(requireContext(), uri, new ImageParts.Callback() {
            @Override
            public void onReady(MultipartBody.Part part) {
                if (isAdded()) {
                    upload(hotelId, part);
                }
            }

            @Override
            public void onError(@StringRes int message) {
                toast(message);
            }
        });
    }

    private void upload(long hotelId, MultipartBody.Part part) {
        setLoading(true);
        ApiClient.get(requireContext()).uploadHotelImage(hotelId, part).enqueue(new ApiCallback<ResponseBody>(this) {
            @Override
            protected void onSuccess(ResponseBody body) {
                toast(R.string.admin_hotels_image_uploaded);
                load();
            }

            @Override
            protected void onError(String message) {
                setLoading(false);
                super.onError(message);
            }
        });
    }

    /** @param existing null = thêm khách sạn mới */
    private void openForm(Hotel existing) {
        DialogHotelFormBinding form = DialogHotelFormBinding.inflate(getLayoutInflater());
        if (existing != null) {
            form.editName.setText(existing.name);
            form.editCity.setText(existing.city);
            form.editAddress.setText(existing.address);
            form.editDescription.setText(existing.description);
        }
        int title = existing == null ? R.string.admin_hotels_create_title : R.string.admin_hotels_edit_title;
        AdminDialogs.showForm(requireContext(), getString(title), form.getRoot(), getString(R.string.admin_confirm),
                false, dialog -> submit(dialog, form, existing));
    }

    private void submit(AlertDialog dialog, DialogHotelFormBinding form, Hotel existing) {
        String name = UiUtils.text(form.editName);
        String city = UiUtils.text(form.editCity);
        String address = UiUtils.text(form.editAddress);
        String description = UiUtils.text(form.editDescription);
        // Web bắt buộc nhập cả 4 ô
        boolean valid = AdminDialogs.requireText(form.layoutName, name, R.string.admin_hotels_name);
        valid &= AdminDialogs.requireText(form.layoutCity, city, R.string.admin_hotels_city);
        valid &= AdminDialogs.requireText(form.layoutAddress, address, R.string.admin_hotels_address);
        valid &= AdminDialogs.requireText(form.layoutDescription, description, R.string.admin_hotels_description);
        if (!valid) {
            return;
        }

        ApiRequests.HotelUpsert body = new ApiRequests.HotelUpsert(existing, name, city, address, description);
        Call<Hotel> call = existing == null
                ? ApiClient.get(requireContext()).createHotel(body)
                : ApiClient.get(requireContext()).updateHotel(existing.id, body);
        int success = existing == null ? R.string.admin_hotels_created : R.string.admin_hotels_updated;
        AdminDialogs.setBusy(dialog, true);
        call.enqueue(new ApiCallback<Hotel>(this) {
            @Override
            protected void onSuccess(Hotel saved) {
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
