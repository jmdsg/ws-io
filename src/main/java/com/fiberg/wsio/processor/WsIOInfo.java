package com.fiberg.wsio.processor;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.stream.Collectors;

class WsIOInfo {

	private String methodName;

	private String operationName;

	private ExecutableElement executableElement;

	private List<String> parameterNames;

	private List<String> parameterNameSpaces;

	private List<TypeMirror> parameterTypes;

	private List<Boolean> parameterRequired;

	private List<Boolean> parameterNillable;

	private List<Tuple2<String, String>> parameterQualifiers;

	private List<Boolean> parameterAttributes;

	private List<String> parameterWrappers;

	private List<Boolean> parameterWrapperRequired;

	private List<Boolean> parameterWrapperNillable;

	private List<String> parameterAdapters;

	private String returnName;

	private String returnNameSpace;

	private TypeMirror returnType;

	private Tuple2<String, String> returnQualifier;

	private String wrapperName;

	private String wrapperNameSpace;

	private Set<WsIOWrapped> wrappers;

	public static WsIOInfo of(String methodName,
	                          String operationName,
	                          ExecutableElement executableElement,
	                          List<String> parameterNames,
	                          List<String> parameterNameSpaces,
	                          List<TypeMirror> parameterTypes,
	                          List<Boolean> parameterRequired,
	                          List<Boolean> parameterNillable,
	                          List<Tuple2<String, String>> parameterQualifiers,
							  List<Boolean> parameterAttributes,
							  List<String> parameterWrappers,
							  List<Boolean> parameterWrapperRequired,
							  List<Boolean> parameterWrapperNillable,
							  List<String> parameterAdapters,
	                          String returnName,
	                          String returnNameSpace,
	                          TypeMirror returnType,
	                          Tuple2<String, String> returnQualifier,
	                          String wrapperName,
	                          String wrapperNameSpace,
	                          Set<WsIOWrapped> wrappers) {

		WsIOInfo info = new WsIOInfo();
		info.methodName = methodName;
		info.operationName = operationName;
		info.executableElement = executableElement;
		info.parameterNames = parameterNames;
		info.parameterNameSpaces = parameterNameSpaces;
		info.parameterTypes = parameterTypes;
		info.parameterRequired = parameterRequired;
		info.parameterNillable = parameterNillable;
		info.parameterQualifiers = parameterQualifiers;
		info.parameterAttributes = parameterAttributes;
		info.parameterWrappers = parameterWrappers;
		info.parameterWrapperRequired = parameterWrapperRequired;
		info.parameterWrapperNillable = parameterWrapperNillable;
		info.parameterAdapters = parameterAdapters;
		info.returnName = returnName;
		info.returnNameSpace = returnNameSpace;
		info.returnType = returnType;
		info.returnQualifier = returnQualifier;
		info.wrapperName = wrapperName;
		info.wrapperNameSpace = wrapperNameSpace;
		info.wrappers = wrappers;
		return info;

	}

	public boolean methodMatch(ExecutableElement method) {
		if (method != null) {
			return method.getSimpleName().toString().equals(methodName)
					&& method.getParameters().stream()
							.map(VariableElement::asType)
							.collect(Collectors.toList())
							.equals(parameterTypes)
					&& method.getReturnType().equals(returnType);
		}
		return false;
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

	public List<String> getParameterNames() {
		return parameterNames;
	}

	public void setParameterNames(List<String> parameterNames) {
		this.parameterNames = parameterNames;
	}

	public List<String> getParameterNameSpaces() {
		return parameterNameSpaces;
	}

	public void setParameterNameSpaces(List<String> parameterNameSpaces) {
		this.parameterNameSpaces = parameterNameSpaces;
	}

	public List<TypeMirror> getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(List<TypeMirror> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public List<Boolean> getParameterRequired() {
		return parameterRequired;
	}

	public void setParameterRequired(List<Boolean> parameterRequired) {
		this.parameterRequired = parameterRequired;
	}

	public List<Boolean> getParameterNillable() {
		return parameterNillable;
	}

	public void setParameterNillable(List<Boolean> parameterNillable) {
		this.parameterNillable = parameterNillable;
	}

	public List<Tuple2<String, String>> getParameterQualifiers() {
		return parameterQualifiers;
	}

	public void setParameterQualifiers(List<Tuple2<String, String>> parameterQualifiers) {
		this.parameterQualifiers = parameterQualifiers;
	}

	public List<Boolean> getParameterAttributes() {
		return parameterAttributes;
	}

	public void setParameterAttributes(List<Boolean> parameterAttributes) {
		this.parameterAttributes = parameterAttributes;
	}

	public List<String> getParameterWrappers() {
		return parameterWrappers;
	}

	public void setParameterWrappers(List<String> parameterWrappers) {
		this.parameterWrappers = parameterWrappers;
	}

	public List<Boolean> getParameterWrapperRequired() {
		return parameterWrapperRequired;
	}

	public void setParameterWrapperRequired(List<Boolean> parameterWrapperRequired) {
		this.parameterWrapperRequired = parameterWrapperRequired;
	}

	public List<Boolean> getParameterWrapperNillable() {
		return parameterWrapperNillable;
	}

	public void setParameterWrapperNillable(List<Boolean> parameterWrapperNillable) {
		this.parameterWrapperNillable = parameterWrapperNillable;
	}

	public List<String> getParameterAdapters() {
		return parameterAdapters;
	}

	public void setParameterAdapters(List<String> parameterAdapters) {
		this.parameterAdapters = parameterAdapters;
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

	public Tuple2<String, String> getReturnQualifier() {
		return returnQualifier;
	}

	public void setReturnQualifier(Tuple2<String, String> returnQualifier) {
		this.returnQualifier = returnQualifier;
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

	public Set<WsIOWrapped> getWrappers() {
		return wrappers;
	}

	public void setWrappers(Set<WsIOWrapped> wrappers) {
		this.wrappers = wrappers;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WsIOInfo wsIOInfo = (WsIOInfo) o;

		if (methodName != null ? !methodName.equals(wsIOInfo.methodName) : wsIOInfo.methodName != null) return false;
		if (operationName != null ? !operationName.equals(wsIOInfo.operationName) : wsIOInfo.operationName != null)
			return false;
		if (executableElement != null ? !executableElement.equals(wsIOInfo.executableElement) : wsIOInfo.executableElement != null)
			return false;
		if (parameterNames != null ? !parameterNames.equals(wsIOInfo.parameterNames) : wsIOInfo.parameterNames != null)
			return false;
		if (parameterNameSpaces != null ? !parameterNameSpaces.equals(wsIOInfo.parameterNameSpaces) : wsIOInfo.parameterNameSpaces != null)
			return false;
		if (parameterTypes != null ? !parameterTypes.equals(wsIOInfo.parameterTypes) : wsIOInfo.parameterTypes != null)
			return false;
		if (parameterRequired != null ? !parameterRequired.equals(wsIOInfo.parameterRequired) : wsIOInfo.parameterRequired != null)
			return false;
		if (parameterNillable != null ? !parameterNillable.equals(wsIOInfo.parameterNillable) : wsIOInfo.parameterNillable != null)
			return false;
		if (parameterQualifiers != null ? !parameterQualifiers.equals(wsIOInfo.parameterQualifiers) : wsIOInfo.parameterQualifiers != null)
			return false;
		if (parameterAttributes != null ? !parameterAttributes.equals(wsIOInfo.parameterAttributes) : wsIOInfo.parameterAttributes != null)
			return false;
		if (parameterWrappers != null ? !parameterWrappers.equals(wsIOInfo.parameterWrappers) : wsIOInfo.parameterWrappers != null)
			return false;
		if (parameterWrapperRequired != null ? !parameterWrapperRequired.equals(wsIOInfo.parameterWrapperRequired) : wsIOInfo.parameterWrapperRequired != null)
			return false;
		if (parameterWrapperNillable != null ? !parameterWrapperNillable.equals(wsIOInfo.parameterWrapperNillable) : wsIOInfo.parameterWrapperNillable != null)
			return false;
		if (parameterAdapters != null ? !parameterAdapters.equals(wsIOInfo.parameterAdapters) : wsIOInfo.parameterAdapters != null)
			return false;
		if (returnName != null ? !returnName.equals(wsIOInfo.returnName) : wsIOInfo.returnName != null) return false;
		if (returnNameSpace != null ? !returnNameSpace.equals(wsIOInfo.returnNameSpace) : wsIOInfo.returnNameSpace != null)
			return false;
		if (returnType != null ? !returnType.equals(wsIOInfo.returnType) : wsIOInfo.returnType != null) return false;
		if (returnQualifier != null ? !returnQualifier.equals(wsIOInfo.returnQualifier) : wsIOInfo.returnQualifier != null)
			return false;
		if (wrapperName != null ? !wrapperName.equals(wsIOInfo.wrapperName) : wsIOInfo.wrapperName != null)
			return false;
		if (wrapperNameSpace != null ? !wrapperNameSpace.equals(wsIOInfo.wrapperNameSpace) : wsIOInfo.wrapperNameSpace != null)
			return false;
		return wrappers != null ? wrappers.equals(wsIOInfo.wrappers) : wsIOInfo.wrappers == null;
	}

	@Override
	public int hashCode() {
		int result = methodName != null ? methodName.hashCode() : 0;
		result = 31 * result + (operationName != null ? operationName.hashCode() : 0);
		result = 31 * result + (executableElement != null ? executableElement.hashCode() : 0);
		result = 31 * result + (parameterNames != null ? parameterNames.hashCode() : 0);
		result = 31 * result + (parameterNameSpaces != null ? parameterNameSpaces.hashCode() : 0);
		result = 31 * result + (parameterTypes != null ? parameterTypes.hashCode() : 0);
		result = 31 * result + (parameterRequired != null ? parameterRequired.hashCode() : 0);
		result = 31 * result + (parameterNillable != null ? parameterNillable.hashCode() : 0);
		result = 31 * result + (parameterQualifiers != null ? parameterQualifiers.hashCode() : 0);
		result = 31 * result + (parameterAttributes != null ? parameterAttributes.hashCode() : 0);
		result = 31 * result + (parameterWrappers != null ? parameterWrappers.hashCode() : 0);
		result = 31 * result + (parameterWrapperRequired != null ? parameterWrapperRequired.hashCode() : 0);
		result = 31 * result + (parameterWrapperNillable != null ? parameterWrapperNillable.hashCode() : 0);
		result = 31 * result + (parameterAdapters != null ? parameterAdapters.hashCode() : 0);
		result = 31 * result + (returnName != null ? returnName.hashCode() : 0);
		result = 31 * result + (returnNameSpace != null ? returnNameSpace.hashCode() : 0);
		result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
		result = 31 * result + (returnQualifier != null ? returnQualifier.hashCode() : 0);
		result = 31 * result + (wrapperName != null ? wrapperName.hashCode() : 0);
		result = 31 * result + (wrapperNameSpace != null ? wrapperNameSpace.hashCode() : 0);
		result = 31 * result + (wrappers != null ? wrappers.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "WsIOInfo{" +
				"methodName='" + methodName + '\'' +
				", operationName='" + operationName + '\'' +
				", executableElement=" + executableElement +
				", parameterNames=" + parameterNames +
				", parameterNameSpaces=" + parameterNameSpaces +
				", parameterTypes=" + parameterTypes +
				", parameterRequired=" + parameterRequired +
				", parameterNillable=" + parameterNillable +
				", parameterQualifiers=" + parameterQualifiers +
				", parameterAttributes=" + parameterAttributes +
				", parameterWrappers=" + parameterWrappers +
				", parameterWrapperRequired=" + parameterWrapperRequired +
				", parameterWrapperNillable=" + parameterWrapperNillable +
				", parameterAdapters=" + parameterAdapters +
				", returnName='" + returnName + '\'' +
				", returnNameSpace='" + returnNameSpace + '\'' +
				", returnType=" + returnType +
				", returnQualifier=" + returnQualifier +
				", wrapperName='" + wrapperName + '\'' +
				", wrapperNameSpace='" + wrapperNameSpace + '\'' +
				", wrappers=" + wrappers +
				'}';
	}

}
