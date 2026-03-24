package com.orderapp.api;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

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
}