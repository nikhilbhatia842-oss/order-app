package com.orderapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.orderapp.api.TelegramBotAPIClient;
import com.orderapp.api.TelegramBotService;
import com.orderapp.api.TelegramResponse;
import com.orderapp.db.AppDatabase;
import com.orderapp.db.OrderRecord;
import com.orderapp.model.OrderData;
import com.orderapp.validator.FormDataValidator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderFormFragment extends Fragment {

    private static final String PREFS_NAME = "order_app_prefs";
    private static final String KEY_SALESMAN_NAME = "salesman_name";

    private EditText etShopName;
    private EditText etOrderDate;
    private EditText etSalesmanName;
    private EditText etNumberOfBoxes;
    private EditText etSpecification;
    private EditText etAmountPerBox;
    private TextView tvTotalAmount;
    private EditText etLocation;
    private EditText etPhoneNumber;
    private TextInputLayout tilPhoneNumber;
    private ActivityResultLauncher<Intent> contactPickerLauncher;
    private static final int REQUEST_CODE_CONTACTS = 101;
    private Button btnSubmit;
    private AlertDialog progressDialog;
    private ProgressBar progressSendingBar;
    private ImageView ivSuccessTick;
    private int lastSubmittedMessageId = -1;
    private CountDownTimer undoCountDownTimer;

    /** Built in memory on success — saved to DB only if the user does NOT press Undo. */
    private OrderRecord pendingRecord;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final Calendar calendar = Calendar.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerContactPickerLauncher();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_form, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupListeners();
        autoFillSalesmanName();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (undoCountDownTimer != null) {
            undoCountDownTimer.cancel();
        }
    }

    // ─── Salesman name persistence ────────────────────────────────────────────

    private void autoFillSalesmanName() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String saved = prefs.getString(KEY_SALESMAN_NAME, "");
        if (!saved.isEmpty()) {
            etSalesmanName.setText(saved);
            etSalesmanName.setSelection(saved.length());
        }
    }

    private void saveSalesmanName(String name) {
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SALESMAN_NAME, name)
                .apply();
    }

    // ─── Contact picker ────────────────────────────────────────────────────────

    private void registerContactPickerLauncher() {
        contactPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri contactUri = result.getData().getData();
                    String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
                    try (Cursor cursor = requireActivity().getContentResolver().query(
                            contactUri, projection, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            String rawNumber = cursor.getString(cursor.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String cleaned = rawNumber.replaceAll("[\\s\\-()]+", "");
                            etPhoneNumber.setText(cleaned);
                            etPhoneNumber.setSelection(cleaned.length());
                        }
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Couldn't read contact number", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    private void openContactsPicker() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            launchContactsPicker();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.READ_CONTACTS},
                REQUEST_CODE_CONTACTS);
        }
    }

    private void launchContactsPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        contactPickerLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchContactsPicker();
            } else {
                Toast.makeText(requireContext(),
                    "Contacts permission denied. Enter number manually.",
                    Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ─── Initialisation ────────────────────────────────────────────────────────

    private void initializeViews(View view) {
        etShopName = view.findViewById(R.id.et_shop_name);
        etOrderDate = view.findViewById(R.id.et_order_date);
        etSalesmanName = view.findViewById(R.id.et_salesman_name);
        etNumberOfBoxes = view.findViewById(R.id.et_number_of_boxes);
        etSpecification = view.findViewById(R.id.et_specification);
        etAmountPerBox = view.findViewById(R.id.et_amount_per_box);
        tvTotalAmount = view.findViewById(R.id.tv_total_amount);
        etLocation = view.findViewById(R.id.et_location);
        etPhoneNumber = view.findViewById(R.id.et_phone_number);
        tilPhoneNumber = view.findViewById(R.id.til_phone_number);
        btnSubmit = view.findViewById(R.id.btn_submit);
    }

    private void setupListeners() {
        etOrderDate.setOnClickListener(v -> showDatePicker());

        TextWatcher numberWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { calculateTotalAmount(); }
        };
        etNumberOfBoxes.addTextChangedListener(numberWatcher);
        etAmountPerBox.addTextChangedListener(numberWatcher);

        tilPhoneNumber.setEndIconOnClickListener(v -> openContactsPicker());
        btnSubmit.setOnClickListener(v -> showConfirmationDialog());
    }

    // ─── Date picker ───────────────────────────────────────────────────────────

    private void showDatePicker() {
        new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    etOrderDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ─── Calculation ───────────────────────────────────────────────────────────

    private void calculateTotalAmount() {
        try {
            String boxesStr = etNumberOfBoxes.getText().toString().trim();
            String amountStr = etAmountPerBox.getText().toString().trim();
            if (!boxesStr.isEmpty() && !amountStr.isEmpty()) {
                int boxes = Integer.parseInt(boxesStr);
                double amount = Double.parseDouble(amountStr);
                tvTotalAmount.setText(String.format(Locale.getDefault(), "₹ %.2f", boxes * amount));
            } else {
                tvTotalAmount.setText("₹ 0.00");
            }
        } catch (NumberFormatException e) {
            tvTotalAmount.setText("₹ 0.00");
        }
    }

    // ─── Form submission ───────────────────────────────────────────────────────

    private void showConfirmationDialog() {
        new AlertDialog.Builder(requireContext(), androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("Confirm Submission")
                .setMessage("Are you sure you want to submit this order?")
                .setPositiveButton("Yes, Submit", (dialog, which) -> submitForm())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void submitForm() {
        android.util.Log.d("SubmitOrder", "═══════════════════════════════════════════");
        android.util.Log.d("SubmitOrder", "🔵 SUBMIT BUTTON PRESSED");

        String shopName      = etShopName.getText().toString().trim();
        String orderDate     = etOrderDate.getText().toString().trim();
        String salesmanName  = etSalesmanName.getText().toString().trim();
        String numberOfBoxes = etNumberOfBoxes.getText().toString().trim();
        String specification = etSpecification.getText().toString().trim();
        String amountPerBox  = etAmountPerBox.getText().toString().trim();
        String location      = etLocation.getText().toString().trim();
        String phoneNumber   = etPhoneNumber.getText().toString().trim();

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

        FormDataValidator.ValidationError validationError = FormDataValidator.validateAllFields(
                shopName, orderDate, salesmanName, numberOfBoxes,
                specification, amountPerBox, location, phoneNumber
        );
        if (validationError != null) {
            android.util.Log.e("SubmitOrder", "❌ VALIDATION FAILED: " + validationError.field + " — " + validationError.message);
            showErrorDialog(validationError.field, validationError.message);
            return;
        }

        try {
            int boxes = Integer.parseInt(numberOfBoxes);
            double amount = Double.parseDouble(amountPerBox);
            OrderData orderData = new OrderData(shopName, orderDate, salesmanName, boxes,
                    specification, amount, location, phoneNumber);
            android.util.Log.d("SubmitOrder", "✅ OrderData created: " + new Gson().toJson(orderData));
            saveSalesmanName(salesmanName);
            sendToTelegram(orderData);
        } catch (NumberFormatException e) {
            showErrorDialog("Error", "Invalid number format in the boxes or amount field.");
        } catch (Exception e) {
            showErrorDialog("Error", "An unexpected error occurred. Please verify your inputs and try again.");
        }
    }

    private void sendToTelegram(OrderData orderData) {
        if (!isNetworkAvailable()) {
            showErrorDialog("No Internet", "Please check your internet connection and try again.");
            return;
        }

        showProgressDialog();
        btnSubmit.setEnabled(false);

        String jsonData    = new Gson().toJson(orderData);
        String caption     = buildTelegramCaption(orderData);
        String jsonFileName = buildJsonFileName(orderData);
        MultipartBody.Part jsonDocument = createJsonDocumentPart(jsonData, jsonFileName);

        TelegramBotService service = TelegramBotAPIClient.createService();
        String botToken = TelegramBotAPIClient.getBotToken();
        String chatId   = TelegramBotAPIClient.getChatId();

        android.util.Log.d("TelegramAPI", "🚀 Sending to Telegram — file: " + jsonFileName);

        Call<TelegramResponse> call = service.sendDocument(
                botToken,
                createFormPart(chatId),
                jsonDocument,
                createFormPart(caption),
                createFormPart("HTML")
        );

        call.enqueue(new Callback<TelegramResponse>() {
            @Override
            public void onResponse(Call<TelegramResponse> call, Response<TelegramResponse> response) {
                try {
                    android.util.Log.d("TelegramAPI", "Response HTTP " + response.code());
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        android.util.Log.d("TelegramAPI", "✅ Order submitted successfully");
                        if (response.body().getResultAsMessage() != null) {
                            lastSubmittedMessageId = response.body().getResultAsMessage().getMessageId();
                            android.util.Log.d("TelegramAPI", "Stored message_id for undo: " + lastSubmittedMessageId);
                        }
                        // Build record in memory — saved only if undo is NOT pressed
                        pendingRecord = buildOrderRecord(orderData);
                        transitionToSuccess();
                    } else {
                        String errorMsg = (response.body() != null && response.body().getDescription() != null)
                                ? response.body().getDescription()
                                : getApiErrorMessage(response.code());
                        android.util.Log.e("TelegramAPI", "❌ FAILURE (HTTP " + response.code() + "): " + errorMsg);
                        hideProgressDialog();
                        if (btnSubmit != null) { btnSubmit.setEnabled(true); btnSubmit.setText("Submit Order"); }
                        showRetryDialog("Submission Failed", errorMsg);
                    }
                } catch (Exception e) {
                    android.util.Log.e("TelegramAPI", "Crash averted inside onResponse", e);
                    hideProgressDialog();
                    if (btnSubmit != null) { btnSubmit.setEnabled(true); btnSubmit.setText("Submit Order"); }
                }
            }

            @Override
            public void onFailure(Call<TelegramResponse> call, Throwable t) {
                try {
                    android.util.Log.e("TelegramAPI", "❌ API FAILED: " + (t != null ? t.getMessage() : "unknown"));
                    hideProgressDialog();
                    if (btnSubmit != null) { btnSubmit.setEnabled(true); btnSubmit.setText("Submit Order"); }
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

    // ─── Order record helpers ──────────────────────────────────────────────────

    private OrderRecord buildOrderRecord(OrderData data) {
        OrderRecord record = new OrderRecord();
        record.shopName      = data.getShopName();
        record.orderDate     = data.getOrderDate();
        record.salesmanName  = data.getSalesmanName();
        record.numberOfBoxes = data.getNumberOfBoxes();
        record.specification = data.getSpecification();
        record.amountPerBox  = data.getAmountPerBox();
        record.totalAmount   = data.getTotalAmount();
        record.location      = data.getLocation();
        record.phoneNumber   = data.getPhoneNumber();
        record.submittedAt        = System.currentTimeMillis();
        record.messageText        = buildPlainTextMessage(data);
        record.telegramMessageId  = lastSubmittedMessageId;
        return record;
    }

    /**
     * Persists the given record to the local database on a background thread.
     * Only call this after confirming the user has NOT pressed Undo.
     */
    private void saveToHistory(OrderRecord record) {
        if (record == null) return;
        Context appCtx = requireContext().getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() ->
            AppDatabase.getInstance(appCtx).orderRecordDao().insertOrder(record)
        );
    }

    // ─── Progress / success UI ─────────────────────────────────────────────────

    private void showProgressDialog() {
        try {
            if (!isAdded() || requireActivity().isFinishing() || requireActivity().isDestroyed()) return;
            View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.dialog_progress, null);
            progressSendingBar = dialogView.findViewById(R.id.progress_sending);
            ivSuccessTick      = dialogView.findViewById(R.id.iv_success_tick);
            progressDialog = new AlertDialog.Builder(requireContext(),
                    androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
            progressDialog.show();
        } catch (Exception e) {
            android.util.Log.e("OrderFormFragment", "Failed to show progress dialog", e);
        }
    }

    private void hideProgressDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            android.util.Log.e("OrderFormFragment", "Failed to dismiss progress dialog", e);
        } finally {
            progressDialog    = null;
            progressSendingBar = null;
            ivSuccessTick      = null;
        }
    }

    private void transitionToSuccess() {
        try {
            if (!isAdded() || requireActivity().isFinishing() || requireActivity().isDestroyed()) return;
            if (progressSendingBar == null || ivSuccessTick == null) {
                hideProgressDialog();
                if (btnSubmit != null) { btnSubmit.setEnabled(true); btnSubmit.setText("Submit Order"); }
                showSuccessDialog();
                return;
            }
            progressSendingBar.setVisibility(View.GONE);
            ivSuccessTick.setAlpha(0f);
            ivSuccessTick.setScaleX(0.3f);
            ivSuccessTick.setScaleY(0.3f);
            ivSuccessTick.setVisibility(View.VISIBLE);
            ivSuccessTick.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(400).start();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    hideProgressDialog();
                    if (btnSubmit != null) { btnSubmit.setEnabled(true); btnSubmit.setText("Submit Order"); }
                    showSuccessDialog();
                } catch (Exception e) {
                    android.util.Log.e("OrderFormFragment", "Error in transitionToSuccess callback", e);
                }
            }, 900);
        } catch (Exception e) {
            android.util.Log.e("OrderFormFragment", "Error in transitionToSuccess, falling back", e);
            hideProgressDialog();
            if (btnSubmit != null) { btnSubmit.setEnabled(true); btnSubmit.setText("Submit Order"); }
            showSuccessDialog();
        }
    }

    private void showSuccessDialog() {
        try {
            if (!isAdded() || requireActivity().isFinishing() || requireActivity().isDestroyed()) return;

            final int messageIdForUndo   = lastSubmittedMessageId;
            final OrderRecord recordToSave = pendingRecord;
            pendingRecord = null; // clear field so it cannot be double-saved

            AlertDialog dialog = new AlertDialog.Builder(requireContext(),
                    androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle("Success")
                    .setMessage("Order submitted successfully!")
                    .setPositiveButton("OK", (d, which) -> {
                        if (undoCountDownTimer != null) undoCountDownTimer.cancel();
                        // Undo not pressed – persist the record
                        saveToHistory(recordToSave);
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
                // Undo pressed – delete from Telegram, do NOT save to history
                deleteSubmittedMessage(messageIdForUndo);
                clearForm();
            });

            undoCountDownTimer = new CountDownTimer(10_000, 1_000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long secsLeft = millisUntilFinished / 1_000;
                    if (isAdded() && !requireActivity().isFinishing() && dialog.isShowing()) {
                        undoButton.setText("Undo (" + secsLeft + ")");
                    }
                }

                @Override
                public void onFinish() {
                    if (isAdded() && !requireActivity().isFinishing() && dialog.isShowing()) {
                        dialog.dismiss();
                        // Undo window expired without pressing Undo – persist the record
                        saveToHistory(recordToSave);
                        clearForm();
                    }
                }
            }.start();

        } catch (Exception e) {
            android.util.Log.e("OrderFormFragment", "Failed to show success dialog", e);
        }
    }

    private void deleteSubmittedMessage(int messageId) {
        if (messageId <= 0) return;
        lastSubmittedMessageId = -1;
        try {
            TelegramBotService service = TelegramBotAPIClient.createService();
            String botToken = TelegramBotAPIClient.getBotToken();
            String chatId   = TelegramBotAPIClient.getChatId();
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
            android.util.Log.e("OrderFormFragment", "deleteSubmittedMessage error", e);
        }
    }

    // ─── Dialogs ───────────────────────────────────────────────────────────────

    private void showErrorDialog(String title, String message) {
        try {
            if (!isAdded() || requireActivity().isFinishing() || requireActivity().isDestroyed()) return;
            new AlertDialog.Builder(requireContext(),
                    androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (Exception e) {
            android.util.Log.e("OrderFormFragment", "Failed to show error dialog", e);
        }
    }

    private void showRetryDialog(String title, String message) {
        try {
            if (!isAdded() || requireActivity().isFinishing() || requireActivity().isDestroyed()) return;
            new AlertDialog.Builder(requireContext(),
                    androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
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
            android.util.Log.e("OrderFormFragment", "Failed to show retry dialog", e);
        }
    }

    // ─── Form helpers ──────────────────────────────────────────────────────────

    private void clearForm() {
        etShopName.setText("");
        etOrderDate.setText("");
        // Salesman name is intentionally kept — it is auto-filled from saved preferences
        etNumberOfBoxes.setText("");
        etSpecification.setText("");
        etAmountPerBox.setText("");
        tvTotalAmount.setText("₹ 0.00");
        etLocation.setText("");
        etPhoneNumber.setText("");
        etShopName.requestFocus();
    }

    // ─── Network helpers ───────────────────────────────────────────────────────

    private boolean isNetworkAvailable() {
        try {
            android.net.ConnectivityManager cm =
                    (android.net.ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return true;
            android.net.Network network = cm.getActiveNetwork();
            if (network == null) return false;
            android.net.NetworkCapabilities cap = cm.getNetworkCapabilities(network);
            return cap != null && (
                    cap.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                    cap.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    cap.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET));
        } catch (SecurityException e) {
            return true;
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

    // ─── Message formatting ────────────────────────────────────────────────────

    /** Plain-text version stored in history (no HTML tags). */
    private String buildPlainTextMessage(OrderData data) {
        return String.format(
                "NEW ORDER\n\n" +
                "Shop Name: %s\n" +
                "Order Date: %s\n" +
                "Salesman: %s\n" +
                "Number of Boxes: %d\n" +
                "Specification: %s\n" +
                "Amount per Box: \u20b9 %.2f\n" +
                "Total Amount: \u20b9 %.2f\n" +
                "Location: %s\n" +
                "Phone: %s\n" +
                "Submitted at: %s",
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

    /** HTML version sent as caption to Telegram. */
    private String formatOrderDataMessage(OrderData data) {
        return String.format(
                "<b>\uD83D\uDCE6 NEW ORDER RECEIVED \uD83D\uDCE6</b>\n\n" +
                "<b>Shop Name:</b> %s\n" +
                "<b>Order Date:</b> %s\n" +
                "<b>Salesman:</b> %s\n" +
                "<b>Number of Boxes:</b> %d\n" +
                "<b>Specification:</b> %s\n" +
                "<b>Amount per Box:</b> \u20b9 %.2f\n" +
                "<b>Total Amount:</b> \u20b9 %.2f\n" +
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
        if (safeShopName.isEmpty()) safeShopName = "order";
        String timestamp = data.getSubmissionTime()
                .replace(" ", "_")
                .replace(":", "-");
        return safeShopName + "_" + timestamp + ".json";
    }

    private MultipartBody.Part createJsonDocumentPart(String jsonData, String fileName) {
        RequestBody documentBody = RequestBody.create(
                jsonData, MediaType.get("application/json; charset=utf-8"));
        return MultipartBody.Part.createFormData("document", fileName, documentBody);
    }

    private RequestBody createFormPart(String value) {
        return RequestBody.create(value, MultipartBody.FORM);
    }
}
