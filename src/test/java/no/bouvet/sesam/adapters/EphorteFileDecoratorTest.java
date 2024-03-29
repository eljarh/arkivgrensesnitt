package no.bouvet.sesam.adapters;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.io.FileWriter;
import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import static org.mockito.Mockito.*;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.DocumentObjectT;

public class EphorteFileDecoratorTest {
    private HttpResponse Response;
    CloseableHttpClient client = HttpClients.createDefault();

    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();

    private HttpResponse get(String url) throws Exception {
        HttpGet get = new HttpGet(url);
        return client.execute(get);
    }

    @Test
    public void testThatGetReadsBytes() throws Exception {
        EphorteFileDecorator decorator = new EphorteFileDecorator();
        HttpResponse response = get("http://www.jtricks.com/download-unknown");
        byte[] bytes = decorator.getContent(response);
        String content = new String(bytes);
        assertEquals("You are reading text file that was supposed to be downloaded\r\n" +
                     "to your hard disk. If your browser offered to save you the file,\r\n" +
                     "then it handled the Content-Disposition header correctly.", content);
    }

    @Test
    public void testThatGetReadsFilenameFromContentDisposition() throws Exception {
        String url = "http://www.jtricks.com/download-unknown";
        EphorteFileDecorator decorator = new EphorteFileDecorator();
        HttpResponse response = get(url);
        String filename = decorator.getFileName(url, response);
        assertEquals("content.txt", filename);
    }

    @Test
    public void testThatGetFallbacksToFilenameFromUrl() throws Exception {
        String url = "http://www.irs.gov/pub/irs-pdf/fw4.pdf";
        EphorteFileDecorator decorator = new EphorteFileDecorator();
        HttpResponse response = get(url);
        String filename = decorator.getFileName(url, response);
        assertEquals("fw4.pdf", filename);
    }

    @Test
    public void testThatProcessFetchesUrlAndUploadsWithFacade() throws Exception {
        EphorteFacade facade = mock(EphorteFacade.class);
        EphorteFileDecorator decorator = new EphorteFileDecorator();
        decorator.setFacade(facade);

        BatchFragment batch = mock(BatchFragment.class);
        Statement s = new Statement("_", "_", "http://www.jtricks.com/download-unknown", true);
        Fragment fragment = new Fragment("_");
        fragment.setDataObject(new DocumentObjectT());
        decorator.process(fragment, s);

        verify(facade).uploadFile(eq("content.txt"), any(byte[].class));
    }

    @Test
    public void testThatProcessFetchesFileAndUploadsWithFacade() throws Exception {
        File testfile = tmpdir.newFile();
        FileWriter outf = new FileWriter(testfile);
        outf.write("hello, world!");
        outf.close();
        
        EphorteFacade facade = mock(EphorteFacade.class);
        EphorteFileDecorator decorator = new EphorteFileDecorator();
        decorator.setFacade(facade);

        System.out.println("File is: '" + testfile.toURI().toString() + "'");
        
        BatchFragment batch = mock(BatchFragment.class);
        Statement s = new Statement("_", "_", testfile.toURI().toString(), true);
        Fragment fragment = new Fragment("_");
        fragment.setDataObject(new DocumentObjectT());
        decorator.process(fragment, s);

        verify(facade).uploadFile(eq(testfile.getName()), any(byte[].class));
    }
}
