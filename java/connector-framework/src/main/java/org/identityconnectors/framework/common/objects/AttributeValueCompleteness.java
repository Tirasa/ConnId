/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Evolveum. All rights reserved.
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

public enum AttributeValueCompleteness {
	/**
	 * The returned attribute has all values. No value is missing.
	 */
	COMPLETE,
	
	/**
	 * The returned attribute contains only some values.
	 * There may be more attribute values on the resource.
	 * If an empty attribute is returned with an INCOMPLETE
	 * flag then it is assumed that the attribute has at least
	 * one value on the resource.
	 */
	INCOMPLETE, 
	
}
