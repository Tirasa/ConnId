/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.common.objects;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.testng.annotations.Test;

public class FilterBuilderTests {

    // =======================================================================
    // Equals..
    // =======================================================================

    @Test
    public void equalsFilter() {
        Attribute attr;
        ConnectorObjectBuilder bld;
        bld = new ConnectorObjectBuilder();
        bld.setUid("233");
        bld.setName(Integer.toString(233));
        attr = AttributeBuilder.build("email", "bob@example.com");
        bld.addAttribute(attr);
        ConnectorObject obj = bld.build();
        Filter f = FilterBuilder.equalTo(attr);
        assertTrue(f.accept(obj));
        bld.addAttribute("email", "something@different.com");
        obj = bld.build();
        assertFalse(f.accept(obj));
        // check when the attribute doesn't exist in the object..
        bld = new ConnectorObjectBuilder();
        bld.setUid("3234");
        bld.setName(Integer.toString(3234));
        bld.addAttribute("adflk", "fafkajwe");
        assertFalse(f.accept(bld.build()));
    }

    // =======================================================================
    // Comparable..
    // =======================================================================

    @Test
    public void greaterThanFilter() {
        Attribute attr;
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        attr = AttributeBuilder.build("count", 3);
        Filter f = FilterBuilder.greaterThan(attr);
        bld.addAttribute("count", 4);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        boolean ret = f.accept(bld.build());
        assertTrue(ret);
        bld.addAttribute("count", 2);
        ret = f.accept(bld.build());
        assertFalse(ret);
    }

    @Test
    public void greaterThanEqualsToFilter() {
        Attribute attr;
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        attr = AttributeBuilder.build("count", 3);
        Filter f = FilterBuilder.greaterThanOrEqualTo(attr);
        bld.addAttribute("count", 4);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        boolean ret = f.accept(bld.build());
        assertTrue(ret);
        bld.addAttribute("count", 2);
        ret = f.accept(bld.build());
        assertFalse(ret);
        bld.addAttribute("count", 3);
        ret = f.accept(bld.build());
        assertTrue(ret);
    }

    @Test
    public void lessThanFilter() {
        Attribute attr;
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        attr = AttributeBuilder.build("count", 50);
        Filter f = FilterBuilder.lessThan(attr);
        bld.addAttribute("count", 49);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        boolean ret = f.accept(bld.build());
        assertTrue(ret);
        bld.addAttribute("count", 51);
        ret = f.accept(bld.build());
        assertFalse(ret);
    }

    @Test
    public void lessThanEqualToFilter() {
        Attribute attr;
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        attr = AttributeBuilder.build("count", 50);
        Filter f = FilterBuilder.lessThanOrEqualTo(attr);
        bld.addAttribute("count", 49);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        boolean ret = f.accept(bld.build());
        assertTrue(ret);
        bld.addAttribute("count", 51);
        ret = f.accept(bld.build());
        assertFalse(ret);
        bld.addAttribute("count", 50);
        ret = f.accept(bld.build());
        assertTrue(ret);
    }

    // =======================================================================
    // String compares..
    // =======================================================================
    @Test
    public void startsWithFilter() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        Attribute attr = AttributeBuilder.build("name", "fred");
        Filter f = FilterBuilder.startsWith(attr);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        bld.addAttribute("name", "fredrick");
        assertTrue(f.accept(bld.build()));
        bld.addAttribute("name", "fasdfklj");
        assertFalse(f.accept(bld.build()));
    }

    @Test
    public void endsWithFilter() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        Attribute attr = AttributeBuilder.build("name", "rick");
        Filter f = FilterBuilder.endsWith(attr);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        bld.addAttribute("name", "fredrick");
        assertTrue(f.accept(bld.build()));
        bld.addAttribute("name", "fakljffd");
        assertFalse(f.accept(bld.build()));
    }

    @Test
    public void constainsWithFilter() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        Attribute attr = AttributeBuilder.build("name", "red");
        Filter f = FilterBuilder.contains(attr);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        bld.addAttribute("name", "fredrick");
        assertTrue(f.accept(bld.build()));
        bld.addAttribute("name", "falkjfklj");
        assertFalse(f.accept(bld.build()));
    }

    // =======================================================================
    // Binary Operators
    // =======================================================================

    @Test
    public void andFilter() {
        Filter filter = null;
        filter = FilterBuilder.and(new TrueFilter(), new TrueFilter());
        assertTrue(filter.accept(null));
        filter = FilterBuilder.and(new TrueFilter(), new FalseFilter());
        assertFalse(filter.accept(null));
        filter = FilterBuilder.and(new FalseFilter(), new TrueFilter());
        assertFalse(filter.accept(null));
        filter = FilterBuilder.and(new FalseFilter(), new FalseFilter());
        assertFalse(filter.accept(null));
    }

    @Test
    public void orFilter() {
        Filter filter = null;
        filter = FilterBuilder.or(new TrueFilter(), new TrueFilter());
        assertTrue(filter.accept(null));
        filter = FilterBuilder.or(new TrueFilter(), new FalseFilter());
        assertTrue(filter.accept(null));
        filter = FilterBuilder.or(new FalseFilter(), new TrueFilter());
        assertTrue(filter.accept(null));
        filter = FilterBuilder.or(new FalseFilter(), new FalseFilter());
        assertFalse(filter.accept(null));
    }

    @Test
    public void notFilter() {
        Filter filter = null;
        filter = FilterBuilder.not(new TrueFilter());
        assertFalse(filter.accept(null));
        filter = FilterBuilder.not(new FalseFilter());
        assertTrue(filter.accept(null));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void illegalArgument() {
        FilterBuilder.lessThan((Attribute) null);
    }

    // =======================================================================
    // Set Contains Filters
    // =======================================================================
    @Test
    public void containsAllValuesFilterTrue() {
        Filter f = null;
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        bld.setUid("1");
        bld.setName("1");
        bld.addAttribute("a", "a", "b", "c");
        f = FilterBuilder.containsAllValues(AttributeBuilder.build("a", "a"));
        assertTrue(f.accept(bld.build()));
    }

    @Test
    public void containsAllValuesFilterFalse() {
        Filter f = null;
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        bld.setUid("2");
        bld.setName("1");
        bld.addAttribute("a", "a", "b", "c");
        f = FilterBuilder.containsAllValues(AttributeBuilder.build("b", "a"));
        assertFalse(f.accept(bld.build()));
        f = FilterBuilder.containsAllValues(AttributeBuilder.build("a", "d"));
        assertFalse(f.accept(bld.build()));
    }

    // =======================================================================
    // Filters
    // =======================================================================

    static class TrueFilter implements Filter {
        public boolean accept(ConnectorObject obj) {
            return true;
        }
    }

    static class FalseFilter implements Filter {
        public boolean accept(ConnectorObject obj) {
            return false;
        }
    }
}
