package com.fiberg.wsio.handler.state;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlEnum
@XmlRootElement
public enum WsIOLanguage {

	@XmlEnumValue("")
	DEFAULT,

	@XmlEnumValue("es")
	SPANISH,

	@XmlEnumValue("en")
	ENGLISH;

	public String value() {
		return name().toLowerCase();
	}

	public static WsIOLanguage fromValue(String v) {
		for (WsIOLanguage status : values()) {
			if (status.name().equalsIgnoreCase(v)) {
				return status;
			}
		}
		return null;
	}

}
