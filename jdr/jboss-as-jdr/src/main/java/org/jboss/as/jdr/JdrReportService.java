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
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.as.server.Services;
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

import java.security.AccessController;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * Service that provides a {@link JdrReportCollector}.
 *
 * @author Brian Stansberry
 * @author Mike M. Clark
 * @author Jesse Jaggars
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

    /**
     * Collect a JDR report when run outside the Application Server.
     */
    public JdrReport standaloneCollect() throws OperationFailedException {
        SosInterpreter interpreter = new SosInterpreter();
        return interpreter.collect();
    }

    /**
     * Collect a JDR report.
     */
    public JdrReport collect() throws OperationFailedException {
        SosInterpreter interpreter = new SosInterpreter();
        serverEnvironment = serverEnvironmentValue.getValue();
        interpreter.setJbossHomeDir(serverEnvironment.getHomeDir().getAbsolutePath());
        interpreter.setReportLocationDir(serverEnvironment.getServerTempDir().getAbsolutePath());
        interpreter.setControllerClient(controllerClient);
        return interpreter.collect();
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
