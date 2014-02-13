package no.bouvet.sesam.adapters;

public class ReferenceNotFound extends Exception {
    private Statement statement;

    public ReferenceNotFound(String message) {
        super(message);
    }

    public ReferenceNotFound(String message, Statement s) {
        super(message);
        this.statement = s;
    }

    public Statement getStatement() {
        return statement;
    }
}
