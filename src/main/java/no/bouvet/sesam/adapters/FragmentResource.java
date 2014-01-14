package no.bouvet.sesam.adapters;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.priv.garshol.duke.utils.NTriplesParser;
import java.io.Reader;
import no.gecko.ncore.client.core.NCore;

@Path("fragment")
public class FragmentResource {
    static Logger log = LoggerFactory.getLogger(FragmentResource.class.getName());

    @POST
    @Consumes("application/ntriples")
    @Produces(MediaType.TEXT_PLAIN)
    public Response handleFragment(@Context SecurityContext context,
                                   @QueryParam("resource") final String resource,
                                   Reader reader) throws Exception {

        log.debug("Incoming fragment with resource: {}", resource);

        EphorteHandler handler = new EphorteHandler(resource);
        NTriplesParser.parse(reader, handler);
        if (handler.shouldUpdate())
            NCore.Objects.update(handler.getDataObjects());
        else
            NCore.Objects.insert(handler.getDataObjects());

        return Response.ok("Success").build();
    }
}
