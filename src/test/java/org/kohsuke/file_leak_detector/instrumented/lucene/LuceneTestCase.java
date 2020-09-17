package org.kohsuke.file_leak_detector.instrumented.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kohsuke.file_leak_detector.ActivityListener;
import org.kohsuke.file_leak_detector.Listener;

/**
 * Test for a custom implementation of FileSystemProvider and DirectoryStream
 * that are used by Apache Lucene. They require special handling to not report
 * file-handle leaks.
 * <br/>
 * Make sure to run this test with injected file-leak-detector as otherwise
 * tests will fail.
 */
public class LuceneTestCase {
    private static final StringWriter output = new StringWriter();
    private Path tempDir;
    private Object obj;
    private FileSystem fileSystem;

    private final ActivityListener listener = new ActivityListener() {
        @Override
        public void open(Object obj, File file) {
            LuceneTestCase.this.obj = obj;
        }

        @Override
        public void open(Object obj, Path file) {
            LuceneTestCase.this.obj = obj;
        }

        @Override
        public void openSocket(Object obj) {
            LuceneTestCase.this.obj = obj;
        }

        @Override
        public void close(Object obj) {
            // sometimes java.util.zip.ZipFile$CleanableResource$FinalizableResource.finalize()
            // will kick in and will close a ZipFile, thus we ignore the corresponding objects here
            if (obj.getClass().getSimpleName().contains("URLJarFile")) {
                return;
            }

            LuceneTestCase.this.obj = obj;
        }

        @Override
        public void fd_open(Object obj) {
            LuceneTestCase.this.obj = obj;
        }
    };

    @BeforeClass
    public static void setup() {
        assertTrue(
                "This test expects the Java Agent to be installed via commandline options",
                Listener.isAgentInstalled());
        Listener.TRACE = new PrintWriter(output);
    }

    @Before
    public void registerListener() {
        ActivityListener.LIST.add(listener);
    }

    @After
    public void unregisterListener() {
        ActivityListener.LIST.remove(listener);
    }

    @Before
    public void prepareOutput() throws Exception {
        FileSystem fs = FileSystems.getDefault();
        fs = new FilterFileSystemProvider("extras://", fs).getFileSystem(null);
        fileSystem = fs.provider().getFileSystem(URI.create("file:///"));

        tempDir = fileSystem.getPath(System.getProperty("java.io.tmpdir"));

        Files.createDirectories(tempDir);
    }

    @Test
    public void testConstants() throws Throwable {
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tempDir);

        assertNotNull("No file record for file " + tempDir + " found", findPathRecord(tempDir));

        assertTrue("Did not have the expected type of 'marker' object: " + obj, obj instanceof DirectoryStream);

        directoryStream.close();

        assertNull("File record for file " + tempDir + " not removed", findPathRecord(tempDir));

        fileSystem.close();

        String traceOutput = output.toString();
        assertTrue(traceOutput.contains("Opened " + tempDir));
        assertTrue(traceOutput.contains("Closed " + tempDir));
    }

    @Test
    public void testWalkFileTree() throws IOException {
        Path path = Files.walkFileTree(tempDir, new NameFileFilter("123"));
        assertEquals(tempDir, path);

        fileSystem.close();

        String traceOutput = output.toString();
        assertTrue(traceOutput.contains("Opened " + tempDir));
        assertTrue(traceOutput.contains("Closed " + tempDir));
    }

    private static Listener.PathRecord findPathRecord(Path path) {
        for (Listener.Record record : Listener.getCurrentOpenFiles()) {
            if (record instanceof Listener.PathRecord) {
                Listener.PathRecord pathRecord = (Listener.PathRecord) record;
                if (pathRecord.path == path || pathRecord.path.getFileName().equals(path.getFileName())) {
                    return pathRecord;
                }
            }
        }
        return null;
    }
}
