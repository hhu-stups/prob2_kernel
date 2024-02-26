package de.prob.cli;

import com.google.common.collect.ComparisonChain;

import java.util.Locale;

public final class CliVersionNumber implements Comparable<CliVersionNumber> {

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
		this.shortVersionString = String.format(Locale.ROOT, "%s.%s.%s-%s", major, minor, service, qualifier);
		this.version = this.shortVersionString + (revision.isEmpty() ? "" : " (" + revision + ")");
	}

	public String getShortVersionString() {
		return this.shortVersionString;
	}

	@Override
	public String toString() {
		return this.version;
	}

	@Override
	public int compareTo(CliVersionNumber o) {
		return ComparisonChain.start()
			.compare(this.major, o.major)
			.compare(this.minor, o.minor)
			.compare(this.service, o.service)
			.compare(this.qualifier, o.qualifier)
			.compare(this.revision, o.revision)
			.result();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof CliVersionNumber)) {
			return false;
		} else {
			CliVersionNumber that = (CliVersionNumber) o;
			return this.version.equals(that.version);
		}
	}

	@Override
	public int hashCode() {
		return this.version.hashCode();
	}
}
