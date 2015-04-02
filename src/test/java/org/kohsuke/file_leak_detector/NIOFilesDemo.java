package org.kohsuke.file_leak_detector;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author Kohsuke Kawaguchi
 */
public class NIOFilesDemo {
    public static void main(String[] args) throws IOException {
    	// unclosed element on purpose here
    	@SuppressWarnings({ "resource", "unused" })
		BufferedReader reader = Files.newBufferedReader(Paths.get("pom.xml"), Charset.defaultCharset());
    	//assertNotNull(reader);
    	reader.close();

    	@SuppressWarnings({ "resource", "unused" })
		InputStream stream = Files.newInputStream(Paths.get("pom.xml"));
    	//assertNotNull(stream);
    	stream.close();

    	@SuppressWarnings({ "resource", "unused" })
    	OutputStream out = Files.newOutputStream(Paths.get(File.createTempFile("NIOFilesDemo", ".tmp").getAbsolutePath()), StandardOpenOption.WRITE);

    	@SuppressWarnings({ "resource", "unused" })
    	DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get("."));
    	
    	System.out.println("Should have 4 unclosed files now...");
    }
}
