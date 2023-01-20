package com.fiberg.wsio.processor;

import com.fiberg.wsio.enumerate.WsIOType;
import io.vavr.collection.List;
import io.vavr.collection.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Objects;

class WsIOExecutable {

	private TypeElement sourceElement;

	private ExecutableElement executableElement;

	private TypeMirror executableMirror;

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

	private WsIOIdentifier returnIdentifier;

	private List<WsIOMember> memberDescriptors;

	private Set<WsIOWrapped> descriptorWrappers;

	private WsIOIdentifier targetIdentifier;

	private WsIOType targetType;

	private WsIODescriptor operationDescriptor;

	public static WsIOExecutable of(TypeElement sourceElement,
									ExecutableElement executableElement,
									TypeMirror executableMirror,
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
									WsIOIdentifier returnIdentifier,
									List<WsIOMember> memberDescriptors,
									Set<WsIOWrapped> descriptorWrappers,
									WsIOIdentifier targetIdentifier,
									WsIOType targetType,
									WsIODescriptor operationDescriptor) {

		WsIOExecutable executable = new WsIOExecutable();
		executable.sourceElement = sourceElement;
		executable.executableElement = executableElement;
		executable.executableMirror = executableMirror;
		executable.executableName = executableName;
		executable.methodPresent = methodPresent;
		executable.methodOperationName = methodOperationName;
		executable.methodAction = methodAction;
		executable.methodExclude = methodExclude;
		executable.resultPresent = resultPresent;
		executable.resultName = resultName;
		executable.resultPartName = resultPartName;
		executable.resultTargetNamespace = resultTargetNamespace;
		executable.resultHeader = resultHeader;
		executable.elementPresent = elementPresent;
		executable.elementName = elementName;
		executable.elementNamespace = elementNamespace;
		executable.elementRequired = elementRequired;
		executable.elementNillable = elementNillable;
		executable.elementDefaultValue = elementDefaultValue;
		executable.elementType = elementType;
		executable.elementWrapperPresent = elementWrapperPresent;
		executable.elementWrapperName = elementWrapperName;
		executable.elementWrapperNamespace = elementWrapperNamespace;
		executable.elementWrapperRequired = elementWrapperRequired;
		executable.elementWrapperNillable = elementWrapperNillable;
		executable.returnType = returnType;
		executable.returnIdentifier = returnIdentifier;
		executable.memberDescriptors = memberDescriptors;
		executable.descriptorWrappers = descriptorWrappers;
		executable.targetIdentifier = targetIdentifier;
		executable.targetType = targetType;
		executable.operationDescriptor = operationDescriptor;
		return executable;

	}

	public TypeElement getSourceElement() {
		return sourceElement;
	}

	public void setSourceElement(TypeElement sourceElement) {
		this.sourceElement = sourceElement;
	}

	public ExecutableElement getExecutableElement() {
		return executableElement;
	}

	public void setExecutableElement(ExecutableElement executableElement) {
		this.executableElement = executableElement;
	}

	public TypeMirror getExecutableMirror() {
		return executableMirror;
	}

	public void setExecutableMirror(TypeMirror executableMirror) {
		this.executableMirror = executableMirror;
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

	public WsIOIdentifier getReturnIdentifier() {
		return returnIdentifier;
	}

	public void setReturnIdentifier(WsIOIdentifier returnIdentifier) {
		this.returnIdentifier = returnIdentifier;
	}

	public List<WsIOMember> getMemberDescriptors() {
		return memberDescriptors;
	}

	public void setMemberDescriptors(List<WsIOMember> memberDescriptors) {
		this.memberDescriptors = memberDescriptors;
	}

	public Set<WsIOWrapped> getDescriptorWrappers() {
		return descriptorWrappers;
	}

	public void setDescriptorWrappers(Set<WsIOWrapped> descriptorWrappers) {
		this.descriptorWrappers = descriptorWrappers;
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

	public WsIODescriptor getOperationDescriptor() {
		return operationDescriptor;
	}

	public void setOperationDescriptor(WsIODescriptor operationDescriptor) {
		this.operationDescriptor = operationDescriptor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WsIOExecutable that = (WsIOExecutable) o;
		return Objects.equals(sourceElement, that.sourceElement) && Objects.equals(executableElement, that.executableElement) && Objects.equals(executableMirror, that.executableMirror) && Objects.equals(executableName, that.executableName) && Objects.equals(methodPresent, that.methodPresent) && Objects.equals(methodOperationName, that.methodOperationName) && Objects.equals(methodAction, that.methodAction) && Objects.equals(methodExclude, that.methodExclude) && Objects.equals(resultPresent, that.resultPresent) && Objects.equals(resultName, that.resultName) && Objects.equals(resultPartName, that.resultPartName) && Objects.equals(resultTargetNamespace, that.resultTargetNamespace) && Objects.equals(resultHeader, that.resultHeader) && Objects.equals(elementPresent, that.elementPresent) && Objects.equals(elementName, that.elementName) && Objects.equals(elementNamespace, that.elementNamespace) && Objects.equals(elementRequired, that.elementRequired) && Objects.equals(elementNillable, that.elementNillable) && Objects.equals(elementDefaultValue, that.elementDefaultValue) && Objects.equals(elementType, that.elementType) && Objects.equals(elementWrapperPresent, that.elementWrapperPresent) && Objects.equals(elementWrapperName, that.elementWrapperName) && Objects.equals(elementWrapperNamespace, that.elementWrapperNamespace) && Objects.equals(elementWrapperRequired, that.elementWrapperRequired) && Objects.equals(elementWrapperNillable, that.elementWrapperNillable) && Objects.equals(returnType, that.returnType) && Objects.equals(returnIdentifier, that.returnIdentifier) && Objects.equals(memberDescriptors, that.memberDescriptors) && Objects.equals(descriptorWrappers, that.descriptorWrappers) && Objects.equals(targetIdentifier, that.targetIdentifier) && targetType == that.targetType && Objects.equals(operationDescriptor, that.operationDescriptor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sourceElement, executableElement, executableMirror, executableName, methodPresent, methodOperationName, methodAction, methodExclude, resultPresent, resultName, resultPartName, resultTargetNamespace, resultHeader, elementPresent, elementName, elementNamespace, elementRequired, elementNillable, elementDefaultValue, elementType, elementWrapperPresent, elementWrapperName, elementWrapperNamespace, elementWrapperRequired, elementWrapperNillable, returnType, returnIdentifier, memberDescriptors, descriptorWrappers, targetIdentifier, targetType, operationDescriptor);
	}

	@Override
	public String toString() {
		return "WsIOExecutable{" +
				"sourceElement=" + sourceElement +
				", executableElement=" + executableElement +
				", executableMirror=" + executableMirror +
				", executableName='" + executableName + '\'' +
				", methodPresent=" + methodPresent +
				", methodOperationName='" + methodOperationName + '\'' +
				", methodAction='" + methodAction + '\'' +
				", methodExclude=" + methodExclude +
				", resultPresent=" + resultPresent +
				", resultName='" + resultName + '\'' +
				", resultPartName='" + resultPartName + '\'' +
				", resultTargetNamespace='" + resultTargetNamespace + '\'' +
				", resultHeader=" + resultHeader +
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
				", returnType=" + returnType +
				", returnIdentifier=" + returnIdentifier +
				", memberDescriptors=" + memberDescriptors +
				", descriptorWrappers=" + descriptorWrappers +
				", targetIdentifier=" + targetIdentifier +
				", targetType=" + targetType +
				", operationDescriptor=" + operationDescriptor +
				'}';
	}

}
