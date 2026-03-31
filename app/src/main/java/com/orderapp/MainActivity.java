package com.orderapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.orderapp.api.TelegramBotAPIClient;
import com.orderapp.api.TelegramBotService;
import com.orderapp.api.TelegramResponse;
import com.orderapp.model.OrderData;
import com.orderapp.validator.FormDataValidator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    
    private EditText etShopName;
    private EditText etOrderDate;
    private EditText etSalesmanName;
    private EditText etNumberOfBoxes;
    private EditText etSpecification;
    private EditText etAmountPerBox;
    private TextView tvTotalAmount;
    private EditText etLocation;
    private EditText etPhoneNumber;
    private Button btnSubmit;
    private AlertDialog progressDialog;
    private ProgressBar progressSendingBar;
    private ImageView ivSuccessTick;
    private int lastSubmittedMessageId = -1;
    private CountDownTimer undoCountDownTimer;

    private DebugBroadcastReceiver debugReceiver;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupListeners();
        registerDebugReceiver();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (undoCountDownTimer != null) {
            undoCountDownTimer.cancel();
        }
        if (debugReceiver != null) {
            try {
                unregisterReceiver(debugReceiver);
            } catch (IllegalArgumentException e) {
                android.util.Log.d("MainActivity", "Debug receiver already unregistered");
            }
        }
    }
    
    private void registerDebugReceiver() {
        debugReceiver = new DebugBroadcastReceiver();
        IntentFilter filter = new IntentFilter(DebugBroadcastReceiver.TEST_ACTION);
        try {
            registerReceiver(debugReceiver, filter, Context.RECEIVER_EXPORTED);
            android.util.Log.d("MainActivity", "Debug broadcast receiver registered");
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to register debug receiver", e);
        }
    }
    
    private void initializeViews() {
        etShopName = findViewById(R.id.et_shop_name);
        etOrderDate = findViewById(R.id.et_order_date);
        etSalesmanName = findViewById(R.id.et_salesman_name);
        etNumberOfBoxes = findViewById(R.id.et_number_of_boxes);
        etSpecification = findViewById(R.id.et_specification);
        etAmountPerBox = findViewById(R.id.et_amount_per_box);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        etLocation = findViewById(R.id.et_location);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        btnSubmit = findViewById(R.id.btn_submit);
    }
    
    private void setupListeners() {
        // Date picker for order date
        etOrderDate.setOnClickListener(v -> showDatePicker());
        
        // Auto-calculate total amount
        TextWatcher numberWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                calculateTotalAmount();
            }
        };
        
        etNumberOfBoxes.addTextChangedListener(numberWatcher);
        etAmountPerBox.addTextChangedListener(numberWatcher);
        
        // Submit button
        btnSubmit.setOnClickListener(v -> showConfirmationDialog());
    }

    private void showConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("Confirm Submission")
                .setMessage("Are you sure you want to submit this order?")
                .setPositiveButton("Yes, Submit", (dialog, which) -> submitForm())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    private void showDatePicker() {
        new DatePickerDialog(
                MainActivity.this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    etOrderDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
    
    private void calculateTotalAmount() {
        try {
            String boxesStr = etNumberOfBoxes.getText().toString().trim();
            String amountStr = etAmountPerBox.getText().toString().trim();
            
            if (!boxesStr.isEmpty() && !amountStr.isEmpty()) {
                int boxes = Integer.parseInt(boxesStr);
                double amount = Double.parseDouble(amountStr);
                double total = boxes * amount;
                
                String totalFormatted = String.format(Locale.getDefault(), "₹ %.2f", total);
                tvTotalAmount.setText(totalFormatted);
            } else {
                tvTotalAmount.setText("₹ 0.00");
            }
        } catch (NumberFormatException e) {
            tvTotalAmount.setText("₹ 0.00");
        }
    }
    
    private void submitForm() {
        long startTime = System.currentTimeMillis();
        android.util.Log.d("SubmitOrder", "════════════════════════════════════════════");
        android.util.Log.d("SubmitOrder", "🔵 SUBMIT BUTTON PRESSED");
        android.util.Log.d("SubmitOrder", "════════════════════════════════════════════");
        android.util.Log.d("SubmitOrder", "Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new java.util.Date()));
        
        // Step 1: Read form field values
        android.util.Log.d("SubmitOrder", "📝 STEP 1: Reading form field values...");
        String shopName = etShopName.getText().toString().trim();
        String orderDate = etOrderDate.getText().toString().trim();
        String salesmanName = etSalesmanName.getText().toString().trim();
        String numberOfBoxes = etNumberOfBoxes.getText().toString().trim();
        String specification = etSpecification.getText().toString().trim();
        String amountPerBox = etAmountPerBox.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        
        android.util.Log.d("SubmitOrder", "  - shopName: '" + shopName + "' (length: " + shopName.length() + ")");
        android.util.Log.d("SubmitOrder", "  - orderDate: '" + orderDate + "'");
        android.util.Log.d("SubmitOrder", "  - salesmanName: '" + salesmanName + "' (length: " + salesmanName.length() + ")");
        android.util.Log.d("SubmitOrder", "  - numberOfBoxes: '" + numberOfBoxes + "'");
        android.util.Log.d("SubmitOrder", "  - specification: '" + specification + "' (length: " + specification.length() + ")");
        android.util.Log.d("SubmitOrder", "  - amountPerBox: '" + amountPerBox + "'");
        android.util.Log.d("SubmitOrder", "  - location: '" + location + "' (length: " + location.length() + ")");
        android.util.Log.d("SubmitOrder", "  - phoneNumber: '" + phoneNumber + "'");
        
        // Step 2: If all fields empty, send sample payload silently
        boolean allFieldsEmpty = shopName.isEmpty() && orderDate.isEmpty() &&
                salesmanName.isEmpty() && numberOfBoxes.isEmpty() &&
                specification.isEmpty() && amountPerBox.isEmpty() &&
                location.isEmpty() && phoneNumber.isEmpty();

        if (allFieldsEmpty) {
            android.util.Log.d("SubmitOrder", "All fields empty — sending sample payload");
            OrderData sampleData = new OrderData(
                    "Sample Shop", "25/03/2026", "Sample Salesman",
                    10, "Sample specification", 250.00, "Sample City", "9876543210"
            );
            sendToTelegram(sampleData);
            return;
        }

        // Step 3: Validate all fields
        android.util.Log.d("SubmitOrder", "✅ STEP 3: Validating all form fields...");
        FormDataValidator.ValidationError validationError = FormDataValidator.validateAllFields(
                shopName, orderDate, salesmanName, numberOfBoxes, 
                specification, amountPerBox, location, phoneNumber
        );
        
        if (validationError != null) {
            android.util.Log.e("SubmitOrder", "❌ VALIDATION FAILED");
            android.util.Log.e("SubmitOrder", "  - Field: " + validationError.field);
            android.util.Log.e("SubmitOrder", "  - Error: " + validationError.message);
            showErrorDialog(validationError.field, validationError.message);
            return;
        }
        
        android.util.Log.d("SubmitOrder", "✅ All fields validated successfully");
        
        // Step 4: Parse numeric values
        android.util.Log.d("SubmitOrder", "🔢 STEP 4: Parsing numeric values...");
        try {
            int boxes = Integer.parseInt(numberOfBoxes);
            double amount = Double.parseDouble(amountPerBox);
            double total = boxes * amount;
            
            android.util.Log.d("SubmitOrder", "  - Boxes: " + boxes + " (type: int)");
            android.util.Log.d("SubmitOrder", "  - Amount per box: " + amount + " (type: double)");
            android.util.Log.d("SubmitOrder", "  - Calculated total: " + total + " (" + boxes + " × " + amount + ")");
            
            // Step 5: Create OrderData object
            android.util.Log.d("SubmitOrder", "🏗️ STEP 5: Creating OrderData object...");
            OrderData orderData = new OrderData(
                    shopName, orderDate, salesmanName, boxes, 
                    specification, amount, location, phoneNumber
            );
            
            android.util.Log.d("SubmitOrder", "✅ OrderData object created successfully");
            android.util.Log.d("SubmitOrder", "  - Full JSON: " + new Gson().toJson(orderData));
            
            // Step 6: Send to Telegram
            android.util.Log.d("SubmitOrder", "📡 STEP 6: Sending to Telegram API...");
            sendToTelegram(orderData);
            
        } catch (NumberFormatException e) {
            android.util.Log.e("SubmitOrder", "❌ NumberFormatException: " + e.getMessage(), e);
            showErrorDialog("Error", "Invalid number format in the boxes or amount field.");
        } catch (Exception e) {
            android.util.Log.e("SubmitOrder", "❌ Unexpected error during form submission: " + e.getMessage(), e);
            showErrorDialog("Error", "An unexpected error occurred. Please verify your inputs and try again.");
        }
        
        long duration = System.currentTimeMillis() - startTime;
        android.util.Log.d("SubmitOrder", "⏱️  submitForm() execution time: " + duration + "ms");
    }
    
    private void sendToTelegram(OrderData orderData) {
        long apiStartTime = System.currentTimeMillis();

        if (!isNetworkAvailable()) {
            showErrorDialog("No Internet", "Please check your internet connection and try again.");
            return;
        }

        android.util.Log.d("TelegramAPI", "════════════════════════════════════════════");
        android.util.Log.d("TelegramAPI", "Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new java.util.Date()));
        
        // Disable submit button during submission
        showProgressDialog();
        btnSubmit.setEnabled(false);
        android.util.Log.d("TelegramAPI", "📌 Submit button disabled");
        
        // Step 1: Get order data details
        android.util.Log.d("TelegramAPI", "📦 STEP 1: Order Data Details");
        android.util.Log.d("TelegramAPI", "  - Shop: " + orderData.getShopName());
        android.util.Log.d("TelegramAPI", "  - Date: " + orderData.getOrderDate());
        android.util.Log.d(
                "TelegramAPI", 
                "  - Boxes: " + orderData.getNumberOfBoxes() + ", Amount: ₹" + orderData.getAmountPerBox() + ", Total: ₹" + orderData.getTotalAmount()
        );
        android.util.Log.d("TelegramAPI", "  - Phone: " + orderData.getPhoneNumber());
        android.util.Log.d("TelegramAPI", "  - Location: " + orderData.getLocation());
        
        // Step 2: Convert to JSON
        android.util.Log.d("TelegramAPI", "📝 STEP 2: Converting to JSON...");
        String jsonData = new Gson().toJson(orderData);
        android.util.Log.d("TelegramAPI", "  - JSON: " + jsonData);
        
        // Step 3: Format caption and JSON document for Telegram
        android.util.Log.d("TelegramAPI", "✏️ STEP 3: Formatting caption and JSON document for Telegram...");
        String caption = buildTelegramCaption(orderData);
        String jsonFileName = buildJsonFileName(orderData);
        MultipartBody.Part jsonDocument = createJsonDocumentPart(jsonData, jsonFileName);
        android.util.Log.d("TelegramAPI", "  - Caption length: " + caption.length() + " characters");
        android.util.Log.d("TelegramAPI", "  - Caption preview: " + caption.substring(0, Math.min(100, caption.length())) + "...");
        android.util.Log.d("TelegramAPI", "  - JSON filename: " + jsonFileName);
        
        // Step 4: Get API configuration
        android.util.Log.d("TelegramAPI", "🔧 STEP 4: Getting API configuration...");
        TelegramBotService service = TelegramBotAPIClient.createService();
        String botToken = TelegramBotAPIClient.getBotToken();
        String chatId = TelegramBotAPIClient.getChatId();
        
        android.util.Log.d("TelegramAPI", "  - Bot Token: " + maskSensitiveData(botToken));
        android.util.Log.d("TelegramAPI", "  - Chat ID: " + chatId);
        android.util.Log.d("TelegramAPI", "  - API Base URL: https://api.telegram.org/");
        
        // Step 5: Build API request
        android.util.Log.d("TelegramAPI", "🔗 STEP 5: Building API request...");
        String endpoint = "bot" + botToken + "/sendDocument";
        String fullUrl = "https://api.telegram.org/" + endpoint;
        android.util.Log.d("TelegramAPI", "  - Full Request URL: " + maskSensitiveData(fullUrl));
        android.util.Log.d("TelegramAPI", "  - Request Method: POST");
        android.util.Log.d("TelegramAPI", "  - Content-Type: multipart/form-data");
        android.util.Log.d("TelegramAPI", "  - Multipart Parts:");
        android.util.Log.d("TelegramAPI", "    • bot_token parameter: " + maskSensitiveData(botToken));
        android.util.Log.d("TelegramAPI", "    • chat_id=" + chatId);
        android.util.Log.d("TelegramAPI", "    • document=" + jsonFileName);
        android.util.Log.d("TelegramAPI", "    • caption=(formatted order summary)");
        android.util.Log.d("TelegramAPI", "    • parse_mode=HTML");
        
        // Step 6: Execute API call
        android.util.Log.d("TelegramAPI", "🚀 STEP 6: Executing API call...");
        long callStartTime = System.currentTimeMillis();

        Call<TelegramResponse> call = service.sendDocument(
                botToken,
            createFormPart(chatId),
            jsonDocument,
            createFormPart(caption),
            createFormPart("HTML")
        );

        android.util.Log.d("TelegramAPI", "  - Call queued at: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new java.util.Date()));

        // Step 7: Handle response
        call.enqueue(new Callback<TelegramResponse>() {
            @Override
            public void onResponse(Call<TelegramResponse> call, Response<TelegramResponse> response) {
                try {
                    long callDuration = System.currentTimeMillis() - callStartTime;
                    android.util.Log.d("TelegramAPI", "Response received in " + callDuration + "ms, HTTP " + response.code());

                    // Log response details
                    android.util.Log.d("TelegramAPI", "📍 Response Details:");
                    android.util.Log.d("TelegramAPI", "  - HTTP Status Code: " + response.code());
                    android.util.Log.d("TelegramAPI", "  - Is Successful: " + response.isSuccessful());
                    android.util.Log.d("TelegramAPI", "  - Headers: " + response.headers());

                    if (response.body() != null) {
                        TelegramResponse body = response.body();
                        android.util.Log.d("TelegramAPI", "  - Response Body:");
                        android.util.Log.d("TelegramAPI", "    • OK: " + body.isSuccess());
                        android.util.Log.d("TelegramAPI", "    • Description: " + body.getDescription());
                        android.util.Log.d("TelegramAPI", "    • Error Code: " + body.getErrorCode());
                        android.util.Log.d("TelegramAPI", "    • Full JSON: " + new Gson().toJson(body));
                    } else {
                        android.util.Log.w("TelegramAPI", "  - Response body is null");
                    }

                    // Check if successful
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        android.util.Log.d("TelegramAPI", "✅ SUCCESS: Order submitted successfully!");
                        long totalDuration = System.currentTimeMillis() - apiStartTime;
                        android.util.Log.d("TelegramAPI", "⏱️  Total API time: " + totalDuration + "ms");
                        if (response.body().getResult() != null) {
                            lastSubmittedMessageId = response.body().getResult().getMessageId();
                            android.util.Log.d("TelegramAPI", "📋 Stored message_id for undo: " + lastSubmittedMessageId);
                        }
                        transitionToSuccess();
                    } else {
                        String errorMsg = (response.body() != null && response.body().getDescription() != null)
                                ? response.body().getDescription()
                                : getApiErrorMessage(response.code());
                        android.util.Log.e("TelegramAPI", "❌ FAILURE (HTTP " + response.code() + "): " + errorMsg);
                        hideProgressDialog();
                        if (btnSubmit != null) {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Submit Order");
                        }
                        showRetryDialog("Submission Failed", errorMsg);
                    }
                } catch (Exception e) {
                    android.util.Log.e("TelegramAPI", "Crash averted inside onResponse", e);
                    hideProgressDialog();
                    if (btnSubmit != null) {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit Order");
                    }
                }
            }

            @Override
            public void onFailure(Call<TelegramResponse> call, Throwable t) {
                try {
                    android.util.Log.e("TelegramAPI", "❌ API REQUEST FAILED: " + (t != null ? t.getClass().getSimpleName() + " - " + t.getMessage() : "unknown"));
                    if (t != null) android.util.Log.e("TelegramAPI", android.util.Log.getStackTraceString(t));

                    hideProgressDialog();
                    if (btnSubmit != null) {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit Order");
                    }

                    String userMsg;
                    if (t instanceof java.net.SocketTimeoutException) {
                        userMsg = "Request timed out. Check your connection and retry.";
                    } else if (t instanceof java.net.UnknownHostException) {
                        userMsg = "Cannot reach the server. Check your internet connection.";
                    } else {
                        userMsg = (t != null && t.getMessage() != null) ? t.getMessage() : "Unable to connect. Please try again.";
                    }
                    showRetryDialog("Network Error", userMsg);
                } catch (Exception e) {
                    android.util.Log.e("TelegramAPI", "Crash averted inside onFailure", e);
                    hideProgressDialog();
                }
            }
        });
    }
    
    private String maskSensitiveData(String data) {
        if (data == null || data.length() < 4) {
            return "***";
        }
        return data.substring(0, 4) + "***" + data.substring(Math.max(0, data.length() - 4));
    }

    private void showProgressDialog() {
        try {
            if (isFinishing() || isDestroyed()) return;
            android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_progress, null);
            progressSendingBar = dialogView.findViewById(R.id.progress_sending);
            ivSuccessTick = dialogView.findViewById(R.id.iv_success_tick);
            progressDialog = new AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
            progressDialog.show();
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to show progress dialog", e);
        }
    }

    private void hideProgressDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to dismiss progress dialog", e);
        } finally {
            progressDialog = null;
            progressSendingBar = null;
            ivSuccessTick = null;
        }
    }

    private void transitionToSuccess() {
        try {
            if (isFinishing() || isDestroyed()) return;
            if (progressSendingBar == null || ivSuccessTick == null) {
                hideProgressDialog();
                if (btnSubmit != null) { btnSubmit.setEnabled(true); btnSubmit.setText("Submit Order"); }
                showSuccessDialog();
                return;
            }
            // Swap spinner for green tick
            progressSendingBar.setVisibility(View.GONE);
            ivSuccessTick.setAlpha(0f);
            ivSuccessTick.setScaleX(0.3f);
            ivSuccessTick.setScaleY(0.3f);
            ivSuccessTick.setVisibility(View.VISIBLE);
            ivSuccessTick.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .start();
            // After letting the tick show, dismiss and show success dialog
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    hideProgressDialog();
                    if (btnSubmit != null) { btnSubmit.setEnabled(true); btnSubmit.setText("Submit Order"); }
                    showSuccessDialog();
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Error in transitionToSuccess delayed callback", e);
                }
            }, 900);
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error in transitionToSuccess, falling back", e);
            hideProgressDialog();
            if (btnSubmit != null) { btnSubmit.setEnabled(true); btnSubmit.setText("Submit Order"); }
            showSuccessDialog();
        }
    }

    private boolean isNetworkAvailable() {
        try {
            android.net.ConnectivityManager cm =
                    (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return true;
            android.net.Network network = cm.getActiveNetwork();
            if (network == null) return false;
            android.net.NetworkCapabilities cap = cm.getNetworkCapabilities(network);
            return cap != null && (
                    cap.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                    cap.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    cap.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET));
        } catch (SecurityException e) {
            android.util.Log.w("TelegramAPI", "ACCESS_NETWORK_STATE not granted, skipping check", e);
            return true; // let the request attempt and fail gracefully
        }
    }

    private String getApiErrorMessage(int httpCode) {
        switch (httpCode) {
            case 400: return "Invalid request — check the Chat ID.";
            case 401: return "Unauthorized — the Bot Token is incorrect.";
            case 403: return "Forbidden — the service is unavailable. Please contact support.";
            case 429: return "Too many requests — please wait a moment and retry.";
            case 500: case 502: case 503: return "Telegram server error — please try again shortly.";
            default:  return "Unexpected error (HTTP " + httpCode + "). Please retry.";
        }
    }
    
    private String formatOrderDataMessage(OrderData data) {
        return String.format(
                "<b>📦 NEW ORDER RECEIVED 📦</b>\n\n" +
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
    
    private void showSuccessDialog() {
        try {
            if (isFinishing() || isDestroyed()) return;

            final int messageIdForUndo = lastSubmittedMessageId;

            AlertDialog dialog = new AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle("Success")
                    .setMessage("Order submitted successfully!")
                    .setPositiveButton("OK", (d, which) -> {
                        if (undoCountDownTimer != null) undoCountDownTimer.cancel();
                        clearForm();
                        d.dismiss();
                    })
                    .setNeutralButton("Undo (10)", null) // listener set after show() to prevent auto-dismiss
                    .setCancelable(false)
                    .show();

            Button undoButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            undoButton.setTextColor(android.graphics.Color.parseColor("#E53935"));
            undoButton.setTypeface(undoButton.getTypeface(), android.graphics.Typeface.BOLD);
            undoButton.setOnClickListener(v -> {
                if (undoCountDownTimer != null) undoCountDownTimer.cancel();
                dialog.dismiss();
                deleteSubmittedMessage(messageIdForUndo);
            });

            undoCountDownTimer = new CountDownTimer(10_000, 1_000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long secsLeft = millisUntilFinished / 1_000;
                    if (!isFinishing() && !isDestroyed() && dialog.isShowing()) {
                        undoButton.setText("Undo (" + secsLeft + ")");
                    }
                }

                @Override
                public void onFinish() {
                    if (!isFinishing() && !isDestroyed() && dialog.isShowing()) {
                        dialog.dismiss();
                        clearForm();
                    }
                }
            }.start();

        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to show success dialog", e);
        }
    }

    private void deleteSubmittedMessage(int messageId) {
        if (messageId <= 0) return;
        lastSubmittedMessageId = -1;
        try {
            TelegramBotService service = TelegramBotAPIClient.createService();
            String botToken = TelegramBotAPIClient.getBotToken();
            String chatId = TelegramBotAPIClient.getChatId();
            service.deleteMessage(botToken, chatId, messageId).enqueue(new Callback<TelegramResponse>() {
                @Override
                public void onResponse(Call<TelegramResponse> call, Response<TelegramResponse> response) {
                    android.util.Log.d("TelegramAPI", "deleteMessage response: HTTP " + response.code());
                }

                @Override
                public void onFailure(Call<TelegramResponse> call, Throwable t) {
                    android.util.Log.e("TelegramAPI", "deleteMessage failed: " + (t != null ? t.getMessage() : "unknown"));
                }
            });
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "deleteSubmittedMessage error", e);
        }
    }

    private void showErrorDialog(String title, String message) {
        try {
            if (isFinishing() || isDestroyed()) return;
            new AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to show error dialog", e);
        }
    }

    private void showRetryDialog(String title, String message) {
        try {
            if (isFinishing() || isDestroyed()) return;
            new AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Retry", (dialog, which) -> {
                        dialog.dismiss();
                        submitForm();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setCancelable(false)
                    .show();
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to show retry dialog", e);
        }
    }
    
    private void clearForm() {
        etShopName.setText("");
        etOrderDate.setText("");
        etSalesmanName.setText("");
        etNumberOfBoxes.setText("");
        etSpecification.setText("");
        etAmountPerBox.setText("");
        tvTotalAmount.setText("₹ 0.00");
        etLocation.setText("");
        etPhoneNumber.setText("");
        
        // Focus on first field
        etShopName.requestFocus();
    }
}
