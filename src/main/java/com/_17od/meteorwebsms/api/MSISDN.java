package com._17od.meteorwebsms.api;

/**
 * This class represents a MSISDN.
 * <p>
 * National and international number formats are accepted in the constructor.
 * <ul>
 * 	<li>+353872294733</li>
 *  <li>00353872294733</li>
 *  <li>0872294733</li>
 * </ul>
 * 
 * If a national number is passed in then the country code defaults to 353(Ireland).
 * 
 * @author itdsmita
 *
 */
public class MSISDN {

	private String countryCode;
	private String nationalDestinationCode;
	private String subscriberNumber;
	private static final String[] SUPPORTED_COUNTRY_CODES = new String[] {"353"}; 
	private static final String[] SUPPORTED_NDC_CODES = new String[] {"083", "085", "086", "087", "088"}; 
	
	public MSISDN(String number) throws MSISDNParseException {
		if (number.charAt(0) == '+') {
			parseIntNationalNumber(number.substring(1));
		} else if (number.startsWith("00")) {
			parseIntNationalNumber(number.substring(2));
		} else {
			parserNationalNumber(number);
		}
	}

	private void parseIntNationalNumber(String number) throws MSISDNParseException {
		// Find the country code from the list of supported codes
		int i = 0;
		do {
			if (number.startsWith(SUPPORTED_COUNTRY_CODES[i])) {
				countryCode = SUPPORTED_COUNTRY_CODES[i];
				// Parse the remaing string with a "0" in front of it (because the international version will
				// have dropped the "0")
				parserNationalNumber("0" + number.substring(SUPPORTED_COUNTRY_CODES[i].length()));
			}
			i++;
		} while (i<SUPPORTED_COUNTRY_CODES.length && countryCode == null);
		
		if (countryCode == null) {
			throw new MSISDNParseException("Unabled to parse the number [" + number + "]");
		}
	}
	
	private void parserNationalNumber(String number) throws MSISDNParseException {
		int i = 0;
		do {
			if (number.startsWith(SUPPORTED_NDC_CODES[i])) {
				if (countryCode == null) {
					countryCode = "353"; 
				}
				nationalDestinationCode = SUPPORTED_NDC_CODES[i];
				subscriberNumber = number.substring(SUPPORTED_NDC_CODES[i].length());
			}
			i++;
		} while (i<SUPPORTED_NDC_CODES.length && nationalDestinationCode == null);

		if (nationalDestinationCode == null) {
			throw new MSISDNParseException("Unabled to parse the number [" + number + "]");
		}
}

	public String getCountryCode() {
		return countryCode;
	}

	public String getNationalDestinationCode() {
		return nationalDestinationCode;
	}

	public String getSubscriberNumber() {
		return subscriberNumber;
	}
	
	public String getInternationalNumber() {
		StringBuffer buf = new StringBuffer("+");
		buf.append(countryCode);
		buf.append(nationalDestinationCode.substring(1));
		buf.append(subscriberNumber);
		return buf.toString();
	}
	
	public String getNationalNumber() {
		StringBuffer buf = new StringBuffer(nationalDestinationCode);
		buf.append(subscriberNumber);
		return buf.toString();
	}

	/**
	 * Returns true if this MSISDN is for an irish mobile
	 * @return
	 */
	public boolean isIrishMobile() {
	    boolean isIrishMobile = false;

	    if (countryCode.equals("353")) {
	        if (nationalDestinationCode.equals("083") ||
	                nationalDestinationCode.equals("085") ||
	                nationalDestinationCode.equals("086") ||
	                nationalDestinationCode.equals("087") ||
	                nationalDestinationCode.equals("088")) {
	            isIrishMobile = true;
	        }
	    }

	    return isIrishMobile;
	}
}
