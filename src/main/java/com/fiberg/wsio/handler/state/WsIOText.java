package com.fiberg.wsio.handler.state;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class WsIOText {

	private WsIOLanguage language;

	private String text;

	public static WsIOText of(WsIOLanguage language, String text) {
		WsIOText stateTextEntry = new WsIOText();
		stateTextEntry.setLanguage(language);
		stateTextEntry.setText(text);
		return stateTextEntry;
	}

	public static WsIOText nolang(String text) {
		WsIOText stateTextEntry = new WsIOText();
		stateTextEntry.setText(text);
		return stateTextEntry;
	}

	public static WsIOText def(String text) {
		WsIOText stateTextEntry = new WsIOText();
		stateTextEntry.setLanguage(WsIOLanguage.DEFAULT);
		stateTextEntry.setText(text);
		return stateTextEntry;
	}

	@XmlAttribute
	public WsIOLanguage getLanguage() {
		return language;
	}

	public void setLanguage(WsIOLanguage language) {
		this.language = language;
	}

	@XmlValue
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WsIOText that = (WsIOText) o;

		if (language != that.language) return false;
		return text != null ? text.equals(that.text) : that.text == null;
	}

	@Override
	public int hashCode() {
		int result = language != null ? language.hashCode() : 0;
		result = 31 * result + (text != null ? text.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "StateTextEntry{" +
				"language=" + language +
				", text='" + text + '\'' +
				'}';
	}

}
