package org.kohsuke.file_leak_detector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Kohsuke Kawaguchi
 */
public class NIOFilesDemo {
    public static void main(String[] args) throws IOException {
    	// unclosed element on purpose here
    	@SuppressWarnings({ "resource", "unused" })
		BufferedReader reader = Files.newBufferedReader(Paths.get("pom.xml"), Charset.defaultCharset());
    	//assertNotNull(reader);

    	@SuppressWarnings({ "resource", "unused" })
		InputStream stream = Files.newInputStream(Paths.get("pom.xml"));
    	//assertNotNull(stream);

    	System.out.println("Should have 2 unclosed files now...");
    }
}
