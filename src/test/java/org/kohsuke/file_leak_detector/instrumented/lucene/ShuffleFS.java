package org.kohsuke.file_leak_detector.instrumented.lucene;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * This code is originally used by Apache Lucene and used here
 * to reproduce a false-positive file-handle leak when custom
 * filesystems are used
 * <br/>
 * <br/>
 * Gives an unpredictable, but deterministic order to directory listings.
 * <p>
 * This can be useful if for instance, you have build servers on
 * linux but developers are using macs.
 */
public class ShuffleFS extends FilterFileSystemProvider {
	final long seed;

	/**
	 * Create a new instance, wrapping {@code delegate}.
	 */
	public ShuffleFS(FileSystem delegate, long seed) {
		super("shuffle://", delegate);
		this.seed = seed;
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
		try (DirectoryStream<Path> stream = super.newDirectoryStream(dir, filter)) {
			// read complete directory listing
			List<Path> contents = new ArrayList<>();
			for (Path path : stream) {
				contents.add(path);
			}
			// sort first based only on filename
			Collections.sort(contents, (path1, path2) -> path1.getFileName().toString().compareTo(path2.getFileName().toString()));
			// sort based on current class seed
			Collections.shuffle(contents, new Random(seed));
			return new DirectoryStream<Path>() {
				@Override
				public Iterator<Path> iterator() {
					return contents.iterator();
				}
				@Override
				public void close() throws IOException {}
			};
		}
	}
}
