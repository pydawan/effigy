/*
 * Copyright (c) 2014 Christopher J. Stehno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stehno.effigy.transform.util

import static java.sql.Types.*

import org.codehaus.groovy.ast.FieldNode

/**
 * Created by cjstehno on 12/6/2014.
 */
class TransformUtils {

    static int findSqlType(final FieldNode fieldNode) {
        if (fieldNode.type.enum) return VARCHAR

        switch (fieldNode.type.name) {
            case 'java.lang.String': return VARCHAR
            case 'java.sql.Date':
            case 'java.util.Date':
                return TIMESTAMP
            case 'java.lang.Boolean':
            case 'boolean':
                return BOOLEAN
            case 'java.lang.Integer': return INTEGER
            case 'java.lang.Long':
            case 'long':
                return BIGINT
            default: return JAVA_OBJECT
        }
    }
}