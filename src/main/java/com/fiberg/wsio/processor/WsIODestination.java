package com.fiberg.wsio.processor;

import javax.lang.model.element.TypeElement;
import java.util.Objects;

class WsIODestination {

	private TypeElement elementType;

	private String elementPackage;

	public static WsIODestination of(TypeElement elementType,
									 String elementPackage) {

		WsIODestination destination = new WsIODestination();
		destination.elementType = elementType;
		destination.elementPackage = elementPackage;
		return destination;

	}

	public TypeElement getElementType() {
		return elementType;
	}

	public void setElementType(TypeElement elementType) {
		this.elementType = elementType;
	}

	public String getElementPackage() {
		return elementPackage;
	}

	public void setElementPackage(String elementPackage) {
		this.elementPackage = elementPackage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WsIODestination that = (WsIODestination) o;
		return Objects.equals(elementType, that.elementType) && Objects.equals(elementPackage, that.elementPackage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(elementType, elementPackage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "WsIODestination{" +
				"elementType=" + elementType +
				", elementPackage='" + elementPackage + '\'' +
				'}';
	}

}
