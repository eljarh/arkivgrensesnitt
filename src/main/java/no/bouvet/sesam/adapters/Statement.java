package no.bouvet.sesam.adapters;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class Statement {
    public final String subject;
    public final String property;
    public String object; // need to be able to mess around with these
    public final boolean literal;

    public Statement(String s, String p, String o, boolean l) {
        subject = s;
        property = p;
        object = o;
        literal = l;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Statement rhs = (Statement) obj;
        return new EqualsBuilder()
            .append(subject, rhs.subject)
            .append(property, rhs.property)
            .append(object, rhs.object)
            .append(literal, rhs.literal)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 13).
            append(subject).
            append(property).
            append(object).
            append(literal).
            toHashCode();

    }
}
