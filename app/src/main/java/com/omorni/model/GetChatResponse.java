package com.omorni.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by V on 3/9/2017.
 */

public class GetChatResponse implements Parcelable {
    private String msg_id;
    private String sender;
    private String receiver;
    private String thread_id;
    private String message;
    private String created;
    private String username;
    private String user_image;
    private String message_type;
    private String thumb;

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.msg_id);
        parcel.writeString(this.sender);
        parcel.writeString(this.receiver);
        parcel.writeString(this.thread_id);
        parcel.writeString(this.message);
        parcel.writeString(this.created);
        parcel.writeString(this.username);
        parcel.writeString(this.user_image);
        parcel.writeString(this.message_type);
        parcel.writeString(this.thumb);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public GetChatResponse() {

    }

    protected GetChatResponse(Parcel in) {
        this.msg_id = in.readString();
        this.sender = in.readString();
        this.receiver = in.readString();
        this.thread_id = in.readString();
        this.message = in.readString();
        this.created = in.readString();
        this.username = in.readString();
        this.user_image = in.readString();
        this.message_type = in.readString();
        this.thumb = in.readString();
    }

    public static final Creator<GetChatResponse> CREATOR = new Creator<GetChatResponse>() {
        @Override
        public GetChatResponse createFromParcel(Parcel in) {
            return new GetChatResponse(in);
        }

        @Override
        public GetChatResponse[] newArray(int size) {
            return new GetChatResponse[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getThread_id() {
        return thread_id;
    }

    public void setThread_id(String thread_id) {
        this.thread_id = thread_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }


}
