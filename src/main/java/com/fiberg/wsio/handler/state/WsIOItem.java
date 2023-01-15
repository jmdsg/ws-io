package com.fiberg.wsio.handler.state;

import jakarta.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = { "identifier", "type", "message", "description" })
public class WsIOItem {

	private String identifier;

	private WsIOText message;

	private WsIOText description;

	private String type;

	public static WsIOItem of(String identifier,
							  WsIOText message,
							  WsIOText description,
							  String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setMessage(message);
		stateElement.setDescription(description);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem id(String identifier) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		return stateElement;
	}

	public static WsIOItem message(WsIOText message) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessage(message);
		return stateElement;
	}

	public static WsIOItem description(WsIOText description) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setDescription(description);
		return stateElement;
	}

	public static WsIOItem type(String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem messageOf(WsIOLanguage language, String message) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessageOf(language, message);
		return stateElement;
	}

	public static WsIOItem descriptionOf(WsIOLanguage language, String description) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setDescriptionOf(language, description);
		return stateElement;
	}

	public static WsIOItem messageDef(String message) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessageDef(message);
		return stateElement;
	}

	public static WsIOItem descriptionDef(String description) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setDescriptionDef(description);
		return stateElement;
	}

	public static WsIOItem messageNoLang(String message) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessageNoLang(message);
		return stateElement;
	}

	public static WsIOItem descriptionNoLang(String description) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setDescriptionNoLang(description);
		return stateElement;
	}

	public static WsIOItem basic(String identifier,
								 WsIOText message) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setMessage(message);
		return stateElement;
	}

	public static WsIOItem simple(WsIOText message,
								  String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessage(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem text(WsIOText message,
								WsIOText description) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessage(message);
		stateElement.setDescription(description);
		return stateElement;
	}

	public static WsIOItem typed(String identifier,
								 String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem noType(String identifier,
								  WsIOText message,
								  WsIOText description) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setMessage(message);
		stateElement.setDescription(description);
		return stateElement;
	}

	public static WsIOItem noDesc(String identifier,
								  WsIOText message,
								  String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setMessage(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem noId(WsIOText message,
								WsIOText description,
								String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessage(message);
		stateElement.setDescription(description);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem basicOf(String identifier,
								   WsIOLanguage language,
								   String message) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageOf(language, message);
		return stateElement;
	}

	public static WsIOItem simpleOf(WsIOLanguage language,
									String message,
									String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessageOf(language, message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem textOf(WsIOLanguage language,
								  String message,
								  String description) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessageOf(language, message);
		stateElement.setDescriptionOf(language, description);
		return stateElement;
	}

	public static WsIOItem basicDef(String identifier,
									String message) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageDef(message);
		return stateElement;
	}

	public static WsIOItem simpleDef(String message,
									 String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessageDef(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem textDef(String message,
								   String description) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessageDef(message);
		stateElement.setDescriptionDef(description);
		return stateElement;
	}

	public static WsIOItem basicNoLang(String identifier,
									   String message) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageNoLang(message);
		return stateElement;
	}

	public static WsIOItem simpleNoLang(String message,
										String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessageNoLang(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem textNoLang(String message,
									  String description) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessageNoLang(message);
		stateElement.setDescriptionNoLang(description);
		return stateElement;
	}

	public static WsIOItem noTypeOf(String identifier,
									WsIOLanguage language,
									String message,
									String description) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageOf(language, message);
		stateElement.setDescriptionOf(language, description);
		return stateElement;
	}

	public static WsIOItem noDescOf(String identifier,
									WsIOLanguage language,
									String message,
									String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageOf(language, message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem noIdOf(WsIOLanguage language,
								  String message,
								  String description,
								  String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessageOf(language, message);
		stateElement.setDescriptionOf(language, description);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem noTypeDef(String identifier,
									 String message,
									 String description) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageDef(message);
		stateElement.setDescriptionDef(description);
		return stateElement;
	}

	public static WsIOItem noDescDef(String identifier,
									 String message,
									 String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageDef(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem noIdDef(String message,
								   String description,
								   String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessageDef(message);
		stateElement.setDescriptionDef(description);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem noTypeNoLang(String identifier,
										String message,
										String description) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageNoLang(message);
		stateElement.setDescriptionNoLang(description);
		return stateElement;
	}

	public static WsIOItem noDescNoLang(String identifier,
										String message,
										String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageNoLang(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOItem noIdNoLang(String message,
									  String description,
									  String type) {
		WsIOItem stateElement = new WsIOItem();
		stateElement.setMessageNoLang(message);
		stateElement.setDescriptionNoLang(description);
		stateElement.setType(type);
		return stateElement;
	}

	@XmlAttribute(name = "id")
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@XmlElement
	public WsIOText getMessage() {
		return message;
	}

	public void setMessage(WsIOText message) {
		this.message = message;
	}

	@XmlElement
	public WsIOText getDescription() {
		return description;
	}

	public void setDescription(WsIOText description) {
		this.description = description;
	}

	@XmlElement
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setMessageOf(WsIOLanguage language, String message) {
		this.message = WsIOText.of(language, message);
	}

	public void setDescriptionOf(WsIOLanguage language, String description) {
		this.description = WsIOText.of(language, description);
	}

	public void setMessageDef(String message) {
		this.message = WsIOText.def(message);
	}

	public void setDescriptionDef(String description) {
		this.description = WsIOText.def(description);
	}

	public void setMessageNoLang(String message) {
		this.message = WsIOText.noLang(message);
	}

	public void setDescriptionNoLang(String description) {
		this.description = WsIOText.noLang(description);
	}

}
