package no.bouvet.sesam.adapters;

import javax.ws.rs.ext.ExceptionMapper;

import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.Response;

@Provider
public class InvalidFragmentMapper implements ExceptionMapper<InvalidFragment> {
    public Response toResponse(InvalidFragment exception) {

        return Response.status(new CustomStatus(422, "Unprocessable Entity")).
            entity(exception.getMessage()).
            build();
    }
}
