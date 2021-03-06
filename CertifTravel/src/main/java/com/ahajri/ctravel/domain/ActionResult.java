package com.ahajri.ctravel.domain;

import java.io.Serializable;
import java.util.HashSet;

import org.springframework.http.HttpStatus;

import com.ahajri.ctravel.constants.ActionResultName;

public class ActionResult implements Serializable {

	/**
	 * Serialization UID
	 */
	private static final long serialVersionUID = 2215562584560868453L;

	/** Set for technical message */
	private final HashSet<String> techMessages = new HashSet<String>();
	private ActionResultName actionResultName;
	private Object jsonReturnData;
	private HttpStatus status;

	public ActionResult() {
		super();
	}

	public HashSet<String> getTechMessages() {
		return techMessages;
	}

	public ActionResultName getActionResultName() {
		return actionResultName;
	}

	public void setActionResultName(ActionResultName actionResultName) {
		this.actionResultName = actionResultName;
	}

	public Object getJsonReturnData() {
		return jsonReturnData;
	}

	public void setJsonReturnData(Object jsonReturnData) {
		this.jsonReturnData = jsonReturnData;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}
}
