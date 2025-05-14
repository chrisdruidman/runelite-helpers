package com.osrshelper.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Custom classloader that loads patched classes from a patch directory or jar before delegating to the parent.
 */
public class PatchedClassLoader extends URLClassLoader {
    private final Path patchDir;
    private final Logger logger;

    public PatchedClassLoader(URL[] urls, ClassLoader parent, Path patchDir, Logger logger) {
        super(urls, parent);
        this.patchDir = patchDir;
        this.logger = logger;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Convert class name to path
        String classPath = name.replace('.', '/') + ".class";
        Path patchedClass = patchDir.resolve(classPath);
        if (Files.exists(patchedClass)) {
            try (InputStream in = Files.newInputStream(patchedClass)) {
                byte[] bytes = in.readAllBytes();
                logger.info("[PatchedClassLoader] Loading patched class: " + name);
                return defineClass(name, bytes, 0, bytes.length);
            } catch (IOException e) {
                logger.warning("[PatchedClassLoader] Failed to load patched class: " + name + ", error: " + e.getMessage());
            }
        }
        // Not patched, delegate to parent
        logger.fine("[PatchedClassLoader] Delegating to parent for class: " + name);
        return super.findClass(name);
    }
}
