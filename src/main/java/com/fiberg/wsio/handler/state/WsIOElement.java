package com.fiberg.wsio.handler.state;

import jakarta.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = { "identifier", "type", "message", "description" })
public class WsIOElement {

	private String identifier;

	private WsIOText message;

	private WsIOText description;

	private String type;

	public static WsIOElement of(String identifier,
	                             WsIOText message,
	                             WsIOText description,
	                             String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessage(message);
		stateElement.setDescription(description);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement id(String identifier) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		return stateElement;
	}

	public static WsIOElement message(WsIOText message) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessage(message);
		return stateElement;
	}

	public static WsIOElement description(WsIOText description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setDescription(description);
		return stateElement;
	}

	public static WsIOElement type(String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement messageOf(WsIOLanguage language, String message) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageOf(language, message);
		return stateElement;
	}

	public static WsIOElement descriptionOf(WsIOLanguage language, String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setDescriptionOf(language, description);
		return stateElement;
	}

	public static WsIOElement messageDef(String message) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageDef(message);
		return stateElement;
	}

	public static WsIOElement descriptionDef(String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setDescriptionDef(description);
		return stateElement;
	}

	public static WsIOElement messageNoLang(String message) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageNoLang(message);
		return stateElement;
	}

	public static WsIOElement descriptionNoLang(String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setDescriptionNoLang(description);
		return stateElement;
	}

	public static WsIOElement basic(String identifier,
	                                WsIOText message) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessage(message);
		return stateElement;
	}

	public static WsIOElement simple(WsIOText message,
	                                 String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessage(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement text(WsIOText message,
	                               WsIOText description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessage(message);
		stateElement.setDescription(description);
		return stateElement;
	}

	public static WsIOElement typed(String identifier,
	                                String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement noType(String identifier,
									 WsIOText message,
									 WsIOText description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessage(message);
		stateElement.setDescription(description);
		return stateElement;
	}

	public static WsIOElement noDesc(String identifier,
									 WsIOText message,
									 String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessage(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement noId(WsIOText message,
								   WsIOText description,
								   String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessage(message);
		stateElement.setDescription(description);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement basicOf(String identifier,
	                                  WsIOLanguage language,
	                                  String message) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageOf(language, message);
		return stateElement;
	}

	public static WsIOElement simpleOf(WsIOLanguage language,
	                                   String message,
	                                   String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageOf(language, message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement textOf(WsIOLanguage language,
	                                 String message,
	                                 String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageOf(language, message);
		stateElement.setDescriptionOf(language, description);
		return stateElement;
	}

	public static WsIOElement basicDef(String identifier,
	                                   String message) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageDef(message);
		return stateElement;
	}

	public static WsIOElement simpleDef(String message,
	                                    String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageDef(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement textDef(String message,
	                                  String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageDef(message);
		stateElement.setDescriptionDef(description);
		return stateElement;
	}

	public static WsIOElement basicNoLang(String identifier,
										  String message) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageNoLang(message);
		return stateElement;
	}

	public static WsIOElement simpleNoLang(String message,
										   String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageNoLang(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement textNoLang(String message,
										 String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageNoLang(message);
		stateElement.setDescriptionNoLang(description);
		return stateElement;
	}

	public static WsIOElement noTypeOf(String identifier,
									   WsIOLanguage language,
									   String message,
									   String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageOf(language, message);
		stateElement.setDescriptionOf(language, description);
		return stateElement;
	}

	public static WsIOElement noDescOf(String identifier,
									   WsIOLanguage language,
									   String message,
									   String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageOf(language, message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement noIdOf(WsIOLanguage language,
									 String message,
									 String description,
									 String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageOf(language, message);
		stateElement.setDescriptionOf(language, description);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement noTypeDef(String identifier,
										String message,
										String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageDef(message);
		stateElement.setDescriptionDef(description);
		return stateElement;
	}

	public static WsIOElement noDescDef(String identifier,
										String message,
										String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageDef(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement noIdDef(String message,
									  String description,
									  String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageDef(message);
		stateElement.setDescriptionDef(description);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement noTypeNoLang(String identifier,
										   String message,
										   String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageNoLang(message);
		stateElement.setDescriptionNoLang(description);
		return stateElement;
	}

	public static WsIOElement noDescNoLang(String identifier,
										   String message,
										   String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageNoLang(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement noIdNoLang(String message,
										 String description,
										 String type) {
		WsIOElement stateElement = new WsIOElement();
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
