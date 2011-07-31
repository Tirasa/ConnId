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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Base class to make it easier to implement Search. 
 * A search filter may contain operators (such as 'contains' or 'in')
 * or may contain logical operators (such as 'AND', 'OR' or 'NOT')
 * that a connector cannot implement using the native API
 * of the target system or application. 
 * A connector developer should subclass <code>AbstractFilterTranslator</code>
 * in order to declare which filter operations the connector does support.
 * This allows the <code>FilterTranslator</code> instance to analyze
 * a specified search filter and reduce the filter to its most efficient form.
 * The default (and worst-case) behavior is to return a null expression, 
 * which means that the connector should return "everything" 
 * (that is, should return all values for every requested attribute)
 * and rely on the common code in the framework to perform filtering.
 * This "fallback" behavior is good (in that it ensures consistency
 * of search behavior across connector implementations) but it is 
 * obviously better for performance and scalability if each connector
 * performs as much filtering as the native API of the target can support.
 * <p> 
 * A subclass should override each of the following methods where possible:
 * <ol>
 *    <li>{@link #createAndExpression}</li>
 *    <li>{@link #createOrExpression}</li>
 *    <li>{@link #createContainsExpression(ContainsFilter, boolean)}</li>
 *    <li>{@link #createEndsWithExpression(EndsWithFilter, boolean)}</li>
 *    <li>{@link #createEqualsExpression(EqualsFilter, boolean)}</li>
 *    <li>{@link #createGreaterThanExpression(GreaterThanFilter, boolean)}</li>
 *    <li>{@link #createGreaterThanOrEqualExpression(GreaterThanOrEqualFilter, boolean)}</li>
 *    <li>{@link #createStartsWithExpression(StartsWithFilter, boolean)}</li>
 *    <li>{@link #createContainsAllValuesExpression(ContainsAllValuesFilter, boolean)}</li>
 * </ol>
 * <p>
 * Translation can then be performed using {@link #translate(Filter)}.
 * <p>
 * @param <T> The result type of the translator. Commonly this will
 * be a string, but there are cases where you might need to return
 * a more complex data structure. For example if you are building a SQL
 * query, you will need not *just* the base WHERE clause but a list
 * of tables that need to be joined together.
 */
abstract public class AbstractFilterTranslator<T> implements FilterTranslator<T> {
    
    /**
     * Main method to be called to translate a filter
     * @param filter The filter to translate.
     * @return The list of queries to be performed. The list
     * <code>size()</code> may be one of the following:
     * <ol>
     *    <li>0 - This
     *    signifies <b>fetch everything</b>. This may occur if your filter 
     *    was null or one of your <code>create*</code> methods returned null.</li>
     *    <li>1 - List contains a single query that will return the results from the filter.
     *    Note that the results may be a <b>superset</b> of those specified by
     *    the filter in the case that one of your <code>create*</code> methods returned null.
     *    That is OK from a behavior standpoint since <code>ConnectorFacade</code> performs
     *    a second level of filtering. However it is undesirable from a performance standpoint. </li>
     *    <li>>1 - List contains multiple queries that must be performed in order to
     *    meet the filter that was passed in. Note that this only occurs if your
     *    {@link #createOrExpression} method can return null. If this happens, it
     *    is the responsibility of the connector implementor to perform each query
     *    and combine the results. In order to eliminate duplicates, the connector
     *    implementation must keep an in-memory <code>HashSet</code> of those UID
     *    that have been visited thus far. This will not scale well if your
     *    result sets are large. Therefore it is <b>recommended</b> that if
     *    at all possible you implement {@link #createOrExpression}</li>
     * </ol>
     */
    public final List<T> translate(Filter filter) {
        if ( filter == null ) {
            return new ArrayList<T>();
        }
        //this must come first
        filter = normalizeNot(filter);
        filter = simplifyAndDistribute(filter);
        //might have simplified it to the everything filter
        if ( filter == null ) {
            return new ArrayList<T>();
        }
        List<T> result = translateInternal(filter);
        //now "optimize" - we can eliminate exact matches at least
        Set<T> set = new HashSet<T>();
        List<T> optimized = new ArrayList<T>(result.size());
        for (T obj : result) {
            if ( set.add(obj) ) {
                optimized.add(obj);
            }
        }
        return optimized;
    }
    
    /**
     * Pushes Not's so that they are just before the leaves of the tree
     */
    private Filter normalizeNot(Filter filter) {
        if ( filter instanceof AndFilter ) {
            AndFilter af = (AndFilter)filter;
            return new AndFilter(normalizeNot(af.getLeft()),
                    normalizeNot(af.getRight()));
        }
        else if ( filter instanceof OrFilter ) {
            OrFilter of = (OrFilter)filter;
            return new OrFilter(normalizeNot(of.getLeft()),
                    normalizeNot(of.getRight()));
        }
        else if ( filter instanceof NotFilter ) {
            NotFilter nf = (NotFilter)filter;
            return negate(normalizeNot(nf.getFilter()));
        }
        else {
            return filter;
        }        
    }
    
    /**
     * Given a filter, create a filter representing its negative.
     * This is used by normalizeNot.
     */
    private Filter negate(Filter filter)
    {
        if ( filter instanceof AndFilter ) {
            AndFilter af = (AndFilter)filter;
            return new OrFilter(negate(af.getLeft()),
                                negate(af.getRight()));
        }
        else if ( filter instanceof OrFilter ) {
            OrFilter of = (OrFilter)filter;
            return new AndFilter(negate(of.getLeft()),
                    negate(of.getRight()));
        }
        else if ( filter instanceof NotFilter ) {
            NotFilter nf = (NotFilter)filter;
            return nf.getFilter();
        }
        else {
            return new NotFilter(filter);
        }
    }
    
    /**
     * Simultaneously prunes those portions of the
     * filter than cannot be implemented and distributes
     * Ands over Ors where needed if the resource does not
     * implement Or.
     * 
     * @param filter Nots must already be normalized
     * @return a simplified filter or null to represent the
     * "everything" filter.
     */
    private Filter simplifyAndDistribute(Filter filter) {
        if ( filter instanceof AndFilter ) {
            AndFilter af = (AndFilter)filter;
            Filter simplifiedLeft =
                simplifyAndDistribute(af.getLeft());
            Filter simplifiedRight =
                simplifyAndDistribute(af.getRight());
            if ( simplifiedLeft == null ) {
                //left is "everything" - just return the right
                return simplifiedRight;
            }
            else if ( simplifiedRight == null ) {
                //right is "everything" - just return the left
                return simplifiedLeft;
            }
            else {
                //simulate translation of the left and right
                //to see where we end up
                List<T> leftExprs =
                    translateInternal(simplifiedLeft);
                List<T> rightExprs =
                    translateInternal(simplifiedRight);
                if (leftExprs.size() == 0) {
                    //This can happen only when one of the create* methods
                    //is inconsistent from one invocation to the next
                    //(simplifiedLeft should have been null 
                    //in the previous 'if' above).
                    throw new IllegalStateException("Translation method is inconsistent: "+leftExprs);
                }
                if (rightExprs.size() == 0) {
                    //This can happen only when one of the create* methods
                    //is inconsistent from one invocation to the next
                    //(simplifiedRight should have been null 
                	//in the previous 'if' above).
                    throw new IllegalStateException("Translation method is inconsistent: "+rightExprs);
                }
                                
                //Simulate ANDing each pair(left,right).
                //If all of them return null (i.e., "everything"), 
                //then the request cannot be filtered.
                boolean anyAndsPossible = false;
                for ( T leftExpr : leftExprs ) {
                    for ( T rightExpr : rightExprs ) {
                        T test = createAndExpression(
                                leftExpr,
                                rightExpr);
                        if ( test != null ) {
                            anyAndsPossible = true;
                            break;
                        }
                    }
                    if ( anyAndsPossible ) {
                        break;
                    }
                }
               
                //If no AND filtering is possible,
                //return whichever of left or right 
                //contains the fewest expressions.
                if (!anyAndsPossible) {
                    if ( leftExprs.size() <= rightExprs.size() ) {
                        return simplifiedLeft;
                    }
                    else {
                        return simplifiedRight;
                    }
                }

                //Since AND filtering is possible for at least
                //one expression, let's distribute.
                if ( leftExprs.size() > 1 ) {
                    //The left can contain more than one expression
                	//only if the left-hand side is an unimplemented OR.
                    //Distribute our AND to the left.
                    OrFilter left = (OrFilter)simplifiedLeft;
                    OrFilter newFilter =
                        new OrFilter(new AndFilter(left.getLeft(),
                                                   simplifiedRight),
                                     new AndFilter(left.getRight(),
                                                   simplifiedRight));
                    return simplifyAndDistribute(newFilter);
                }
                else if ( rightExprs.size() > 1 ) {
                    //The right can contain more than one expression 
                	//only if the right-hand side is an unimplemented OR.
                    //Distribute our AND to the right.
                    OrFilter right = (OrFilter)simplifiedRight;
                    OrFilter newFilter =
                        new OrFilter(new AndFilter(simplifiedLeft,
                            right.getLeft()),
                        new AndFilter(simplifiedLeft,
                            right.getRight()));
                    return simplifyAndDistribute(newFilter);                    
                }
                else {
                    //Each side contains exactly one expression
                	//and the translator does implement AND
                    //(anyAndsPossible must be true
                    //for them to have hit this branch).
                    assert anyAndsPossible;
                    return new AndFilter(simplifiedLeft,simplifiedRight);                        
                }
            }
        }
        else if ( filter instanceof OrFilter ) {
            OrFilter of = (OrFilter)filter;
            Filter simplifiedLeft =
                simplifyAndDistribute(of.getLeft());
            Filter simplifiedRight =
                simplifyAndDistribute(of.getRight());
            //If either left or right reduces to "everything", 
            //then simplify the OR to "everything".
            if ( simplifiedLeft == null || simplifiedRight == null ) {
                return null;
            }
            //otherwise
            return new OrFilter(simplifiedLeft,
                    simplifiedRight);
        }
        else {
            //Otherwise, it's a NOT(LEAF) or a LEAF.
            //Simulate creating it.
            T expr = createLeafExpression(filter);
            if ( expr == null ) {
                //If the expression cannot be implemented, 
            	//return the "everything" filter.
                return null;
            }
            else {
                //Otherwise, return the filter.
                return filter;
            }
        }
    }
    
    /**
     * Translates the filter into a list of expressions. 
     * The filter must have already been transformed 
     * using normalizeNot followed by a simplifyAndDistribute.
     * @param filter A filter (normalized, simplified, and distibuted)
     * @return A list of expressions or empty list for everything.
     */
    private List<T> translateInternal(Filter filter) {
        if ( filter instanceof AndFilter ) {
            T result = translateAnd((AndFilter)filter);
            List<T> rv = new ArrayList<T>();
            if ( result != null ) {
                rv.add(result);
            }
            return rv;
        }
        else if ( filter instanceof OrFilter ) {
            return translateOr((OrFilter)filter);
        }
        else {
            //otherwise it's either a leaf or a NOT (leaf)
            T expr = createLeafExpression(filter);
            List<T> exprs = new ArrayList<T>();
            if ( expr != null ) {
                exprs.add(expr);
            }
            return exprs;
        }
    }
    
    private T translateAnd( AndFilter filter ) {
        List<T> leftExprs = translateInternal(filter.getLeft());
        List<T> rightExprs = translateInternal(filter.getRight());
        if ( leftExprs.size() != 1 ) {
            //this can happen only if one of the create* methods
            //is inconsistent from one invocation to the next
            //(at this point we've already been simplified and
            //distributed).
            throw new IllegalStateException("Translation method is inconsistent: "+leftExprs);
        }
        if ( rightExprs.size() != 1 ) {
            //this can happen only if one of the create* methods
            //is inconsistent from one invocation to the next
            //(at this point we've already been simplified and
            //distributed).
            throw new IllegalStateException("Translation method is inconsistent: "+rightExprs);
        }
        T rv = createAndExpression(leftExprs.get(0), rightExprs.get(0));
        if ( rv == null ) {
            //This could happen only if we're inconsistent
            //(since the simplify logic already should have removed
            //any expression that cannot be filtered).
            throw new IllegalStateException("createAndExpression is inconsistent");            
        }
        return rv;
    }
    
    private List<T> translateOr( OrFilter filter ) {
        List<T> leftExprs = translateInternal(filter.getLeft());
        List<T> rightExprs = translateInternal(filter.getRight());
        if ( leftExprs.size() == 0 ) {
            //This can happen only if one of the create* methods
            //is inconsistent from one invocation to the next.
            throw new IllegalStateException("Translation method is inconsistent");            
        }
        if ( rightExprs.size() == 0 ) {
            //This can happen only if one of the create* methods
            //methods is inconsistent from on invocation to the next.
            throw new IllegalStateException("Translation method is inconsistent");
        }
        if ( leftExprs.size() == 1 && rightExprs.size() == 1 ) {
            //If each side contains exactly one expression,
        	//try to create a combined expression.
            T val = createOrExpression(leftExprs.get(0), rightExprs.get(0));
            if ( val != null ) {
                List<T> rv = new ArrayList<T>();
                rv.add(val);
                return rv;
            }
            //Otherwise, fall through
        }
        
        //Return a list of queries from the left and from the right
        List<T> rv = new ArrayList<T>(leftExprs.size()+rightExprs.size());
        rv.addAll(leftExprs);
        rv.addAll(rightExprs);
        return rv;
    }
    
    /**
     * Creates an expression for a LEAF or a NOT(leaf)
     * @param filter Must be either a leaf or a NOT(leaf)
     * @return The expression
     */
    private T createLeafExpression(Filter filter) {
        Filter leafFilter;
        boolean not;
        if ( filter instanceof NotFilter ) {
            NotFilter nf = (NotFilter)filter;
            leafFilter = nf.getFilter();
            not = true;
        }
        else {
            leafFilter = filter;
            not = false;
        }
        T expr = createLeafExpression(leafFilter,not);
        return expr;
    }
    
    /**
     * Creates a Leaf expression
     * @param filter Must be a leaf expression
     * @param not Is ! to be applied to the leaf expression
     * @return The expression or null (for everything)
     */
    private T createLeafExpression(Filter filter, boolean not) {
        if ( filter instanceof ContainsFilter ) {
            return createContainsExpression((ContainsFilter)filter, not);
        }
        else if (filter instanceof EndsWithFilter) {
            return createEndsWithExpression((EndsWithFilter)filter,not);
        }
        else if ( filter instanceof EqualsFilter ) {
            return createEqualsExpression((EqualsFilter)filter, not);
        }
        else if ( filter instanceof GreaterThanFilter ) {
            return createGreaterThanExpression((GreaterThanFilter)filter, not);
        }
        else if ( filter instanceof GreaterThanOrEqualFilter ) {
            return createGreaterThanOrEqualExpression((GreaterThanOrEqualFilter)filter, not);
        }
        else if ( filter instanceof LessThanFilter ) {
            return createLessThanExpression((LessThanFilter)filter, not);
        }
        else if ( filter instanceof LessThanOrEqualFilter ) {
            return createLessThanOrEqualExpression((LessThanOrEqualFilter)filter, not);
        }
        else if (filter instanceof StartsWithFilter) {
            return createStartsWithExpression((StartsWithFilter)filter,not);
        }
        else if (filter instanceof ContainsAllValuesFilter) {
            return createContainsAllValuesExpression((ContainsAllValuesFilter)filter, not);
        }
        else {
            //unrecognized expression - nothing we can do
            return null;
        }
    }
    
    /**
     * Should be overridden by subclasses to create an AND expression
     * if the native resource supports AND.
     * @param leftExpression The left expression. Will never be null.
     * @param rightExpression The right expression. Will never be null.
     * @return The AND expression. A return value of null means 
     * a native AND query cannot be created for the given expressions. 
     * In this case, the resulting query will consist of the 
     * leftExpression only.
     */
    protected T createAndExpression(T leftExpression, T rightExpression) {
        return null;
    }
    
    /**
     * Should be overridden by subclasses to create an OR expression
     * if the native resource supports OR.
     * @param leftExpression The left expression. Will never be null.
     * @param rightExpression The right expression. Will never be null.
     * @return The OR expression. A return value of null means 
     * a native OR query cannot be created for the given expressions. 
     * In this case, {@link #translate} may return multiple queries, each
     * of which must be run and results combined. 
     */
    protected T createOrExpression(T leftExpression, T rightExpression) {
        return null;
    }
    
    /**
     * Should be overridden by subclasses to create a CONTAINS expression
     * if the native resource supports CONTAINS.
     * @param filter The contains filter. Will never be null.
     * @param not True if this should be a  NOT CONTAINS
     * @return The CONTAINS expression. A return value of null means 
     * a native CONTAINS query cannot be created for the given filter. 
     * In this case, {@link #translate} may return an empty query set, meaning
     * fetch <b>everything</b>. The filter will be re-applied in memory
     * to the resulting object stream. This does not scale well, so
     * if possible, you should implement this method.
     */
    protected T createContainsExpression(ContainsFilter filter, boolean not) {
        return null;
    }
    
    /**
     * Should be overridden by subclasses to create a ENDS-WITH expression
     * if the native resource supports ENDS-WITH.
     * @param filter The contains filter. Will never be null.
     * @param not True if this should be a NOT ENDS-WITH
     * @return The ENDS-WITH expression. A return value of null means 
     * a native ENDS-WITH query cannot be created for the given filter. 
     * In this case, {@link #translate} may return an empty query set, meaning
     * fetch <b>everything</b>. The filter will be re-applied in memory
     * to the resulting object stream. This does not scale well, so
     * if possible, you should implement this method.
     */
    protected T createEndsWithExpression(EndsWithFilter filter, boolean not) {
        return null;
    }
    
    /**
     * Should be overridden by subclasses to create a EQUALS expression
     * if the native resource supports EQUALS.
     * @param filter The contains filter. Will never be null.
     * @param not True if this should be a NOT EQUALS
     * @return The EQUALS expression. A return value of null means 
     * a native EQUALS query cannot be created for the given filter. 
     * In this case, {@link #translate} may return an empty query set, meaning
     * fetch <b>everything</b>. The filter will be re-applied in memory
     * to the resulting object stream. This does not scale well, so
     * if possible, you should implement this method.
     */
    protected T createEqualsExpression(EqualsFilter filter, boolean not) {
        return null;
    }
    
    /**
     * Should be overridden by subclasses to create a GREATER-THAN expression
     * if the native resource supports GREATER-THAN.
     * @param filter The contains filter. Will never be null.
     * @param not True if this should be a NOT GREATER-THAN
     * @return The GREATER-THAN expression. A return value of null means 
     * a native GREATER-THAN query cannot be created for the given filter. 
     * In this case, {@link #translate} may return an empty query set, meaning
     * fetch <b>everything</b>. The filter will be re-applied in memory
     * to the resulting object stream. This does not scale well, so
     * if possible, you should implement this method.
     */
    protected T createGreaterThanExpression(GreaterThanFilter filter, boolean not) {
        return null;
    }
    
    /**
     * Should be overridden by subclasses to create a GREATER-THAN-EQUAL expression
     * if the native resource supports GREATER-THAN-EQUAL.
     * @param filter The contains filter. Will never be null.
     * @param not True if this should be a NOT GREATER-THAN-EQUAL
     * @return The GREATER-THAN-EQUAL expression. A return value of null means 
     * a native GREATER-THAN-EQUAL query cannot be created for the given filter. 
     * In this case, {@link #translate} may return an empty query set, meaning
     * fetch <b>everything</b>. The filter will be re-applied in memory
     * to the resulting object stream. This does not scale well, so
     * if possible, you should implement this method.
     */
    protected T createGreaterThanOrEqualExpression(GreaterThanOrEqualFilter filter, boolean not) {
        return null;
    }
    
    /**
     * Should be overridden by subclasses to create a LESS-THAN expression
     * if the native resource supports LESS-THAN.
     * @param filter The contains filter. Will never be null.
     * @param not True if this should be a NOT LESS-THAN
     * @return The LESS-THAN expression. A return value of null means 
     * a native LESS-THAN query cannot be created for the given filter. 
     * In this case, {@link #translate} may return an empty query set, meaning
     * fetch <b>everything</b>. The filter will be re-applied in memory
     * to the resulting object stream. This does not scale well, so
     * if possible, you should implement this method.
     */
    protected T createLessThanExpression(LessThanFilter filter, boolean not) {
        return null;
    }
    
    /**
     * Should be overridden by subclasses to create a LESS-THAN-EQUAL expression
     * if the native resource supports LESS-THAN-EQUAL.
     * @param filter The contains filter. Will never be null.
     * @param not True if this should be a NOT LESS-THAN-EQUAL
     * @return The LESS-THAN-EQUAL expression. A return value of null means 
     * a native LESS-THAN-EQUAL query cannot be created for the given filter. 
     * In this case, {@link #translate} may return an empty query set, meaning
     * fetch <b>everything</b>. The filter will be re-applied in memory
     * to the resulting object stream. This does not scale well, so
     * if possible, you should implement this method.
     */
    protected T createLessThanOrEqualExpression(LessThanOrEqualFilter filter, boolean not) {
        return null;
    }
    
    /**
     * Should be overridden by subclasses to create a STARTS-WITH expression
     * if the native resource supports STARTS-WITH.
     * @param filter The contains filter. Will never be null.
     * @param not True if this should be a NOT STARTS-WITH
     * @return The STARTS-WITH expression. A return value of null means 
     * a native STARTS-WITH query cannot be created for the given filter. 
     * In this case, {@link #translate} may return an empty query set, meaning
     * fetch <b>everything</b>. The filter will be re-applied in memory
     * to the resulting object stream. This does not scale well, so
     * if possible, you should implement this method.
     */
    protected T createStartsWithExpression(StartsWithFilter filter, boolean not) {
        return null;
    }

    /**
     * Should be overridden by subclasses to create a CONTAINS-ALL-VALUES
     * expression if the native resource supports a contains all values.
     * 
     * @param filter
     *            The contains all filter. Will never be null.
     * @param not
     *            True if this should be a NOT CONTAINS-ALL-VALUES.
     * @return The CONTAINS-ALL-VALUES expression. A return value of null means
     *         a native CONTAINS-ALL-VALUES query cannot be created for the
     *         given filter. In this case, {@link #translate} may return an
     *         empty query set, meaning fetch <b>everything</b>. The filter
     *         will be re-applied in memory to the resulting object stream. This
     *         does not scale well, so if possible, you should implement this
     *         method.
     */
    protected T createContainsAllValuesExpression(
            ContainsAllValuesFilter filter, boolean not) {
        return null;
    }
}
