package com.fiberg.wsio.processor;

class WsIOAdapterInfo {

	private String adapterValue;

	private String adapterType;

	public static WsIOAdapterInfo of(String adapterValue,
                                     String adapterType) {

		WsIOAdapterInfo info = new WsIOAdapterInfo();
		info.adapterValue = adapterValue;
		info.adapterType = adapterType;
		return info;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WsIOAdapterInfo wsIOMemberInfo = (WsIOAdapterInfo) o;

		if (adapterValue != null ? !adapterValue.equals(wsIOMemberInfo.adapterValue) : wsIOMemberInfo.adapterValue != null) return false;
		return adapterType != null ? adapterType.equals(wsIOMemberInfo.adapterType) : wsIOMemberInfo.adapterType == null;
	}

	@Override
	public int hashCode() {
		int result = adapterValue != null ? adapterValue.hashCode() : 0;
		result = 31 * result + (adapterType != null ? adapterType.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "WsIOAdapterInfo{" +
				"adapterValue='" + adapterValue + '\'' +
				", adapterType='" + adapterType + '\'' +
				'}';
	}

}
