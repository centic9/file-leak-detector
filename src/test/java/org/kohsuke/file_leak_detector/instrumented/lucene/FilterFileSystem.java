package org.kohsuke.file_leak_detector.instrumented.lucene;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Objects;
import java.util.Set;

public class FilterFileSystem extends FileSystem {

    protected final FilterFileSystemProvider parent;

    protected final FileSystem delegate;

    public FilterFileSystem(FilterFileSystemProvider parent, FileSystem delegate) {
        this.parent = Objects.requireNonNull(parent);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public FileSystemProvider provider() {
        return parent;
    }

    @Override
    public void close() throws IOException {
        if (delegate == FileSystems.getDefault()) {
            // you can't close the default provider!
            parent.onClose();
        } else {
            //noinspection unused
            try (FileSystem d = delegate) {
                parent.onClose();
            }
        }
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public boolean isReadOnly() {
        return delegate.isReadOnly();
    }

    @Override
    public String getSeparator() {
        return delegate.getSeparator();
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return delegate.supportedFileAttributeViews();
    }

    @Override
    public Path getPath(String first, String... more) {
        return new FilterPath(delegate.getPath(first, more), this);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        final PathMatcher matcher = delegate.getPathMatcher(syntaxAndPattern);
        return path -> {
            if (path instanceof FilterPath) {
                return matcher.matches(((FilterPath) path).delegate);
            }
            return false;
        };
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return delegate.getUserPrincipalLookupService();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return delegate.newWatchService();
    }
}
