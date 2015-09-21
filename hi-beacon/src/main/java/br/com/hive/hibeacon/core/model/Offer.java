package br.com.hive.hibeacon.core.model;

import java.util.Date;

/**
 * Created by FBisca on 18/09/2015.
 */
public class Offer {

    private String message;
    private Date expirationDate;

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
