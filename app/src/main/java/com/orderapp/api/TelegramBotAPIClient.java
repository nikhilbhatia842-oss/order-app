package com.orderapp.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Telegram Bot API Client configuration
 */
public class TelegramBotAPIClient {
    
    // Placeholder - Replace with your actual bot token from BotFather
    private static final String BOT_TOKEN = "8725847412:AAEr9HFaX5NRMjsEILRm2hldElGarYukx8g";
    
    // Placeholder - Replace with your actual chat ID or group ID
    private static final String CHAT_ID = "8353059211";
    
    private static final String BASE_URL = "https://api.telegram.org/";
    
    public static String getBotToken() {
        return BOT_TOKEN;
    }
    
    public static String getChatId() {
        return CHAT_ID;
    }
    
    /**
     * Create a configured Telegram API service
     */
    public static TelegramBotService createService() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addNetworkInterceptor((chain) -> {
                    android.util.Log.d("TelegramAPI", "Request URL: " + chain.request().url());
                    return chain.proceed(chain.request());
                })
                .build();
        
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .build();
        
        return retrofit.create(TelegramBotService.class);
    }
}
