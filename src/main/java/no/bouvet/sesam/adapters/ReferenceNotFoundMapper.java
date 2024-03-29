package no.bouvet.sesam.adapters;

import javax.ws.rs.ext.ExceptionMapper;

import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.Response;

@Provider
public class ReferenceNotFoundMapper implements ExceptionMapper<ReferenceNotFound> {
    public Response toResponse(ReferenceNotFound exception) {
        return Response.status(new CustomStatus(424, "Failed Dependency"))
            .entity(exception.getMessage()).type("text/plain")
            .build();
    }
}
