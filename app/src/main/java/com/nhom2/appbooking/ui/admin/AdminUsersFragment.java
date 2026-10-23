package com.nhom2.appbooking.ui.admin;

import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.model.UserProfile;
import com.nhom2.appbooking.databinding.DialogUserFormBinding;
import com.nhom2.appbooking.databinding.ItemAdminUserBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.BindingAdapter;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.util.FormRules;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;

/** Mục "Quản lý Người dùng": danh sách, thêm, sửa, xóa tài khoản (như AdminUsers trên web). */
public class AdminUsersFragment extends AdminListFragment {

    /** Web ẩn nút sửa / xóa của tài khoản quản trị gốc để không ai khóa mất quyền admin. */
    private static final String PROTECTED_USERNAME = "admin";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    private BindingAdapter<UserProfile, ItemAdminUserBinding> adapter;

    @Override
    protected int titleRes() {
        return R.string.admin_users_title;
    }

    @Override
    protected void setUp() {
        adapter = new BindingAdapter<>(ItemAdminUserBinding::inflate, this::bind);
        binding.recycler.setAdapter(adapter);
        showAddButton(R.string.admin_users_add, v -> openForm(null));
    }

    @Override
    protected void load() {
        setLoading(true);
        ApiClient.get(requireContext()).getUsers().enqueue(new ApiCallback<List<UserProfile>>(this) {
            @Override
            protected void onSuccess(List<UserProfile> users) {
                setLoading(false);
                adapter.submit(users);
                showEmpty(adapter.isEmpty(), R.string.admin_users_empty);
            }

            @Override
            protected void onError(String message) {
                setLoading(false);
                super.onError(getString(R.string.admin_users_load_failed));
            }
        });
    }

    private void bind(ItemAdminUserBinding b, UserProfile user) {
        String name = user.username == null ? "" : user.username;
        b.textAvatar.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.ROOT));
        b.textUsername.setText(name);
        b.textEmail.setText(user.email);
        b.textId.setText(user.id == null ? "" : getString(R.string.admin_id, user.id.intValue()));
        AdminStatusUi.role(b.textRole, user.role);

        boolean editable = user.id != null && !PROTECTED_USERNAME.equals(user.username);
        b.layoutActions.setVisibility(editable ? View.VISIBLE : View.GONE);
        b.buttonEdit.setOnClickListener(v -> openForm(user));
        b.buttonDelete.setOnClickListener(v -> AdminDialogs.confirmDelete(requireContext(),
                R.string.admin_users_delete_title, R.string.admin_users_delete_body, () -> delete(user)));
    }

    private void delete(UserProfile user) {
        ApiClient.get(requireContext()).deleteUser(user.id).enqueue(new ApiCallback<String>(this) {
            @Override
            protected void onSuccess(String body) {
                toast(R.string.admin_users_deleted);
                load();
            }
        });
    }

    /** @param existing null = thêm tài khoản mới */
    private void openForm(UserProfile existing) {
        DialogUserFormBinding form = DialogUserFormBinding.inflate(getLayoutInflater());
        if (existing != null) {
            form.editUsername.setText(existing.username);
            form.editUsername.setEnabled(false);
            form.editEmail.setText(existing.email);
            form.groupRole.check(ROLE_ADMIN.equals(existing.role) ? R.id.radio_admin : R.id.radio_user);
            // Sửa tài khoản không đổi mật khẩu (như web): để trống thì backend giữ mật khẩu cũ
            form.sectionPassword.setVisibility(View.GONE);
        }
        int title = existing == null ? R.string.admin_users_create_title : R.string.admin_users_edit_title;
        AdminDialogs.showForm(requireContext(), getString(title), form.getRoot(), getString(R.string.admin_confirm),
                false, dialog -> submit(dialog, form, existing));
    }

    private void submit(AlertDialog dialog, DialogUserFormBinding form, UserProfile existing) {
        String username = UiUtils.text(form.editUsername);
        String email = UiUtils.text(form.editEmail);
        String password = UiUtils.text(form.editPassword);
        String role = form.groupRole.getCheckedRadioButtonId() == R.id.radio_admin ? ROLE_ADMIN : ROLE_USER;

        boolean valid = AdminDialogs.requireText(form.layoutUsername, username, R.string.username);
        boolean emailOk = FormRules.isEmail(email);
        form.layoutEmail.setError(emailOk ? null : getString(R.string.admin_users_email_invalid));
        valid &= emailOk;
        if (existing == null) {
            valid &= AdminDialogs.requireText(form.layoutPassword, password, R.string.password);
        }
        if (!valid) {
            return;
        }

        Call<UserProfile> call;
        int success;
        if (existing == null) {
            call = ApiClient.get(requireContext()).createUser(
                    new ApiRequests.UserUpsert(null, username, email, password, role, null));
            success = R.string.admin_users_created;
        } else {
            // PUT lưu đè cả bản ghi: gửi lại ảnh đại diện để không bị xóa mất
            call = ApiClient.get(requireContext()).updateUser(existing.id,
                    new ApiRequests.UserUpsert(existing.id, existing.username, email, null, role, existing.avatarUrl));
            success = R.string.admin_users_updated;
        }
        AdminDialogs.setBusy(dialog, true);
        call.enqueue(new ApiCallback<UserProfile>(this) {
            @Override
            protected void onSuccess(UserProfile body) {
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
