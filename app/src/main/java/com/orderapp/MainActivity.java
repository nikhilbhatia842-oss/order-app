package com.orderapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.orderapp.api.TelegramBotAPIClient;
import com.orderapp.api.TelegramBotService;
import com.orderapp.api.TelegramResponse;
import com.orderapp.model.OrderData;
import com.orderapp.validator.FormDataValidator;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
        btnSubmit.setOnClickListener(v -> submitForm());
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
        
        // Step 2: Check if all fields are empty
        android.util.Log.d("SubmitOrder", "📋 STEP 2: Checking if all fields are empty...");
        boolean allFieldsEmpty = shopName.isEmpty() && orderDate.isEmpty() && 
                                 salesmanName.isEmpty() && numberOfBoxes.isEmpty() && 
                                 specification.isEmpty() && amountPerBox.isEmpty() && 
                                 location.isEmpty() && phoneNumber.isEmpty();
        
        android.util.Log.d("SubmitOrder", "  - All fields empty: " + allFieldsEmpty);
        
        if (allFieldsEmpty) {
            android.util.Log.d("SubmitOrder", "🧪 TEST MODE ACTIVATED - Creating sample data");
            Toast.makeText(this, "📋 Test Mode: Sending sample data...", Toast.LENGTH_SHORT).show();
            
            // Create sample OrderData for testing
            OrderData sampleData = new OrderData(
                    "Test Shop",
                    "24/03/2026",
                    "Test Salesman",
                    100,
                    "Test specification with sample details",
                    250.50,
                    "Test Location",
                    "9876543210"
            );
            
            android.util.Log.d("SubmitOrder", "  - Sample data JSON: " + new Gson().toJson(sampleData));
            android.util.Log.d("SubmitOrder", "🔄 Sending test sample to Telegram...");
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
            showErrorDialog("Error", "Invalid input format");
        }
        
        long duration = System.currentTimeMillis() - startTime;
        android.util.Log.d("SubmitOrder", "⏱️  submitForm() execution time: " + duration + "ms");
    }
    
    private void sendToTelegram(OrderData orderData) {
        long apiStartTime = System.currentTimeMillis();
        
        android.util.Log.d("TelegramAPI", "════════════════════════════════════════════");
        android.util.Log.d("TelegramAPI", "🌐 TELEGRAM API REQUEST INITIATED");
        android.util.Log.d("TelegramAPI", "════════════════════════════════════════════");
        android.util.Log.d("TelegramAPI", "Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new java.util.Date()));
        
        // Disable submit button during submission
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");
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
        
        // Step 3: Format message for Telegram
        android.util.Log.d("TelegramAPI", "✏️ STEP 3: Formatting message for Telegram...");
        String message = formatOrderDataMessage(orderData);
        android.util.Log.d("TelegramAPI", "  - Message length: " + message.length() + " characters");
        android.util.Log.d("TelegramAPI", "  - Message preview: " + message.substring(0, Math.min(100, message.length())) + "...");
        android.util.Log.d("TelegramAPI", "  - Full message:\n" + message);
        
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
        String endpoint = "bot" + botToken + "/sendMessage";
        String fullUrl = "https://api.telegram.org/" + endpoint;
        android.util.Log.d("TelegramAPI", "  - Full Request URL: " + maskSensitiveData(fullUrl));
        android.util.Log.d("TelegramAPI", "  - Request Method: POST");
        android.util.Log.d("TelegramAPI", "  - Content-Type: application/x-www-form-urlencoded");
        android.util.Log.d("TelegramAPI", "  - Query Parameters:");
        android.util.Log.d("TelegramAPI", "    • bot_token parameter: " + maskSensitiveData(botToken));
        android.util.Log.d("TelegramAPI", "    • chat_id=" + chatId);
        android.util.Log.d("TelegramAPI", "    • text=(formatted message)");
        android.util.Log.d("TelegramAPI", "    • parse_mode=HTML");
        
        // Step 6: Execute API call
        android.util.Log.d("TelegramAPI", "🚀 STEP 6: Executing API call...");
        long callStartTime = System.currentTimeMillis();
        
        // URL-encode the bot token to handle the colon safely
        String encodedBotToken;
        try {
            encodedBotToken = URLEncoder.encode(botToken, StandardCharsets.UTF_8.toString());
            android.util.Log.d("TelegramAPI", "  - Bot token URL-encoded: " + maskSensitiveData(encodedBotToken));
        } catch (Exception e) {
            android.util.Log.e("TelegramAPI", "  - Failed to URL-encode bot token: " + e.getMessage(), e);
            encodedBotToken = botToken; // Fall back to original
        }
        
        Call<TelegramResponse> call = service.sendMessage(
                encodedBotToken,
                chatId,
                message,
                "HTML"
        );
        
        android.util.Log.d("TelegramAPI", "  - Call queued at: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new java.util.Date()));
        
        // Step 7: Handle response
        call.enqueue(new Callback<TelegramResponse>() {
            @Override
            public void onResponse(Call<TelegramResponse> call, Response<TelegramResponse> response) {
                long callDuration = System.currentTimeMillis() - callStartTime;
                
                android.util.Log.d("TelegramAPI", "════════════════════════════════════════════");
                android.util.Log.d("TelegramAPI", "✅ API RESPONSE RECEIVED");
                android.util.Log.d("TelegramAPI", "════════════════════════════════════════════");
                android.util.Log.d("TelegramAPI", "⏱️  Response time: " + callDuration + "ms");
                android.util.Log.d("TelegramAPI", "📊 STEP 7: Handling response...");
                
                // Re-enable submit button
                btnSubmit.setEnabled(true);
                btnSubmit.setText("✓ SUBMIT ORDER");
                android.util.Log.d("TelegramAPI", "  - Submit button re-enabled");
                
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
                    android.util.Log.d("TelegramAPI", "✅ SUCCESS: Order submitted to Telegram successfully!");
                    android.util.Log.d("TelegramAPI", "🎉 SUBMISSION COMPLETE");
                    long totalDuration = System.currentTimeMillis() - apiStartTime;
                    android.util.Log.d("TelegramAPI", "⏱️  Total API time: " + totalDuration + "ms");
                    showSuccessDialog();
                } else {
                    String errorMsg = response.body() != null ? response.body().getDescription() : "Unknown error (Code: " + response.code() + ")";
                    android.util.Log.e("TelegramAPI", "❌ FAILURE: " + errorMsg);
                    android.util.Log.e("TelegramAPI", "  - Error occurred at: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new java.util.Date()));
                    showRetryDialog("Submission Error", errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<TelegramResponse> call, Throwable t) {
                long callDuration = System.currentTimeMillis() - callStartTime;
                
                android.util.Log.e("TelegramAPI", "════════════════════════════════════════════");
                android.util.Log.e("TelegramAPI", "❌ API REQUEST FAILED");
                android.util.Log.e("TelegramAPI", "════════════════════════════════════════════");
                android.util.Log.e("TelegramAPI", "⏱️  Time before failure: " + callDuration + "ms");
                
                // Re-enable submit button
                btnSubmit.setEnabled(true);
                btnSubmit.setText("✓ SUBMIT ORDER");
                android.util.Log.e("TelegramAPI", "  - Submit button re-enabled");
                
                // Log failure details
                android.util.Log.e("TelegramAPI", "🚨 Error Details:");
                android.util.Log.e("TelegramAPI", "  - Exception Type: " + t.getClass().getSimpleName());
                android.util.Log.e("TelegramAPI", "  - Exception Message: " + (t.getMessage() != null ? t.getMessage() : "null"));
                android.util.Log.e("TelegramAPI", "  - Stack Trace: " + android.util.Log.getStackTraceString(t));
                
                String errorDetail = t.getMessage() != null ? t.getMessage() : "Unable to connect to Telegram API";
                android.util.Log.e("TelegramAPI", "  - User friendly message: " + errorDetail);
                
                android.util.Log.e("TelegramAPI", "💡 Common Causes:");
                android.util.Log.e("TelegramAPI", "  1. No internet connection");
                android.util.Log.e("TelegramAPI", "  2. Telegram API is unreachable");
                android.util.Log.e("TelegramAPI", "  3. Bot token is invalid");
                android.util.Log.e("TelegramAPI", "  4. Chat ID is invalid");
                android.util.Log.e("TelegramAPI", "  5. Request timeout");
                
                showRetryDialog("Network Error", "Make sure:\n• Internet is ON\n• Bot was started in Telegram\n\nError: " + errorDetail);
            }
        });
    }
    
    private String maskSensitiveData(String data) {
        if (data == null || data.length() < 4) {
            return "***";
        }
        return data.substring(0, 4) + "***" + data.substring(Math.max(0, data.length() - 4));
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
    
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("Order submitted successfully to Telegram!")
                .setPositiveButton("OK", (dialog, which) -> {
                    clearForm();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }
    
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    private void showRetryDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Retry", (dialog, which) -> {
                    dialog.dismiss();
                    submitForm();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
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
