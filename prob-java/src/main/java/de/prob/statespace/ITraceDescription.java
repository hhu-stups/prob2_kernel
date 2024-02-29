package de.prob.statespace;

import java.util.List;

/**
 * Classes that implement this interface have the inherent property of being
 * able to generate a Trace through a specified {@link StateSpace} object. This
 * can be calculated by calling the {@link ITraceDescription#getTrace(StateSpace)} method.
 * 
 * @author joy
 * 
 */
public interface ITraceDescription {

	/**
	 * Generates a {@link Trace} through the {@link StateSpace}. May call the
	 * {@link StateSpace#getTrace(List)} or
	 * {@link StateSpace#getTrace(String)} methods in order to generate the
	 * trace.
	 * 
	 * @param s
	 *            {@link StateSpace} for which this trace should be generated
	 * @return {@link Trace} through the {@link StateSpace}
	 * @throws RuntimeException
	 *             when the class is not able to create the specified
	 *             {@link Trace}
	 */
	Trace getTrace(StateSpace s);
}
