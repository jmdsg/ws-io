package com.fiberg.wsio.handler.state;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlEnum
@XmlRootElement
public enum WsIODetail {

	@XmlEnumValue("general")
	GENERAL,

	@XmlEnumValue("specific")
	SPECIFIC;

	public String value() {
		return name().toLowerCase();
	}

	public static WsIODetail fromValue(String v) {
		for (WsIODetail status : values()) {
			if (status.name().equalsIgnoreCase(v)) {
				return status;
			}
		}
		return null;
	}

}
