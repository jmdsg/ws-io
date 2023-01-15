package com.fiberg.wsio.processor;

import io.vavr.collection.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

class WsIOMemberInfo {

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

	private WsIOQualifierInfo qualifierInfo;

	private List<WsIOAdapterInfo> adapterInfos;

	public static WsIOMemberInfo of(Element typeElement,
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
									WsIOQualifierInfo qualifierInfo,
									List<WsIOAdapterInfo> adapterInfos) {

		WsIOMemberInfo info = new WsIOMemberInfo();
		info.typeElement = typeElement;
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
		info.qualifierInfo = qualifierInfo;
		info.adapterInfos = adapterInfos;
		return info;

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

	public WsIOQualifierInfo getQualifierInfo() {
		return qualifierInfo;
	}

	public void setQualifierInfo(WsIOQualifierInfo qualifierInfo) {
		this.qualifierInfo = qualifierInfo;
	}

	public List<WsIOAdapterInfo> getAdapterInfos() {
		return adapterInfos;
	}

	public void setAdapterInfos(List<WsIOAdapterInfo> adapterInfos) {
		this.adapterInfos = adapterInfos;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WsIOMemberInfo wsIOMemberInfo = (WsIOMemberInfo) o;

		if (typeElement != null ? !typeElement.equals(wsIOMemberInfo.typeElement) : wsIOMemberInfo.typeElement != null) return false;
		if (typeMirror != null ? !typeMirror.equals(wsIOMemberInfo.typeMirror) : wsIOMemberInfo.typeMirror != null)
			return false;
		if (paramPresent != null ? !paramPresent.equals(wsIOMemberInfo.paramPresent) : wsIOMemberInfo.paramPresent != null)
			return false;
		if (paramName != null ? !paramName.equals(wsIOMemberInfo.paramName) : wsIOMemberInfo.paramName != null)
			return false;
		if (paramPartName != null ? !paramPartName.equals(wsIOMemberInfo.paramPartName) : wsIOMemberInfo.paramPartName != null)
			return false;
		if (paramTargetNamespace != null ? !paramTargetNamespace.equals(wsIOMemberInfo.paramTargetNamespace) : wsIOMemberInfo.paramTargetNamespace != null)
			return false;
		if (paramMode != null ? !paramMode.equals(wsIOMemberInfo.paramMode) : wsIOMemberInfo.paramMode != null)
			return false;
		if (paramHeader != null ? !paramHeader.equals(wsIOMemberInfo.paramHeader) : wsIOMemberInfo.paramHeader != null)
			return false;
		if (elementPresent != null ? !elementPresent.equals(wsIOMemberInfo.elementPresent) : wsIOMemberInfo.elementPresent != null)
			return false;
		if (elementName != null ? !elementName.equals(wsIOMemberInfo.elementName) : wsIOMemberInfo.elementName != null)
			return false;
		if (elementNamespace != null ? !elementNamespace.equals(wsIOMemberInfo.elementNamespace) : wsIOMemberInfo.elementNamespace != null)
			return false;
		if (elementRequired != null ? !elementRequired.equals(wsIOMemberInfo.elementRequired) : wsIOMemberInfo.elementRequired != null)
			return false;
		if (elementNillable != null ? !elementNillable.equals(wsIOMemberInfo.elementNillable) : wsIOMemberInfo.elementNillable != null)
			return false;
		if (elementDefaultValue != null ? !elementDefaultValue.equals(wsIOMemberInfo.elementDefaultValue) : wsIOMemberInfo.elementDefaultValue != null)
			return false;
		if (elementType != null ? !elementType.equals(wsIOMemberInfo.elementType) : wsIOMemberInfo.elementType != null)
			return false;
		if (elementWrapperPresent != null ? !elementWrapperPresent.equals(wsIOMemberInfo.elementWrapperPresent) : wsIOMemberInfo.elementWrapperPresent != null)
			return false;
		if (elementWrapperName != null ? !elementWrapperName.equals(wsIOMemberInfo.elementWrapperName) : wsIOMemberInfo.elementWrapperName != null)
			return false;
		if (elementWrapperNamespace != null ? !elementWrapperNamespace.equals(wsIOMemberInfo.elementWrapperNamespace) : wsIOMemberInfo.elementWrapperNamespace != null)
			return false;
		if (elementWrapperRequired != null ? !elementWrapperRequired.equals(wsIOMemberInfo.elementWrapperRequired) : wsIOMemberInfo.elementWrapperRequired != null)
			return false;
		if (elementWrapperNillable != null ? !elementWrapperNillable.equals(wsIOMemberInfo.elementWrapperNillable) : wsIOMemberInfo.elementWrapperNillable != null)
			return false;
		if (attributePresent != null ? !attributePresent.equals(wsIOMemberInfo.attributePresent) : wsIOMemberInfo.attributePresent != null)
			return false;
		if (attributeName != null ? !attributeName.equals(wsIOMemberInfo.attributeName) : wsIOMemberInfo.attributeName != null)
			return false;
		if (attributeNamespace != null ? !attributeNamespace.equals(wsIOMemberInfo.attributeNamespace) : wsIOMemberInfo.attributeNamespace != null)
			return false;
		if (attributeRequired != null ? !attributeRequired.equals(wsIOMemberInfo.attributeRequired) : wsIOMemberInfo.attributeRequired != null)
			return false;
		if (adapterPresent != null ? !adapterPresent.equals(wsIOMemberInfo.adapterPresent) : wsIOMemberInfo.adapterPresent != null)
			return false;
		if (qualifierInfo != null ? !qualifierInfo.equals(wsIOMemberInfo.qualifierInfo) : wsIOMemberInfo.qualifierInfo != null)
			return false;
		return adapterInfos != null ? adapterInfos.equals(wsIOMemberInfo.adapterInfos) : wsIOMemberInfo.adapterInfos == null;
	}

	@Override
	public int hashCode() {
		int result = typeElement != null ? typeElement.hashCode() : 0;
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
		result = 31 * result + (qualifierInfo != null ? qualifierInfo.hashCode() : 0);
		result = 31 * result + (adapterInfos != null ? adapterInfos.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "WsIOMemberInfo{" +
				"typeElement='" + typeElement + '\'' +
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
				", qualifierInfo='" + qualifierInfo + '\'' +
				", adapterInfos='" + adapterInfos + '\'' +
				'}';
	}

}
