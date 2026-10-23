package com.nhom2.appbooking.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.model.Room;
import com.nhom2.appbooking.databinding.ActivityAdminRoomsBinding;
import com.nhom2.appbooking.databinding.DialogRoomFormBinding;
import com.nhom2.appbooking.databinding.ItemAdminRoomBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.BindingAdapter;
import com.nhom2.appbooking.ui.ImageLoader;
import com.nhom2.appbooking.ui.ImageParts;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.util.FormRules;
import com.nhom2.appbooking.util.Formatters;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

/** "Quản lý phòng" của một khách sạn: thêm, sửa, xóa, tải ảnh phòng (Drawer phòng trong AdminHotels trên web). */
public class AdminRoomsActivity extends AppCompatActivity {

    private static final String EXTRA_HOTEL_ID = "hotel_id";
    private static final String EXTRA_HOTEL_NAME = "hotel_name";
    private static final String IMAGE_MIME = "image/*";
    // Giá trị mặc định khi thêm phòng mới, như form trên web
    private static final int DEFAULT_CAPACITY = 2;
    private static final int DEFAULT_QUANTITY = 1;
    private static final int MIN_PRICE = 1;
    private static final int MIN_CAPACITY = 1;
    private static final int MIN_QUANTITY = 0;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    private ActivityAdminRoomsBinding binding;
    private BindingAdapter<Room, ItemAdminRoomBinding> adapter;
    private long hotelId;
    private Long pendingImageRoomId;

    public static void start(Context context, long hotelId, String hotelName) {
        context.startActivity(new Intent(context, AdminRoomsActivity.class)
                .putExtra(EXTRA_HOTEL_ID, hotelId)
                .putExtra(EXTRA_HOTEL_NAME, hotelName));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminRoomsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        hotelId = getIntent().getLongExtra(EXTRA_HOTEL_ID, -1L);
        String hotelName = getIntent().getStringExtra(EXTRA_HOTEL_NAME);
        binding.toolbar.setTitle(getString(R.string.admin_rooms_title, hotelName == null ? "" : hotelName));
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.list.textTitle.setText(R.string.admin_rooms_list_title);
        binding.list.buttonAdd.setText(R.string.admin_rooms_add);
        binding.list.buttonAdd.setVisibility(View.VISIBLE);
        binding.list.buttonAdd.setOnClickListener(v -> openForm(null));

        adapter = new BindingAdapter<>(ItemAdminRoomBinding::inflate, this::bind);
        binding.list.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.list.recycler.setAdapter(adapter);
        binding.list.swipe.setColorSchemeResources(R.color.primary);
        binding.list.swipe.setOnRefreshListener(this::load);
        load();
    }

    private void load() {
        binding.list.swipe.setRefreshing(true);
        ApiClient.get(this).getRooms(hotelId).enqueue(new ApiCallback<List<Room>>(this) {
            @Override
            protected void onSuccess(List<Room> rooms) {
                binding.list.swipe.setRefreshing(false);
                adapter.submit(rooms);
                binding.list.textEmpty.setText(R.string.admin_rooms_empty);
                binding.list.textEmpty.setVisibility(adapter.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            protected void onError(String message) {
                binding.list.swipe.setRefreshing(false);
                super.onError(getString(R.string.admin_rooms_load_failed));
            }
        });
    }

    private void bind(ItemAdminRoomBinding b, Room room) {
        ImageLoader.load(b.imageRoom, room.firstImage());
        b.textName.setText(room.name);
        b.textType.setText(room.type);
        b.textPrice.setText(room.pricePerNight == null ? "—"
                : getString(R.string.per_night, Formatters.money(room.pricePerNight)));
        b.textCounts.setText(getString(R.string.admin_rooms_counts, valueOr(room.capacity), valueOr(room.quantity)));

        b.buttonImage.setOnClickListener(v -> {
            pendingImageRoomId = room.id;
            pickImage.launch(IMAGE_MIME);
        });
        b.buttonEdit.setOnClickListener(v -> openForm(room));
        b.buttonDelete.setOnClickListener(v -> AdminDialogs.confirmDelete(this,
                R.string.admin_rooms_delete_title, R.string.admin_rooms_delete_body, () -> delete(room)));
    }

    private static int valueOr(Integer value) {
        return value == null ? 0 : value;
    }

    private void delete(Room room) {
        ApiClient.get(this).deleteRoom(room.id).enqueue(new ApiCallback<String>(this) {
            @Override
            protected void onSuccess(String body) {
                toast(R.string.admin_rooms_deleted);
                load();
            }
        });
    }

    private void onImagePicked(Uri uri) {
        Long roomId = pendingImageRoomId;
        pendingImageRoomId = null;
        if (uri == null || roomId == null) {
            return;
        }
        ImageParts.read(this, uri, new ImageParts.Callback() {
            @Override
            public void onReady(MultipartBody.Part part) {
                if (!isFinishing() && !isDestroyed()) {
                    upload(roomId, part);
                }
            }

            @Override
            public void onError(@StringRes int message) {
                toast(message);
            }
        });
    }

    private void upload(long roomId, MultipartBody.Part part) {
        binding.list.swipe.setRefreshing(true);
        ApiClient.get(this).uploadRoomImage(roomId, part).enqueue(new ApiCallback<ResponseBody>(this) {
            @Override
            protected void onSuccess(ResponseBody body) {
                toast(R.string.admin_hotels_image_uploaded);
                load();
            }

            @Override
            protected void onError(String message) {
                binding.list.swipe.setRefreshing(false);
                super.onError(message);
            }
        });
    }

    /** @param existing null = thêm phòng mới */
    private void openForm(Room existing) {
        DialogRoomFormBinding form = DialogRoomFormBinding.inflate(getLayoutInflater());
        if (existing == null) {
            form.editCapacity.setText(String.valueOf(DEFAULT_CAPACITY));
            form.editQuantity.setText(String.valueOf(DEFAULT_QUANTITY));
        } else {
            form.editName.setText(existing.name);
            form.editType.setText(existing.type);
            if (existing.pricePerNight != null) {
                form.editPrice.setText(String.valueOf(Math.round(existing.pricePerNight)));
            }
            form.editCapacity.setText(String.valueOf(valueOr(existing.capacity)));
            form.editQuantity.setText(String.valueOf(valueOr(existing.quantity)));
            form.editDescription.setText(existing.description);
        }
        int title = existing == null ? R.string.admin_rooms_create_title : R.string.admin_rooms_edit_title;
        AdminDialogs.showForm(this, getString(title), form.getRoot(), getString(R.string.admin_confirm), false,
                dialog -> submit(dialog, form, existing));
    }

    private void submit(AlertDialog dialog, DialogRoomFormBinding form, Room existing) {
        String name = UiUtils.text(form.editName);
        String type = UiUtils.text(form.editType);
        Double price = FormRules.numberAtLeast(UiUtils.text(form.editPrice), MIN_PRICE);
        Integer capacity = FormRules.intAtLeast(UiUtils.text(form.editCapacity), MIN_CAPACITY);
        Integer quantity = FormRules.intAtLeast(UiUtils.text(form.editQuantity), MIN_QUANTITY);
        String description = UiUtils.text(form.editDescription);

        boolean valid = AdminDialogs.requireText(form.layoutName, name, R.string.admin_rooms_name);
        valid &= AdminDialogs.requireText(form.layoutType, type, R.string.admin_rooms_type);
        form.layoutPrice.setError(price == null ? getString(R.string.admin_rooms_price_invalid) : null);
        form.layoutCapacity.setError(capacity == null ? getString(R.string.admin_rooms_capacity_invalid) : null);
        form.layoutQuantity.setError(quantity == null ? getString(R.string.admin_rooms_quantity_invalid) : null);
        if (!valid || price == null || capacity == null || quantity == null) {
            return;
        }

        ApiRequests.RoomUpsert body = new ApiRequests.RoomUpsert(name, type, price, capacity, quantity,
                description.isEmpty() ? null : description);
        Call<Room> call = existing == null
                ? ApiClient.get(this).createRoom(hotelId, body)
                : ApiClient.get(this).updateRoom(existing.id, body);
        int success = existing == null ? R.string.admin_rooms_created : R.string.admin_rooms_updated;
        AdminDialogs.setBusy(dialog, true);
        call.enqueue(new ApiCallback<Room>(this) {
            @Override
            protected void onSuccess(Room saved) {
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

    private void toast(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
