package com.nhom2.appbooking.ui.chat;

import android.net.Uri;

final class AssistantMessage {
    final String text;
    final boolean fromUser;
    final Uri attachmentUri;
    final String attachmentName;
    final boolean imageAttachment;

    AssistantMessage(String text, boolean fromUser, Uri attachmentUri,
                     String attachmentName, boolean imageAttachment) {
        this.text = text;
        this.fromUser = fromUser;
        this.attachmentUri = attachmentUri;
        this.attachmentName = attachmentName;
        this.imageAttachment = imageAttachment;
    }
}
