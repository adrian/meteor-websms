package com._17od.meteorwebsms.api;

public interface Operator {

	public void sendMessage(String message, MSISDN recipient) throws OperatorException;

}
