package org.kohsuke.file_leak_detector.instrumented.lucene;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;

public class FilterDirectoryStream implements DirectoryStream<Path> {

    protected final DirectoryStream<Path> delegate;

    protected final FileSystem fileSystem;

    public FilterDirectoryStream(DirectoryStream<Path> delegate, FileSystem fileSystem) {
        this.delegate = Objects.requireNonNull(delegate);
        this.fileSystem = Objects.requireNonNull(fileSystem);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public Iterator<Path> iterator() {
        return delegate.iterator();
    }
}
