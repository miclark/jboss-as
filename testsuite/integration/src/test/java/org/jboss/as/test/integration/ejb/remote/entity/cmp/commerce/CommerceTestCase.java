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
package org.jboss.as.test.integration.ejb.remote.entity.cmp.commerce;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.test.integration.ejb.entity.cmp.AbstractCmpTest;
import org.jboss.as.test.integration.ejb.entity.cmp.CmpTestRunner;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CommerceTestCase extends AbstractCmpTest {
    @Deployment
    public static Archive<?> deploy() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "cmp-commerce.jar");
        jar.addPackage(CommerceTestCase.class.getPackage());
        jar.addAsManifestResource("ejb/remote/entity/cmp/commerce/ejb-jar.xml", "ejb-jar.xml");
        jar.addAsManifestResource("ejb/remote/entity/cmp/commerce/jboss.xml", "jboss.xml");
        jar.addAsManifestResource("ejb/remote/entity/cmp/commerce/jbosscmp-jdbc.xml", "jbosscmp-jdbc.xml");
        AbstractCmpTest.addDeploymentAssets(jar);
        return jar;
    }

    private OrderHome getOrderHome() {
        try {
            return (OrderHome) iniCtx.lookup("java:module/OrderEJB!" + OrderHome.class.getName());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception in getOrderHome: " + e.getMessage());
        }
        return null;
    }

    private LineItemHome getLineItemHome() {
        try {
            return (LineItemHome) iniCtx.lookup("java:module/LineItemEJB!" + LineItemHome.class.getName());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception in getLineItemHome: " + e.getMessage());
        }
        return null;
    }

    private ProductHome getProductHome() {
        try {
            return (ProductHome) iniCtx.lookup("java:module/ProductEJB!" + ProductHome.class.getName());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception in getProductHome: " + e.getMessage());
        }
        return null;
    }

    private AddressHome getAddressHome() {
        try {
            return (AddressHome) iniCtx.lookup("java:module/AddressEJB!" + AddressHome.class.getName());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception in getAddressHome: " + e.getMessage());
        }
        return null;
    }

    @Test
    public void test_setInPostCreate() throws Exception {

        OrderHome oh = getOrderHome();
        LineItemHome lh = getLineItemHome();

        LineItem l = lh.create();

        Order o = oh.create();
        l = lh.createWithOrderId(o.getOrdernumber());
        assertEquals(l.getOrderNumber(), o.getOrdernumber());
    }

    @Test
    public void test_dvo() throws Exception {
        OrderHome orderHome = getOrderHome();

        Order order = orderHome.create();
        Long ordernumber = order.getOrdernumber();

        // setup credit card
        Card creditCard = new Card();
        creditCard.setType(Card.VISA);
        creditCard.setCardNumber("1111-2222-3333-4444");
        creditCard.setCardHolder(new FormalName("Dain", 'S', "Sundstrom"));
        creditCard.setBillingZip(55414);

        order.setCreditCard(creditCard);
        assertEquals(order.getCreditCard(), creditCard);

        order = null;
        order = orderHome.findByPrimaryKey(ordernumber);
        assertEquals(order.getCreditCard(), creditCard);
    }

    @Test
    public void test_getOrdersShippedToCA() throws Exception {
        OrderHome orderHome = getOrderHome();
        AddressHome addressHome = getAddressHome();

        Order orderCA1 = orderHome.create();
        Address shipCA1 = addressHome.create();
        shipCA1.setState("CA");
        orderCA1.setShippingAddressId(shipCA1.getId());

        Order orderCA2 = orderHome.create();
        Address shipCA2 = addressHome.create();
        shipCA2.setState("CA");
        orderCA2.setShippingAddressId(shipCA2.getId());

        Order orderMN = orderHome.create();
        Address shipMN = addressHome.create();
        shipMN.setState("MN");
        orderMN.setShippingAddressId(shipMN.getId());

        Set s = orderMN.getOrdersShippedToCA();
        System.out.println(s);
        assertTrue(s.contains(orderCA1));
        assertTrue(s.contains(orderCA2));
        assertTrue(!s.contains(orderMN));
        assertTrue(s.size() == 2);

        s = orderMN.getOrdersShippedToCA2();
        System.out.println(s);
        assertTrue(s.contains(orderCA1));
        assertTrue(s.contains(orderCA2));
        assertTrue(!s.contains(orderMN));
        assertTrue(s.size() == 2);
    }

    @Test
    public void test_getStatesShipedTo() throws Exception {
        OrderHome orderHome = getOrderHome();
        AddressHome addressHome = getAddressHome();

        Order orderCA1 = orderHome.create();
        Address shipCA1 = addressHome.create();
        shipCA1.setState("CA");
        orderCA1.setShippingAddressId(shipCA1.getId());

        Order orderCA2 = orderHome.create();
        Address shipCA2 = addressHome.create();
        shipCA2.setState("CA");
        orderCA2.setShippingAddressId(shipCA2.getId());

        Order orderMN = orderHome.create();
        Address shipMN = addressHome.create();
        shipMN.setState("MN");
        orderMN.setShippingAddressId(shipMN.getId());

        System.out.println("orderMN.getStatesShipedTo();");
        Collection c = orderMN.getStatesShipedTo();
        System.out.println(c);
        assertTrue(c.contains("CA"));
        assertTrue(c.contains("MN"));
        assertTrue(c.size() == 3);

        c = orderMN.getStatesShipedTo2();
        System.out.println(c);
        assertTrue(c.contains("CA"));
        assertTrue(c.contains("MN"));
        assertTrue(c.size() == 3);
    }

    @Test
    public void test_getAddressesInCA() throws Exception {
        OrderHome orderHome = getOrderHome();
        AddressHome addressHome = getAddressHome();

        Order orderCA1 = orderHome.create();
        Address shipCA1 = addressHome.create();
        shipCA1.setState("CA");
        orderCA1.setShippingAddressId(shipCA1.getId());
        Address billCA1 = addressHome.create();
        billCA1.setState("CA");
        orderCA1.setBillingAddressId(billCA1.getId());

        Order orderCA2 = orderHome.create();
        Address shipCA2 = addressHome.create();
        shipCA2.setState("CA");
        orderCA2.setShippingAddressId(shipCA2.getId());
        orderCA2.setBillingAddressId(shipCA2.getId());

        Order orderMN = orderHome.create();
        Address shipMN = addressHome.create();
        shipMN.setState("MN");
        orderMN.setShippingAddressId(shipMN.getId());

        Collection c = orderMN.getAddressesInCA();
        System.out.println(c);
        assertTrue(c.contains(shipCA1));
        assertTrue(c.contains(shipCA2));
        assertTrue(c.contains(billCA1));
        assertTrue(c.size() == 3);

        c = orderMN.getAddressesInCA2();
        System.out.println(c);
        assertTrue(c.contains(shipCA1));
        assertTrue(c.contains(shipCA2));
        assertTrue(c.contains(billCA1));
        assertTrue(c.size() == 3);
    }


    @Test
    public void test_findDoubleJoin() throws Exception {
        OrderHome orderHome = getOrderHome();
        LineItemHome lineItemHome = getLineItemHome();

        Order order1 = orderHome.create();
        LineItem line1 = lineItemHome.create();
        line1.setQuantity(1);
        order1.addLineItemId(line1.getId());
        LineItem line2 = lineItemHome.create();
        line2.setQuantity(2);
        order1.addLineItemId(line2.getId());

        Order order2 = orderHome.create();
        LineItem line3 = lineItemHome.create();
        line3.setQuantity(2);
        order2.addLineItemId(line3.getId());

        Collection c = orderHome.findDoubleJoin(1, 2);
        System.out.println(c);
        assertTrue(c.contains(order1));
        assertTrue(!c.contains(order2));
        assertTrue(c.size() == 1);
    }

    @Test
    public void testIsIdentical() throws Exception {
        OrderHome orderHome = getOrderHome();
        Order order = orderHome.create(new Long(111));

        LineItemHome liHome = getLineItemHome();
        LineItem lineItem = liHome.create(new Long(111));

        assertTrue("order.isIdentical(order)", order.isIdentical(order));
        assertTrue("!order.isIdentical(lineItem)", !order.isIdentical(lineItem));
    }

    @Test
    public void testOverloadedEjbSelects() throws Exception {
        getAddressHome().selectAddresses("");
    }

    @After
    public void tearDownEjb() throws Exception {
        deleteAllOrders(getOrderHome());
        deleteAllLineItems(getLineItemHome());
        deleteAllProducts(getProductHome());
        deleteAllAddresses(getAddressHome());
    }

    public void deleteAllOrders(OrderHome orderHome) throws Exception {
        Iterator orders = orderHome.findAll().iterator();
        while (orders.hasNext()) {
            Order order = (Order) orders.next();
            order.remove();
        }
    }

    public void deleteAllLineItems(LineItemHome lineItemHome) throws Exception {
        Iterator lineItems = lineItemHome.findAll().iterator();
        while (lineItems.hasNext()) {
            LineItem lineItem = (LineItem) lineItems.next();
            lineItem.remove();
        }
    }

    public void deleteAllProducts(ProductHome productHome) throws Exception {
        Iterator products = productHome.findAll().iterator();
        while (products.hasNext()) {
            Product product = (Product) products.next();
            product.remove();
        }
    }

    public void deleteAllAddresses(AddressHome addressHome) throws Exception {
        Iterator addresses = addressHome.findAll().iterator();
        while (addresses.hasNext()) {
            Address address = (Address) addresses.next();
            address.remove();
        }
    }
}
