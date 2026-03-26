package io.spring.boot.controller;

public class PaymentProviderException extends RuntimeException {
	
    public PaymentProviderException(String message) {
        super(message);
    }
}
