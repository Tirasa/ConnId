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
 * Portions Copyrighted 2014 ForgeRock AS. 
 * Portions Copyrighted 2018 ConnId. 
 */
package org.identityconnectors.framework.common.objects;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.common.objects.filter.FilterVisitor;
import org.testng.annotations.Test;

public class FilterBuilderTests {

    // =======================================================================
    // Equals..
    // =======================================================================

    @Test
    public void equalsFilter() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        bld.setUid("233");
        bld.setName(Integer.toString(233));
        Attribute attr = AttributeBuilder.build("email", "bob@example.com");
        bld.addAttribute(attr);
        ConnectorObject obj = bld.build();

        // check equals match
        Filter filter = FilterBuilder.equalTo(attr);
        assertTrue(filter.accept(obj));

        // check equals does not match
        bld.addAttribute("email", "something@different.com");
        obj = bld.build();
        assertFalse(filter.accept(obj));

        // check equals ignore case does not match
        bld.addAttribute("email", "bob@EXAMPLE.com");
        obj = bld.build();
        assertFalse(filter.accept(obj));

        // check when the attribute doesn't exist in the object..
        bld = new ConnectorObjectBuilder();
        bld.setUid("3234");
        bld.setName(Integer.toString(3234));
        bld.addAttribute("adflk", "fafkajwe");
        assertFalse(filter.accept(bld.build()));
    }

    // =======================================================================
    // Comparable..
    // =======================================================================

    @Test
    public void greaterThanFilter() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        Attribute attr = AttributeBuilder.build("count", 3);
        Filter filter = FilterBuilder.greaterThan(attr);
        bld.addAttribute("count", 4);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        boolean ret = filter.accept(bld.build());
        assertTrue(ret);
        bld.addAttribute("count", 2);
        ret = filter.accept(bld.build());
        assertFalse(ret);
    }

    @Test
    public void greaterThanEqualsToFilter() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        Attribute attr = AttributeBuilder.build("count", 3);
        Filter filter = FilterBuilder.greaterThanOrEqualTo(attr);
        bld.addAttribute("count", 4);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        boolean ret = filter.accept(bld.build());
        assertTrue(ret);
        bld.addAttribute("count", 2);
        ret = filter.accept(bld.build());
        assertFalse(ret);
        bld.addAttribute("count", 3);
        ret = filter.accept(bld.build());
        assertTrue(ret);
    }

    @Test
    public void lessThanFilter() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        Attribute attr = AttributeBuilder.build("count", 50);
        Filter filter = FilterBuilder.lessThan(attr);
        bld.addAttribute("count", 49);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        boolean ret = filter.accept(bld.build());
        assertTrue(ret);
        bld.addAttribute("count", 51);
        ret = filter.accept(bld.build());
        assertFalse(ret);
    }

    @Test
    public void lessThanEqualToFilter() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        Attribute attr = AttributeBuilder.build("count", 50);
        Filter filter = FilterBuilder.lessThanOrEqualTo(attr);
        bld.addAttribute("count", 49);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        boolean ret = filter.accept(bld.build());
        assertTrue(ret);
        bld.addAttribute("count", 51);
        ret = filter.accept(bld.build());
        assertFalse(ret);
        bld.addAttribute("count", 50);
        ret = filter.accept(bld.build());
        assertTrue(ret);
    }

    // =======================================================================
    // String compares..
    // =======================================================================
    @Test
    public void startsWithFilter() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        Attribute attr = AttributeBuilder.build("name", "fred");
        Filter filter = FilterBuilder.startsWith(attr);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        bld.addAttribute("name", "fredrick");
        assertTrue(filter.accept(bld.build()));
        bld.addAttribute("name", "fasdfklj");
        assertFalse(filter.accept(bld.build()));
    }

    @Test
    public void endsWithFilter() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        Attribute attr = AttributeBuilder.build("name", "rick");
        Filter filter = FilterBuilder.endsWith(attr);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        bld.addAttribute("name", "fredrick");
        assertTrue(filter.accept(bld.build()));
        bld.addAttribute("name", "fakljffd");
        assertFalse(filter.accept(bld.build()));
    }

    @Test
    public void containsFilter() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        Attribute attr = AttributeBuilder.build("name", "red");
        Filter filter = FilterBuilder.contains(attr);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        bld.addAttribute("name", "fredrick");
        assertTrue(filter.accept(bld.build()));
        bld.addAttribute("name", "falkjfklj");
        assertFalse(filter.accept(bld.build()));
    }

    @Test
    public void equalsIgnoreCaseFilter() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        Attribute attr = AttributeBuilder.build("name", "red");
        Filter filter = FilterBuilder.equalsIgnoreCase(attr);
        bld.setUid("1");
        bld.setName(Integer.toString(1));
        bld.addAttribute("name", "ReD");
        assertTrue(filter.accept(bld.build()));
        bld.addAttribute("name", "falkjfklj");
        assertFalse(filter.accept(bld.build()));
    }
    
    // =======================================================================
    // Binary Operators
    // =======================================================================

    @Test
    public void andFilter() {
        Filter filter;
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
        Filter filter;
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
        Filter filter;
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
        Filter filter;
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        bld.setUid("1");
        bld.setName("1");
        bld.addAttribute("a", "a", "b", "c");
        filter = FilterBuilder.containsAllValues(AttributeBuilder.build("a", "a"));
        assertTrue(filter.accept(bld.build()));
    }

    @Test
    public void containsAllValuesFilterFalse() {
        Filter filter;
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        bld.setUid("2");
        bld.setName("1");
        bld.addAttribute("a", "a", "b", "c");
        filter = FilterBuilder.containsAllValues(AttributeBuilder.build("b", "a"));
        assertFalse(filter.accept(bld.build()));
        filter = FilterBuilder.containsAllValues(AttributeBuilder.build("a", "d"));
        assertFalse(filter.accept(bld.build()));
    }

    // =======================================================================
    // Filters
    // =======================================================================

    static class TrueFilter implements Filter {
        @Override
        public boolean accept(ConnectorObject obj) {
            return true;
        }

        @Override
        public <R, P> R accept(FilterVisitor<R, P> v, P p) {
            return v.visitExtendedFilter(p, this);
        }
    }

    static class FalseFilter implements Filter {
        @Override
        public boolean accept(ConnectorObject obj) {
            return false;
        }

        @Override
        public <R, P> R accept(FilterVisitor<R, P> v, P p) {
            return v.visitExtendedFilter(p, this);
        }
    }
}
