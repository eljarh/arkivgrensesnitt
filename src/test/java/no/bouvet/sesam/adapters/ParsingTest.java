/*
package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.functions.v2.FunctionDescriptor;
import no.gecko.ephorte.services.functions.v2.FunctionResult;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CasePartyT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.DocumentDescriptionT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.DocumentObjectT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.RegistryEntryDocumentT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.RegistryEntryT;
import no.gecko.ncore.client.core.NCore;
import no.gecko.ncore.client.core.ObjectModel;

import org.junit.Test;
import junit.framework.Assert;

public class ParsingTest {
    @Test
    public void ParseRDFToCase() {

    }

    public void CreateStructure() {
        // upload a file
        String uploadedFileName = NCore.Documents.uploadFile("testFile.pdf",
                                                             "ObjectModelService", FileUtils.readFileToByteArray(testFile1));

        // create a case
        CaseT ca = new CaseT();
        ca.setTitle("case : " + getCurrentTime());

        // create a case party
        CasePartyT cp = new CasePartyT();
        cp.setName("Case party 1");
        cp.setCase(ca);

        // create a registry entry
        RegistryEntryT re = new RegistryEntryT();
        re.setTitle("registry entry : " + getCurrentTime());
        re.setRegistryEntryTypeId("X");
        re.setCase(ca);

        // create a document description
        DocumentDescriptionT dd = new DocumentDescriptionT();
        dd.setDocumentTitle("document description : " + getCurrentTime());

        // create a document object
        DocumentObjectT dobj = new DocumentObjectT();
        dobj.setVersionNumber(1);
        dobj.setVariantFormatId("P");
        dobj.setFilePath(uploadedFileName);
        dobj.setFileformatId("PDF");
        dobj.setDocumentDescription(dd);

        // create a registry entry document
        RegistryEntryDocumentT red = new RegistryEntryDocumentT();
        red.setDocumentLinkTypeId("H");
        red.setRegistryEntry(re);
        red.setDocumentDescription(dd);
    }

    public void Search() {
	private static void doSearches() throws Exception {
            int index = 0;
            int takeCount = 7;

            // search for all cases
            while (true) {

                List<DataObjectT> cases = NCore.Objects
                    .filteredQuery(ObjectModel.Case, "Id>0", new String[] {},
                                   index, takeCount);

                // no more cases
                if (cases.size() == 0) {
                    break;
                }

                System.out.println("~~~~~~~~~~");
                for (DataObjectT dataObject : cases) {

                    CaseT ca = (CaseT) dataObject;
                    System.out.println(ca.getId() + " : " + ca.getTitle());
                }

                index += takeCount;
            }
	}
    }
}
*/
