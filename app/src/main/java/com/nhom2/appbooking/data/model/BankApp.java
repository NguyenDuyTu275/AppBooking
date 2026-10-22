package com.nhom2.appbooking.data.model;

/** Một app ngân hàng trong danh sách deeplink của VietQR. */
public class BankApp {
    public String appId;
    public String appName;
    public String bankName;
    public String appLogo;
    /** 1 = app tự điền STK, số tiền, nội dung; 0 = chỉ mở được app. */
    public int autofill;
    public long monthlyInstall;

    public boolean isAutofill() {
        return autofill == 1;
    }
}
