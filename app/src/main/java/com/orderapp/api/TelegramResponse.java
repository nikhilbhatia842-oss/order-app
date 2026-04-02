package com.orderapp.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Response from Telegram Bot API
 */
public class TelegramResponse {
    @SerializedName("ok")
    private boolean success;

    /**
     * Flexible result field: Telegram returns a Message object for sendDocument/sendMessage,
     * but a plain boolean (true) for deleteMessage. Using JsonElement handles both cases.
     */
    @SerializedName("result")
    private JsonElement result;

    @SerializedName("error_code")
    private int errorCode;

    @SerializedName("description")
    private String description;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public JsonElement getResult() {
        return result;
    }

    /**
     * Returns a TelegramMessage (with message_id) when result is a JSON object,
     * or null for responses where result is a boolean (e.g. deleteMessage).
     */
    public TelegramMessage getResultAsMessage() {
        if (result != null && result.isJsonObject()) {
            JsonObject obj = result.getAsJsonObject();
            if (obj.has("message_id")) {
                TelegramMessage msg = new TelegramMessage();
                msg.setMessageId(obj.get("message_id").getAsInt());
                return msg;
            }
        }
        return null;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Telegram message result
     */
    public static class TelegramMessage {
        @SerializedName("message_id")
        private int messageId;

        public int getMessageId() {
            return messageId;
        }

        public void setMessageId(int messageId) {
            this.messageId = messageId;
        }
    }
}