package de.prob.statespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OperationInfo {

	public enum Type {
		CLASSICAL_B, EVENTB, CSP, XTL;

		public static OperationInfo.Type fromProlog(final String prologName) {
			switch (prologName) {
				case "classic":
					return CLASSICAL_B;

				case "eventb_operation":
					return EVENTB;

				case "csp":
					return CSP;

				case "xtl":
					return XTL;

				default:
					throw new IllegalArgumentException("Unknown Prolog operation type: " + prologName);
			}
		}
	}

	// all fields are @JsonInclude(JsonInclude.Include.NON_EMPTY)

	@JsonInclude // name is required
	private final String operationName;
	private final List<String> parameterNames;
	private final List<String> outputParameterNames;
	@JsonInclude // default value for boolean value is false
	private final boolean topLevel;
	private final OperationInfo.Type type;
	private final List<String> readVariables;
	private final List<String> writtenVariables;
	private final List<String> nonDetWrittenVariables;
	private final Map<String, String> typeMap;

	/**
	 * @param operationName          teh name of the operation
	 * @param parameterNames         name of the parameters
	 * @param outputParameterNames   name of the output parameters
	 * @param topLevel               operation is toplevel
	 * @param type                   type of the operation
	 * @param readVariables          read variables
	 * @param writtenVariables       written variables
	 * @param nonDetWrittenVariables non deterministic written variables
	 */
	public OperationInfo(
		final String operationName,
		final List<String> parameterNames,
		final List<String> outputParameterNames,
		final boolean topLevel,
		final OperationInfo.Type type,
		final List<String> readVariables,
		final List<String> writtenVariables,
		final List<String> nonDetWrittenVariables
	) {
		this(operationName, parameterNames, outputParameterNames, topLevel, type, readVariables, writtenVariables, nonDetWrittenVariables, null);
	}

	/**
	 * Annotation is used by jackson to construct objects
	 *
	 * @param operationName          teh name of the operation
	 * @param parameterNames         name of the parameters
	 * @param outputParameterNames   name of the output parameters
	 * @param topLevel               operation is toplevel
	 * @param type                   type of the operation
	 * @param readVariables          read variables
	 * @param writtenVariables       written variables
	 * @param nonDetWrittenVariables non deterministic written variables
	 * @param typeMap                map mapping the used identifiers to their types
	 */
	@JsonCreator
	public OperationInfo(
		@JsonProperty("operationName") final String operationName,
		@JsonProperty("parameterNames") final List<String> parameterNames,
		@JsonProperty("outputParameterNames") final List<String> outputParameterNames,
		@JsonProperty("topLevel") final boolean topLevel,
		@JsonProperty("type") final OperationInfo.Type type,
		@JsonProperty("readVariables") final List<String> readVariables,
		@JsonProperty("writtenVariables") final List<String> writtenVariables,
		@JsonProperty("nonDetWrittenVariables") final List<String> nonDetWrittenVariables,
		@JsonProperty("typeMap") final Map<String, String> typeMap
	) {
		this.operationName = operationName != null ? operationName : "";
		this.parameterNames = parameterNames != null ? parameterNames : new ArrayList<>();
		this.outputParameterNames = outputParameterNames != null ? outputParameterNames : new ArrayList<>();
		this.topLevel = topLevel;
		this.type = type;
		this.readVariables = readVariables != null ? readVariables : new ArrayList<>();
		this.writtenVariables = writtenVariables != null ? writtenVariables : new ArrayList<>();
		this.nonDetWrittenVariables = nonDetWrittenVariables != null ? nonDetWrittenVariables : new ArrayList<>();
		this.typeMap = typeMap != null ? typeMap : new HashMap<>();
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

	@JsonIgnore
	public List<String> getAllVariables() {
		return Stream.of(readVariables, writtenVariables, nonDetWrittenVariables).flatMap(Collection::stream).collect(Collectors.toList());
	}

	public Map<String, String> getTypeMap() {
		return typeMap;
	}

	@JsonIgnore
	public Set<String> getAllIdentifier() {
		return Stream.of(readVariables, writtenVariables, nonDetWrittenVariables, getParameterNames(), getOutputParameterNames())
			.flatMap(Collection::stream).collect(Collectors.toSet());
	}

	public OperationInfo createOperationInfoWithNewTypeMap(Map<String, String> info) {
		return new OperationInfo(operationName, parameterNames, outputParameterNames, topLevel, type, readVariables, writtenVariables, nonDetWrittenVariables, info);
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
			.add("types", this.typeMap)
			.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		OperationInfo other = (OperationInfo) obj;
		return this.isTopLevel() == other.isTopLevel()
			&& this.getOperationName().equals(other.getOperationName())
			&& this.getParameterNames().equals(other.getParameterNames())
			&& this.getOutputParameterNames().equals(other.getOutputParameterNames())
			&& this.getType() == other.getType()
			&& this.getReadVariables().equals(other.getReadVariables())
			&& this.getWrittenVariables().equals(other.getWrittenVariables())
			&& this.getNonDetWrittenVariables().equals(other.getNonDetWrittenVariables())
			&& this.getTypeMap().equals(other.getTypeMap());
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			this.getOperationName(),
			this.getParameterNames(),
			this.getOutputParameterNames(),
			this.isTopLevel(),
			this.getType(),
			this.getReadVariables(),
			this.getWrittenVariables(),
			this.getNonDetWrittenVariables(),
			this.getTypeMap()
		);
	}
}
