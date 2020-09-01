package ar.delellis.quicknotes.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Note {
    @Expose
    @SerializedName("id") private int id;

    @Expose
    @SerializedName("title") private String title;

    @Expose
    @SerializedName("content") private String content;

    @Expose
    @SerializedName("color") private String color;

    @Expose
    @SerializedName("timestamp") private int timestamp;

    @Expose
    @SerializedName("is_shared") private boolean is_shared;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getIsShared() {
        return is_shared;
    }

    public void setIsShared(boolean is_shared) {
        this.is_shared = is_shared;
    }

}
