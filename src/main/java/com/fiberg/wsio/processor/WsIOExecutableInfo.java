package com.fiberg.wsio.processor;

import io.vavr.collection.List;
import io.vavr.collection.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

class WsIOExecutableInfo {

	private ExecutableElement executableElement;

	private String executableName;

	private Boolean methodPresent;

	private String methodOperationName;

	private String methodAction;

	private Boolean methodExclude;

	private Boolean resultPresent;

	private String resultName;

	private String resultPartName;

	private String resultTargetNamespace;

	private Boolean resultHeader;

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

	private TypeMirror returnType;

	private WsIOQualifierInfo returnQualifierInfo;

	private List<WsIOMemberInfo> memberInfos;

	private Set<WsIOWrapped> descriptorWrappers;

	public static WsIOExecutableInfo of(ExecutableElement executableElement,
										String executableName,
										Boolean methodPresent,
										String methodOperationName,
										String methodAction,
										Boolean methodExclude,
										Boolean resultPresent,
										String resultName,
										String resultPartName,
										String resultTargetNamespace,
										Boolean resultHeader,
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
										TypeMirror returnType,
										WsIOQualifierInfo returnQualifierInfo,
										List<WsIOMemberInfo> memberInfos,
										Set<WsIOWrapped> descriptorWrappers) {

		WsIOExecutableInfo info = new WsIOExecutableInfo();
		info.executableElement = executableElement;
		info.executableName = executableName;
		info.methodPresent = methodPresent;
		info.methodOperationName = methodOperationName;
		info.methodAction = methodAction;
		info.methodExclude = methodExclude;
		info.resultPresent = resultPresent;
		info.resultName = resultName;
		info.resultPartName = resultPartName;
		info.resultTargetNamespace = resultTargetNamespace;
		info.resultHeader = resultHeader;
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
		info.returnType = returnType;
		info.returnQualifierInfo = returnQualifierInfo;
		info.memberInfos = memberInfos;
		info.descriptorWrappers = descriptorWrappers;
		return info;

	}

	public ExecutableElement getExecutableElement() {
		return executableElement;
	}

	public void setExecutableElement(ExecutableElement executableElement) {
		this.executableElement = executableElement;
	}

	public String getExecutableName() {
		return executableName;
	}

	public void setExecutableName(String executableName) {
		this.executableName = executableName;
	}

	public Boolean getMethodPresent() {
		return methodPresent;
	}

	public void setMethodPresent(Boolean methodPresent) {
		this.methodPresent = methodPresent;
	}

	public String getMethodOperationName() {
		return methodOperationName;
	}

	public void setMethodOperationName(String methodOperationName) {
		this.methodOperationName = methodOperationName;
	}

	public String getMethodAction() {
		return methodAction;
	}

	public void setMethodAction(String methodAction) {
		this.methodAction = methodAction;
	}

	public Boolean getMethodExclude() {
		return methodExclude;
	}

	public void setMethodExclude(Boolean methodExclude) {
		this.methodExclude = methodExclude;
	}

	public Boolean getResultPresent() {
		return resultPresent;
	}

	public void setResultPresent(Boolean resultPresent) {
		this.resultPresent = resultPresent;
	}

	public String getResultName() {
		return resultName;
	}

	public void setResultName(String resultName) {
		this.resultName = resultName;
	}

	public String getResultPartName() {
		return resultPartName;
	}

	public void setResultPartName(String resultPartName) {
		this.resultPartName = resultPartName;
	}

	public String getResultTargetNamespace() {
		return resultTargetNamespace;
	}

	public void setResultTargetNamespace(String resultTargetNamespace) {
		this.resultTargetNamespace = resultTargetNamespace;
	}

	public Boolean getResultHeader() {
		return resultHeader;
	}

	public void setResultHeader(Boolean resultHeader) {
		this.resultHeader = resultHeader;
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

	public TypeMirror getReturnType() {
		return returnType;
	}

	public void setReturnType(TypeMirror returnType) {
		this.returnType = returnType;
	}

	public WsIOQualifierInfo getReturnQualifierInfo() {
		return returnQualifierInfo;
	}

	public void setReturnQualifierInfo(WsIOQualifierInfo returnQualifierInfo) {
		this.returnQualifierInfo = returnQualifierInfo;
	}

	public List<WsIOMemberInfo> getMemberInfos() {
		return memberInfos;
	}

	public void setMemberInfos(List<WsIOMemberInfo> memberInfos) {
		this.memberInfos = memberInfos;
	}

	public Set<WsIOWrapped> getDescriptorWrappers() {
		return descriptorWrappers;
	}

	public void setDescriptorWrappers(Set<WsIOWrapped> descriptorWrappers) {
		this.descriptorWrappers = descriptorWrappers;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WsIOExecutableInfo wsIOExecutableInfo = (WsIOExecutableInfo) o;

		if (executableElement != null ? !executableElement.equals(wsIOExecutableInfo.executableElement) : wsIOExecutableInfo.executableElement != null) return false;
		if (executableName != null ? !executableName.equals(wsIOExecutableInfo.executableName) : wsIOExecutableInfo.executableName != null)
			return false;
		if (methodPresent != null ? !methodPresent.equals(wsIOExecutableInfo.methodPresent) : wsIOExecutableInfo.methodPresent != null)
			return false;
		if (methodOperationName != null ? !methodOperationName.equals(wsIOExecutableInfo.methodOperationName) : wsIOExecutableInfo.methodOperationName != null)
			return false;
		if (methodAction != null ? !methodAction.equals(wsIOExecutableInfo.methodAction) : wsIOExecutableInfo.methodAction != null)
			return false;
		if (methodExclude != null ? !methodExclude.equals(wsIOExecutableInfo.methodExclude) : wsIOExecutableInfo.methodExclude != null)
			return false;
		if (resultPresent != null ? !resultPresent.equals(wsIOExecutableInfo.resultPresent) : wsIOExecutableInfo.resultPresent != null)
			return false;
		if (resultName != null ? !resultName.equals(wsIOExecutableInfo.resultName) : wsIOExecutableInfo.resultName != null)
			return false;
		if (resultPartName != null ? !resultPartName.equals(wsIOExecutableInfo.resultPartName) : wsIOExecutableInfo.resultPartName != null)
			return false;
		if (resultTargetNamespace != null ? !resultTargetNamespace.equals(wsIOExecutableInfo.resultTargetNamespace) : wsIOExecutableInfo.resultTargetNamespace != null)
			return false;
		if (resultHeader != null ? !resultHeader.equals(wsIOExecutableInfo.resultHeader) : wsIOExecutableInfo.resultHeader != null)
			return false;
		if (elementPresent != null ? !elementPresent.equals(wsIOExecutableInfo.elementPresent) : wsIOExecutableInfo.elementPresent != null)
			return false;
		if (elementName != null ? !elementName.equals(wsIOExecutableInfo.elementName) : wsIOExecutableInfo.elementName != null)
			return false;
		if (elementNamespace != null ? !elementNamespace.equals(wsIOExecutableInfo.elementNamespace) : wsIOExecutableInfo.elementNamespace != null)
			return false;
		if (elementNillable != null ? !elementNillable.equals(wsIOExecutableInfo.elementNillable) : wsIOExecutableInfo.elementNillable != null)
			return false;
		if (elementRequired != null ? !elementRequired.equals(wsIOExecutableInfo.elementRequired) : wsIOExecutableInfo.elementRequired != null)
			return false;
		if (elementDefaultValue != null ? !elementDefaultValue.equals(wsIOExecutableInfo.elementDefaultValue) : wsIOExecutableInfo.elementDefaultValue != null)
			return false;
		if (elementType != null ? !elementType.equals(wsIOExecutableInfo.elementType) : wsIOExecutableInfo.elementType != null)
			return false;
		if (elementWrapperPresent != null ? !elementWrapperPresent.equals(wsIOExecutableInfo.elementWrapperPresent) : wsIOExecutableInfo.elementWrapperPresent != null)
			return false;
		if (elementWrapperName != null ? !elementWrapperName.equals(wsIOExecutableInfo.elementWrapperName) : wsIOExecutableInfo.elementWrapperName != null)
			return false;
		if (elementWrapperNamespace != null ? !elementWrapperNamespace.equals(wsIOExecutableInfo.elementWrapperNamespace) : wsIOExecutableInfo.elementWrapperNamespace != null)
			return false;
		if (elementWrapperNillable != null ? !elementWrapperNillable.equals(wsIOExecutableInfo.elementWrapperNillable) : wsIOExecutableInfo.elementWrapperNillable != null)
			return false;
		if (elementWrapperRequired != null ? !elementWrapperRequired.equals(wsIOExecutableInfo.elementWrapperRequired) : wsIOExecutableInfo.elementWrapperRequired != null)
			return false;
		if (returnType != null ? !returnType.equals(wsIOExecutableInfo.returnType) : wsIOExecutableInfo.returnType != null)
			return false;
		if (returnQualifierInfo != null ? !returnQualifierInfo.equals(wsIOExecutableInfo.returnQualifierInfo) : wsIOExecutableInfo.returnQualifierInfo != null)
			return false;
		if (memberInfos != null ? !memberInfos.equals(wsIOExecutableInfo.memberInfos) : wsIOExecutableInfo.memberInfos != null)
			return false;
		return descriptorWrappers != null ? descriptorWrappers.equals(wsIOExecutableInfo.descriptorWrappers) : wsIOExecutableInfo.descriptorWrappers == null;
	}

	@Override
	public int hashCode() {
		int result = executableName != null ? executableName.hashCode() : 0;
		result = 31 * result + (executableElement != null ? executableElement.hashCode() : 0);
		result = 31 * result + (executableName != null ? executableName.hashCode() : 0);
		result = 31 * result + (methodPresent != null ? methodPresent.hashCode() : 0);
		result = 31 * result + (methodOperationName != null ? methodOperationName.hashCode() : 0);
		result = 31 * result + (methodAction != null ? methodAction.hashCode() : 0);
		result = 31 * result + (methodExclude != null ? methodExclude.hashCode() : 0);
		result = 31 * result + (resultPresent != null ? resultPresent.hashCode() : 0);
		result = 31 * result + (resultName != null ? resultName.hashCode() : 0);
		result = 31 * result + (resultPartName != null ? resultPartName.hashCode() : 0);
		result = 31 * result + (resultTargetNamespace != null ? resultTargetNamespace.hashCode() : 0);
		result = 31 * result + (resultHeader != null ? resultHeader.hashCode() : 0);
		result = 31 * result + (elementPresent != null ? elementPresent.hashCode() : 0);
		result = 31 * result + (elementName != null ? elementName.hashCode() : 0);
		result = 31 * result + (elementNamespace != null ? elementNamespace.hashCode() : 0);
		result = 31 * result + (elementNillable != null ? elementNillable.hashCode() : 0);
		result = 31 * result + (elementRequired != null ? elementRequired.hashCode() : 0);
		result = 31 * result + (elementDefaultValue != null ? elementDefaultValue.hashCode() : 0);
		result = 31 * result + (elementType != null ? elementType.hashCode() : 0);
		result = 31 * result + (elementWrapperPresent != null ? elementWrapperPresent.hashCode() : 0);
		result = 31 * result + (elementWrapperName != null ? elementWrapperName.hashCode() : 0);
		result = 31 * result + (elementWrapperNamespace != null ? elementWrapperNamespace.hashCode() : 0);
		result = 31 * result + (elementWrapperNillable != null ? elementWrapperNillable.hashCode() : 0);
		result = 31 * result + (elementWrapperRequired != null ? elementWrapperRequired.hashCode() : 0);
		result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
		result = 31 * result + (returnQualifierInfo != null ? returnQualifierInfo.hashCode() : 0);
		result = 31 * result + (memberInfos != null ? memberInfos.hashCode() : 0);
		result = 31 * result + (descriptorWrappers != null ? descriptorWrappers.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "WsIOExecutableInfo{" +
				"executableElement='" + executableElement + '\'' +
				", executableName='" + executableName + '\'' +
				", methodPresent='" + methodPresent + '\'' +
				", methodOperationName='" + methodOperationName + '\'' +
				", methodAction='" + methodAction + '\'' +
				", methodExclude='" + methodExclude + '\'' +
				", resultPresent='" + resultPresent + '\'' +
				", resultName='" + resultName + '\'' +
				", resultPartName='" + resultPartName + '\'' +
				", resultTargetNamespace='" + resultTargetNamespace + '\'' +
				", resultHeader='" + resultHeader + '\'' +
				", elementPresent='" + elementPresent + '\'' +
				", elementName='" + elementName + '\'' +
				", elementNamespace='" + elementNamespace + '\'' +
				", elementNillable='" + elementNillable + '\'' +
				", elementRequired='" + elementRequired + '\'' +
				", elementDefaultValue='" + elementDefaultValue + '\'' +
				", elementType='" + elementType + '\'' +
				", elementWrapperPresent='" + elementWrapperPresent + '\'' +
				", elementWrapperName='" + elementWrapperName + '\'' +
				", elementWrapperNamespace='" + elementWrapperNamespace + '\'' +
				", elementWrapperNillable='" + elementWrapperNillable + '\'' +
				", elementWrapperRequired='" + elementWrapperRequired + '\'' +
				", returnType='" + returnType + '\'' +
				", returnQualifierInfo='" + returnQualifierInfo + '\'' +
				", memberInfos='" + memberInfos + '\'' +
				", descriptorWrappers=" + descriptorWrappers +
				'}';
	}

}
