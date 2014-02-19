package no.bouvet.sesam.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HeaderIterator;
import org.apache.http.Header;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.NameValuePair;
import java.io.File;
import org.apache.http.HttpEntity;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import java.io.FileOutputStream;
import org.apache.http.HttpResponse;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

public class EphorteFileDecorator implements Decorator {
    static Logger log = LoggerFactory.getLogger(Fragment.class.getName());

    public Object process(DataObjectT obj, EphorteFacade facade, BatchFragment batch, Statement s) throws Exception {
        String url = s.object;
        log.info("Processing file: {}", url);

        String fileName;
        byte[] data;

        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() > 299) {
                throw new RuntimeException(
                    "Error " + response.getStatusLine().getStatusCode() + " " +
                    response.getStatusLine().getReasonPhrase() + " retrieving "+
                    "file from " + url);
            }

            fileName = getFileName(url, response);
            data = getContent(response);
        } finally {
            client.close();
        }

        return facade.uploadFile(fileName, data);
    }

    public String getFileName(String url, HttpResponse response) throws Exception {
        HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator("Content-Disposition"));
        while (it.hasNext()) {
            HeaderElement e = (HeaderElement) it.next();
            NameValuePair[] pairs = e.getParameters();
            for (NameValuePair pair : pairs) {
                if (pair.getName().equals("filename"))
                    return pair.getValue();
            }
        }

        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    public byte[] getContent(HttpResponse response) throws Exception {
        HttpEntity entity = response.getEntity();
        InputStream stream = entity.getContent();

        try {
            return IOUtils.toByteArray(stream);
        } finally {
            stream.close();
        }
    }
}
