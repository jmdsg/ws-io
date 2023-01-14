package com.fiberg.wsio.processor;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

class WsIOParameterInfo {

	private VariableElement variableElement;

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

	private WsIOQualifierInfo qualifierInfo;

	public static WsIOParameterInfo of(VariableElement variableElement,
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
									   WsIOQualifierInfo qualifierInfo) {

		WsIOParameterInfo info = new WsIOParameterInfo();
		info.variableElement = variableElement;
		info.typeMirror = typeMirror;
		info.paramPresent = paramPresent;
		info.paramName = paramName;
		info.paramPartName = paramPartName;
		info.paramTargetNamespace = paramTargetNamespace;
		info.paramMode = paramMode;
		info.paramHeader = paramHeader;
		info.elementPresent = elementPresent;
		info.elementName = elementName;
		info.elementNamespace = elementNamespace;
		info.elementRequired = elementRequired;
		info.elementNillable = elementNillable;
		info.elementDefaultValue = elementDefaultValue;
		info.elementType = elementType;
		info.elementWrapperPresent = elementWrapperPresent;
		info.elementWrapperName = elementWrapperName;
		info.elementWrapperNamespace = elementWrapperNamespace;
		info.elementWrapperRequired = elementWrapperRequired;
		info.elementWrapperNillable = elementWrapperNillable;
		info.attributePresent = attributePresent;
		info.attributeName = attributeName;
		info.attributeNamespace = attributeNamespace;
		info.attributeRequired = attributeRequired;
		info.adapterPresent = adapterPresent;
		info.adapterValue = adapterValue;
		info.adapterType = adapterType;
		info.qualifierInfo = qualifierInfo;
		return info;

	}

	public VariableElement getVariableElement() {
		return variableElement;
	}

	public void setVariableElement(VariableElement variableElement) {
		this.variableElement = variableElement;
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

	public WsIOQualifierInfo getQualifierInfo() {
		return qualifierInfo;
	}

	public void setQualifierInfo(WsIOQualifierInfo qualifierInfo) {
		this.qualifierInfo = qualifierInfo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WsIOParameterInfo wsIOParameterInfo = (WsIOParameterInfo) o;

		if (variableElement != null ? !variableElement.equals(wsIOParameterInfo.variableElement) : wsIOParameterInfo.variableElement != null) return false;
		if (typeMirror != null ? !typeMirror.equals(wsIOParameterInfo.typeMirror) : wsIOParameterInfo.typeMirror != null)
			return false;
		if (paramPresent != null ? !paramPresent.equals(wsIOParameterInfo.paramPresent) : wsIOParameterInfo.paramPresent != null)
			return false;
		if (paramName != null ? !paramName.equals(wsIOParameterInfo.paramName) : wsIOParameterInfo.paramName != null)
			return false;
		if (paramPartName != null ? !paramPartName.equals(wsIOParameterInfo.paramPartName) : wsIOParameterInfo.paramPartName != null)
			return false;
		if (paramTargetNamespace != null ? !paramTargetNamespace.equals(wsIOParameterInfo.paramTargetNamespace) : wsIOParameterInfo.paramTargetNamespace != null)
			return false;
		if (paramMode != null ? !paramMode.equals(wsIOParameterInfo.paramMode) : wsIOParameterInfo.paramMode != null)
			return false;
		if (paramHeader != null ? !paramHeader.equals(wsIOParameterInfo.paramHeader) : wsIOParameterInfo.paramHeader != null)
			return false;
		if (elementPresent != null ? !elementPresent.equals(wsIOParameterInfo.elementPresent) : wsIOParameterInfo.elementPresent != null)
			return false;
		if (elementName != null ? !elementName.equals(wsIOParameterInfo.elementName) : wsIOParameterInfo.elementName != null)
			return false;
		if (elementNamespace != null ? !elementNamespace.equals(wsIOParameterInfo.elementNamespace) : wsIOParameterInfo.elementNamespace != null)
			return false;
		if (elementRequired != null ? !elementRequired.equals(wsIOParameterInfo.elementRequired) : wsIOParameterInfo.elementRequired != null)
			return false;
		if (elementNillable != null ? !elementNillable.equals(wsIOParameterInfo.elementNillable) : wsIOParameterInfo.elementNillable != null)
			return false;
		if (elementDefaultValue != null ? !elementDefaultValue.equals(wsIOParameterInfo.elementDefaultValue) : wsIOParameterInfo.elementDefaultValue != null)
			return false;
		if (elementType != null ? !elementType.equals(wsIOParameterInfo.elementType) : wsIOParameterInfo.elementType != null)
			return false;
		if (elementWrapperPresent != null ? !elementWrapperPresent.equals(wsIOParameterInfo.elementWrapperPresent) : wsIOParameterInfo.elementWrapperPresent != null)
			return false;
		if (elementWrapperName != null ? !elementWrapperName.equals(wsIOParameterInfo.elementWrapperName) : wsIOParameterInfo.elementWrapperName != null)
			return false;
		if (elementWrapperNamespace != null ? !elementWrapperNamespace.equals(wsIOParameterInfo.elementWrapperNamespace) : wsIOParameterInfo.elementWrapperNamespace != null)
			return false;
		if (elementWrapperRequired != null ? !elementWrapperRequired.equals(wsIOParameterInfo.elementWrapperRequired) : wsIOParameterInfo.elementWrapperRequired != null)
			return false;
		if (elementWrapperNillable != null ? !elementWrapperNillable.equals(wsIOParameterInfo.elementWrapperNillable) : wsIOParameterInfo.elementWrapperNillable != null)
			return false;
		if (attributePresent != null ? !attributePresent.equals(wsIOParameterInfo.attributePresent) : wsIOParameterInfo.attributePresent != null)
			return false;
		if (attributeName != null ? !attributeName.equals(wsIOParameterInfo.attributeName) : wsIOParameterInfo.attributeName != null)
			return false;
		if (attributeNamespace != null ? !attributeNamespace.equals(wsIOParameterInfo.attributeNamespace) : wsIOParameterInfo.attributeNamespace != null)
			return false;
		if (attributeRequired != null ? !attributeRequired.equals(wsIOParameterInfo.attributeRequired) : wsIOParameterInfo.attributeRequired != null)
			return false;
		if (adapterPresent != null ? !adapterPresent.equals(wsIOParameterInfo.adapterPresent) : wsIOParameterInfo.adapterPresent != null)
			return false;
		if (adapterValue != null ? !adapterValue.equals(wsIOParameterInfo.adapterValue) : wsIOParameterInfo.adapterValue != null)
			return false;
		if (adapterType != null ? !adapterType.equals(wsIOParameterInfo.adapterType) : wsIOParameterInfo.adapterType != null)
			return false;
		return qualifierInfo != null ? qualifierInfo.equals(wsIOParameterInfo.qualifierInfo) : wsIOParameterInfo.qualifierInfo == null;
	}

	@Override
	public int hashCode() {
		int result = variableElement != null ? variableElement.hashCode() : 0;
		result = 31 * result + (typeMirror != null ? typeMirror.hashCode() : 0);
		result = 31 * result + (paramPresent != null ? paramPresent.hashCode() : 0);
		result = 31 * result + (paramName != null ? paramName.hashCode() : 0);
		result = 31 * result + (paramPartName != null ? paramPartName.hashCode() : 0);
		result = 31 * result + (paramTargetNamespace != null ? paramTargetNamespace.hashCode() : 0);
		result = 31 * result + (paramMode != null ? paramMode.hashCode() : 0);
		result = 31 * result + (paramHeader != null ? paramHeader.hashCode() : 0);
		result = 31 * result + (elementPresent != null ? elementPresent.hashCode() : 0);
		result = 31 * result + (elementName != null ? elementName.hashCode() : 0);
		result = 31 * result + (elementNamespace != null ? elementNamespace.hashCode() : 0);
		result = 31 * result + (elementRequired != null ? elementRequired.hashCode() : 0);
		result = 31 * result + (elementNillable != null ? elementNillable.hashCode() : 0);
		result = 31 * result + (elementDefaultValue != null ? elementDefaultValue.hashCode() : 0);
		result = 31 * result + (elementType != null ? elementType.hashCode() : 0);
		result = 31 * result + (elementWrapperPresent != null ? elementWrapperPresent.hashCode() : 0);
		result = 31 * result + (elementWrapperName != null ? elementWrapperName.hashCode() : 0);
		result = 31 * result + (elementWrapperNamespace != null ? elementWrapperNamespace.hashCode() : 0);
		result = 31 * result + (elementWrapperRequired != null ? elementWrapperRequired.hashCode() : 0);
		result = 31 * result + (elementWrapperNillable != null ? elementWrapperNillable.hashCode() : 0);
		result = 31 * result + (attributePresent != null ? attributePresent.hashCode() : 0);
		result = 31 * result + (attributeName != null ? attributeName.hashCode() : 0);
		result = 31 * result + (attributeNamespace != null ? attributeNamespace.hashCode() : 0);
		result = 31 * result + (attributeRequired != null ? attributeRequired.hashCode() : 0);
		result = 31 * result + (adapterPresent != null ? adapterPresent.hashCode() : 0);
		result = 31 * result + (adapterValue != null ? adapterValue.hashCode() : 0);
		result = 31 * result + (adapterType != null ? adapterType.hashCode() : 0);
		result = 31 * result + (qualifierInfo != null ? qualifierInfo.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "WsIOParameterInfo{" +
				"variableElement='" + variableElement + '\'' +
				", typeMirror='" + typeMirror + '\'' +
				", paramPresent='" + paramPresent + '\'' +
				", paramName='" + paramName + '\'' +
				", paramPartName='" + paramPartName + '\'' +
				", paramTargetNamespace='" + paramTargetNamespace + '\'' +
				", paramMode='" + paramMode + '\'' +
				", paramHeader='" + paramHeader + '\'' +
				", elementPresent='" + elementPresent + '\'' +
				", elementName='" + elementName + '\'' +
				", elementNamespace='" + elementNamespace + '\'' +
				", elementRequired='" + elementRequired + '\'' +
				", elementNillable='" + elementNillable + '\'' +
				", elementDefaultValue='" + elementDefaultValue + '\'' +
				", elementType='" + elementType + '\'' +
				", elementWrapperPresent='" + elementWrapperPresent + '\'' +
				", elementWrapperName='" + elementWrapperName + '\'' +
				", elementWrapperNamespace='" + elementWrapperNamespace + '\'' +
				", elementWrapperRequired='" + elementWrapperRequired + '\'' +
				", elementWrapperNillable='" + elementWrapperNillable + '\'' +
				", attributePresent='" + attributePresent + '\'' +
				", attributeName='" + attributeName + '\'' +
				", attributeNamespace='" + attributeNamespace + '\'' +
				", attributeRequired='" + attributeRequired + '\'' +
				", adapterPresent='" + adapterPresent + '\'' +
				", adapterValue='" + adapterValue + '\'' +
				", adapterType='" + adapterType + '\'' +
				", qualifierInfo='" + qualifierInfo + '\'' +
				'}';
	}

}
