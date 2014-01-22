package no.bouvet.sesam.adapters;

import javax.ws.rs.ext.ExceptionMapper;

import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.Response;

@Provider
public class ReferenceNotFoundMapper implements ExceptionMapper<ReferenceNotFound> {
    public Response toResponse(ReferenceNotFound exception) {

        return Response.status(Response.Status.NOT_FOUND).
            entity(exception.getMessage()).
            build();
    }
}
