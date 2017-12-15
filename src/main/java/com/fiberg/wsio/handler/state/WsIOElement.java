package com.fiberg.wsio.handler.state;

import javax.xml.bind.annotation.*;

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

	public static WsIOElement messageNolang(String message) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageNolang(message);
		return stateElement;
	}

	public static WsIOElement descriptionNolang(String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setDescriptionNolang(description);
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

	public static WsIOElement notype(String identifier,
	                                 WsIOText message,
	                                 WsIOText description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessage(message);
		stateElement.setDescription(description);
		return stateElement;
	}

	public static WsIOElement nodesc(String identifier,
	                                 WsIOText message,
	                                 String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessage(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement noid(WsIOText message,
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

	public static WsIOElement basicNolang(String identifier,
	                                      String message) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageNolang(message);
		return stateElement;
	}

	public static WsIOElement simpleNolang(String message,
	                                       String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageNolang(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement textNolang(String message,
	                                     String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageNolang(message);
		stateElement.setDescriptionNolang(description);
		return stateElement;
	}

	public static WsIOElement notypeOf(String identifier,
	                                   WsIOLanguage language,
	                                   String message,
	                                   String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageOf(language, message);
		stateElement.setDescriptionOf(language, description);
		return stateElement;
	}

	public static WsIOElement nodescOf(String identifier,
	                                   WsIOLanguage language,
	                                   String message,
	                                   String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageOf(language, message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement noidOf(WsIOLanguage language,
	                                 String message,
	                                 String description,
	                                 String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageOf(language, message);
		stateElement.setDescriptionOf(language, description);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement notypeDef(String identifier,
	                                    String message,
	                                    String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageDef(message);
		stateElement.setDescriptionDef(description);
		return stateElement;
	}

	public static WsIOElement nodescDef(String identifier,
	                                    String message,
	                                    String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageDef(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement noidDef(String message,
	                                  String description,
	                                  String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageDef(message);
		stateElement.setDescriptionDef(description);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement notypeNolang(String identifier,
	                                       String message,
	                                       String description) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageNolang(message);
		stateElement.setDescriptionNolang(description);
		return stateElement;
	}

	public static WsIOElement nodescNolang(String identifier,
	                                       String message,
	                                       String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setIdentifier(identifier);
		stateElement.setMessageNolang(message);
		stateElement.setType(type);
		return stateElement;
	}

	public static WsIOElement noidNolang(String message,
	                                     String description,
	                                     String type) {
		WsIOElement stateElement = new WsIOElement();
		stateElement.setMessageNolang(message);
		stateElement.setDescriptionNolang(description);
		stateElement.setType(type);
		return stateElement;
	}

	@XmlElement
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

	public void setMessageNolang(String message) {
		this.message = WsIOText.nolang(message);
	}

	public void setDescriptionNolang(String description) {
		this.description = WsIOText.nolang(description);
	}

}
