package com.nhom2.appbooking.util;

import com.nhom2.appbooking.data.model.BankApp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Deeplink VietQR mở app ngân hàng (https://www.vietqr.io/danh-sach-api/deeplink-app-ngan-hang).
 * Backend dựng sẵn phần thanh toán (ba, am, tn, bn); app chỉ thêm app khách chọn và link quay lại.
 */
public final class BankAppLinks {

    private BankAppLinks() {
    }

    public static String build(String baseDeeplink, String appId, String returnUrl) {
        return baseDeeplink + "&app=" + encode(appId) + "&url=" + encode(returnUrl);
    }

    /** App tự điền thông tin lên trước, sau đó app nhiều người dùng hơn. */
    public static List<BankApp> sort(List<BankApp> apps) {
        List<BankApp> sorted = new ArrayList<>(apps);
        sorted.sort((a, b) -> {
            if (a.isAutofill() != b.isAutofill()) {
                return a.isAutofill() ? -1 : 1;
            }
            return Long.compare(b.monthlyInstall, a.monthlyInstall);
        });
        return sorted;
    }

    public static List<BankApp> filter(List<BankApp> apps, String query) {
        List<BankApp> result = new ArrayList<>();
        for (BankApp app : apps) {
            if (TextSearch.matches(query, app.appName, app.bankName, app.appId)) {
                result.add(app);
            }
        }
        return result;
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
