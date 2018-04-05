package com.omorni.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by V on 3/10/2017.
 */

public class PayPalResponse implements Parcelable {
    private String req_id;
    private String buyer_id;
    private String seller_id;
    private String package_id;
    private String req_la;
    private String req_lng;
    private String status;
    private String created_date;
    private String first_name;
    private String last_name;
    private String mobile;
    private String seller_latitude;
    private String seller_longitude;
    private String buyer_mobile;
    private String normal_charges;
    private String total_amounts;
    private String buyer_name;
    private String main_amount;
    private String processing_fees;
    private String processing_fees_ammount;
    private String thread_id;
    private String seller_address;
    private String buyer_address;
    private String category;
    private String payment_method;
    private String package_name;
    private String req_location;
    private String user_image;
    private String seller_image;
    private String checkin_time;
    private String checkout_time;
    private String distance;
    private String order_id;
    private String request_type;
    private String price;
    private String job_started;
    private String is_ratted;
    private String seller_fee;
    private String seller_fee_amount;
    private String seller_amount;
    private String additional_charges;
    private String extra_hours;
    private String extra_amount;
    private String post_title;

    private String vat_number;
    private String vat_tax;
    private String vat_tax_ammount;

    public String getVat_number() {
        return vat_number;
    }

    public void setVat_number(String vat_number) {
        this.vat_number = vat_number;
    }

    public String getVat_tax() {
        return vat_tax;
    }

    public void setVat_tax(String vat_tax) {
        this.vat_tax = vat_tax;
    }

    public String getVat_tax_ammount() {
        return vat_tax_ammount;
    }

    public void setVat_tax_ammount(String vat_tax_ammount) {
        this.vat_tax_ammount = vat_tax_ammount;
    }

    public String getPost_title() {
        return post_title;
    }

    public void setPost_title(String post_title) {
        this.post_title = post_title;
    }

    public String getExtra_hours() {
        return extra_hours;
    }

    public void setExtra_hours(String extra_hours) {
        this.extra_hours = extra_hours;
    }

    public String getAdditional_charges() {
        return additional_charges;
    }

    public void setAdditional_charges(String additional_charges) {
        this.additional_charges = additional_charges;
    }

    public String getSeller_fee() {
        return seller_fee;
    }

    public void setSeller_fee(String seller_fee) {
        this.seller_fee = seller_fee;
    }

    public String getSeller_fee_amount() {
        return seller_fee_amount;
    }

    public void setSeller_fee_amount(String seller_fee_amount) {
        this.seller_fee_amount = seller_fee_amount;
    }

    public String getSeller_amount() {
        return seller_amount;
    }

    public void setSeller_amount(String seller_amount) {
        this.seller_amount = seller_amount;
    }

    public String getIs_ratted() {
        return is_ratted;
    }

    public void setIs_ratted(String is_ratted) {
        this.is_ratted = is_ratted;
    }

    public String getJob_started() {
        return job_started;
    }

    public void setJob_started(String job_started) {
        this.job_started = job_started;
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

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
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

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getSeller_image() {
        return seller_image;
    }

    public void setSeller_image(String seller_image) {
        this.seller_image = seller_image;
    }

    public String getReq_location() {
        return req_location;
    }

    public void setReq_location(String req_location) {
        this.req_location = req_location;
    }

    private String start_date, start_time, is_scheduled;

    protected PayPalResponse(Parcel in) {
        this.req_id = in.readString();
        this.buyer_id = in.readString();
        this.seller_id = in.readString();
        this.package_id = in.readString();
        this.req_la = in.readString();
        this.req_lng = in.readString();
        this.status = in.readString();
        this.created_date = in.readString();
        this.first_name = in.readString();
        this.last_name = in.readString();
        this.mobile = in.readString();
        this.seller_latitude = in.readString();
        this.seller_longitude = in.readString();
        this.buyer_mobile = in.readString();
        this.normal_charges = in.readString();
        this.total_amounts = in.readString();
        this.buyer_name = in.readString();
        this.main_amount = in.readString();
        this.processing_fees = in.readString();
        this.processing_fees_ammount = in.readString();
        this.thread_id = in.readString();
        this.seller_address = in.readString();
        this.buyer_address = in.readString();
        this.category = in.readString();
        this.payment_method = in.readString();
        this.package_name = in.readString();

        this.start_date = in.readString();
        this.start_time = in.readString();
        this.is_scheduled = in.readString();
        this.req_location = in.readString();

        this.user_image = in.readString();
        this.seller_image = in.readString();

        this.checkin_time = in.readString();
        this.checkout_time = in.readString();
        this.distance = in.readString();
        this.order_id = in.readString();
        this.request_type = in.readString();
        this.price = in.readString();
        this.job_started = in.readString();
        this.is_ratted = in.readString();
        this.seller_fee = in.readString();
        this.seller_fee_amount = in.readString();
        this.seller_amount = in.readString();
        this.additional_charges = in.readString();
        this.extra_hours = in.readString();
        this.extra_amount = in.readString();
        this.post_title = in.readString();

        this.vat_number = in.readString();
        this.vat_tax = in.readString();
        this.vat_tax_ammount = in.readString();
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

    public String getExtra_amount() {
        return extra_amount;
    }

    public void setExtra_amount(String extra_amount) {
        this.extra_amount = extra_amount;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.req_id);
        parcel.writeString(this.buyer_id);
        parcel.writeString(this.seller_id);
        parcel.writeString(this.package_id);
        parcel.writeString(this.req_la);
        parcel.writeString(this.req_lng);
        parcel.writeString(this.status);
        parcel.writeString(this.created_date);
        parcel.writeString(this.first_name);
        parcel.writeString(this.last_name);
        parcel.writeString(this.mobile);
        parcel.writeString(this.seller_latitude);
        parcel.writeString(this.seller_longitude);
        parcel.writeString(this.buyer_mobile);
        parcel.writeString(this.normal_charges);
        parcel.writeString(this.total_amounts);
        parcel.writeString(this.buyer_name);
        parcel.writeString(this.main_amount);
        parcel.writeString(this.processing_fees);
        parcel.writeString(this.processing_fees_ammount);
        parcel.writeString(this.thread_id);
        parcel.writeString(this.seller_address);
        parcel.writeString(this.buyer_address);
        parcel.writeString(this.category);
        parcel.writeString(this.payment_method);
        parcel.writeString(this.package_name);

        parcel.writeString(this.start_date);
        parcel.writeString(this.start_time);
        parcel.writeString(this.is_scheduled);
        parcel.writeString(this.req_location);

        parcel.writeString(this.user_image);
        parcel.writeString(this.seller_image);

        parcel.writeString(this.checkin_time);
        parcel.writeString(this.checkout_time);
        parcel.writeString(this.distance);
        parcel.writeString(this.order_id);

        parcel.writeString(this.request_type);
        parcel.writeString(this.price);
        parcel.writeString(this.job_started);
        parcel.writeString(this.is_ratted);
        parcel.writeString(this.seller_fee);
        parcel.writeString(this.seller_fee_amount);
        parcel.writeString(this.seller_amount);
        parcel.writeString(this.additional_charges);
        parcel.writeString(this.extra_hours);
        parcel.writeString(this.extra_amount);
        parcel.writeString(this.post_title);

        parcel.writeString(this.vat_number);
        parcel.writeString(this.vat_tax);
        parcel.writeString(this.vat_tax_ammount);

    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSeller_address() {
        return seller_address;
    }

    public void setSeller_address(String seller_address) {
        this.seller_address = seller_address;
    }

    public String getBuyer_address() {
        return buyer_address;
    }

    public void setBuyer_address(String buyer_address) {
        this.buyer_address = buyer_address;
    }

    public String getSeller_latitude() {
        return seller_latitude;
    }

    public void setSeller_latitude(String seller_latitude) {
        this.seller_latitude = seller_latitude;
    }

    public String getSeller_longitude() {
        return seller_longitude;
    }

    public void setSeller_longitude(String seller_longitude) {
        this.seller_longitude = seller_longitude;
    }

    public String getMain_amount() {
        return main_amount;
    }

    public void setMain_amount(String main_amount) {
        this.main_amount = main_amount;
    }

    public String getProcessing_fees() {
        return processing_fees;
    }

    public void setProcessing_fees(String processing_fees) {
        this.processing_fees = processing_fees;
    }

    public String getProcessing_fees_ammount() {
        return processing_fees_ammount;
    }

    public void setProcessing_fees_ammount(String processing_fees_ammount) {
        this.processing_fees_ammount = processing_fees_ammount;
    }

    public String getThread_id() {
        return thread_id;
    }

    public void setThread_id(String thread_id) {
        this.thread_id = thread_id;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }


    public String getTotal_amounts() {
        return total_amounts;
    }

    public void setTotal_amounts(String total_amounts) {
        this.total_amounts = total_amounts;
    }

    public String getNormal_charges() {
        return normal_charges;
    }

    public void setNormal_charges(String normal_charges) {
        this.normal_charges = normal_charges;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public String getRequest_id() {
        return req_id;
    }

    public void setRequest_id(String order_id) {
        this.req_id = order_id;
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

    public String getPackage_id() {
        return package_id;
    }

    public void setPackage_id(String package_id) {
        this.package_id = package_id;
    }

    public String getReq_la() {
        return req_la;
    }

    public void setReq_la(String req_la) {
        this.req_la = req_la;
    }

    public String getReq_lng() {
        return req_lng;
    }

    public void setReq_lng(String req_lng) {
        this.req_lng = req_lng;
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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getBuyer_name() {
        return buyer_name;
    }

    public void setBuyer_name(String buyer_name) {
        this.buyer_name = buyer_name;
    }

    public String getBuyer_mobile() {
        return buyer_mobile;
    }

    public void setBuyer_mobile(String buyer_mobile) {
        this.buyer_mobile = buyer_mobile;
    }

    public static Creator<PayPalResponse> getCREATOR() {
        return CREATOR;
    }

    public PayPalResponse() {

    }


    public static final Creator<PayPalResponse> CREATOR = new Creator<PayPalResponse>() {
        @Override
        public PayPalResponse createFromParcel(Parcel in) {
            return new PayPalResponse(in);
        }

        @Override
        public PayPalResponse[] newArray(int size) {
            return new PayPalResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


}
