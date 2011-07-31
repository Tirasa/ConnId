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
package org.identityconnectors.framework.common.objects.filter;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;

/**
 * FilterBuilder creates a {@linkplain Filter filter} 
 * that will {@linkplain Filter#accept match} any {@code ConnectorObject} 
 * that satisfies all of the selection criteria 
 * that were specified using this builder.
 * 
 * @author Will Droste
 * @version $Revision: 1.7 $
 * @since 1.0
 */
public final class FilterBuilder {
  
    /**
     * Select only an input <code>ConnectorObject</code>
     * with a value for the specified <code>Attribute</code>
     * that <em>contains as a final substring</em>
     * the value of the specified <code>Attribute</code>.
     * <p>
     * For example, if the specified <code>Attribute</code> were 
     * <code>{"hairColor": "d"}</code>,
     * <br>this would match any <code>ConnectorObject</code>
     * with a value such as 
     * <br>&nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "red"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "blond"}</code>
     * <br>
     * but would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only values such as
     * <br>&nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "blonde"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "auburn"}</code>.
     * <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only <code>{"hairColor": null}</code>
     * <br>or that lacks the attribute <code>"hairColor"</code>.
     * 
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one value</em> 
     *            to test against each value of the corresponding 
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code> method 
     *            will return <code>true</code> if at least one value 
     *            of the corresponding attribute of the <code>ConnectorObject</code> 
     *            <em>contains as its last part</em> 
     *            the value of the specified <code>Attribute</code>;
     *            otherwise <code>false</code>.
     */
    public static Filter endsWith(final Attribute attr) {
        return new EndsWithFilter(attr);
    }

    /**
     * Select only an input <code>ConnectorObject</code>
     * with a value for the specified <code>Attribute</code>
     * that contains as an <em>initial substring</em>
     * the value of the specified <code>Attribute</code>.
     * <p>
     * For example, if the specified <code>Attribute</code> were 
     * <code>{"hairColor": "b"}</code>,
     * <br>this would match any <code>ConnectorObject</code> with a value such as
     * <br>&nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "brown"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "blond"}</code>
     * <br>
     * but would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only values such as
     * <br>&nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "red"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "auburn"}</code>.
     * <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only <code>{"hairColor": null}</code>
     * <br>or that lacks the attribute <code>"hairColor"</code>.
     * 
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one value</em> 
     *            to test against each value of the corresponding
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code> method 
     *            will return <code>true</code> if at least one value 
     *            of the corresponding attribute of the <code>ConnectorObject</code> 
     *            <em>contains as its first part</em> 
     *            the value of the specified <code>Attribute</code>;
     *            otherwise <code>false</code>.
     */
    public static Filter startsWith(final Attribute attr) {
        return new StartsWithFilter(attr);
    }

    /**
     * Select only an input <code>ConnectorObject</code>
     * with a value for the specified <code>Attribute</code>
     * that <em>contains as any substring</em>
     * the value of the specified <code>Attribute</code>.
     * <p>
     * For example, if the specified <code>Attribute</code> were 
     * <code>{"hairColor": "a"}</code>,
     * <br>this would match any <code>ConnectorObject</code> with a value such as
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "auburn"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "gray"}</code>
     * <br>
     * but would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only 
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "red"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "grey"}</code>.
     * <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only <code>{"hairColor": null}</code>
     * <br>or that lacks the attribute <code>"hairColor"</code>.
     * 
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one</em> value 
     *            to test against each value of the corresponding
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code> method 
     *            will return <code>true</code> if at least one value 
     *            of the corresponding attribute of the <code>ConnectorObject</code> 
     *            <em>contains anywhere within it</em> 
     *            the value of the specified <code>Attribute</code>;
     *            otherwise <code>false</code>.
     */
    public static Filter contains(final Attribute attr) {
        return new ContainsFilter(attr);
    }

    /**
     * Select only an input <code>ConnectorObject</code>
     * with a value for the specified <code>Attribute</code>
     * that is <em>lexically equal to</em> 
     * the value of the specified <code>Attribute</code>.
     * <p>
     * <b>NOTE: Is comparison case-sensitive?</b>
     * <p>
     * For example, if the specified <code>Attribute</code> were 
     * <code>{"hairColor": "brown"}</code>,
     * <br>this would match any <code>ConnectorObject</code> with a value such as
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brown"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "BROWN"}</code>
     * <br>
     * but would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only 
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brownish-gray"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "auburn"}</code>.
     * <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only <code>{"hairColor": null}</code>
     * <br>or that lacks the attribute <code>"hairColor"</code>.
     * <p>
     * <b>NOTE:</b> <i>Lexical</i> comparison of two string values
     * compares the characters of each value,
     * even if the string values could be interpreted as numeric.
     * The values <code>"01"</code> and <code>"1"</code> are unequal lexically,
     * although they would be equivalent arithmetically.
     * <p>
     * Two attributes with binary syntax are equal if and only if
     * their constituent bytes match.
     *
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one value</em> 
     *            to test against each value of the corresponding 
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code> method 
     *            will return <code>true</code> if at least one value 
     *            of the corresponding attribute of the <code>ConnectorObject</code> 
     *            <em>matches lexically</em> 
     *            the value of the specified <code>Attribute</code>;
     *            otherwise <code>false</code>.
     */
    public static Filter equalTo(final Attribute attr) {
        return new EqualsFilter(attr);
    }

    /**
     * Select only an input <code>ConnectorObject</code>
     * with a value for the specified <code>Attribute</code>
     * that is <em>lexically greater than or equal to</em> 
     * the value of the specified <code>Attribute</code>.
     * <p>
     * <b>NOTE: Is comparison case-sensitive?</b>
     * <p>
     * For example, if the specified <code>Attribute</code> were 
     * <code>{"hairColor": "brown"}</code>,
     * <br>this would match any <code>ConnectorObject</code> with a value such as
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brown"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brownish-gray"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "red"}</code>
     * <br>
     * but would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only 
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "black"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "blond"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "auburn"}</code>.
     * <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only <code>{"hairColor": null}</code>
     * <br>or that lacks the attribute <code>"hairColor"</code>.
     * <p>
     * <b>NOTE:</b> <i>Lexical</i> comparison of two string values
     * compares the characters of each value,
     * even if the string values could be interpreted as numeric.
     * <br>When compared lexically, <code>"99"</code> is greater than <code>"123"</code>.  
     * <br>When compared arithmetically, <code>99</code> is less than <code>123</code>.
     *
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one value</em> 
     *            to test against each value of the corresponding
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code> method 
     *            will return <code>true</code> if at least one value 
     *            of the corresponding attribute of the <code>ConnectorObject</code> 
     *            <em>matches or sorts alphabetically after</em> 
     *            the value of the specified <code>Attribute</code>;
     *            otherwise <code>false</code>.
     */
    public static Filter greaterThanOrEqualTo(final Attribute attr) {
        return new GreaterThanOrEqualFilter(attr);
    }

    /**
     * Select only an input <code>ConnectorObject</code>
     * with a value for the specified <code>Attribute</code>
     * that is <em>lexically less than or equal to</em> 
     * the value of the specified <code>Attribute</code>.
     * <p>
     * <b>NOTE: Is comparison case-sensitive?</b>
     * <p>
     * For example, if the specified <code>Attribute</code> were 
     * <code>{"hairColor": "brown"}</code>,
     * <br>this would match any <code>ConnectorObject</code> with a value such as
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brown"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "black"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "blond"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "auburn"}</code>
     * <br>
     * but would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only 
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brownish-gray"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "red"}</code>
     * <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only <code>{"hairColor": null}</code>
     * <br>or that lacks the attribute <code>"hairColor"</code>.
     * <p>
     * <b>NOTE:</b> <i>Lexical</i> comparison of two string values
     * compares the characters of each value,
     * even if the string values could be interpreted as numeric.
     * <br>When compared lexically, <code>"99"</code> is greater than <code>"123"</code>.  
     * <br>When compared arithmetically, <code>99</code> is less than <code>123</code>.
     *
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one value</em>
     *            to test against each value of the corresponding
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code> method 
     *            will return <code>true</code> if at least one value 
     *            of the corresponding attribute of the <code>ConnectorObject</code> 
     *            <em>matches or sorts alphabetically before</em> 
     *            the value of the specified <code>Attribute</code>;
     *            otherwise <code>false</code>.
     */
    public static Filter lessThanOrEqualTo(final Attribute attr) {
        return new LessThanOrEqualFilter(attr);
    }

    /**
     * Select only an input <code>ConnectorObject</code>
     * with a value for the specified <code>Attribute</code>
     * that is <em>lexically less than</em> 
     * the value of the specified <code>Attribute</code>.
     * <p>
     * <b>NOTE: Is comparison case-sensitive?</b>
     * <p>
     * For example, if the specified <code>Attribute</code> were 
     * <code>{"hairColor": "brown"}</code>,
     * <br>this would match any <code>ConnectorObject</code> with a value such as
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "black"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "blond"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "auburn"}</code>
     * <br>
     * but would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only 
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brown"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brownish-gray"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "red"}</code>
     * <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only <code>{"hairColor": null}</code>
     * <br>or that lacks the attribute <code>"hairColor"</code>.
     * <p>
     * <b>NOTE:</b> <i>Lexical</i> comparison of two string values
     * compares the characters of each value,
     * even if the string values could be interpreted as numeric.
     * <br>When compared lexically, <code>"99"</code> is greater than <code>"123"</code>.  
     * <br>When compared arithmetically, <code>99</code> is less than <code>123</code>.
     *
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one value</em> 
     *            to test against each value of the corresponding 
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code> method 
     *            will return <code>true</code> if at least one value 
     *            of the corresponding attribute of the <code>ConnectorObject</code> 
     *            <em>sorts alphabetically before</em> 
     *            the value of the specified <code>Attribute</code>;
     *            otherwise <code>false</code>.
     */
    public static Filter lessThan(final Attribute attr) {
        return new LessThanFilter(attr);
    }

    /**
     * Select only an input <code>ConnectorObject</code>
     * with a value for the specified <code>Attribute</code>
     * that is <em>lexically greater than</em> 
     * the value of the specified <code>Attribute</code>.
     * <p>
     * <b>NOTE: Is comparison case-sensitive?</b>
     * <p>
     * For example, if the specified <code>Attribute</code> were 
     * <code>{"hairColor": "brown"}</code>,
     * <br>this would match any <code>ConnectorObject</code> with a value such as
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brownish-gray"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "red"}</code>
     * <br>
     * but would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only 
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brown"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "black"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "blond"}</code> or
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "auburn"}</code>.
     * <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code>
     * that contains only <code>{"hairColor": null}</code>
     * <br>or that lacks the attribute <code>"hairColor"</code>.
     * <p>
     * <b>NOTE:</b> <i>Lexical</i> comparison of two string values
     * compares the characters of each value,
     * even if the string values could be interpreted as numeric.
     * <br>When compared lexically, <code>"99"</code> is greater than <code>"123"</code>.  
     * <br>When compared arithmetically, <code>99</code> is less than <code>123</code>.
     *
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one value</em> 
     *            to test against each value of the corresponding 
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code> method 
     *            will return <code>true</code> if at least one value 
     *            of the corresponding attribute of the <code>ConnectorObject</code> 
     *            <em>sorts alphabetically after</em> 
     *            the value of the specified <code>Attribute</code>;
     *            otherwise <code>false</code>.
     */
    public static Filter greaterThan(final Attribute attr) {
        return new GreaterThanFilter(attr);
    }

    /**
     * Logically "ANDs" together the two specified instances of {@link Filter}.
     * The resulting <i>conjunct</i> <code>Filter</code> is true if and only if
     * both of the specified filters are true.
     * 
     * @param leftHandSide
     *            left-hand-side filter.
     * @param rightHandSide
     *            right-hand-side filter.
     * @return the result of <code>(leftHandSide &amp;&amp; rightHandSide)</code>
     */
    public static Filter and(final Filter leftHandSide, final Filter rightHandSide) {
        return new AndFilter(leftHandSide, rightHandSide);
    }

    /**
     * Logically "OR" together the two specified instances of {@link Filter}.
     * The resulting <i>disjunct</i> <code>Filter</code> is true if and only if
     * at least one of the specified filters is true.
     * 
     * @param leftHandSide
     *            left-hand-side filter.
     * @param rightHandSide
     *            right-hand-side filter.
     * @return the result of <code>(leftHandSide || rightHandSide)</code>
     */
    public static Filter or(final Filter leftHandSide, final Filter rightHandSide) {
        return new OrFilter(leftHandSide, rightHandSide);
    }

    /**
     * Logically negate the specified {@link Filter}.
     * The resulting <code>Filter</code> is true if and only if
     * the specified filter is false.
     * 
     * @param filter
     *            the <code>Filter</code> to negate.
     * @return the result of <code>(!filter)</code>.
     */
    public static Filter not(final Filter filter) {
        return new NotFilter(filter);
    }

    /**
     * Select only an input {@link ConnectorObject} with a value for the
     * specified <code>Attribute</code> that contains all the values from the
     * specified <code>Attribute</code>.
     * <p>
     * For example, if the specified {@link Attribute} were
     * <code>{"hairColor": "brown", "red"}</code>, <br>
     * this would match any <code>ConnectorObject</code> with values such as
     * <ul>
     * <li><code>{"hairColor": "black", "brown", "red"}</code></li>
     * <li><code>{"hairColor": "blond", "brown", "red"}</code></li>
     * <li><code>{"hairColor": "auburn", "brown", "red"}</code></li>
     * </ul>
     * but would <em>not</em> match any {@link ConnectorObject} that contains
     * only
     * <ul>
     * <li><code>{"hairColor": "brown"}</code></li>
     * <li><code>{"hairColor": "brownish-gray"}</code></li>
     * <li><code>{"hairColor": "red"}</code></li>
     * </ul>
     * This also would <em>not</em> match any {@link ConnectorObject} that
     * contains only <code>{"hairColor": null}</code> <br>
     * or that lacks the attribute <code>"hairColor"</code>.
     * <p>
     * <b>NOTE:</b> <i>Lexical</i> comparison of two string values compares
     * the characters of each value, even if the string values could be
     * interpreted as numeric. <br>
     * 
     * @param attr
     *            {@link Attribute} <em>containing exactly one value</em> to
     *            test against each value of the corresponding
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of {@link Filter} whose <code>accept()</code>
     *         method will return <code>true</code> if at least one value of
     *         the corresponding attribute of the {@link ConnectorObject}
     *         <em>sorts alphabetically before</em> the value of the specified
     *         {@link Attribute}; otherwise <code>false</code>.
     */
    public static Filter containsAllValues(final Attribute attr) {
        return new ContainsAllValuesFilter(attr);
    }
}
