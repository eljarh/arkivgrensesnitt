package no.bouvet.sesam.adapters;

public interface Decorator {
    Object process(EphorteFacade facade, String field) throws Exception;
}
