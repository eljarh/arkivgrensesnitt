package no.bouvet.sesam.adapters;

public interface Decorator {
    Object process(EphorteFacade facade, BatchFragment fragment, Statement statement) throws Exception;
}
