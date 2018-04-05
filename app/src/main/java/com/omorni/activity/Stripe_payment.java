package com.omorni.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.omorni.R;
import com.omorni.fragment.PaymentForm;
import com.omorni.fragment.ProgressDialogFragment;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;


public class Stripe_payment extends FragmentActivity {

    /*
     * Change this to your publishable key.
     *
     * You can get your key here: https://manage.stripe.com/account/apikeys
     */

    public static final String PUBLISHABLE_KEY = "pk_test_ELxSggXqBbYyEs8WPNgPLfeb";
//    public static final String PUBLISHABLE_KEY = " com.stripe.example.service.extra.publishablekey";
    String tokenid,requestid,amount,currency,message;

    private ProgressDialogFragment progressFragment;
    Bundle extras;
    ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe_payment);

        progressFragment = ProgressDialogFragment.newInstance(R.string.progressMessage);
        pDialog = new ProgressDialog(this);

    }

    public void saveCreditCard(PaymentForm form) {


        Card card = new Card(
                form.getCardNumber(),
                form.getExpMonth(),
                form.getExpYear(),
                form.getCvc());
        card.setCurrency(form.getCurrency());

        boolean validation = card.validateCard();
        if (validation) {
            startProgress();
            new Stripe().createToken(
                    card,
                    PUBLISHABLE_KEY,
                    new TokenCallback() {

                        public void onError(Exception error) {
                            handleError(error.getLocalizedMessage());
                            finishProgress();
                        }

                        @Override
                        public void onSuccess(Token token) {
//                            getTokenList().addToList(token);
                             tokenid = token.getId();
//

                            Log.e("Token Id is :::", "" + tokenid);

                            finishProgress();
                        }
                    });
        } else if (!card.validateNumber()) {
            handleError("The card number that you entered is invalid");
        } else if (!card.validateExpiryDate()) {
            handleError("The expiration date that you entered is invalid");
        } else if (!card.validateCVC()) {
            handleError("The CVC code that you entered is invalid");
        } else {
            handleError("The card details that you entered are invalid");
        }
    }

    private void startProgress() {
        progressFragment.show(getSupportFragmentManager(), "progress");
    }

    private void finishProgress() {
        progressFragment.dismiss();
    }

    private void handleError(String error) {
//        DialogFragment fragment = ErrorDialogFragment.newInstance("",error);
//        fragment.show(getSupportFragmentManager(), "error");
    }





}
