package com.omorni.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by user on 3/6/2017.
 */

public class OrderResponse implements Parcelable {

    String id;
    String buyer_id;
    String seller_id;
    String package_id;
    String status;
    String created_date;
    String first_name;
    String last_name;
    String email;
    String job_description;
    String user_image;
    String package_name;
    String normal_charges;
    String additional_charges;
    String name;
    String description;
    String main_hours;
    String extra_hours;
    String total_rating_user;
    String avgrating;
    String language;

    String start_date;
    String start_time;
    String is_scheduled;
    String checkin_time;
    String checkout_time;
    private String request_type;
    private String price;
    private String seller_image;
    private String package_type;
    private String end_time;

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getPackage_type() {
        return package_type;
    }

    public void setPackage_type(String package_type) {
        this.package_type = package_type;
    }

    public String getSeller_image() {
        return seller_image;
    }

    public void setSeller_image(String seller_image) {
        this.seller_image = seller_image;
    }

    public String getProcessing_fee() {
        return processing_fee;
    }

    public void setProcessing_fee(String processing_fee) {
        this.processing_fee = processing_fee;
    }

    public String getMain_amount() {
        return main_amount;
    }

    public void setMain_amount(String main_amount) {
        this.main_amount = main_amount;
    }


    private String processing_fee;
    private String main_amount;
    private String vat_tax;

    public String getVat_tax() {
        return vat_tax;
    }

    public void setVat_tax(String vat_tax) {
        this.vat_tax = vat_tax;
    }

    private ArrayList<AllReviewsResponse> allreviews;

    public ArrayList<AllReviewsResponse> getAllreviews() {
        return allreviews;
    }

    public void setAllreviews(ArrayList<AllReviewsResponse> allreviews) {
        this.allreviews = allreviews;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    protected OrderResponse(Parcel in) {
        this.id = in.readString();
        this.buyer_id = in.readString();
        this.seller_id = in.readString();
        this.package_id = in.readString();
        this.status = in.readString();
        this.created_date = in.readString();
        this.first_name = in.readString();
        this.last_name = in.readString();
        this.email = in.readString();
        this.job_description = in.readString();
        this.user_image = in.readString();
        this.package_name = in.readString();
        this.normal_charges = in.readString();
        this.additional_charges = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.main_hours = in.readString();
        this.extra_hours = in.readString();
        this.total_rating_user = in.readString();
        this.avgrating = in.readString();
        this.category = in.readString();
        this.language = in.readString();

        this.start_date = in.readString();
        this.start_time = in.readString();
        this.is_scheduled = in.readString();
        this.checkin_time = in.readString();
        this.checkout_time = in.readString();
        this.request_type = in.readString();
        this.price = in.readString();
        this.processing_fee = in.readString();
        this.seller_image = in.readString();
        this.end_time = in.readString();
        this.vat_tax = in.readString();
        this.allreviews = in.createTypedArrayList(AllReviewsResponse.CREATOR);

    }

    public static final Creator<OrderResponse> CREATOR = new Creator<OrderResponse>() {
        @Override
        public OrderResponse createFromParcel(Parcel in) {
            return new OrderResponse(in);
        }

        @Override
        public OrderResponse[] newArray(int size) {
            return new OrderResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.id);
        parcel.writeString(this.buyer_id);
        parcel.writeString(this.seller_id);
        parcel.writeString(this.package_id);
        parcel.writeString(this.status);
        parcel.writeString(this.created_date);
        parcel.writeString(this.first_name);
        parcel.writeString(this.last_name);
        parcel.writeString(this.email);
        parcel.writeString(this.job_description);
        parcel.writeString(this.user_image);
        parcel.writeString(this.package_name);
        parcel.writeString(this.normal_charges);
        parcel.writeString(this.additional_charges);
        parcel.writeString(this.name);
        parcel.writeString(this.description);
        parcel.writeString(this.main_hours);
        parcel.writeString(this.extra_hours);
        parcel.writeString(this.total_rating_user);
        parcel.writeString(this.avgrating);
        parcel.writeString(this.category);
        parcel.writeString(this.language);

        parcel.writeString(this.start_date);
        parcel.writeString(this.start_time);
        parcel.writeString(this.is_scheduled);
        parcel.writeString(this.checkin_time);
        parcel.writeString(this.checkout_time);
        parcel.writeString(this.request_type);
        parcel.writeString(this.price);
        parcel.writeString(this.processing_fee);
        parcel.writeString(this.seller_image);
        parcel.writeString(this.end_time);
        parcel.writeString(this.vat_tax);

        parcel.writeTypedList(allreviews);
    }


    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getIs_scheduled() {
        return is_scheduled;
    }

    public void setIs_scheduled(String is_scheduled) {
        this.is_scheduled = is_scheduled;
    }

    public String getCheckin_time() {
        return checkin_time;
    }

    public void setCheckin_time(String checkin_time) {
        this.checkin_time = checkin_time;
    }

    public String getCheckout_time() {
        return checkout_time;
    }

    public void setCheckout_time(String checkout_time) {
        this.checkout_time = checkout_time;
    }


    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getJob_description() {
        return job_description;
    }

    public void setJob_description(String job_description) {
        this.job_description = job_description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    String category;

    public String getPackage_id() {
        return package_id;
    }

    public void setPackage_id(String package_id) {
        this.package_id = package_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBuyer_id() {
        return buyer_id;
    }

    public void setBuyer_id(String buyer_id) {
        this.buyer_id = buyer_id;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
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


    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getNormal_charges() {
        return normal_charges;
    }

    public void setNormal_charges(String normal_charges) {
        this.normal_charges = normal_charges;
    }

    public String getAdditional_charges() {
        return additional_charges;
    }

    public void setAdditional_charges(String additional_charges) {
        this.additional_charges = additional_charges;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMain_hours() {
        return main_hours;
    }

    public void setMain_hours(String main_hours) {
        this.main_hours = main_hours;
    }

    public String getExtra_hours() {
        return extra_hours;
    }

    public void setExtra_hours(String extra_hours) {
        this.extra_hours = extra_hours;
    }

    public String getTotal_rating_user() {
        return total_rating_user;
    }

    public void setTotal_rating_user(String total_rating_user) {
        this.total_rating_user = total_rating_user;
    }

    public String getAvgrating() {
        return avgrating;
    }

    public void setAvgrating(String avgrating) {
        this.avgrating = avgrating;
    }


    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }


    public OrderResponse() {

    }


}
