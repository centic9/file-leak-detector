package org.kohsuke.file_leak_detector;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class FilesLinesDemo {
    public static void main(String[] args) throws IOException {
    	// unclosed element on purpose here
    	/* Can only compile on Java 8 and newer...
    	@SuppressWarnings("resource")
		Stream<String> lines = Files.lines(Paths.get("pom.xml"));
    	assertNotNull(lines);*/

    	System.out.println("Done");
    }
}
