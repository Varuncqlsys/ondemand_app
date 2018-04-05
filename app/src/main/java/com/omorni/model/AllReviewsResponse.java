package com.omorni.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by V on 2/27/2017.
 */

public class AllReviewsResponse implements Parcelable {
    private String rating;
    private String comment;
    private String first_name;
    private String user_image;
    private String rating_date;

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public static Creator getCREATOR() {
        return CREATOR;
    }

    public String getRating_date() {
        return rating_date;
    }

    public void setRating_date(String rating_date) {
        this.rating_date = rating_date;
    }

    private String last_name;


    public AllReviewsResponse() {

    }

    public AllReviewsResponse(Parcel in) {
        this.rating = in.readString();
        this.comment = in.readString();
        this.first_name = in.readString();
        this.last_name = in.readString();
        this.user_image = in.readString();
        this.rating_date = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.rating);
        parcel.writeString(this.comment);
        parcel.writeString(this.first_name);
        parcel.writeString(this.last_name);
        parcel.writeString(this.user_image);
        parcel.writeString(this.rating_date);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public AllReviewsResponse createFromParcel(Parcel in) {
            return new AllReviewsResponse(in);
        }

        public AllReviewsResponse[] newArray(int size) {
            return new AllReviewsResponse[size];
        }
    };
}
