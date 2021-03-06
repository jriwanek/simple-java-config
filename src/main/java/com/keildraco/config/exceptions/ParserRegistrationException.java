package com.keildraco.config.exceptions;

/**
 *
 * @author Daniel Hazelton
 */
public class ParserRegistrationException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -5778693947692239386L;

	/**
	 *
	 * @param parserName
	 */
	public ParserRegistrationException(final String parserName) {
		super("An issue occurred while registering parser " + parserName);
	}

	/**
	 *
	 * @param parserName
	 * @param originalException
	 */
	public ParserRegistrationException(final String parserName, final Exception originalException) {
		super("An exception occurred while registering parser " + parserName + " -- "
				+ originalException.getClass() + " :: " + originalException.getMessage());
	}
}
