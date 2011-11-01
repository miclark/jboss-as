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

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.logging.Logger;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import java.net.URL;
import java.util.Date;

/**
 * Wraps up the access to the jython interpreter to encapsulate its use
 * for running sosreport.
 *
 * @author Mike M. Clark
 */
public class SosInterpreter {
    private static final Logger log = Logger.getLogger(JdrReportService.class);

    private String jbossHomeDir = System.getProperty("jboss.home.dir");
    private String reportLocationDir = System.getProperty("user.dir");
    private ModelControllerClient controllerClient = null;

    public JdrReport collect() {
        log.info("Collecting jdr.");
        Date startTime = new Date();

        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.exec("import sys");
        interpreter.exec("import shlex");

        String pyLocation = getPythonScriptLocation();
        log.debug("Location of py script: " + pyLocation);

        String homeDir = getJbossHomeDir();
        String locationDir = getReportLocationDir();

        PyObject report = null;
        try {
            interpreter.exec("sys.path.append(\"" + pyLocation + "\")");

            // If we have a controller client, use it to
            // get runtime information.
            if (controllerClient != null) {
                interpreter.exec("import sos");
                interpreter.set("controller_client_proxy",
                        new ModelControllerClientProxy(controllerClient));
                interpreter.exec("sos.controllerClient = controller_client_proxy");
            }

            interpreter.exec("from sos.sosreport import main");
            interpreter.exec("args = shlex.split('-k eap6.home=" + homeDir + " --tmp-dir=" + locationDir +" -o eap6 --batch --report --compression-type=zip --silent')");
            interpreter.exec("reportLocation = main(args)");
            report = interpreter.get("reportLocation");
            interpreter.cleanup();
        } catch (Throwable t) {
            Py.printException(t);
            interpreter.cleanup();
        }

        Date endTime = new Date();

        JdrReport result = new JdrReport();
        result.setStartTime(startTime);
        result.setEndTime(endTime);
        result.setLocation(report.asString());
        return result;
    }

    /**
     * Sets the location for where the report archive will be created.
     *
     * @param dir location of generated report archive
     */
    public void setReportLocationDir(String dir) {
        reportLocationDir = dir;
    }

    /**
     * Location for the generated report archive.  The default value
     * is the current working directory as specified in the <code>user.dir</code>
     * System property.
     *
     * @return location for the archive
     */
    public String getReportLocationDir() {
        return reportLocationDir;
    }

    public void setControllerClient(ModelControllerClient controllerClient) {
       this.controllerClient = controllerClient;
    }

    /**
     * Location of the JBoss distribution.
     *
     * @return JBoss home location.  If not set the value of the <code>jboss.home.dir</code>
     * System property is used.  If this value is not set, the current working directory,
     * as specified by the <code>user.dir</code> System property is used.
     */
    public String getJbossHomeDir() {
        if (jbossHomeDir == null) {
            jbossHomeDir = System.getProperty("user.dir");
        }
        return jbossHomeDir;
    }

    public void setJbossHomeDir(String jbossHomeDir) {
        this.jbossHomeDir = jbossHomeDir;
    }


    public static String getPath(String path) {
        return path.split(":", 2)[1].split("!")[0];
    }

    private String getPythonScriptLocation() {
        URL pyURL = this.getClass().getClassLoader().getResource("sos");
        return SosInterpreter.getPath(pyURL.getPath());
    }

}
