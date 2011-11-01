package org.jboss.as.jdr;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class SosInterpreterTestCase {

    @Test
    public void testUnixPath() {
        String path = "file:/path/to/thing";
        assertEquals("/path/to/thing", SosInterpreter.getPath(path));
    }

    @Test
    public void testWindowsPath() {
        String path = "file:C:\\path\\to\\thing";
        assertEquals("C:\\path\\to\\thing", SosInterpreter.getPath(path));
    }

    @Test
    public void testUnixPathWithJar() {
        String path = "file:/path/to/thing.jar!/path/inside/jar";
        assertEquals("/path/to/thing.jar", SosInterpreter.getPath(path));
    }

    @Test
    public void testWindowsPathWithJar() {
        String path = "file:C:\\path\\to\\thing.jar!/path/inside/jar";
        assertEquals("C:\\path\\to\\thing.jar", SosInterpreter.getPath(path));
    }
}
