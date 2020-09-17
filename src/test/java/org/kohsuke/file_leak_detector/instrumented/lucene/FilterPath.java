package org.kohsuke.file_leak_detector.instrumented.lucene;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.function.Consumer;

public class FilterPath implements Path {

    @Override
    public void forEach(Consumer<? super Path> action) {

    }

    protected final Path delegate;

    protected final FileSystem fileSystem;

    public FilterPath(Path delegate, FileSystem fileSystem) {
        this.delegate = delegate;
        this.fileSystem = fileSystem;
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return delegate.isAbsolute();
    }

    @Override
    public Path getRoot() {
        Path root = delegate.getRoot();
        if (root == null) {
            return null;
        }
        return wrap(root);
    }

    @Override
    public Path getFileName() {
        Path fileName = delegate.getFileName();
        if (fileName == null) {
            return null;
        }
        return wrap(fileName);
    }

    @Override
    public Path getParent() {
        Path parent = delegate.getParent();
        if (parent == null) {
            return null;
        }
        return wrap(parent);
    }

    @Override
    public int getNameCount() {
        return delegate.getNameCount();
    }

    @Override
    public Path getName(int index) {
        return wrap(delegate.getName(index));
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return wrap(delegate.subpath(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path other) {
        return delegate.startsWith(toDelegate(other));
    }

    @Override
    public boolean startsWith(String other) {
        return delegate.startsWith(other);
    }

    @Override
    public boolean endsWith(Path other) {
        return delegate.endsWith(toDelegate(other));
    }

    @Override
    public boolean endsWith(String other) {
        return delegate.startsWith(other);
    }

    @Override
    public Path normalize() {
        return wrap(delegate.normalize());
    }

    @Override
    public Path resolve(Path other) {
        return wrap(delegate.resolve(toDelegate(other)));
    }

    @Override
    public Path resolve(String other) {
        return wrap(delegate.resolve(other));
    }

    @Override
    public Path resolveSibling(Path other) {
        return wrap(delegate.resolveSibling(toDelegate(other)));
    }

    @Override
    public Path resolveSibling(String other) {
        return wrap(delegate.resolveSibling(other));
    }

    @Override
    public Path relativize(Path other) {
        return wrap(delegate.relativize(toDelegate(other)));
    }

    @Override
    public URI toUri() {
        return delegate.toUri();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public Path toAbsolutePath() {
        return wrap(delegate.toAbsolutePath());
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return wrap(delegate.toRealPath(options));
    }

    @Override
    public File toFile() {
        return delegate.toFile();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers)
            throws IOException {
        return delegate.register(watcher, events, modifiers);
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        return delegate.register(watcher, events);
    }

    @Override
    public Iterator<Path> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Path other) {
        return delegate.compareTo(toDelegate(other));
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FilterPath other = (FilterPath) obj;
        if (delegate == null) {
            if (other.delegate != null) {
                return false;
            }
        } else if (!delegate.equals(other.delegate)) {
            return false;
        }
        if (fileSystem == null) {
            return other.fileSystem == null;
        } else {
            return fileSystem.equals(other.fileSystem);
        }
    }

    protected Path wrap(Path other) {
        return new FilterPath(other, fileSystem);
    }

    protected Path toDelegate(Path path) {
        if (path instanceof FilterPath) {
            FilterPath fp = (FilterPath) path;
            if (fp.fileSystem != fileSystem) {
                throw new ProviderMismatchException(
                        "mismatch, expected: " + fileSystem.provider().getClass() + ", got: " + fp.fileSystem.provider()
                                .getClass());
            }
            return fp.delegate;
        } else {
            throw new ProviderMismatchException("mismatch, expected: FilterPath, got: " + path.getClass());
        }
    }
}
