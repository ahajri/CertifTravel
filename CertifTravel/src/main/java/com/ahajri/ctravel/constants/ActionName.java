package com.ahajri.ctravel.constants;

public enum ActionName {

	INSERT_FRAGMENT("IF"), UPDATE_FRAGMENT("UF"), UPSERT_FRAGMENT("UIF"), DELETE_FRAGMENT("DF"), SEARCH_DOCUMENT(
			"SD"), CREATE_DOCUEMNT("CD"), DELETE_DOCUMENT("DD");

	private String name;

	private ActionName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
