/*
 * Copyright (c) 2015 Christopher J. Stehno
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

package com.stehno.effigy.transform

import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.tools.GeneralUtils

import static com.stehno.effigy.transform.model.EntityModel.entityTable
import static com.stehno.effigy.transform.sql.SelectSqlBuilder.select
import static com.stehno.effigy.transform.util.JdbcTemplateHelper.queryForObjectX
import static org.codehaus.groovy.ast.ClassHelper.*
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS

/**
 * Transformer used to process the <code>@Exists</code> annotations.
 */
class ExistsTransformer extends MethodImplementingTransformation {

    @Override
    protected boolean isValidReturnType(ClassNode returnType, ClassNode entityNode) {
        returnType in [Boolean_TYPE, boolean_TYPE]
    }

    @Override
    @SuppressWarnings('GroovyAssignabilityCheck')
    protected void implementMethod(AnnotationNode annotationNode, ClassNode repoNode, ClassNode entityNode, MethodNode methodNode) {
        def sql = select().column('count(*)').from(entityTable(entityNode))

        applyParameters(sql, new AnnotatedMethod(annotationNode, entityNode, methodNode))

        updateMethod repoNode, methodNode, returnS(new BinaryExpression(
            queryForObjectX(
                sql.limit('1').build(),
                new ClassExpression(Integer_TYPE),
                sql.params
            ),
            GeneralUtils.NE,
            constX(0)
        ))
    }
}
