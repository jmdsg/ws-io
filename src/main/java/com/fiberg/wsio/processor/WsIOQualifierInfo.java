package com.fiberg.wsio.processor;

class WsIOQualifierInfo {

	private String qualifierPrefix;

	private String qualifierSuffix;

	public static WsIOQualifierInfo of(String qualifierPrefix,
									   String qualifierSuffix) {

		WsIOQualifierInfo info = new WsIOQualifierInfo();
		info.qualifierPrefix = qualifierPrefix;
		info.qualifierSuffix = qualifierSuffix;
		return info;

	}

	public String getQualifierPrefix() {
		return qualifierPrefix;
	}

	public void setQualifierPrefix(String qualifierPrefix) {
		this.qualifierPrefix = qualifierPrefix;
	}

	public String getQualifierSuffix() {
		return qualifierSuffix;
	}

	public void setQualifierSuffix(String qualifierSuffix) {
		this.qualifierSuffix = qualifierSuffix;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WsIOQualifierInfo wsIOParameterInfo = (WsIOQualifierInfo) o;

		if (qualifierPrefix != null ? !qualifierPrefix.equals(wsIOParameterInfo.qualifierPrefix) : wsIOParameterInfo.qualifierPrefix != null) return false;
		return qualifierSuffix != null ? qualifierSuffix.equals(wsIOParameterInfo.qualifierSuffix) : wsIOParameterInfo.qualifierSuffix == null;
	}

	@Override
	public int hashCode() {
		int result = qualifierPrefix != null ? qualifierPrefix.hashCode() : 0;
		result = 31 * result + (qualifierSuffix != null ? qualifierSuffix.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "WsIOQualifierInfo{" +
				"qualifierPrefix='" + qualifierPrefix + '\'' +
				", qualifierSuffix='" + qualifierSuffix + '\'' +
				'}';
	}

}
