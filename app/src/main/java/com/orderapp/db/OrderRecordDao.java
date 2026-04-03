package com.orderapp.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OrderRecordDao {

    @Insert
    void insertOrder(OrderRecord order);

    @Query("SELECT * FROM order_history WHERE TRIM(shopName) != '' AND LOWER(shopName) LIKE LOWER(:prefix) || '%' ORDER BY submittedAt DESC LIMIT :maxResults")
    List<OrderRecord> findRecentOrdersByShopPrefix(String prefix, int maxResults);

    @Query("SELECT * FROM order_history ORDER BY submittedAt DESC")
    List<OrderRecord> getAllOrdersSortedByDate();

    @Query("DELETE FROM order_history WHERE id = :id")
    void deleteById(int id);
}
