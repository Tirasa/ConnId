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
using System;

namespace Org.IdentityConnectors.Common
{
    /// <summary>
    /// Represents a Pair of objects
    /// </summary>
    public class Pair<T1, T2>
    {
        public Pair()
        {
        }

        public Pair(T1 first, T2 second)
        {
            First = first;
            Second = second;
        }

        public T1 First { get; set; }
        public T2 Second { get; set; }

        public override bool Equals(object obj)
        {
            Pair<T1, T2> other = obj as Pair<T1, T2>;
            if (other != null)
            {
                return Object.Equals(First, other.First) &&
                    Object.Equals(Second, other.Second);
            }
            return false;
        }

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

        public override string ToString()
        {
            return "( " + First + ", " + Second + " )";
        }
    }
}