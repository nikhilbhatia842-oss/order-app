package com.orderapp.api;

import com.google.gson.annotations.SerializedName;

/**
 * Response from Telegram Bot API
 */
public class TelegramResponse {
    @SerializedName("ok")
    private boolean success;

    @SerializedName("result")
    private TelegramMessage result;

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

    public TelegramMessage getResult() {
        return result;
    }

    public void setResult(TelegramMessage result) {
        this.result = result;
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