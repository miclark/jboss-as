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

/**
 * Provides metadata about and access to the data collected by a {@link JdrReportCollector}.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 * @author Mike M. Clark
 */
public class JdrReport {
    private long startTimestamp;
    private String reportLocation;

    public JdrReport(long timestamp) {
        this.startTimestamp = timestamp;
    }

    /**
     * Indicates the time the JDR report collection was initiated.
     */
    public long getStartTimestamp() {
        return startTimestamp;
    }

    /**
     * Indicates the location of the generated JDR report.
     *
     * @return location of report.
     */
    public String getReportLocation() {
        return reportLocation;
    }

    public void setReportLocation(String reportLocation) {
        this.reportLocation = reportLocation;
    }
}
