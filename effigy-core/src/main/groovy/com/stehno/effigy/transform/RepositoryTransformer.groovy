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

import static com.stehno.effigy.logging.Logger.info
import static com.stehno.effigy.transform.util.AnnotationUtils.hasAnnotation
import static org.codehaus.groovy.ast.ClassHelper.make

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

import java.lang.reflect.Modifier

/**
 * Transformer used for processing the EffigyRepository annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class RepositoryTransformer implements ASTTransformation {

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        ClassNode repositoryNode = nodes[1] as ClassNode

        removeAbstract repositoryNode
        applyRepositoryAnnotation repositoryNode
        injectJdbcTemplate repositoryNode
    }

    private static void removeAbstract(ClassNode repositoryClassNode) {
        if (Modifier.isAbstract(repositoryClassNode.modifiers)) {
            repositoryClassNode.modifiers = Modifier.PUBLIC
            info RepositoryTransformer, 'Removed abstract from repository class ({}).', repositoryClassNode.name
        }
    }

    /**
     * Apply the Spring Repository annotation so that the Effigy Repository is essentially an extension of it.
     */
    private static void applyRepositoryAnnotation(ClassNode repositoryNode) {
        if (!hasAnnotation(repositoryNode, Repository)) {
            repositoryNode.addAnnotation(new AnnotationNode(make(Repository)))
        }
    }

    private static void injectJdbcTemplate(ClassNode repositoryClassNode) {
        FieldNode jdbcTemplateFieldNode = new FieldNode(
            'jdbcTemplate', Modifier.PRIVATE, new ClassNode(JdbcTemplate), repositoryClassNode, new EmptyExpression()
        )

        jdbcTemplateFieldNode.addAnnotation(new AnnotationNode(new ClassNode(Autowired)))

        repositoryClassNode.addField(jdbcTemplateFieldNode)

        info RepositoryTransformer, 'Added autowired JdbcTemplate property to repository class ({}).', repositoryClassNode.name
    }
}