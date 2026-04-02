package com.orderapp.history;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.orderapp.BuildConfig;
import com.orderapp.R;
import com.orderapp.api.TelegramBotAPIClient;
import com.orderapp.api.TelegramBotService;
import com.orderapp.api.TelegramResponse;
import com.orderapp.db.AppDatabase;
import com.orderapp.db.OrderRecord;

import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private TextView tvEmpty;
    private HistoryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvHistory = view.findViewById(R.id.rv_history);
        tvEmpty   = view.findViewById(R.id.tv_empty_history);

        // Long-press listener is only wired in debug builds; null otherwise (adapter ignores it)
        HistoryAdapter.OnItemLongClickListener longPress = BuildConfig.DEBUG
                ? this::showDebugDeleteDialog
                : null;

        adapter = new HistoryAdapter(this::showDetailDialog, longPress);
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    // ─── Data loading ──────────────────────────────────────────────────────────

    private void loadHistory() {
        Context appCtx = requireContext().getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            List<OrderRecord> records = AppDatabase.getInstance(appCtx)
                    .orderRecordDao().getAllOrdersSortedByDate();
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded()) return;
                adapter.setData(records);
                boolean empty = records == null || records.isEmpty();
                tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                rvHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
            });
        });
    }

    // ─── Detail dialog (tap) ───────────────────────────────────────────────────

    private void showDetailDialog(OrderRecord record) {
        new AlertDialog.Builder(requireContext(),
                androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(record.shopName)
                .setMessage(record.messageText)
                .setPositiveButton("Close", (d, w) -> d.dismiss())
                .show();
    }

    // ─── Debug delete dialog (long-press, DEBUG builds only) ──────────────────

    private void showDebugDeleteDialog(OrderRecord record) {
        String telegramInfo = record.telegramMessageId > 0
                ? "The notification sent to the team will also be removed."
                : "Only the local record will be removed.";

        new AlertDialog.Builder(requireContext(),
                androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("[DEBUG] Delete Order")
                .setMessage("Delete \"" + record.shopName + "\" from history?\n\n" + telegramInfo)
                .setPositiveButton("Delete", (d, w) -> {
                    d.dismiss();
                    deleteOrder(record);
                })
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .show();
    }

    private void deleteOrder(OrderRecord record) {
        // 1. Delete local record from DB
        Context appCtx = requireContext().getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(appCtx).orderRecordDao().deleteById(record.id);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Local record deleted", Toast.LENGTH_SHORT).show();
                loadHistory();
            });
        });

        // 2. Delete Telegram message if we have a valid message_id
        if (record.telegramMessageId > 0) {
            TelegramBotService service = TelegramBotAPIClient.createService();
            service.deleteMessage(
                    TelegramBotAPIClient.getBotToken(),
                    TelegramBotAPIClient.getChatId(),
                    record.telegramMessageId
            ).enqueue(new Callback<TelegramResponse>() {
                @Override
                public void onResponse(Call<TelegramResponse> call, Response<TelegramResponse> response) {
                    boolean ok = response.isSuccessful()
                            && response.body() != null
                            && response.body().isSuccess();
                    android.util.Log.d("DebugDelete",
                            "deleteMessage HTTP " + response.code() + " ok=" + ok);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                ok ? "Order notification removed" : "Could not remove order notification (HTTP " + response.code() + ")",
                                Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFailure(Call<TelegramResponse> call, Throwable t) {
                    android.util.Log.e("DebugDelete", "deleteMessage failed: " + (t != null ? t.getMessage() : "unknown"));
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Could not remove order notification: network error", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }
}
