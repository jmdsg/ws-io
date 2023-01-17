package com.fiberg.wsio.handler.state;

import jakarta.xml.bind.annotation.*;

import java.util.Objects;

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

	public static WsIOText noLang(String text) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WsIOText wsIOText = (WsIOText) o;
		return language == wsIOText.language && Objects.equals(text, wsIOText.text);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(language, text);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "WsIOText{" +
				"language=" + language +
				", text='" + text + '\'' +
				'}';
	}

}
