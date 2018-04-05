package com.omorni.utils;

import com.omorni.model.PayFortData;

/**
 * Created by AIM on 11/21/2017.
 */

public interface IPaymentRequestCallBack {
    void onPaymentRequestResponse(int responseType, PayFortData responseData);
}
