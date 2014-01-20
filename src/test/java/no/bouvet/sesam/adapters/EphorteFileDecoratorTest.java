package no.bouvet.sesam.adapters;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

public class EphorteFileDecoratorTest {
    private HttpResponse Response;
    CloseableHttpClient client = HttpClients.createDefault();

    private HttpResponse get(String url) throws Exception {
        HttpGet get = new HttpGet(url);
        return client.execute(get);
    }

    @Test
    public void testThatGetReadsFile() throws Exception {
        EphorteFileDecorator decorator = new EphorteFileDecorator();
        HttpResponse response = get("http://www.jtricks.com/download-unknown");
        File file = decorator.getFile(response);
        String content = FileUtils.readFileToString(file, "UTF-8");
        assertEquals("You are reading text file that was supposed to be downloaded\r\n" +
                     "to your hard disk. If your browser offered to save you the file,\r\n" +
                     "then it handled the Content-Disposition header correctly.", content);
    }

    @Test
    public void testThatGetReadsFilenameFromContentDisposition() throws Exception {
        EphorteFileDecorator decorator = new EphorteFileDecorator();
        HttpResponse response = get("http://www.jtricks.com/download-unknown");
        String filename = decorator.getFileName(response);
        assertEquals("content.txt", filename);
    }
}
