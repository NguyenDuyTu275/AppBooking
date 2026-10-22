package com.nhom2.appbooking.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.ResponseBody;
import retrofit2.Response;

/** Lỗi backend trả về dạng {"message": "...", "code": "error...."}. */
public final class ApiError {

    /** Thông báo đã dịch sẵn từ backend; null nếu thân lỗi không có. */
    public final String message;
    /** Mã lỗi nghiệp vụ (vd error.booking.notOwner); null nếu là lỗi mặc định của Spring. */
    public final String code;

    ApiError(String message, String code) {
        this.message = message;
        this.code = code;
    }

    public static ApiError from(Response<?> response) {
        try (ResponseBody body = response.errorBody()) {
            if (body != null) {
                return parse(body.string());
            }
        } catch (Exception ignored) {
            // Thân lỗi không đọc được: dùng thông báo mặc định
        }
        return new ApiError(null, null);
    }

    static ApiError parse(String json) {
        try {
            JsonElement element = JsonParser.parseString(json);
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                return new ApiError(text(object, "message"), text(object, "code"));
            }
        } catch (RuntimeException ignored) {
            // Không phải JSON
        }
        return new ApiError(null, null);
    }

    private static String text(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? null : value.getAsString();
    }
}
