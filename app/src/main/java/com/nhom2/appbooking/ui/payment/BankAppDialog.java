package com.nhom2.appbooking.ui.payment;

import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.model.BankApp;
import com.nhom2.appbooking.data.model.BankAppList;
import com.nhom2.appbooking.databinding.DialogBankAppsBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.util.BankAppLinks;

import java.util.ArrayList;
import java.util.List;

/** Popup chọn app ngân hàng (danh sách deeplink Android của VietQR). */
public final class BankAppDialog {

    private static final String BANK_APPS_URL = "https://api.vietqr.io/v2/android-app-deeplinks";
    private static final int COLUMNS = 3;

    /** Danh sách ít thay đổi: tải một lần mỗi lần mở app. */
    private static List<BankApp> cachedApps;

    private BankAppDialog() {
    }

    public static void show(AppCompatActivity activity, BankAppAdapter.OnSelect onSelect) {
        DialogBankAppsBinding binding = DialogBankAppsBinding.inflate(activity.getLayoutInflater());
        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.bank_picker_title)
                .setView(binding.getRoot())
                .setNegativeButton(R.string.back, null)
                .create();

        List<BankApp> allApps = new ArrayList<>();
        BankAppAdapter adapter = new BankAppAdapter(app -> {
            dialog.dismiss();
            onSelect.onSelect(app);
        });
        binding.recyclerBanks.setLayoutManager(new GridLayoutManager(activity, COLUMNS));
        binding.recyclerBanks.setAdapter(adapter);
        binding.editSearchBank.addTextChangedListener(UiUtils.afterTextChanged(
                text -> adapter.submit(BankAppLinks.filter(allApps, text))));
        dialog.show();

        if (cachedApps != null) {
            binding.progressBanks.setVisibility(View.GONE);
            allApps.addAll(cachedApps);
            adapter.submit(cachedApps);
            return;
        }

        ApiClient.get(activity).getBankApps(BANK_APPS_URL).enqueue(new ApiCallback<BankAppList>(activity) {
            @Override
            protected void onSuccess(BankAppList body) {
                binding.progressBanks.setVisibility(View.GONE);
                if (body == null || body.apps == null || body.apps.isEmpty()) {
                    onError(activity.getString(R.string.bank_list_failed));
                    return;
                }
                cachedApps = BankAppLinks.sort(body.apps);
                allApps.addAll(cachedApps);
                adapter.submit(cachedApps);
            }

            @Override
            protected void onError(String message) {
                binding.progressBanks.setVisibility(View.GONE);
                binding.textBankError.setText(R.string.bank_list_failed);
                binding.textBankError.setVisibility(View.VISIBLE);
            }
        });
    }
}
