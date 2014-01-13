package no.bouvet.sesam.adapters;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class Statement {
    public final String subject;
    public final String property;
    public final String object;

    public Statement(String s, String p, String o) {
        subject = s;
        property = p;
        object = o;
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
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 13).
            append(subject).
            append(property).
            append(object).
            toHashCode();

    }
}