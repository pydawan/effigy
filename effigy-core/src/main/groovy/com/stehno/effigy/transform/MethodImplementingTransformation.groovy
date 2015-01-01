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

package com.stehno.effigy.transform

import com.stehno.effigy.annotation.Limit
import com.stehno.effigy.annotation.Offset
import com.stehno.effigy.transform.sql.SqlTemplate
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter

import static com.stehno.effigy.logging.Logger.error
import static com.stehno.effigy.logging.Logger.trace
import static com.stehno.effigy.transform.model.EntityModel.entityProperty
import static com.stehno.effigy.transform.util.AnnotationUtils.extractString
import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * Abstract parent class for the Effigy CRUD method implementation annotation transformers.
 */
abstract class MethodImplementingTransformation implements RepositoryMethodVisitor {

    @Override
    void visit(ClassNode repoNode, ClassNode entityNode, AnnotationNode annotationNode, MethodNode methodNode) {
        trace getClass(), 'Implementing method ({}) for repository ({})', methodNode.name, repoNode.name
        try {
            if (isValidReturnType(methodNode.returnType, entityNode)) {
                implementMethod annotationNode, repoNode, entityNode, methodNode

            } else {
                error(
                    getClass(),
                    'Return type for repository ({}) method ({}) is not valid for the provided annotation ({}).',
                    repoNode.name,
                    methodNode.name,
                    annotationNode.classNode.nameWithoutPackage
                )
                throw new EffigyTransformationException()
            }

        } catch (EffigyTransformationException etex) {
            throw etex

        } catch (ex) {
            error(
                getClass(),
                'Unable to implement {} method ({}) for ({}): {}',
                annotationNode.classNode.nameWithoutPackage,
                methodNode.name,
                repoNode.name,
                ex.message
            )
            ex.printStackTrace()
            throw ex
        }
    }

    abstract protected boolean isValidReturnType(ClassNode returnType, ClassNode entityNode)

    abstract protected void implementMethod(AnnotationNode annotationNode, ClassNode repoNode, ClassNode entityNode, MethodNode methodNode)

    protected static boolean isDeclaredMethod(ClassNode repoNode, MethodNode methodNode) {
        repoNode.hasDeclaredMethod(methodNode.name, methodNode.parameters)
    }

    @SuppressWarnings('GroovyAssignabilityCheck')
    protected static List extractParameters(AnnotationNode annotationNode, ClassNode entityNode, MethodNode methodNode, boolean ignoreFirst = false) {
        def wheres = []
        def params = []

        SqlTemplate template = extractSqlTemplate(annotationNode)
        if (template) {
            wheres << template.sql(entityNode)
            params.addAll(template.variableNames().collect { vn -> varX(vn[1..-1]) })

        } else {
            parameters(methodNode.parameters, ignoreFirst).findAll { p -> !p.getAnnotations(make(Limit)) && !p.getAnnotations(make(Offset)) }.each { mp ->
                wheres << "${entityProperty(entityNode, mp.name).columnName}=?"
                params << varX(mp.name)
            }
        }

        [wheres, params]
    }

    private static List parameters(Parameter[] params, boolean ignoreFirst) {
        if (ignoreFirst) {
            (params as List).tail()
        } else {
            params
        }
    }

    private static SqlTemplate extractSqlTemplate(final AnnotationNode node) {
        String value = extractString(node, 'value')
        value ? new SqlTemplate(value) : null
    }
}
