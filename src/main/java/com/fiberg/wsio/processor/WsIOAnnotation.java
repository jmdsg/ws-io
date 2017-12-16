package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.WsIOMessage;
import com.fiberg.wsio.annotation.WsIOMessageWrapper;

class WsIOAnnotation {

	private String packageName;

	private String packagePath;


	private String packagePrefix;

	private String packageSuffix;

	private String packageStart;

	private String packageMiddle;

	private String packageEnd;

	private String packageJs;

	static WsIOAnnotation ofNull(WsIOMessage annotation) {
		if (annotation != null) {
			return of(annotation);
		}
		return null;
	}

	static WsIOAnnotation ofNull(WsIOMessageWrapper annotation) {
		if (annotation != null) {
			return of(annotation);
		}
		return null;
	}

	static WsIOAnnotation of(WsIOMessage annotation) {
		return of(annotation.packageName(), annotation.packagePath(), annotation.packagePrefix(),
				annotation.packageSuffix(), annotation.packageStart(), annotation.packageMiddle(),
				annotation.packageEnd(), annotation.packageJs());
	}

	static WsIOAnnotation of(WsIOMessageWrapper annotation) {
		return of(annotation.packageName(), annotation.packagePath(), annotation.packagePrefix(),
				annotation.packageSuffix(), annotation.packageStart(), annotation.packageMiddle(),
				annotation.packageEnd(), annotation.packageJs());
	}

	private static WsIOAnnotation of(String packageName,
	                                 String packagePath,
	                                 String packagePrefix,
	                                 String packageSuffix,
	                                 String packageStart,
	                                 String packageMiddle,
	                                 String packageEnd,
	                                 String packageJs) {
		WsIOAnnotation generator = new WsIOAnnotation();
		generator.setPackageName(packageName);
		generator.setPackagePath(packagePath);
		generator.setPackagePrefix(packagePrefix);
		generator.setPackageSuffix(packageSuffix);
		generator.setPackageStart(packageStart);
		generator.setPackageMiddle(packageMiddle);
		generator.setPackageEnd(packageEnd);
		generator.setPackageJs(packageJs);
		return generator;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPackagePath() {
		return packagePath;
	}

	public void setPackagePath(String packagePath) {
		this.packagePath = packagePath;
	}

	public String getPackagePrefix() {
		return packagePrefix;
	}

	public void setPackagePrefix(String packagePrefix) {
		this.packagePrefix = packagePrefix;
	}

	public String getPackageSuffix() {
		return packageSuffix;
	}

	public void setPackageSuffix(String packageSuffix) {
		this.packageSuffix = packageSuffix;
	}

	public String getPackageStart() {
		return packageStart;
	}

	public void setPackageStart(String packageStart) {
		this.packageStart = packageStart;
	}

	public String getPackageMiddle() {
		return packageMiddle;
	}

	public void setPackageMiddle(String packageMiddle) {
		this.packageMiddle = packageMiddle;
	}

	public String getPackageEnd() {
		return packageEnd;
	}

	public void setPackageEnd(String packageEnd) {
		this.packageEnd = packageEnd;
	}

	public String getPackageJs() {
		return packageJs;
	}

	public void setPackageJs(String packageJs) {
		this.packageJs = packageJs;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WsIOAnnotation that = (WsIOAnnotation) o;

		if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) return false;
		if (packagePath != null ? !packagePath.equals(that.packagePath) : that.packagePath != null) return false;
		if (packagePrefix != null ? !packagePrefix.equals(that.packagePrefix) : that.packagePrefix != null)
			return false;
		if (packageSuffix != null ? !packageSuffix.equals(that.packageSuffix) : that.packageSuffix != null)
			return false;
		if (packageStart != null ? !packageStart.equals(that.packageStart) : that.packageStart != null) return false;
		if (packageMiddle != null ? !packageMiddle.equals(that.packageMiddle) : that.packageMiddle != null)
			return false;
		if (packageEnd != null ? !packageEnd.equals(that.packageEnd) : that.packageEnd != null) return false;
		return packageJs != null ? packageJs.equals(that.packageJs) : that.packageJs == null;
	}

	@Override
	public int hashCode() {
		int result = packageName != null ? packageName.hashCode() : 0;
		result = 31 * result + (packagePath != null ? packagePath.hashCode() : 0);
		result = 31 * result + (packagePrefix != null ? packagePrefix.hashCode() : 0);
		result = 31 * result + (packageSuffix != null ? packageSuffix.hashCode() : 0);
		result = 31 * result + (packageStart != null ? packageStart.hashCode() : 0);
		result = 31 * result + (packageMiddle != null ? packageMiddle.hashCode() : 0);
		result = 31 * result + (packageEnd != null ? packageEnd.hashCode() : 0);
		result = 31 * result + (packageJs != null ? packageJs.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "WsIOAnnotation{" +
				"packageName='" + packageName + '\'' +
				", packagePath='" + packagePath + '\'' +
				", packagePrefix='" + packagePrefix + '\'' +
				", packageSuffix='" + packageSuffix + '\'' +
				", packageStart='" + packageStart + '\'' +
				", packageMiddle='" + packageMiddle + '\'' +
				", packageEnd='" + packageEnd + '\'' +
				", packageJs='" + packageJs + '\'' +
				'}';
	}

}