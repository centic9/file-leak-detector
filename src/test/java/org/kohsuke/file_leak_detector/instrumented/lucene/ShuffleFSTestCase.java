package org.kohsuke.file_leak_detector.instrumented.lucene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.file_leak_detector.ActivityListener;
import org.kohsuke.file_leak_detector.Listener;
import org.kohsuke.file_leak_detector.Listener.FileRecord;

/**
 * Test for a custom implementation of FileSystemProvider and DirectoryStream
 * that are used by Apache Lucene. They require special handling to not report
 * file-handle leaks.
 * <br/>
 * Make sure to run this test with injected file-leak-detector as otherwise
 * tests will fail.
 */
public class ShuffleFSTestCase {
	private static final StringWriter output = new StringWriter();
	private Path tempDir;
	private Object obj;
	private FileSystem fileSystem;

	private final ActivityListener listener = new ActivityListener() {
		@Override
		public void open(Object obj, File file) {
			ShuffleFSTestCase.this.obj = obj;
		}

		@Override
		public void open(Object obj, Path file) {
			ShuffleFSTestCase.this.obj = obj;
		}

		@Override
		public void openSocket(Object obj) {
			ShuffleFSTestCase.this.obj = obj;
		}

		@Override
		public void close(Object obj) {
			ShuffleFSTestCase.this.obj = obj;
		}

		@Override
		public void fd_open(Object obj) {
			ShuffleFSTestCase.this.obj = obj;
		}
	};

	@BeforeAll
	public static void setup() {
		assertTrue(Listener.isAgentInstalled(),
				"This test expects the Java Agent to be installed via commandline options");
		Listener.TRACE = new PrintWriter(output);
	}

	@BeforeEach
	public void registerListener() {
		ActivityListener.LIST.add(listener);
	}

	@AfterEach
	public void unregisterListener() {
		ActivityListener.LIST.remove(listener);
	}

	@BeforeEach
	public void prepareOutput() throws Exception {
		FileSystem fs = FileSystems.getDefault();
		fs = new ShuffleFS(new FilterFileSystemProvider("extras://", fs).getFileSystem(null), 1L).getFileSystem(null);
		fileSystem = fs.provider().getFileSystem(URI.create("file:///"));

		tempDir = fileSystem.getPath(System.getProperty("java.io.tmpdir"));

		Files.createDirectories(tempDir);
	}

	@Test
	public void testConstants() throws Throwable {
		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tempDir);

		assertNotNull(findFileRecord(tempDir.toFile()), "No file record for file " + tempDir + " found");

		assertTrue(obj instanceof DirectoryStream,
				"Did not have the expected type of 'marker' object: " + obj);

		directoryStream.close();

		assertNull(findFileRecord(tempDir.toFile()), "File record for file " + tempDir + " not removed");

		fileSystem.close();

		String traceOutput = output.toString();
		assertTrue(traceOutput.contains("Opened " + tempDir));
		assertTrue(traceOutput.contains("Closed " + tempDir));
	}

	@Test
	public void testWalkFileTree() throws IOException {
		Path path = Files.walkFileTree(tempDir, new NameFileFilter("123"));
		assertEquals(tempDir, path);

		assertNull(findFileRecord(tempDir.toFile()), "File record for file " + tempDir + " not removed");

		fileSystem.close();

		String traceOutput = output.toString();
		assertTrue(traceOutput.contains("Opened " + tempDir));
		assertTrue(traceOutput.contains("Closed " + tempDir));
	}

	private static FileRecord findFileRecord(File file) {
		for (Listener.Record record : Listener.getCurrentOpenFiles()) {
			if (record instanceof FileRecord) {
				FileRecord fileRecord = (FileRecord) record;
				if (fileRecord.file == file || fileRecord.file.getName().equals(file.getName())) {
					return fileRecord;
				}
			}
		}
		return null;
	}
}
