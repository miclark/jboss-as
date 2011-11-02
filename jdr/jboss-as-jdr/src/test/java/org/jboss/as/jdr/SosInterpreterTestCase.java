/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.jdr;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link SosInterpreter} class.
 *
 * @author Jesse Jaggars
 * @author Mike M. Clark
 */
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
