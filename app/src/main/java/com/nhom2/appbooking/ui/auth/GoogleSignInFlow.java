package com.nhom2.appbooking.ui.auth;

import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.nhom2.appbooking.BuildConfig;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.SessionManager;
import com.nhom2.appbooking.ui.ApiCallback;

/**
 * Đăng nhập bằng Google giống website: xin Google một ID token cấp cho OAuth client "Web" mà backend kiểm tra
 * (google.client-id), rồi gửi token đó lên /auth/google để nhận JWT.
 *
 * Google chỉ trả token khi tên gói (applicationId com.nhom2.booking) và SHA-1 của khóa ký app đã được khai báo là
 * OAuth client "Android" trong cùng Google Cloud project với client "Web".
 */
final class GoogleSignInFlow {

    interface Listener {
        void onLoading(boolean loading);

        /** JWT đã được lưu vào phiên đăng nhập. */
        void onSignedIn();
    }

    private static final String TAG = "GoogleSignIn";

    private GoogleSignInFlow() {
    }

    static void start(AppCompatActivity activity, Listener listener) {
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(new GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build())
                .build();

        listener.onLoading(true);
        CredentialManager.create(activity).getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        String idToken = idTokenOf(response.getCredential());
                        if (idToken == null) {
                            fail(activity, listener, R.string.google_failed);
                        } else {
                            exchangeForJwt(activity, idToken, listener);
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        if (e instanceof GetCredentialCancellationException) {
                            listener.onLoading(false);
                        } else if (e instanceof NoCredentialException) {
                            fail(activity, listener, R.string.google_no_account);
                        } else {
                            // Hay gặp nhất: chưa khai báo OAuth client Android (tên gói + SHA-1) trên Google Cloud
                            Log.w(TAG, "Google sign-in failed", e);
                            fail(activity, listener, R.string.google_unavailable);
                        }
                    }
                });
    }

    private static String idTokenOf(Credential credential) {
        if (!(credential instanceof CustomCredential)
                || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
            Log.w(TAG, "Unexpected credential type: " + credential.getType());
            return null;
        }
        try {
            return GoogleIdTokenCredential.createFrom(credential.getData()).getIdToken();
        } catch (Exception e) {
            // createFrom (Kotlin) ném GoogleIdTokenParsingException nhưng không khai báo @Throws,
            // nên Java không cho bắt đích danh exception đó
            Log.w(TAG, "Unreadable Google credential", e);
            return null;
        }
    }

    private static void exchangeForJwt(AppCompatActivity activity, String idToken, Listener listener) {
        ApiClient.get(activity)
                .googleLogin(new ApiRequests.GoogleLogin(idToken))
                .enqueue(new ApiCallback<String>(activity) {
                    @Override
                    protected void onSuccess(String token) {
                        try {
                            new SessionManager(activity).save(token);
                        } catch (IllegalArgumentException e) {
                            onError(activity.getString(R.string.error_generic));
                            return;
                        }
                        listener.onSignedIn();
                    }

                    @Override
                    protected void onError(String message) {
                        listener.onLoading(false);
                        super.onError(message);
                    }
                });
    }

    private static void fail(AppCompatActivity activity, Listener listener, int messageRes) {
        listener.onLoading(false);
        Toast.makeText(activity, messageRes, Toast.LENGTH_LONG).show();
    }
}
