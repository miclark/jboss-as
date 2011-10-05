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

import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.threads.Element;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

public final class JdrReportSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>,
        XMLElementWriter<SubsystemMarshallingContext> {

    static final JdrReportSubsystemParser INSTANCE = new JdrReportSubsystemParser();

    public static JdrReportSubsystemParser getInstance() {
        return INSTANCE;
    }

    public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {

       // final ModelNode address = new ModelNode();
       // address.add(SUBSYSTEM, JdrReportExtension.SUBSYSTEM_NAME);
       // address.protect();

        ParseUtils.requireNoContent(reader);

        //  TODO: Not sure why Util.getEmptyOperation() not used here.
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).set(SUBSYSTEM, JdrReportExtension.SUBSYSTEM_NAME);
        list.add(subsystem);



        //readSubsystemRootElementAttributes(reader, subsystem);

        /* -- don't need this unless we have elements
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (org.jboss.as.jdr.Namespace.forUri(reader.getNamespaceURI())) {
                case JDR_1_0: {
                    readJdrElements(reader, list, address);
                    break;
                }
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }
        */
    }

    private void readSubsystemRootElementAttributes(XMLExtendedStreamReader reader, ModelNode subsystem) throws XMLStreamException {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            ParseUtils.requireNoNamespaceAttribute(reader, i);
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case ENABLED: {
                    subsystem.get(CommonAttributes.ENABLED).set(value);
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
        }

        // Attribute is required.  Throw an exception if not defined.
        //if (!subsystem.hasDefined(CommonAttributes.ENABLED)) {
        //    throw ParseUtils.missingRequired(reader, EnumSet.of(Attribute.ENABLED));
        //}
    }

    private void readJdrElements(final XMLExtendedStreamReader reader, final List<ModelNode> list, final ModelNode address)
            throws XMLStreamException {
        String name = null;
        final Element element = Element.forName(reader.getLocalName());
        switch (element) {
            default: {
                // TODO remove this whole method unless there are valid child elements
                throw unexpectedElement(reader);
            }
        }
    }

    /** {@inheritDoc} */
    public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context)
            throws XMLStreamException {

        context.startSubsystemElement(org.jboss.as.jdr.Namespace.CURRENT.getUriString(), false);
        ModelNode node = context.getModelNode();
        if (node.isDefined()) {
            writeJdrElements(writer, node);
        }
        writer.writeEndElement();
    }

    /* TODO: why public? */
    public void writeJdrElements(final XMLExtendedStreamWriter writer, ModelNode node) throws XMLStreamException {
        writer.writeAttribute(Attribute.ENABLED.getLocalName(), node.require(CommonAttributes.ENABLED).toString());
    }
}
