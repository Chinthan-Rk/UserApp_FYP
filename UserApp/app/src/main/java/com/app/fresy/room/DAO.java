package com.app.fresy.room;

import com.app.fresy.room.table.CartEntity;
import com.app.fresy.room.table.NotificationEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface DAO {

    /* table cart transaction ------------------------------------------------------------------ */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCart(CartEntity cart);

    @Update
    public void updateCart(CartEntity cart);

    @Query("DELETE FROM cart WHERE id = :id")
    void deleteCart(long id);

    @Query("DELETE FROM cart")
    void deleteAllCart();

    @Query("SELECT * FROM cart ORDER BY saved_date DESC")
    List<CartEntity> getAllCart();

    @Query("SELECT COUNT(id) FROM cart")
    Integer getCartCount();

    @Query("SELECT * FROM cart WHERE id = :id LIMIT 1")
    CartEntity getCart(long id);

    /* table notification transaction ----------------------------------------------------------- */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotification(NotificationEntity notification);

    @Query("DELETE FROM notification WHERE id = :id")
    void deleteNotification(long id);

    @Query("DELETE FROM notification")
    void deleteAllNotification();

    @Query("SELECT * FROM notification ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    List<NotificationEntity> getNotificationByPage(int limit, int offset);

    @Query("SELECT * FROM notification WHERE id = :id LIMIT 1")
    NotificationEntity getNotification(long id);

    @Query("SELECT COUNT(id) FROM notification WHERE read = 0")
    Integer getNotificationUnreadCount();

    @Query("SELECT COUNT(id) FROM notification")
    Integer getNotificationCount();
}
