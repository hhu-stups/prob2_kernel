/* 
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, 
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html) 
 */

package de.prob.animator.domainobjects;

import java.util.HashMap;
import java.util.Map;

public enum SymmetryReductionOption {
	off(0, "No Symmetry Reduction"), nauty(1, "Nauty"), flood(2,
			"Permutation Flooding"), hash(3, "Symmetry Marker (Hash)");

	private final String description;
	private final int pos;

	SymmetryReductionOption(final int pos, final String description) {
		this.pos = pos;
		this.description = description;
	}

	public final boolean isDefault() {
		return this == off;
	}

	public final String getDescription() {
		return description;
	}

	private static final Map<Integer, SymmetryReductionOption> lookup = new HashMap<>();

	static {
		for (SymmetryReductionOption s : values()) {
			lookup.put(s.getPos(), s);
		}
	}

	public final int getPos() {
		return pos;
	}

	public static SymmetryReductionOption get(final int code) {
		return lookup.get(code);
	}

}
