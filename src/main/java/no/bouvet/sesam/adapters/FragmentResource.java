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

@Path("fragment")
public class FragmentResource {
    static Logger log = LoggerFactory.getLogger(FragmentResource.class.getName());

    @POST
    @Consumes("application/ntriples")
    @Produces(MediaType.TEXT_PLAIN)
    public Response handleFragment(@Context SecurityContext context,
                                   @QueryParam("resource") final String resource,
                                   String source) throws Exception {
        log.debug("Incoming fragment <{}> with body:\n{}", resource, source);

        Fragment fragment = new Fragment(resource, source);
        EphorteFacade.getInstance().save(fragment);

        return Response.ok("Success").build();
    }
}
