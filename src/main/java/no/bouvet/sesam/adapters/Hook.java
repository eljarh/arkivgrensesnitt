
package no.bouvet.sesam.adapters;

import java.util.Map;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

/**
 * Hooks can be registered with the facade so that they run after the
 * object is created/looked up for a fragment, but before it's
 * populated. This is necessary to allow certain workarounds for
 * over-eager validation in ePhorte.
 */
public interface Hook {

    /**
     * Gives the hook the facade. Called by the facade before anything
     * runs.
     */
    public void setFacade(EphorteFacade facade);

    /**
     * Actually runs the hook, to do whatever it wants to do.
     */
    public void run(Fragment fragment, Map<String, Object> ids);
    
}
