package de.prob.statespace;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class OperationInfo {
	public enum Type {
		CLASSICAL_B, EVENTB, CSP;

		public static OperationInfo.Type fromProlog(final String prologName) {
			switch (prologName) {
				case "classic":
					return CLASSICAL_B;

				case "eventb_operation":
					return EVENTB;

				case "csp":
					return CSP;

				default:
					throw new IllegalArgumentException("Unknown Prolog operation type: " + prologName);
			}
		}
	}

	private final String operationName;
	private final List<String> parameterNames;
	private final List<String> outputParameterNames;
	private final boolean topLevel;
	private final OperationInfo.Type type;
	private final List<String> readVariables;
	private final List<String> writtenVariables;
	private final List<String> nonDetWrittenVariables;

	/**
	 * Annotation is used by jackson to construct objects
	 * @param operationName teh name of the operation
	 * @param parameterNames name of the parameters
	 * @param outputParameterNames name of the output parameters
	 * @param topLevel operation is toplevel
	 * @param type type of the operation
	 * @param readVariables read variables
	 * @param writtenVariables written variables
	 * @param nonDetWrittenVariables non deterministic written variables
	 */
	public OperationInfo(
			@JsonProperty("operationName") final String operationName,
			@JsonProperty("parameterNames") final List<String> parameterNames,
			@JsonProperty("outputParameterNames") final List<String> outputParameterNames,
			@JsonProperty("topLevel") final boolean topLevel,
			@JsonProperty("type") final OperationInfo.Type type,
			@JsonProperty("readVariables") final List<String> readVariables,
			@JsonProperty("writtenVariables") final List<String> writtenVariables,
			@JsonProperty("nonDetWrittenVariables") final List<String> nonDetWrittenVariables
	) {
		this.operationName = operationName;
		this.parameterNames = parameterNames;
		this.outputParameterNames = outputParameterNames;
		this.topLevel = topLevel;
		this.type = type;
		this.readVariables = readVariables;
		this.writtenVariables = writtenVariables;
		this.nonDetWrittenVariables = nonDetWrittenVariables;
	}

	public String getOperationName() {
		return operationName;
	}

	public List<String> getParameterNames() {
		return parameterNames;
	}

	public List<String> getOutputParameterNames() {
		return outputParameterNames;
	}

	public boolean isTopLevel() {
		return this.topLevel;
	}

	public OperationInfo.Type getType() {
		return this.type;
	}

	public List<String> getReadVariables() {
		return this.readVariables;
	}

	public List<String> getWrittenVariables() {
		return this.writtenVariables;
	}

	public List<String> getNonDetWrittenVariables() {
		return this.nonDetWrittenVariables;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("operationName", this.operationName)
			.add("parameterNames", this.parameterNames)
			.add("outputParameterNames", this.outputParameterNames)
			.add("topLevel", this.topLevel)
			.add("type", this.type)
			.add("readVariables", this.readVariables)
			.add("writtenVariables", this.writtenVariables)
			.add("nonDetWrittenVariables", this.nonDetWrittenVariables)
			.toString();
	}
}
