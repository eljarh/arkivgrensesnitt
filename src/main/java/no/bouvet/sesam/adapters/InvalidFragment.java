package no.bouvet.sesam.adapters;

public class InvalidFragment extends RuntimeException {
    public InvalidFragment(String message, Exception e) {
        super(message, e);
    }

    public InvalidFragment(String message) {
        super(message);
    }
}
