package com.fiberg.wsio.handler.state;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlEnum
@XmlRootElement
public enum WsIOStatus {

	@XmlEnumValue("success")
	SUCCESS,

	@XmlEnumValue("failure")
	FAILURE,

	@XmlEnumValue("partial")
	PARTIAL;

	public String value() {
		return name().toLowerCase();
	}

	public static WsIOStatus fromValue(String v) {
		for (WsIOStatus status : values()) {
			if (status.name().equalsIgnoreCase(v)) {
				return status;
			}
		}
		return null;
	}

}
