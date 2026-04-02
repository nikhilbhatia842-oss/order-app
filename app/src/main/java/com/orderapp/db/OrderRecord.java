package com.orderapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "order_history")
public class OrderRecord {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String shopName;
    public String orderDate;
    public String salesmanName;
    public int numberOfBoxes;
    public String specification;
    public double amountPerBox;
    public double totalAmount;
    public String location;
    public String phoneNumber;

    /** Unix timestamp millis of when the order was submitted — used for sorting. */
    public long submittedAt;

    /** Plain-text copy of the message that was sent to Telegram. */
    public String messageText;

    /**
     * Telegram message_id returned by the sendDocument API.
     * Stored locally only — never sent to Telegram.
     * Used by the debug delete feature to remove the message from the chat.
     * -1 means not available (e.g. old record before this field existed).
     */
    public int telegramMessageId;
}
