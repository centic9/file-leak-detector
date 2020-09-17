package org.kohsuke.file_leak_detector.instrumented.lucene;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class FilterFileSystemProvider extends FileSystemProvider {

    protected final FileSystemProvider delegate;
    protected FileSystem fileSystem;
    protected final String scheme;

    public FilterFileSystemProvider(String scheme, FileSystem delegateInstance) {
        this.scheme = Objects.requireNonNull(scheme);
        Objects.requireNonNull(delegateInstance);
        this.delegate = delegateInstance.provider();
        this.fileSystem = new FilterFileSystem(this, delegateInstance);
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) {
        if (fileSystem == null) {
            throw new IllegalStateException("subclass did not initialize singleton filesystem");
        }
        return fileSystem;
    }

    @Override
    public FileSystem newFileSystem(Path path, Map<String, ?> env) {
        if (fileSystem == null) {
            throw new IllegalStateException("subclass did not initialize singleton filesystem");
        }
        return fileSystem;
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        if (fileSystem == null) {
            throw new IllegalStateException("subclass did not initialize singleton filesystem");
        }
        return fileSystem;
    }

    @Override
    public Path getPath(URI uri) {
        if (fileSystem == null) {
            throw new IllegalStateException("subclass did not initialize singleton filesystem");
        }
        Path path = delegate.getPath(uri);
        return new FilterPath(path, fileSystem);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        delegate.createDirectory(toDelegate(dir), attrs);
    }

    @Override
    public void delete(Path path) throws IOException {
        delegate.delete(toDelegate(path));
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        delegate.copy(toDelegate(source), toDelegate(target), options);
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        delegate.move(toDelegate(source), toDelegate(target), options);
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return delegate.isSameFile(toDelegate(path), toDelegate(path2));
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return delegate.isHidden(toDelegate(path));
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return delegate.getFileStore(toDelegate(path));
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        delegate.checkAccess(toDelegate(path), modes);
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return delegate.getFileAttributeView(toDelegate(path), type, options);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        return delegate.readAttributes(toDelegate(path), type, options);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return delegate.readAttributes(toDelegate(path), attributes, options);
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        delegate.setAttribute(toDelegate(path), attribute, value, options);
    }

    @Override
    public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
        return delegate.newInputStream(toDelegate(path), options);
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
        return delegate.newOutputStream(toDelegate(path), options);
    }

    @Override
    public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException {
        return delegate.newFileChannel(toDelegate(path), options, attrs);
    }

    @Override
    public AsynchronousFileChannel newAsynchronousFileChannel(Path path,
            Set<? extends OpenOption> options,
            ExecutorService executor,
            FileAttribute<?>... attrs) throws IOException {
        return delegate.newAsynchronousFileChannel(toDelegate(path), options, executor, attrs);
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException {
        return delegate.newByteChannel(toDelegate(path), options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, final DirectoryStream.Filter<? super Path> filter)
            throws IOException {
        return new FilterDirectoryStream(delegate.newDirectoryStream(toDelegate(dir), filter), fileSystem);
    }

    @Override
    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException {
        delegate.createSymbolicLink(toDelegate(link), toDelegate(target), attrs);
    }

    @Override
    public void createLink(Path link, Path existing) throws IOException {
        delegate.createLink(toDelegate(link), toDelegate(existing));
    }

    @Override
    public boolean deleteIfExists(Path path) throws IOException {
        return delegate.deleteIfExists(toDelegate(path));
    }

    @Override
    public Path readSymbolicLink(Path link) throws IOException {
        return delegate.readSymbolicLink(toDelegate(link));
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

    /**
     * Override to trigger some behavior when the filesystem is closed.
     * <p>
     * This is always called for each FilterFileSystemProvider in the chain.
     */
    protected void onClose() {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + delegate + ")";
    }
}
