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
// @author David Adam

// do not modify imports
import org.identityconnectors.contract.data.groovy.Lazy
import org.identityconnectors.contract.data.groovy.Random
import org.identityconnectors.contract.data.groovy.Get



/*
 * this configuration is used just by JUnit GroovyDataProvider
 */

aSimpleString = "If you think you can do a thing or think you can't do a thing, you're right. (H. Ford)"
sampleMap.foo.bar = [ 0:"foo", 1:"bar", 2:"wooow!"]
eggs.spam.sausage = "the spanish inquisition"
abc = "abc"

// test random functionality
randomNewAge = Lazy.random("####", Long.class);
remus = Lazy.random("####", Integer.class);

// map escaping of invalid names
attributeMap['string'] = 'Good morning!'
attributeMapSecond['stringSec'] = 'Good morning Mrs. Smith!'
Delete.account['__NAME__'].string = 'blaf'
account['__NAME__'].string = 'blaf blaf'

//literals macro replacement testing
Tfloat= Lazy.random('#####\\.##', Float.class)

//Test ByteArray 
byteArray.test = Lazy.random('AAAAA', byte[].class)

//Test Character
charTest = Lazy.random('AAA', Character.class)

//LIST:
// test list of strings (random)
multi.Tstring=[Lazy.random("AAAAA##") , Lazy.random("AAAAA##")]
//recursive version (lists)
multi.recursive.Tstring=[Lazy.random("AAAAA##") , [Lazy.random("AAA##") , [Lazy.random("AAA##") , Lazy.random('AAAAA\\_##')]]]

abar="foo"
b="bar"
random="aaa" + Lazy.random("####") + "." + Lazy.random("####") + Lazy.get("a"+Lazy.get("b")) 
randomPure="aaa" + Lazy.random("####") + "." + Lazy.random("####")
foo.bla.horror.random="aaa" + Lazy.random("####") + "." + Lazy.random("####") + Lazy.get("a"+Lazy.get("b"))

// test nested parameters
SchemaXX {
	sample = 'Mysterious universe'
}// Schema

// attributes containing __
Schema."__NAME__".attribute.account = 'Ahoj ship!'

// repetitive prefix
aaa.bbb.xxx = "ahoj"

// iterative parameter
i9.param = "foobar"

// get test
rumulus = Lazy.get("remus")

// testing suffix parsing ConfigSlurper
aaa.bbb.ccc = "ahoj"
aaa.bbb.ddd = "ship2"
aaa.bbb.eee = "ship"

mapWithLazyCalls = [key1: Lazy.random("###"), key2: ("rrr" + Lazy.random("###"))]


abcAccount{
    all."__NAME__"="CONUSR-" + Lazy.random("AAAAA")
    all."__PASSWORD__"="tstpwd"
    
    tst.name = "String"
    tst.id = 15
    tst.arl = ["elm1","elm2"]
    tst.ara = ["elm1","elm2"] as String []
    tst.bool = true
}


sampleFooBarList = ["a", "b", "b"];
sampleFooBarListWithLazy = ["a", "b", Lazy.random("AAA\\X")];

Xfirst = "FIRST" + Lazy.random("###")
Xlast = "LAST" + Lazy.random("###")
Xfull = Lazy.get("Xfirst") + " " + Lazy.get("Xlast")

generatedPassword = Lazy.random("###AAA\\a\\h\\o\\j", org.identityconnectors.common.security.GuardedString.class)