package com.fiberg.wsio.processor;

import com.fiberg.wsio.enumerate.WsIOType;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.util.Objects;

class WsIOMember {

	private Element typeElement;

	private TypeMirror typeMirror;

	private Boolean paramPresent;

	private String paramName;

	private String paramPartName;

	private String paramTargetNamespace;

	private String paramMode;

	private Boolean paramHeader;

	private Boolean elementPresent;

	private String elementName;

	private String elementNamespace;

	private Boolean elementRequired;

	private Boolean elementNillable;

	private String elementDefaultValue;

	private String elementType;

	private Boolean elementWrapperPresent;

	private String elementWrapperName;

	private String elementWrapperNamespace;

	private Boolean elementWrapperRequired;

	private Boolean elementWrapperNillable;

	private Boolean attributePresent;

	private String attributeName;

	private String attributeNamespace;

	private Boolean attributeRequired;

	private Boolean adapterPresent;

	private String adapterValue;

	private String adapterType;

	private Boolean transientPresent;

	private Boolean valuePresent;

	private WsIOIdentifier targetIdentifier;

	private WsIOType targetType;

	public static WsIOMember of(Element typeElement,
								TypeMirror typeMirror,
								Boolean paramPresent,
								String paramName,
								String paramPartName,
								String paramTargetNamespace,
								String paramMode,
								Boolean paramHeader,
								Boolean elementPresent,
								String elementName,
								String elementNamespace,
								Boolean elementRequired,
								Boolean elementNillable,
								String elementDefaultValue,
								String elementType,
								Boolean elementWrapperPresent,
								String elementWrapperName,
								String elementWrapperNamespace,
								Boolean elementWrapperRequired,
								Boolean elementWrapperNillable,
								Boolean attributePresent,
								String attributeName,
								String attributeNamespace,
								Boolean attributeRequired,
								Boolean adapterPresent,
								String adapterValue,
								String adapterType,
								Boolean transientPresent,
								Boolean valuePresent,
								WsIOIdentifier targetIdentifier,
								WsIOType targetType) {

		WsIOMember member = new WsIOMember();
		member.typeElement = typeElement;
		member.typeMirror = typeMirror;
		member.paramPresent = paramPresent;
		member.paramName = paramName;
		member.paramPartName = paramPartName;
		member.paramTargetNamespace = paramTargetNamespace;
		member.paramMode = paramMode;
		member.paramHeader = paramHeader;
		member.elementPresent = elementPresent;
		member.elementName = elementName;
		member.elementNamespace = elementNamespace;
		member.elementRequired = elementRequired;
		member.elementNillable = elementNillable;
		member.elementDefaultValue = elementDefaultValue;
		member.elementType = elementType;
		member.elementWrapperPresent = elementWrapperPresent;
		member.elementWrapperName = elementWrapperName;
		member.elementWrapperNamespace = elementWrapperNamespace;
		member.elementWrapperRequired = elementWrapperRequired;
		member.elementWrapperNillable = elementWrapperNillable;
		member.attributePresent = attributePresent;
		member.attributeName = attributeName;
		member.attributeNamespace = attributeNamespace;
		member.attributeRequired = attributeRequired;
		member.adapterPresent = adapterPresent;
		member.adapterValue = adapterValue;
		member.adapterType = adapterType;
		member.transientPresent = transientPresent;
		member.valuePresent = valuePresent;
		member.targetIdentifier = targetIdentifier;
		member.targetType = targetType;
		return member;

	}

	public Element getTypeElement() {
		return typeElement;
	}

	public void setTypeElement(Element typeElement) {
		this.typeElement = typeElement;
	}

	public TypeMirror getTypeMirror() {
		return typeMirror;
	}

	public void setTypeMirror(TypeMirror typeMirror) {
		this.typeMirror = typeMirror;
	}

	public Boolean getParamPresent() {
		return paramPresent;
	}

	public void setParamPresent(Boolean paramPresent) {
		this.paramPresent = paramPresent;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamPartName() {
		return paramPartName;
	}

	public void setParamPartName(String paramPartName) {
		this.paramPartName = paramPartName;
	}

	public String getParamTargetNamespace() {
		return paramTargetNamespace;
	}

	public void setParamTargetNamespace(String paramTargetNamespace) {
		this.paramTargetNamespace = paramTargetNamespace;
	}

	public String getParamMode() {
		return paramMode;
	}

	public void setParamMode(String paramMode) {
		this.paramMode = paramMode;
	}

	public Boolean getParamHeader() {
		return paramHeader;
	}

	public void setParamHeader(Boolean paramHeader) {
		this.paramHeader = paramHeader;
	}

	public Boolean getElementPresent() {
		return elementPresent;
	}

	public void setElementPresent(Boolean elementPresent) {
		this.elementPresent = elementPresent;
	}

	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public String getElementNamespace() {
		return elementNamespace;
	}

	public void setElementNamespace(String elementNamespace) {
		this.elementNamespace = elementNamespace;
	}

	public Boolean getElementRequired() {
		return elementRequired;
	}

	public void setElementRequired(Boolean elementRequired) {
		this.elementRequired = elementRequired;
	}

	public Boolean getElementNillable() {
		return elementNillable;
	}

	public void setElementNillable(Boolean elementNillable) {
		this.elementNillable = elementNillable;
	}

	public String getElementDefaultValue() {
		return elementDefaultValue;
	}

	public void setElementDefaultValue(String elementDefaultValue) {
		this.elementDefaultValue = elementDefaultValue;
	}

	public String getElementType() {
		return elementType;
	}

	public void setElementType(String elementType) {
		this.elementType = elementType;
	}

	public Boolean getElementWrapperPresent() {
		return elementWrapperPresent;
	}

	public void setElementWrapperPresent(Boolean elementWrapperPresent) {
		this.elementWrapperPresent = elementWrapperPresent;
	}

	public String getElementWrapperName() {
		return elementWrapperName;
	}

	public void setElementWrapperName(String elementWrapperName) {
		this.elementWrapperName = elementWrapperName;
	}

	public String getElementWrapperNamespace() {
		return elementWrapperNamespace;
	}

	public void setElementWrapperNamespace(String elementWrapperNamespace) {
		this.elementWrapperNamespace = elementWrapperNamespace;
	}

	public Boolean getElementWrapperRequired() {
		return elementWrapperRequired;
	}

	public void setElementWrapperRequired(Boolean elementWrapperRequired) {
		this.elementWrapperRequired = elementWrapperRequired;
	}

	public Boolean getElementWrapperNillable() {
		return elementWrapperNillable;
	}

	public void setElementWrapperNillable(Boolean elementWrapperNillable) {
		this.elementWrapperNillable = elementWrapperNillable;
	}

	public Boolean getAttributePresent() {
		return attributePresent;
	}

	public void setAttributePresent(Boolean attributePresent) {
		this.attributePresent = attributePresent;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeNamespace() {
		return attributeNamespace;
	}

	public void setAttributeNamespace(String attributeNamespace) {
		this.attributeNamespace = attributeNamespace;
	}

	public Boolean getAttributeRequired() {
		return attributeRequired;
	}

	public void setAttributeRequired(Boolean attributeRequired) {
		this.attributeRequired = attributeRequired;
	}

	public Boolean getAdapterPresent() {
		return adapterPresent;
	}

	public void setAdapterPresent(Boolean adapterPresent) {
		this.adapterPresent = adapterPresent;
	}

	public String getAdapterValue() {
		return adapterValue;
	}

	public void setAdapterValue(String adapterValue) {
		this.adapterValue = adapterValue;
	}

	public String getAdapterType() {
		return adapterType;
	}

	public void setAdapterType(String adapterType) {
		this.adapterType = adapterType;
	}

	public Boolean getTransientPresent() {
		return transientPresent;
	}

	public void setTransientPresent(Boolean transientPresent) {
		this.transientPresent = transientPresent;
	}

	public Boolean getValuePresent() {
		return valuePresent;
	}

	public void setValuePresent(Boolean valuePresent) {
		this.valuePresent = valuePresent;
	}

	public WsIOIdentifier getTargetIdentifier() {
		return targetIdentifier;
	}

	public void setTargetIdentifier(WsIOIdentifier targetIdentifier) {
		this.targetIdentifier = targetIdentifier;
	}

	public WsIOType getTargetType() {
		return targetType;
	}

	public void setTargetType(WsIOType targetType) {
		this.targetType = targetType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WsIOMember that = (WsIOMember) o;
		return Objects.equals(typeElement, that.typeElement) && Objects.equals(typeMirror, that.typeMirror) && Objects.equals(paramPresent, that.paramPresent) && Objects.equals(paramName, that.paramName) && Objects.equals(paramPartName, that.paramPartName) && Objects.equals(paramTargetNamespace, that.paramTargetNamespace) && Objects.equals(paramMode, that.paramMode) && Objects.equals(paramHeader, that.paramHeader) && Objects.equals(elementPresent, that.elementPresent) && Objects.equals(elementName, that.elementName) && Objects.equals(elementNamespace, that.elementNamespace) && Objects.equals(elementRequired, that.elementRequired) && Objects.equals(elementNillable, that.elementNillable) && Objects.equals(elementDefaultValue, that.elementDefaultValue) && Objects.equals(elementType, that.elementType) && Objects.equals(elementWrapperPresent, that.elementWrapperPresent) && Objects.equals(elementWrapperName, that.elementWrapperName) && Objects.equals(elementWrapperNamespace, that.elementWrapperNamespace) && Objects.equals(elementWrapperRequired, that.elementWrapperRequired) && Objects.equals(elementWrapperNillable, that.elementWrapperNillable) && Objects.equals(attributePresent, that.attributePresent) && Objects.equals(attributeName, that.attributeName) && Objects.equals(attributeNamespace, that.attributeNamespace) && Objects.equals(attributeRequired, that.attributeRequired) && Objects.equals(adapterPresent, that.adapterPresent) && Objects.equals(adapterValue, that.adapterValue) && Objects.equals(adapterType, that.adapterType) && Objects.equals(transientPresent, that.transientPresent) && Objects.equals(valuePresent, that.valuePresent) && Objects.equals(targetIdentifier, that.targetIdentifier) && targetType == that.targetType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(typeElement, typeMirror, paramPresent, paramName, paramPartName, paramTargetNamespace, paramMode, paramHeader, elementPresent, elementName, elementNamespace, elementRequired, elementNillable, elementDefaultValue, elementType, elementWrapperPresent, elementWrapperName, elementWrapperNamespace, elementWrapperRequired, elementWrapperNillable, attributePresent, attributeName, attributeNamespace, attributeRequired, adapterPresent, adapterValue, adapterType, transientPresent, valuePresent, targetIdentifier, targetType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "WsIOMember{" +
				"typeElement=" + typeElement +
				", typeMirror=" + typeMirror +
				", paramPresent=" + paramPresent +
				", paramName='" + paramName + '\'' +
				", paramPartName='" + paramPartName + '\'' +
				", paramTargetNamespace='" + paramTargetNamespace + '\'' +
				", paramMode='" + paramMode + '\'' +
				", paramHeader=" + paramHeader +
				", elementPresent=" + elementPresent +
				", elementName='" + elementName + '\'' +
				", elementNamespace='" + elementNamespace + '\'' +
				", elementRequired=" + elementRequired +
				", elementNillable=" + elementNillable +
				", elementDefaultValue='" + elementDefaultValue + '\'' +
				", elementType='" + elementType + '\'' +
				", elementWrapperPresent=" + elementWrapperPresent +
				", elementWrapperName='" + elementWrapperName + '\'' +
				", elementWrapperNamespace='" + elementWrapperNamespace + '\'' +
				", elementWrapperRequired=" + elementWrapperRequired +
				", elementWrapperNillable=" + elementWrapperNillable +
				", attributePresent=" + attributePresent +
				", attributeName='" + attributeName + '\'' +
				", attributeNamespace='" + attributeNamespace + '\'' +
				", attributeRequired=" + attributeRequired +
				", adapterPresent=" + adapterPresent +
				", adapterValue='" + adapterValue + '\'' +
				", adapterType='" + adapterType + '\'' +
				", transientPresent=" + transientPresent +
				", valuePresent=" + valuePresent +
				", targetIdentifier=" + targetIdentifier +
				", targetType=" + targetType +
				'}';
	}

}
