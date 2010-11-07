package com._17od.meteorwebsms.api;

import com._17od.meteorwebsms.api.MSISDN;
import com._17od.meteorwebsms.api.MSISDNParseException;

import junit.framework.TestCase;

public class MSISDNTest extends TestCase {

	public void testIntNationalNumberWithPlus() throws MSISDNParseException {
		MSISDN msisdn = new MSISDN("+353872238443");
		assertEquals("353", msisdn.getCountryCode());
		assertEquals("087", msisdn.getNationalDestinationCode());
		assertEquals("2238443", msisdn.getSubscriberNumber());
	}

	public void testIntNationalNumberWith00() throws MSISDNParseException {
		MSISDN msisdn = new MSISDN("00353872238443");
		assertEquals("353", msisdn.getCountryCode());
		assertEquals("087", msisdn.getNationalDestinationCode());
		assertEquals("2238443", msisdn.getSubscriberNumber());
	}

	public void testNationalNumber() throws MSISDNParseException {
		MSISDN msisdn = new MSISDN("0872238443");
		assertEquals("353", msisdn.getCountryCode());
		assertEquals("087", msisdn.getNationalDestinationCode());
		assertEquals("2238443", msisdn.getSubscriberNumber());
	}

	public void testBadIntNationalNumber() throws MSISDNParseException {
		try {
			new MSISDN("+123872238443");
			fail("Expected to fail because of bad country code");
		} catch (MSISDNParseException e) {
			// OK to get here
		}
	}

	public void testBadNationalNumber() throws MSISDNParseException {
		try {
			new MSISDN("0842238443");
			fail("Expected to fail because of bad NDC");
		} catch (MSISDNParseException e) {
			// OK to get here
		}
	}

}
