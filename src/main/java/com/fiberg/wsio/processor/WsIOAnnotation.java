package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.WsIOClone;
import com.fiberg.wsio.annotation.WsIOMessage;
import com.fiberg.wsio.annotation.WsIOMessageWrapper;

import java.util.Objects;

class WsIOAnnotation {

	private String packageName;

	private String packagePath;

	private String packagePrefix;

	private String packageSuffix;

	private String packageStart;

	private String packageMiddle;

	private String packageEnd;

	private String packageFunc;

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
				annotation.packageEnd(), annotation.packageFunc());
	}

	static WsIOAnnotation of(WsIOMessageWrapper annotation) {
		return of(annotation.packageName(), annotation.packagePath(), annotation.packagePrefix(),
				annotation.packageSuffix(), annotation.packageStart(), annotation.packageMiddle(),
				annotation.packageEnd(), annotation.packageFunc());
	}

	static WsIOAnnotation of(WsIOClone annotation) {
		return of(annotation.packageName(), annotation.packagePath(), annotation.packagePrefix(),
				annotation.packageSuffix(), annotation.packageStart(), annotation.packageMiddle(),
				annotation.packageEnd(), annotation.packageFunc());
	}

	private static WsIOAnnotation of(String packageName,
	                                 String packagePath,
	                                 String packagePrefix,
	                                 String packageSuffix,
	                                 String packageStart,
	                                 String packageMiddle,
	                                 String packageEnd,
	                                 String packageFunc) {
		WsIOAnnotation generator = new WsIOAnnotation();
		generator.setPackageName(packageName);
		generator.setPackagePath(packagePath);
		generator.setPackagePrefix(packagePrefix);
		generator.setPackageSuffix(packageSuffix);
		generator.setPackageStart(packageStart);
		generator.setPackageMiddle(packageMiddle);
		generator.setPackageEnd(packageEnd);
		generator.setPackageFunc(packageFunc);
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

	public String getPackageFunc() {
		return packageFunc;
	}

	public void setPackageFunc(String packageFunc) {
		this.packageFunc = packageFunc;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WsIOAnnotation that = (WsIOAnnotation) o;
		return Objects.equals(packageName, that.packageName) && Objects.equals(packagePath, that.packagePath) && Objects.equals(packagePrefix, that.packagePrefix) && Objects.equals(packageSuffix, that.packageSuffix) && Objects.equals(packageStart, that.packageStart) && Objects.equals(packageMiddle, that.packageMiddle) && Objects.equals(packageEnd, that.packageEnd) && Objects.equals(packageFunc, that.packageFunc);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(packageName, packagePath, packagePrefix, packageSuffix, packageStart, packageMiddle, packageEnd, packageFunc);
	}

	/**
	 * {@inheritDoc}
	 */
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
				", packageFunc='" + packageFunc + '\'' +
				'}';
	}

}