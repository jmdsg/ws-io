package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.WsIOClone;

import java.util.Objects;

public class WsIOIdentifier {

	private String identifierPrefix;

	private String identifierSuffix;

	public static WsIOIdentifier of(String identifierPrefix,
									String identifierSuffix) {

		WsIOIdentifier identifier = new WsIOIdentifier();
		identifier.identifierPrefix = identifierPrefix;
		identifier.identifierSuffix = identifierSuffix;
		return identifier;

	}

	public static WsIOIdentifier of(WsIOClone clone) {
		return of(clone.prefix(), clone.suffix());
	}

	public String getIdentifierPrefix() {
		return identifierPrefix;
	}

	public void setIdentifierPrefix(String identifierPrefix) {
		this.identifierPrefix = identifierPrefix;
	}

	public String getIdentifierSuffix() {
		return identifierSuffix;
	}

	public void setIdentifierSuffix(String identifierSuffix) {
		this.identifierSuffix = identifierSuffix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WsIOIdentifier that = (WsIOIdentifier) o;
		return Objects.equals(identifierPrefix, that.identifierPrefix) && Objects.equals(identifierSuffix, that.identifierSuffix);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(identifierPrefix, identifierSuffix);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "WsIOIdentifier{" +
				"identifierPrefix='" + identifierPrefix + '\'' +
				", identifierSuffix='" + identifierSuffix + '\'' +
				'}';
	}

}
