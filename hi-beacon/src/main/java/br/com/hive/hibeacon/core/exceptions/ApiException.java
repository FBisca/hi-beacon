package br.com.hive.hibeacon.core.exceptions;

/**
 * Created by FBisca on 16/10/2015.
 */
public class ApiException extends RuntimeException {

    public ApiException(String detailMessage) {
        super(detailMessage);
    }

    public ApiException() {
    }
}
