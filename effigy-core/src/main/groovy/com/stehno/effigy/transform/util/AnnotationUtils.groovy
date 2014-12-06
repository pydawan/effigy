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

import static com.stehno.effigy.transform.util.StringUtils.camelCaseToUnderscore
import static org.codehaus.groovy.ast.ClassHelper.make

import com.stehno.effigy.annotation.Column
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.ClassExpression

/**
 * Created by cjstehno on 11/26/2014.
 */
class AnnotationUtils {

    public static ClassNode extractClass(AnnotationNode annotation, String key) {
        def pair = annotation.members.find { pair -> pair.key == key; };
        if (pair == null) return null;

        assert (pair.value instanceof ClassExpression)
        return pair.value.type;
    }

    public static String extractString(AnnotationNode annotation, String key) {
        def pair = annotation.members.find { pair -> pair.key == key; };
        if (pair) {
            return pair.value.value;
        } else {
            return null;
        }
    }

    // TODO: this does not really belong here, but ok for now
    static String extractFieldName(final FieldNode field) {
        AnnotationNode fieldColumnAnnot = field.getAnnotations(make(Column))[0]
        if (fieldColumnAnnot) {
            return extractString(fieldColumnAnnot, 'value')

        } else {
            return camelCaseToUnderscore(field.name)
        }
    }

    /**
     * Determines whether or not the AnnotatedNode is annotated with at least one of the given annotations.
     *
     * @param node
     * @param annotationClass
     * @return
     */
    public static boolean hasAnnotation(AnnotatedNode node, Class... annotationClass) {
        annotationClass.find { ac ->
            node.getAnnotations(ClassHelper.make(ac))
        }
    }
}