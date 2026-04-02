package com.orderapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orderapp.api.TelegramBotAPIClient;
import com.orderapp.api.TelegramBotService;
import com.orderapp.api.TelegramResponse;
import com.orderapp.model.OrderData;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Debug broadcast receiver for ADB testing
 * Allows triggering order submission via: adb shell am broadcast -a com.orderapp.TEST_SUBMIT
 */
public class DebugBroadcastReceiver extends BroadcastReceiver {
    
    private static final String TAG = "DebugBroadcastReceiver";
    public static final String TEST_ACTION = "com.orderapp.TEST_SUBMIT";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TEST_ACTION.equals(intent.getAction())) {
            Log.d(TAG, "Test broadcast received, submitting sample order...");
            submitSampleOrder(context);
        }
    }
    
    private void submitSampleOrder(Context context) {
        // Create sample OrderData for testing
        OrderData sampleData = new OrderData(
                "Test Shop ADB",
                "24/03/2026",
                "Test Salesman ADB",
                100,
                "Test specification via ADB debugging",
                250.50,
                "Test Location ADB",
                "9876543210"
        );
        
        Log.d(TAG, "Sample data created: " + new Gson().toJson(sampleData));
        
        // Send to Telegram
        TelegramBotService service = TelegramBotAPIClient.createService();
        String botToken = TelegramBotAPIClient.getBotToken();
        String chatId = TelegramBotAPIClient.getChatId();
        
        Log.d(TAG, "Bot Token: " + botToken);
        Log.d(TAG, "Chat ID: " + chatId);
        
        String caption = buildTelegramCaption(sampleData);
        String jsonData = new Gson().toJson(sampleData);
        String jsonFileName = buildJsonFileName(sampleData);
        Log.d(TAG, "Caption to send: " + caption);
        Log.d(TAG, "JSON filename: " + jsonFileName);
        
        Call<TelegramResponse> call = service.sendDocument(
                botToken,
            createFormPart(chatId),
            createJsonDocumentPart(jsonData, jsonFileName),
            createFormPart(caption),
            createFormPart("HTML")
        );
        
        call.enqueue(new Callback<TelegramResponse>() {
            @Override
            public void onResponse(Call<TelegramResponse> call, Response<TelegramResponse> response) {
                String logMsg = "Response Code: " + response.code() + ", Success: " + 
                        (response.body() != null ? response.body().isSuccess() : "null");
                Log.d(TAG, logMsg);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "SUCCESS: Order submitted to Telegram!");
                    Toast.makeText(context, "✓ Order submitted successfully via ADB!", Toast.LENGTH_LONG).show();
                } else {
                    String errorMsg = response.body() != null ? response.body().getDescription() : 
                            "Unknown error (Code: " + response.code() + ")";
                    Log.e(TAG, "ERROR: " + errorMsg);
                    Toast.makeText(context, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<TelegramResponse> call, Throwable t) {
                String errorDetail = t.getMessage() != null ? t.getMessage() : "Network error";
                Log.e(TAG, "NETWORK ERROR: " + errorDetail, t);
                Toast.makeText(context, "Network Error: " + errorDetail, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private String formatOrderDataMessage(OrderData data) {
        return String.format(
                "<b>📦 TEST ORDER VIA ADB 📦</b>\n\n" +
                "<b>Shop Name:</b> %s\n" +
                "<b>Order Date:</b> %s\n" +
                "<b>Salesman:</b> %s\n" +
                "<b>Number of Boxes:</b> %d\n" +
                "<b>Specification:</b> %s\n" +
                "<b>Amount per Box:</b> ₹ %.2f\n" +
                "<b>Total Amount:</b> ₹ %.2f\n" +
                "<b>Location:</b> %s\n" +
                "<b>Phone:</b> %s\n" +
                "<b>Submitted at:</b> %s",
                data.getShopName(),
                data.getOrderDate(),
                data.getSalesmanName(),
                data.getNumberOfBoxes(),
                data.getSpecification().isEmpty() ? "N/A" : data.getSpecification(),
                data.getAmountPerBox(),
                data.getTotalAmount(),
                data.getLocation(),
                data.getPhoneNumber(),
                data.getSubmissionTime()
        );
    }

    private String buildTelegramCaption(OrderData data) {
        String message = formatOrderDataMessage(data);
        if (message.length() > 1024) {
            return message.substring(0, 1021) + "...";
        }
        return message;
    }

    private String buildJsonFileName(OrderData data) {
        String shopName = data.getShopName() == null ? "order" : data.getShopName().trim();
        String safeShopName = shopName.replaceAll("[^a-zA-Z0-9_-]+", "_").replaceAll("_+", "_");
        if (safeShopName.isEmpty()) {
            safeShopName = "order";
        }
        String timestamp = data.getSubmissionTime()
                .replace(" ", "_")
                .replace(":", "-");
        return safeShopName + "_" + timestamp + ".json";
    }

    private MultipartBody.Part createJsonDocumentPart(String jsonData, String fileName) {
        RequestBody documentBody = RequestBody.create(
                jsonData,
                MediaType.get("application/json; charset=utf-8")
        );
        return MultipartBody.Part.createFormData("document", fileName, documentBody);
    }

    private RequestBody createFormPart(String value) {
        return RequestBody.create(value, MultipartBody.FORM);
    }
}
