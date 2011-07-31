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
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.impl.api.local.operations.UpdateImpl;
import org.junit.Assert;
import org.junit.Test;


/**
 * Testing for the merging of the various attribute during update.
 */
public class UpdateImplTests {

    @Test(expected=NullPointerException.class)
    public void validateUidArg() {
        UpdateImpl.validateInput(ObjectClass.ACCOUNT, null, new HashSet<Attribute>(),true);
    }

    @Test(expected=NullPointerException.class)
    public void validateObjectClassArg() {
        UpdateImpl.validateInput(null, new Uid("foo"), new HashSet<Attribute>(),true);
    }
    
    @Test(expected=NullPointerException.class)
    public void validateAttrsArg() {
        UpdateImpl.validateInput(ObjectClass.ACCOUNT, new Uid("foo"),null,true);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void validateUidAttribute() {
        Set<Attribute> attrs=new HashSet<Attribute>();
        attrs.add(new Uid("foo"));
        UpdateImpl.validateInput(ObjectClass.ACCOUNT, new Uid("foo"),attrs,true);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void validateAddWithNullAttribute() {
        Set<Attribute> attrs = new HashSet<Attribute>();
        attrs.add(AttributeBuilder.build("something"));
        UpdateImpl.validateInput(ObjectClass.ACCOUNT, new Uid("foo"), attrs, true);        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void validateAttemptToAddName() {
        Set<Attribute> attrs = new HashSet<Attribute>();
        attrs.add(new Name("fadf"));
        UpdateImpl.validateInput(ObjectClass.ACCOUNT, new Uid("foo"),attrs,true);                
    }

    @Test
    public void validateAttemptToAddDeleteOperationalAttribute() {
        // list of all the operational attributes..
        List<Attribute> list = new ArrayList<Attribute>();
        list.add(AttributeBuilder.buildEnabled(false));
        list.add(AttributeBuilder.buildLockOut(true));
        list.add(AttributeBuilder.buildCurrentPassword("fadsf".toCharArray()));
        list.add(AttributeBuilder.buildPasswordExpirationDate(new Date()));
        list.add(AttributeBuilder.buildPassword("fadsf".toCharArray()));
        for (Attribute attr : list) {
            Set<Attribute> attrs = new HashSet<Attribute>();
            attrs.add(attr);
            try {
                UpdateImpl.validateInput(ObjectClass.ACCOUNT, new Uid("1"), attrs,true);
                Assert.fail("Failed: " + attr.getName());
            } catch (IllegalArgumentException e) {
                // this is a good thing..
            }
        }
    }
    
    @Test
    public void mergeAddAttribute() {
        UpdateImpl up = new UpdateImpl(null, null);
        Set<Attribute> actual;
        Set<Attribute> base = CollectionUtil.<Attribute>newSet();
        Set<Attribute> expected = CollectionUtil.<Attribute>newSet();
        Set<Attribute> changeset = CollectionUtil.<Attribute>newSet();
        // attempt to add a value to an attribute..
        Attribute cattr = AttributeBuilder.build("abc", 2);
        changeset.add(cattr);
        expected.add(AttributeBuilder.build("abc", 2));        
        actual = up.merge(changeset, base, true);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void mergeAddToExistingAttribute() {
        UpdateImpl up = new UpdateImpl(null, null);
        Set<Attribute> actual;
        Set<Attribute> base = CollectionUtil.<Attribute>newSet();
        Set<Attribute> expected = CollectionUtil.<Attribute>newSet();
        Set<Attribute> changeset = CollectionUtil.<Attribute>newSet();
        // attempt to add a value to an attribute..
        Attribute battr = AttributeBuilder.build("abc", 1);
        Attribute cattr = AttributeBuilder.build("abc", 2);
        base.add(battr);
        changeset.add(cattr);
        expected.add(AttributeBuilder.build("abc", 1, 2));        
        actual = up.merge(changeset, base,true);
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void mergeDeleteNonExistentAttribute() {
        UpdateImpl up = new UpdateImpl(null, null);
        Set<Attribute> actual;
        Set<Attribute> base = CollectionUtil.<Attribute>newSet();
        Set<Attribute> expected = CollectionUtil.<Attribute>newSet();
        Set<Attribute> changeset = CollectionUtil.<Attribute>newSet();
        // attempt to add a value to an attribute..
        Attribute cattr = AttributeBuilder.build("abc", 2);
        changeset.add(cattr);
        actual = up.merge(changeset, base, false);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void mergeDeleteToExistingAttribute() {
        UpdateImpl up = new UpdateImpl(null, null);
        Set<Attribute> actual;
        Set<Attribute> base = CollectionUtil.<Attribute>newSet();
        Set<Attribute> expected = CollectionUtil.<Attribute>newSet();
        Set<Attribute> changeset = CollectionUtil.<Attribute>newSet();
        // attempt to add a value to an attribute..
        Attribute battr = AttributeBuilder.build("abc", 1, 2);
        Attribute cattr = AttributeBuilder.build("abc", 2);
        base.add(battr);
        changeset.add(cattr);
        expected.add(AttributeBuilder.build("abc", 1));
        actual = up.merge(changeset, base, false);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void mergeDeleteToExistingAttributeCompletely() {
        UpdateImpl up = new UpdateImpl(null, null);
        Set<Attribute> actual;
        Set<Attribute> base = CollectionUtil.<Attribute>newSet();
        Set<Attribute> expected = CollectionUtil.<Attribute>newSet();
        Set<Attribute> changeset = CollectionUtil.<Attribute>newSet();
        // attempt to add a value to an attribute..
        Attribute battr = AttributeBuilder.build("abc", 1, 2);
        Attribute cattr = AttributeBuilder.build("abc", 1, 2);
        base.add(battr);
        changeset.add(cattr);
        expected.add(AttributeBuilder.build("abc"));
        actual = up.merge(changeset, base, false);
        Assert.assertEquals(expected, actual);
    }

    
}
