package com.omorni.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by test on 2/22/2017.
 */

public class SellerData implements Parcelable {

    private String sellerId;
    private String first_name;
    private String last_name;
    private String email;
    private String mobile;
    private String location;
    private String job_description;
    private String user_image;
    private String is_verify;
    private String user_type;
    private String status;
    private String created_date;
    private String latitude;
    private String longitude;
    private String on_duty;
    private String distance;
    private String favourite;
    private String rating;
    private String language;
    private String omorni_processing_fee;
    private String req_lat;
    private String req_lng;
    private String seller_category;
    private String service_title;
    private String vat_tax;

    public String getVat_tax() {
        return vat_tax;
    }

    public void setVat_tax(String vat_tax) {
        this.vat_tax = vat_tax;
    }

    public String getService_title() {
        return service_title;
    }

    public void setService_title(String service_title) {
        this.service_title = service_title;
    }

    private ArrayList<AllPackageResponse> allPackages;
    private ArrayList<AllReviewsResponse> allreviews;

    public ArrayList<AllReviewsResponse> getAllreviews() {
        return allreviews;
    }

    public void setAllreviews(ArrayList<AllReviewsResponse> allreviews) {
        this.allreviews = allreviews;
    }

    public String getMarkerid() {
        return markerid;
    }

    public void setMarkerid(String markerid) {
        this.markerid = markerid;
    }

    private String markerid;

    public String getReq_lat() {
        return req_lat;
    }

    public void setReq_lat(String req_lat) {
        this.req_lat = req_lat;
    }

    public String getReq_lng() {
        return req_lng;
    }

    public void setReq_lng(String req_lng) {
        this.req_lng = req_lng;
    }

    public String getOmorni_processing_fee() {
        return omorni_processing_fee;
    }

    public void setOmorni_processing_fee(String omorni_processing_fee) {
        this.omorni_processing_fee = omorni_processing_fee;
    }


    private String total_rating_user;


    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
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


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJob_description() {
        return job_description;
    }

    public void setJob_description(String job_description) {
        this.job_description = job_description;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getIs_verify() {
        return is_verify;
    }

    public void setIs_verify(String is_verify) {
        this.is_verify = is_verify;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
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

    public String getOn_duty() {
        return on_duty;
    }

    public void setOn_duty(String on_duty) {
        this.on_duty = on_duty;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getFavourite() {
        return favourite;
    }

    public void setFavourite(String favourite) {
        this.favourite = favourite;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public ArrayList<AllPackageResponse> getAllPackages() {
        return allPackages;
    }

    public void setAllPackages(ArrayList<AllPackageResponse> allPackages) {
        this.allPackages = allPackages;
    }

    public String getTotal_rating_user() {
        return total_rating_user;
    }

    public void setTotal_rating_user(String total_rating_user) {
        this.total_rating_user = total_rating_user;
    }

    public static Creator<SellerData> getCREATOR() {
        return CREATOR;
    }


    /***
     * default constructor
     ***/
    public SellerData() {
    }

    public SellerData(Parcel in) {
        this.sellerId = in.readString();
        this.first_name = in.readString();
        this.last_name = in.readString();
        this.email = in.readString();
        this.mobile = in.readString();
        this.location = in.readString();
        this.job_description = in.readString();
        this.user_image = in.readString();
        this.is_verify = in.readString();
        this.user_type = in.readString();
        this.status = in.readString();
        this.created_date = in.readString();
        this.latitude = in.readString();
        this.longitude = in.readString();
        this.on_duty = in.readString();
        this.distance = in.readString();
        this.favourite = in.readString();
        this.rating = in.readString();
        this.language = in.readString();
        this.omorni_processing_fee = in.readString();
        this.total_rating_user = in.readString();
        this.req_lat = in.readString();
        this.req_lng = in.readString();
        this.markerid = in.readString();
        this.seller_category = in.readString();
        this.service_title = in.readString();
        this.vat_tax = in.readString();

        this.allPackages = in.createTypedArrayList(AllPackageResponse.CREATOR);
        this.allreviews = in.createTypedArrayList(AllReviewsResponse.CREATOR);

    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSeller_category() {
        return seller_category;
    }

    public void setSeller_category(String seller_category) {
        this.seller_category = seller_category;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sellerId);
        dest.writeString(first_name);
        dest.writeString(last_name);
        dest.writeString(email);
        dest.writeString(mobile);
        dest.writeString(location);
        dest.writeString(job_description);
        dest.writeString(user_image);
        dest.writeString(is_verify);
        dest.writeString(user_type);
        dest.writeString(status);
        dest.writeString(created_date);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(on_duty);
        dest.writeString(distance);
        dest.writeString(favourite);
        dest.writeString(rating);

        dest.writeString(language);
        dest.writeString(omorni_processing_fee);
        dest.writeString(total_rating_user);
        dest.writeString(req_lat);
        dest.writeString(req_lng);
        dest.writeString(markerid);
        dest.writeString(seller_category);
        dest.writeString(service_title);
        dest.writeString(vat_tax);

        dest.writeTypedList(allPackages);
        dest.writeTypedList(allreviews);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SellerData> CREATOR = new Creator<SellerData>() {
        @Override
        public SellerData createFromParcel(Parcel in) {
            return new SellerData(in);
        }

        @Override
        public SellerData[] newArray(int size) {
            return new SellerData[size];
        }
    };


}
