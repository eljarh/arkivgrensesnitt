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

public class EphorteFileDecorator {
    static Logger log = LoggerFactory.getLogger(Fragment.class.getName());

    public EphorteFileDecorator() {
    }

    public String process(String url) throws Exception {
        log.info("Processing file: {}", url);

        String fileName;
        byte[] data;

        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);

            fileName = getFileName(response);
            data = getContent(response);
        } finally {
            client.close();
        }

        return null; // uploadFile(fileName, file);
    }

    public String getFileName(HttpResponse response) throws Exception {
        HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator("Content-Disposition"));
        while (it.hasNext()) {
            HeaderElement e = (HeaderElement) it.next();
            NameValuePair[] pairs = e.getParameters();
            for (NameValuePair pair : pairs) {
                if (pair.getName().equals("filename"))
                    return pair.getValue();
            }
        }

        return null;
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

        /*
        next uploadFileNameResult;

        try {
            log.debug("Opplasting: Input filnavn = " + input.getFileName());
            log.debug("Opplasting: this.storageId = " + this.storageId);
            log.debug("Opplasting: filen eksisterer = " + input.getFile().exists());
            //log.debug("Document Service - Using username " + ClientConfig.getUsername() + " password " + ClientConfig.getPassword() + " database " + ClientConfig.getDatabase());

            uploadFileNameResult = NCore.Documents.uploadFileResult(input.getFileName(),
                                                                    this.storageId, FileUtils.readFileToByteArray(input.getFile()));
            String fileName = uploadFileNameResult.getFileName();
            String identifier = uploadFileNameResult.getIdentifier();
            this.documentServiceUploadFileResult.setFileName(fileName);
            this.documentServiceUploadFileResult.setFileIdentifier(identifier);
            log.debug("Opplastet filnavn2: " + fileName + " Identifier: " + identifier);
        }catch (ClientTransportException cte){
            throw new EphorteNetworkException(cte);
        } catch (UnknownHostException uhe){
            throw new EphorteNetworkException(uhe);
        } catch (Exception e) {
            log.error(e);
            log.error("Upload av file " + input.getFileName() + "failed " + e);
            this.documentServiceUploadFileResult.setResultOK(false);
        }

        log.info("DocumentServiceUploadFileImpl.execute before return");
        return this.documentServiceUploadFileResult;
        */
}
