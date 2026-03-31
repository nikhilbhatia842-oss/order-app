package com.orderapp.api;

import retrofit2.Call;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Part;

/**
 * Telegram Bot API Service interface
 */
public interface TelegramBotService {
    
    @FormUrlEncoded
    @POST("bot{botToken}/sendMessage")
    Call<TelegramResponse> sendMessage(
            @Path(value = "botToken", encoded = true) String botToken,
            @Field("chat_id") String chatId,
            @Field("text") String text,
            @Field("parse_mode") String parseMode
    );

        @Multipart
        @POST("bot{botToken}/sendDocument")
        Call<TelegramResponse> sendDocument(
            @Path(value = "botToken", encoded = true) String botToken,
            @Part("chat_id") RequestBody chatId,
            @Part MultipartBody.Part document,
            @Part("caption") RequestBody caption,
            @Part("parse_mode") RequestBody parseMode
        );

    @FormUrlEncoded
    @POST("bot{botToken}/deleteMessage")
    Call<TelegramResponse> deleteMessage(
            @Path(value = "botToken", encoded = true) String botToken,
            @Field("chat_id") String chatId,
            @Field("message_id") int messageId
    );
}