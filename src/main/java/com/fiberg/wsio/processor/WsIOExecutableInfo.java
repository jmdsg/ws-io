package com.fiberg.wsio.processor;

import io.vavr.collection.List;
import io.vavr.collection.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

class WsIOExecutableInfo {

	private ExecutableElement executableElement;

	private String methodName;

	private String operationName;

	private List<WsIOParameterInfo> parameterInfos;

	private String returnName;

	private String returnNameSpace;

	private TypeMirror returnType;

	private WsIOQualifierInfo returnQualifierInfo;

	private String wrapperName;

	private String wrapperNameSpace;

	private Set<WsIOWrapped> executableWrappers;

	public static WsIOExecutableInfo of(ExecutableElement executableElement,
										String methodName,
										String operationName,
										List<WsIOParameterInfo> parameterInfos,
										String returnName,
										String returnNameSpace,
										TypeMirror returnType,
										WsIOQualifierInfo returnQualifierInfo,
										String wrapperName,
										String wrapperNameSpace,
										Set<WsIOWrapped> executableWrappers) {

		WsIOExecutableInfo info = new WsIOExecutableInfo();
		info.executableElement = executableElement;
		info.methodName = methodName;
		info.operationName = operationName;
		info.parameterInfos = parameterInfos;
		info.returnName = returnName;
		info.returnNameSpace = returnNameSpace;
		info.returnType = returnType;
		info.returnQualifierInfo = returnQualifierInfo;
		info.wrapperName = wrapperName;
		info.wrapperNameSpace = wrapperNameSpace;
		info.executableWrappers = executableWrappers;
		return info;

	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public ExecutableElement getExecutableElement() {
		return executableElement;
	}

	public void setExecutableElement(ExecutableElement executableElement) {
		this.executableElement = executableElement;
	}

	public List<WsIOParameterInfo> getParameterInfos() {
		return parameterInfos;
	}

	public void setParameterInfos(List<WsIOParameterInfo> parameterInfos) {
		this.parameterInfos = parameterInfos;
	}

	public String getReturnName() {
		return returnName;
	}

	public void setReturnName(String returnName) {
		this.returnName = returnName;
	}

	public String getReturnNameSpace() {
		return returnNameSpace;
	}

	public void setReturnNameSpace(String returnNameSpace) {
		this.returnNameSpace = returnNameSpace;
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

	public String getWrapperName() {
		return wrapperName;
	}

	public void setWrapperName(String wrapperName) {
		this.wrapperName = wrapperName;
	}

	public String getWrapperNameSpace() {
		return wrapperNameSpace;
	}

	public void setWrapperNameSpace(String wrapperNameSpace) {
		this.wrapperNameSpace = wrapperNameSpace;
	}

	public Set<WsIOWrapped> getExecutableWrappers() {
		return executableWrappers;
	}

	public void setExecutableWrappers(Set<WsIOWrapped> executableWrappers) {
		this.executableWrappers = executableWrappers;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WsIOExecutableInfo wsIOExecutableInfo = (WsIOExecutableInfo) o;

		if (methodName != null ? !methodName.equals(wsIOExecutableInfo.methodName) : wsIOExecutableInfo.methodName != null) return false;
		if (operationName != null ? !operationName.equals(wsIOExecutableInfo.operationName) : wsIOExecutableInfo.operationName != null)
			return false;
		if (executableElement != null ? !executableElement.equals(wsIOExecutableInfo.executableElement) : wsIOExecutableInfo.executableElement != null)
			return false;
		if (parameterInfos != null ? !parameterInfos.equals(wsIOExecutableInfo.parameterInfos) : wsIOExecutableInfo.parameterInfos != null)
			return false;
		if (returnName != null ? !returnName.equals(wsIOExecutableInfo.returnName) : wsIOExecutableInfo.returnName != null) return false;
		if (returnNameSpace != null ? !returnNameSpace.equals(wsIOExecutableInfo.returnNameSpace) : wsIOExecutableInfo.returnNameSpace != null)
			return false;
		if (returnType != null ? !returnType.equals(wsIOExecutableInfo.returnType) : wsIOExecutableInfo.returnType != null) return false;
		if (returnQualifierInfo != null ? !returnQualifierInfo.equals(wsIOExecutableInfo.returnQualifierInfo) : wsIOExecutableInfo.returnQualifierInfo != null)
			return false;
		if (wrapperName != null ? !wrapperName.equals(wsIOExecutableInfo.wrapperName) : wsIOExecutableInfo.wrapperName != null)
			return false;
		if (wrapperNameSpace != null ? !wrapperNameSpace.equals(wsIOExecutableInfo.wrapperNameSpace) : wsIOExecutableInfo.wrapperNameSpace != null)
			return false;
		return executableWrappers != null ? executableWrappers.equals(wsIOExecutableInfo.executableWrappers) : wsIOExecutableInfo.executableWrappers == null;
	}

	@Override
	public int hashCode() {
		int result = methodName != null ? methodName.hashCode() : 0;
		result = 31 * result + (operationName != null ? operationName.hashCode() : 0);
		result = 31 * result + (executableElement != null ? executableElement.hashCode() : 0);
		result = 31 * result + (parameterInfos != null ? parameterInfos.hashCode() : 0);
		result = 31 * result + (returnName != null ? returnName.hashCode() : 0);
		result = 31 * result + (returnNameSpace != null ? returnNameSpace.hashCode() : 0);
		result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
		result = 31 * result + (returnQualifierInfo != null ? returnQualifierInfo.hashCode() : 0);
		result = 31 * result + (wrapperName != null ? wrapperName.hashCode() : 0);
		result = 31 * result + (wrapperNameSpace != null ? wrapperNameSpace.hashCode() : 0);
		result = 31 * result + (executableWrappers != null ? executableWrappers.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "WsIOExecutableInfo{" +
				"methodName='" + methodName + '\'' +
				", operationName='" + operationName + '\'' +
				", executableElement=" + executableElement +
				", parameterInfos=" + parameterInfos +
				", returnName='" + returnName + '\'' +
				", returnNameSpace='" + returnNameSpace + '\'' +
				", returnType=" + returnType +
				", returnQualifierInfo=" + returnQualifierInfo +
				", wrapperName='" + wrapperName + '\'' +
				", wrapperNameSpace='" + wrapperNameSpace + '\'' +
				", executableWrappers=" + executableWrappers +
				'}';
	}

}
