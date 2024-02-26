package de.prob.animator.domainobjects;


import java.util.Map;
import java.util.Objects;


public class VisBSVGObject {

	private final String id;

	private final String object;

	private final Map<String, String> attributes;

	public VisBSVGObject(String id, String object, Map<String, String> attributes) {
		this.id = id;
		this.object = object;
		this.attributes = attributes;
	}

	public String getId() {
		return id;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public String getObject() {
		return object;
	}

	@Override
	public String toString() {
		return "VisBSVGObject{" +
				"id='" + id + '\'' +
				", object='" + object + '\'' +
				", attributes=" + attributes +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VisBSVGObject that = (VisBSVGObject) o;
		return Objects.equals(id, that.id) && Objects.equals(object, that.object) && Objects.equals(attributes, that.attributes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, object, attributes);
	}
}
