package com.ahajri.ctravel.constants;

/**
 * 
 * @author
 *         <p>
 *         ahajri
 *         </p>
 */
public enum Keys {

	EXEC_SUCCESSFUL("execSuccessful"), MESSAGE("msg"),  EXEC_DURATION(
			"execDuration"), HTTP_STATUS("httpStatus"), DOCUMENT("document"), EMAIL("email"), PASSWORD("password"), RETURNED_DATA("returnedData");

	private String value;

	private Keys(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
