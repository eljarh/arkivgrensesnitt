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
import org.apache.commons.lang.StringUtils;
import java.util.List;

@Path("fragment")
public class FragmentResource {
    static Logger log = LoggerFactory.getLogger(FragmentResource.class.getName());

    @POST
    @Consumes("application/ntriples,text/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public Response handleFragment(@Context SecurityContext context,
                                   @QueryParam("resource") final List<String> resourceIds,
                                   String source) throws Exception {
        log.debug("Incoming batch-fragment <{}> with body:\n{}", StringUtils.join(resourceIds, ", "), source);

        BatchFragment batch = new BatchFragment(resourceIds, source);
        EphorteFacade.getInstance().save(batch);

        return Response.ok("Success").build();
    }
}
