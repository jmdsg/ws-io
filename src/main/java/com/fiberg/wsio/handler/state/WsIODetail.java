package com.fiberg.wsio.handler.state;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

@XmlEnum
@XmlRootElement
public enum WsIODetail {

	@XmlEnumValue("general")
	GENERAL,

	@XmlEnumValue("element")
	ELEMENT;

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
