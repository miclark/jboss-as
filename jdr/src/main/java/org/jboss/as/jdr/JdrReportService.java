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

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.as.server.Services;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.threads.JBossThreadFactory;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import java.io.IOException;
import java.security.AccessController;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Service that provides a {@link JdrReportCollector}.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public class JdrReportService implements JdrReportCollector, Service<JdrReportCollector> {

    public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("jdr", "collector");

    private static final Logger log = Logger.getLogger(JdrReportService.class);

    public static ServiceController<JdrReportCollector> addService(final ServiceTarget target, final ServiceVerificationHandler verificationHandler) {

        JdrReportService service = new JdrReportService();
        return target.addService(SERVICE_NAME, service)
                .addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, service.serverEnvironmentValue)
                .addDependency(Services.JBOSS_SERVER_CONTROLLER, ModelController.class, service.modelControllerValue)
                .addListener(verificationHandler)
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();
    }

    private final InjectedValue<ServerEnvironment> serverEnvironmentValue = new InjectedValue<ServerEnvironment>();
    private final InjectedValue<ModelController> modelControllerValue = new InjectedValue<ModelController>();
    private ExecutorService executorService;
    private ServerEnvironment serverEnvironment;
    private ModelControllerClient controllerClient;

    public JdrReport collect(JdrReportRequest request) {
        // Use the ServerEnvironment to find location of files, use the
        // ModelControllerClient to query in-memory state
        log.info("Collecting jdr.");

        ModelNode simpleRequest = new ModelNode();
        simpleRequest.get("operation").set("read-operation-names");
        simpleRequest.get("address").add("subsystem", "web");
        try {
            ModelNode response = controllerClient.execute(simpleRequest);
            log.info("Response from " + simpleRequest.toJSONString(true) + ": " + response.toJSONString(true));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Create an instance of the PythonInterpreter
        PythonInterpreter interpreter = new PythonInterpreter();

        // Do some simple python things as placeholder for
        // executing the jdr scripts.
        interpreter.exec("import sys");
        interpreter.exec("print sys");
        interpreter.exec("print sys.path");

        // Set variable values within the PythonInterpreter instance
        interpreter.set("a", new PyInteger(42));
        interpreter.exec("print a");
        interpreter.exec("x = 2+2");

        // Obtain the value of an object from the PythonInterpreter and store it
        // into a PyObject.
        PyObject x = interpreter.get("x");
        log.info("Python 2+2 = " + x);

        return new JdrReport(123456);
    }

    public void start(StartContext context) throws StartException {
        final ThreadFactory threadFactory = new JBossThreadFactory(new ThreadGroup("JdrReportCollector-threads"), Boolean.FALSE, null, "%G - %t", null, null, AccessController.getContext());
        // TODO give some more thought to what concurrency characteristics are desirable
        final ExecutorService executorService = Executors.newCachedThreadPool(threadFactory);
        serverEnvironment = serverEnvironmentValue.getValue();
        controllerClient = modelControllerValue.getValue().createClient(executorService);
    }

    public void stop(StopContext context) {
        executorService.shutdownNow();
    }

    public JdrReportService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }
}
