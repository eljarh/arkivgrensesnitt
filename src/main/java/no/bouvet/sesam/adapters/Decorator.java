package no.bouvet.sesam.adapters;

public interface Decorator {
    String process(EphorteFacade facade, String field) throws Exception;
}
