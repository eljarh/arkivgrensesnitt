package no.bouvet.sesam.adapters;

import javax.ws.rs.WebApplicationException;

import static javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.StatusType;
import static javax.ws.rs.core.Response.Status.Family;
import static javax.ws.rs.core.Response.ResponseBuilder;

import javax.ws.rs.core.Response;

public class CustomStatus implements StatusType {
    public CustomStatus(final int statusCode,
                        final String reasonPhrase) {
        super();

        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    @Override
    public Family getFamily() {
        switch (statusCode / 100) {
        case 1: return Family.INFORMATIONAL;
        case 2: return Family.SUCCESSFUL;
        case 3: return Family.REDIRECTION;
        case 4: return Family.CLIENT_ERROR;
        case 5: return Family.SERVER_ERROR;
        default: return Family.OTHER;
        }
    }

    @Override
    public String getReasonPhrase() { return reasonPhrase; }

    @Override
    public int getStatusCode() { return statusCode; }

    public ResponseBuilder responseBuilder() { return Response.status(this); }

    public Response build() { return responseBuilder().build(); }

    public WebApplicationException except() {
        return new WebApplicationException(build());
    }

    private final int statusCode;
    private final String reasonPhrase;
}
