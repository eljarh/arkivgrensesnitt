package no.bouvet.sesam.adapters;

public class InvalidFragment extends Exception {
    public InvalidFragment(Exception e, String message) {
        super(message, e);
    }
}
