package de.prob.cli;

import java.util.Objects;

import com.google.common.collect.ComparisonChain;

public class CliVersionNumber implements Comparable<CliVersionNumber> {
	public final String major;
	public final String minor;
	public final String service;
	public final String qualifier;
	public final String revision;
	private final String shortVersionString;
	private final String version;

	public CliVersionNumber(String major, String minor, String service, String qualifier, String revision) {
		this.major = major;
		this.minor = minor;
		this.service = service;
		this.qualifier = qualifier;
		this.revision = revision;
		this.shortVersionString = String.format("%s.%s.%s-%s", major, minor, service, qualifier);
		this.version = this.shortVersionString + (revision.isEmpty() ? "" : " (" + revision + ")");
	}

	public String getShortVersionString() {
		return shortVersionString;
	}

	@Override
	public String toString() {
		return version;
	}

	@Override
	public int compareTo(CliVersionNumber o) {
		return ComparisonChain.start().compare(major, o.major).compare(minor, o.minor).compare(service, o.service)
				.compare(qualifier, o.qualifier).compare(revision, o.revision).result();
	}

	@Override
	public boolean equals(Object that) {
		return that != null && this.toString().equals(that.toString());
	}

	@Override
	public int hashCode() {
		return Objects.hash(version);
	}

}
