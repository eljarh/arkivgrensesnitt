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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.StringReader;

import no.priv.garshol.duke.utils.NTriplesParser;
import org.apache.commons.io.IOUtils;

@Path("fragment")
public class FragmentResource {
    static Logger log = LoggerFactory.getLogger(FragmentResource.class.getName());

    @POST
    @Consumes("application/ntriples")
    @Produces(MediaType.TEXT_PLAIN)
    public Response handleFragment(@Context SecurityContext context,
                                   @QueryParam("resource") final String resource,
                                   Reader reader) throws Exception {

        if (log.isDebugEnabled()) {
            String input = new String(IOUtils.toByteArray(reader, "UTF-8"));
            reader = new StringReader(input);
            log.debug("Incoming fragment <{}> with body:\n{}", resource, input);
        }

        EphorteHandler handler = new EphorteHandler(resource);
        NTriplesParser.parse(reader, handler);
        EphorteFacade.save(handler);

        return Response.ok("Success").build();
    }
}
