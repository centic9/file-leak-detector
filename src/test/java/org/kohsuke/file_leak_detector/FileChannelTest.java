package org.kohsuke.file_leak_detector;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.kohsuke.asm5.ClassReader;
import org.kohsuke.asm5.util.TraceClassVisitor;

public class FileChannelTest {
    public static void main(String[] args) throws IOException {
    	// unclosed element on purpose here
    	@SuppressWarnings({ "resource", "unused" })
    	FileChannel channel = FileChannel.open(Paths.get("pom.xml"), StandardOpenOption.READ);
    	//assertNotNull(channel);
    	//channel.close();

    	System.out.println("Should have 1 unclosed file now...");

        //printClass("java.nio.channels.FileChannel");
        //printClass(FileChannelTest.class.getCanonicalName());
    	
    	//Listener.dump(System.out);
    }

	public static void printClass(String clazz) throws IOException {
		int flags = ClassReader.SKIP_DEBUG;
        ClassReader cr;
        cr = new ClassReader(clazz);
        cr.accept(new TraceClassVisitor(new PrintWriter(System.out)), flags);
	}

    public Object testMethod() {
    	Object t = testMethod2();
    	return t;
    }

    public Object testMethod2() {
    	return "test";
    }
}
