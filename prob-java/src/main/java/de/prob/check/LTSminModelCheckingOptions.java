package de.prob.check;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.MoreObjects;

public final class LTSminModelCheckingOptions {

	public enum Backend {
		SEQUENTIAL("sequential"),
		SYMBOLIC("symbolic");

		private final String prologName;

		Backend(String prologName) {
			this.prologName = prologName;
		}

		public String getPrologName() {
			return this.prologName;
		}
	}

	public enum Option {
		NO_INV("noinv"),
		NO_DEAD("nodead"),
		POR("por");

		private final String prologName;

		Option(String prologName) {
			this.prologName = prologName;
		}

		public String getPrologName() {
			return this.prologName;
		}
	}

	public static final LTSminModelCheckingOptions DEFAULT = new LTSminModelCheckingOptions(Backend.SEQUENTIAL, EnumSet.noneOf(Option.class))
			.checkInvariantViolations(true)
			.checkDeadlocks(false);

	private final Backend backend;
	private final EnumSet<Option> options;

	private LTSminModelCheckingOptions(Backend backend, Collection<Option> options) {
		this.backend = backend;
		this.options = EnumSet.copyOf(options);
	}

	public Backend backend() {
		return this.backend;
	}

	public LTSminModelCheckingOptions backend(Backend backend) {
		return new LTSminModelCheckingOptions(backend, this.options);
	}

	public Set<Option> getPrologOptions() {
		return Collections.unmodifiableSet(this.options);
	}

	public boolean checkInvariantViolations() {
		return !this.options.contains(Option.NO_INV);
	}

	public LTSminModelCheckingOptions checkInvariantViolations(boolean value) {
		return this.changeOption(Option.NO_INV, !value);
	}

	public boolean checkDeadlocks() {
		return !this.options.contains(Option.NO_DEAD);
	}

	public LTSminModelCheckingOptions checkDeadlocks(boolean value) {
		return this.changeOption(Option.NO_DEAD, !value);
	}

	public boolean partialOrderReduction() {
		return this.options.contains(Option.POR);
	}

	public LTSminModelCheckingOptions partialOrderReduction(boolean value) {
		return this.changeOption(Option.POR, value);
	}

	private LTSminModelCheckingOptions changeOption(Option o, boolean value) {
		if (value == this.options.contains(o)) {
			return this;
		}
		EnumSet<Option> copyOf = EnumSet.copyOf(this.options);
		if (value) {
			copyOf.add(o);
		} else {
			copyOf.remove(o);
		}
		return new LTSminModelCheckingOptions(this.backend, copyOf);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LTSminModelCheckingOptions)) {
			return false;
		}
		LTSminModelCheckingOptions that = (LTSminModelCheckingOptions) o;
		return this.backend() == that.backend() && Objects.equals(this.options, that.options);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.backend(), this.options);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("backend", this.backend())
				.add("options", this.options)
				.toString();
	}
}
