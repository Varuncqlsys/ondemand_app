package com.omorni.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by user on 6/24/2017.
 */

public class EventResponse implements Parcelable {

    String id;
    String event_name;
    String event_des;
    String event_location;
    String latitude;
    String longitude;
    String start_time;
    String end_time;
    String event_repeat;
    String repeat_type;
    String alert_type;
    String event_date;
    String event_end_date;
    String even_type;
    String name;
    String time;
    String price;
    String request_type;

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public String getNormal_charges() {
        return normal_charges;
    }

    public void setNormal_charges(String normal_charges) {
        this.normal_charges = normal_charges;
    }

    String normal_charges;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public String getEvent_des() {
        return event_des;
    }

    public void setEvent_des(String event_des) {
        this.event_des = event_des;
    }

    public String getEvent_location() {
        return event_location;
    }

    public void setEvent_location(String event_location) {
        this.event_location = event_location;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getEvent_repeat() {
        return event_repeat;
    }

    public void setEvent_repeat(String event_repeat) {
        this.event_repeat = event_repeat;
    }

    public String getRepeat_type() {
        return repeat_type;
    }

    public void setRepeat_type(String repeat_type) {
        this.repeat_type = repeat_type;
    }

    public String getAlert_type() {
        return alert_type;
    }

    public void setAlert_type(String alert_type) {
        this.alert_type = alert_type;
    }

    public String getEvent_date() {
        return event_date;
    }

    public void setEvent_date(String event_date) {
        this.event_date = event_date;
    }

    public String getEven_type() {
        return even_type;
    }

    public void setEven_type(String even_type) {
        this.even_type = even_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEvent_end_date() {
        return event_end_date;
    }

    public void setEvent_end_date(String event_end_date) {
        this.event_end_date = event_end_date;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public EventResponse() {

    }

    public EventResponse(Parcel in) {
        this.id = in.readString();
        this.event_name = in.readString();
        this.event_des = in.readString();
        this.event_location = in.readString();
        this.latitude = in.readString();
        this.longitude = in.readString();
        this.start_time = in.readString();
        this.end_time = in.readString();
        this.event_repeat = in.readString();
        this.repeat_type = in.readString();
        this.alert_type = in.readString();
        this.event_date = in.readString();
        this.event_end_date = in.readString();
        this.even_type = in.readString();
        this.name = in.readString();
        this.time = in.readString();
        this.price = in.readString();
        this.request_type = in.readString();
        this.normal_charges = in.readString();
    }

    public static final Creator CREATOR = new Creator() {
        public EventResponse createFromParcel(Parcel in) {
            return new EventResponse(in);
        }

        public EventResponse[] newArray(int size) {
            return new EventResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.id);
        parcel.writeString(this.event_name);
        parcel.writeString(this.event_des);
        parcel.writeString(this.event_location);
        parcel.writeString(this.latitude);
        parcel.writeString(this.longitude);
        parcel.writeString(this.start_time);
        parcel.writeString(this.end_time);
        parcel.writeString(this.event_repeat);
        parcel.writeString(this.repeat_type);
        parcel.writeString(this.alert_type);
        parcel.writeString(this.event_date);
        parcel.writeString(this.event_end_date);
        parcel.writeString(this.even_type);
        parcel.writeString(this.name);
        parcel.writeString(this.time);
        parcel.writeString(this.price);
        parcel.writeString(this.request_type);
        parcel.writeString(this.normal_charges);
    }
}
