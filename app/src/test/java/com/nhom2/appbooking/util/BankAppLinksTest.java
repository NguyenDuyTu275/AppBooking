package com.nhom2.appbooking.util;

import static org.junit.Assert.assertEquals;

import com.nhom2.appbooking.data.model.BankApp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BankAppLinksTest {

    private static final String BASE =
            "https://dl.vietqr.io/pay?ba=10001473192@tpb&am=1500000&tn=DH+42&bn=NGUYEN+DUY+TU";

    @Test
    public void addsChosenAppAndEncodedReturnUrl() {
        String link = BankAppLinks.build(BASE, "mb", "https://web.vn/booking/payment?bookingId=42");

        assertEquals(BASE + "&app=mb&url=https%3A%2F%2Fweb.vn%2Fbooking%2Fpayment%3FbookingId%3D42", link);
    }

    @Test
    public void appsThatPrefillComeFirstThenMostInstalled() {
        List<BankApp> sorted = BankAppLinks.sort(Arrays.asList(
                app("tpb", 0, 90_000), app("acb", 1, 60_000), app("mb", 1, 400_000)));

        assertEquals(Arrays.asList("mb", "acb", "tpb"), ids(sorted));
    }

    @Test
    public void filterMatchesAppOrBankNameWithoutMarks() {
        BankApp tpb = app("tpb", 0, 1);
        tpb.appName = "TPBank Mobile";
        tpb.bankName = "Ngân hàng TMCP Tiên Phong";
        BankApp mb = app("mb", 1, 1);
        mb.appName = "MB Bank";
        mb.bankName = "Ngân hàng TMCP Quân đội";

        assertEquals(Arrays.asList("tpb"), ids(BankAppLinks.filter(Arrays.asList(tpb, mb), "tien phong")));
        assertEquals(Arrays.asList("mb"), ids(BankAppLinks.filter(Arrays.asList(tpb, mb), "MB")));
    }

    private static BankApp app(String id, int autofill, long installs) {
        BankApp app = new BankApp();
        app.appId = id;
        app.appName = id;
        app.autofill = autofill;
        app.monthlyInstall = installs;
        return app;
    }

    private static List<String> ids(List<BankApp> apps) {
        List<String> ids = new ArrayList<>();
        for (BankApp app : apps) {
            ids.add(app.appId);
        }
        return ids;
    }
}
