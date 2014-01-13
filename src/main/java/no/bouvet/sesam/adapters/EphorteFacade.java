package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import java.util.List;
import no.gecko.ncore.client.core.NCore;
import java.util.ArrayList;
import no.priv.garshol.duke.utils.ObjectUtils;

public class EphorteFacade {
    public static void setExternalId(DataObjectT obj, String externalId) {
        ObjectUtils.setBeanProperty(obj, "custom-attribute-2", externalId, null);
    }

    public static DataObjectT getByExternalId(String typeName, String externalId) throws Exception {
        List<DataObjectT> results = NCore.Objects.filteredQuery(getSearchName(typeName), getSearchString(typeName, externalId), new String[] {}, null, null);
        int found = results.size();

        if (found == 0)
            return null;
        if (found == 1)
            return results.get(0);

        throw new RuntimeException("Found multiple " + typeName + "s with external id = " + externalId);
    }

    public static String getSearchName(String typeName) {
        int lastPeriod = typeName.lastIndexOf(".");
        return typeName.substring(lastPeriod + 1, typeName.length() - 1);
    }

    public static String getSearchString(String typeName, String externalId) {
        return getAttributeName(typeName) + "=" + externalId;
    }

    public static String getAttributeName(String typeName) {
        return "CustomAttribute2";
    }
}
