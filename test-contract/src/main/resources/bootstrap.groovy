// -- START LICENSE
// Copyright 2008 Sun Microsystems, Inc. All rights reserved.
// 
// U.S. Government Rights - Commercial software. Government users 
// are subject to the Sun Microsystems, Inc. standard license agreement
// and applicable provisions of the FAR and its supplements.
// 
// Use is subject to license terms.
// 
// This distribution may include materials developed by third parties.
// Sun, Sun Microsystems, the Sun logo, Java and Project Identity 
// Connectors are trademarks or registered trademarks of Sun 
// Microsystems, Inc. or its subsidiaries in the U.S. and other
// countries.
// 
// UNIX is a registered trademark in the U.S. and other countries,
// exclusively licensed through X/Open Company, Ltd. 
// 
// -----------
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// 
// Copyright 2008 Sun Microsystems, Inc. All rights reserved. 
// 
// The contents of this file are subject to the terms of the Common Development
// and Distribution License(CDDL) (the License).  You may not use this file
// except in  compliance with the License. 
// 
// You can obtain a copy of the License at
// http://identityconnectors.dev.java.net/CDDLv1.0.html
// See the License for the specific language governing permissions and 
// limitations under the License.  
// 
// When distributing the Covered Code, include this CDDL Header Notice in each
// file and include the License file at identityconnectors/legal/license.txt.
// If applicable, add the following below this CDDL Header, with the fields 
// enclosed by brackets [] replaced by your own identifying information: 
// "Portions Copyrighted [year] [name of copyright owner]"
// -----------
// -- END LICENSE
//
// @author Zdenek Louzensky, David Adam

/*
 * Bootstrap configuration script for contract tests.
 */

// !!! import all the helper classes for configuration scripts.
// TODO resolve duplicity of imports in bootstrap.groovy and configfile.groovy
import org.identityconnectors.contract.data.groovy.Lazy
import org.identityconnectors.contract.data.groovy.Random
import org.identityconnectors.contract.data.groovy.Get
import org.identityconnectors.contract.exceptions.ObjectNotFoundException;


//redefine plus method on String
String.metaClass.plus = {Random random->
    Random newRandom = new Random(null);
    newRandom.getSuccessors() << delegate << random
    return newRandom;
}

String.metaClass.plus = {Get get->
    Get newGet = new Get(null);
    newGet.getSuccessors() << delegate << get
    return newGet;
}




/* 
 * Default data for common types
 */
Tstring = Lazy.random("AAAAA##")
Tinteger= Lazy.random("##", Integer.class)
Tint= Lazy.random("##", Integer.class)
Tlong= Lazy.random("#####", Long.class)
Tbiginteger=Lazy.random("#####", java.math.BigInteger.class)
Tfloat=Lazy.random("#####\\.##", Float.class)
Tdouble=Lazy.random("#####\\.##", Double.class)
Tbigdecimal=Lazy.random("#####\\.##", java.math.BigDecimal.class)
Tboolean=false
Tbytearray=Lazy.random(".............", byte[].class)
Tcharacter=Lazy.random(".", Character.class)
Tchar=Lazy.random(".", Character.class)
Tguardedstring=Lazy.random("AAAAA##", org.identityconnectors.common.security.GuardedString.class)



// Default data for multivalue attributes of common types
multi.Tstring=[Lazy.random("AAAAA##") , Lazy.random("AAAAA##")]
multi.Tinteger=[Lazy.random("##", Integer.class), Lazy.random("##", Integer.class)]
multi.Tlong=[Lazy.random("#####", Long.class), Lazy.random("#####", Long.class)]
multi.Tbiginteger=[Lazy.random("#####", java.math.BigInteger.class), Lazy.random("#####", java.math.BigInteger.class)]
multi.Tfloat=[Lazy.random("#####\\.##", Float.class), Lazy.random("#####\\.##", Float.class)]
multi.Tdouble=[Lazy.random("#####\\.##", Double.class), Lazy.random("#####\\.##", Double.class)]
multi.Tbigdecimal=[Lazy.random("#####\\.##", java.math.BigDecimal.class), Lazy.random("#####\\.##", java.math.BigDecimal.class)]
multi.Tboolean=[false, false]
multi.Tbytearray=
	[Lazy.random(".............", byte[].class), Lazy.random(".............", byte[].class)]
multi.Tcharacter = [Lazy.random(".", Character.class), Lazy.random(".", Character.class)]