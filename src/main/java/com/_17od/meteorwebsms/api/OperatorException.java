package com._17od.meteorwebsms.api;

public class OperatorException extends Exception {

    private static final long serialVersionUID = 7573892923186481775L;

    public OperatorException(String message) {
        super(message);
    }

    public OperatorException(String message, Exception e) {
        super(message, e);
    }

}
