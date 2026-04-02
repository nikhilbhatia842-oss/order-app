package com.orderapp.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.orderapp.R;
import com.orderapp.db.OrderRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(OrderRecord record);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(OrderRecord record);
    }

    private List<OrderRecord> records = new ArrayList<>();
    private final OnItemClickListener listener;
    private final OnItemLongClickListener longClickListener;

    public HistoryAdapter(OnItemClickListener listener, OnItemLongClickListener longClickListener) {
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    public void setData(List<OrderRecord> data) {
        records = data != null ? data : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderRecord record = records.get(position);
        holder.tvShopName.setText(record.shopName);
        holder.tvDate.setText(record.orderDate);
        holder.tvSalesman.setText(record.salesmanName);
        holder.tvTotal.setText(String.format(Locale.getDefault(), "\u20b9 %.2f", record.totalAmount));
        holder.tvSubmittedAt.setText(
                new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(new Date(record.submittedAt)));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(record));
        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(record);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvShopName, tvDate, tvSalesman, tvTotal, tvSubmittedAt;

        ViewHolder(View v) {
            super(v);
            tvShopName   = v.findViewById(R.id.tv_history_shop_name);
            tvDate       = v.findViewById(R.id.tv_history_order_date);
            tvSalesman   = v.findViewById(R.id.tv_history_salesman);
            tvTotal      = v.findViewById(R.id.tv_history_total);
            tvSubmittedAt = v.findViewById(R.id.tv_history_submitted_at);
        }
    }
}
