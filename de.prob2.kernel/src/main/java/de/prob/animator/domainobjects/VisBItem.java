package de.prob.animator.domainobjects;

import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.PrologTerm;


import java.util.Objects;

/**
 * The VisBItem is designed for the JSON / VisB file
 */
public class VisBItem {
    private String id;
    private String attribute;
    private String value; // B Formula to compute value of attribute for SVG object id

    /**
     *
     * @param id this has to be the id used in the svg file to correspond with that svg element
     * @param attribute this has to be an actual svg attribute
     * @param value this formula has to provide a valid value usable with the given attribute
     */
    public VisBItem(String id, String attribute, String value) {
        this.id = id;
        this.attribute = attribute.toLowerCase();
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getValue() {
        return value;
    }


    public static VisBItem fromPrologTerm(final PrologTerm term) {
        BindingGenerator.getCompoundTerm(term, "set_attr", 3);
        final String id = PrologTerm.atomicString(term.getArgument(1));
        final String attribute = PrologTerm.atomicString(term.getArgument(2));
        final String value = PrologTerm.atomicString(term.getArgument(3));
        return new VisBItem(id, attribute, value);
    }

    @Override
    public String toString(){
        return "{ID: " + this.id +", ATTRIBUTE: "+this.attribute+", VALUE: "+this.value+"} ";
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof VisBItem)) {
            return false;
        }
        VisBItem other = (VisBItem) obj;
        return this.id.equals(other.id) && this.attribute.equals(other.attribute) && this.value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attribute, value);
    }
}
