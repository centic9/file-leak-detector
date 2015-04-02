package org.kohsuke.file_leak_detector;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class FilesLinesDemo {
    public static void main(String[] args) throws IOException {
    	// unclosed element on purpose here
/*    	@SuppressWarnings({ "resource", "unused" })
		Stream<String> lines = Files.lines(Paths.get("pom.xml"));*/
    	//assertNotNull(lines);

    	System.out.println("Done");
    }
}
