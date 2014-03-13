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
 * Portions Copyrighted 2012-2014 ForgeRock AS.
 */
using System;

namespace Org.IdentityConnectors.Common
{
    /// <summary>
    /// Represents a Pair of objects.
    /// </summary>
    public class Pair<T1, T2>
    {

        public Pair()
        {
        }

        /// <summary>
        /// <para>
        /// Obtains an immutable pair of from two objects inferring the generic
        /// types.
        /// </para>
        /// 
        /// <para>
        /// This factory allows the pair to be created using inference to obtain the
        /// generic types.
        /// </para>
        /// </summary>
        /// @param <L>
        ///            the left element type </param>
        /// @param <R>
        ///            the right element type </param>
        /// <param name="left">
        ///            the left element, may be null </param>
        /// <param name="right">
        ///            the right element, may be null </param>
        /// <returns> a pair formed from the two parameters, not null </returns>
        /// <remarks>Since 1.4</remarks>
        public static Pair<L, R> Of<L, R>(L left, R right)
        {
            return new Pair<L, R>(left, right);
        }

        public Pair(T1 f, T2 s)
        {
            this.First = f;
            this.Second = s;
        }

        public T1 First { get; set; }
        public T2 Second { get; set; }


        public override int GetHashCode()
        {
            int rv = 0;
            if (First != null)
            {
                rv ^= First.GetHashCode();
            }
            if (Second != null)
            {
                rv ^= Second.GetHashCode();
            }
            return rv;
        }

        public new bool Equals(object o1, object o2)
        {
            if (o1 == null)
            {
                return o2 == null;
            }
            else if (o2 == null)
            {
                return false;
            }
            else
            {
                return o1.Equals(o2);
            }
        }

        public bool Equals(Pair<T1, T2> obj)
        {
            Pair<T1, T2> other = obj as Pair<T1, T2>;
            if (other != null)
            {
                return Object.Equals(First, other.First) &&
                    Object.Equals(Second, other.Second);
            }
            return false;
        }

        public override bool Equals(object @object)
        {
            var other = @object as Pair<T1, T2>;
            return other != null && Equals(other);
        }

        public override string ToString()
        {
            return "( " + this.First + ", " + this.Second + " )";
        }        
    }
}