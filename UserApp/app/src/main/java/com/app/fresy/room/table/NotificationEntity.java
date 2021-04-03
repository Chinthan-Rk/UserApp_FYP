package com.app.fresy.room.table;

import com.app.fresy.model.Notification;

import java.io.Serializable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/*
 * To save notification
 */

@Entity(tableName = "notification")
public class NotificationEntity implements Serializable {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "data")
    public String data;

    // extra attribute
    @ColumnInfo(name = "read")
    public Boolean read = false;

    @ColumnInfo(name = "created_at")
    public Long created_at;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Long created_at) {
        this.created_at = created_at;
    }

    public static NotificationEntity entity(Notification notification) {
        NotificationEntity entity = new NotificationEntity();
        entity.setId(notification.id);
        entity.setTitle(notification.title);
        entity.setContent(notification.content);
        entity.setType(notification.type);
        entity.setData(notification.data);
        entity.setRead(notification.read);
        entity.setCreated_at(notification.created_at);
        return entity;
    }
}
