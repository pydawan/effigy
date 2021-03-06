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

import com.stehno.effigy.transform.model.AssociationPropertyModel
import groovy.util.logging.Slf4j
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation

import static com.stehno.effigy.transform.model.EntityModel.associations
import static com.stehno.effigy.transform.model.EntityModel.identifier
import static com.stehno.effigy.transform.util.AstUtils.codeS
import static com.stehno.effigy.transform.util.AstUtils.methodN
import static java.lang.reflect.Modifier.PROTECTED
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GenericsUtils.newClass

/**
 * Generates the entity association saving method for <cod>@Create</code> and <code>@Update</code> annotations.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION) @Slf4j
class AssociationSaveMethodInjector implements RepositoryMethodVisitor {

    private static final String ENTITY = 'entity'

    @Override
    void visit(ClassNode repoNode, ClassNode entityNode, AnnotationNode annotationNode, MethodNode methodNode) {
        associations(entityNode).each { AssociationPropertyModel ap ->
            injectAssociationSaveMethod repoNode, entityNode, ap
        }
    }

    @SuppressWarnings('GStringExpressionWithinString')
    private static void injectAssociationSaveMethod(ClassNode repositoryNode, ClassNode entityNode, AssociationPropertyModel assoc) {
        def methodName = "save${assoc.propertyName.capitalize()}"
        def methodParams = [param(newClass(entityNode), ENTITY)] as Parameter[]

        if (!repositoryNode.hasMethod(methodName, methodParams)) {
            log.debug 'Injecting association ({}) save method for entity ({})', assoc.propertyName, entityNode
            try {
                // FIXME: pre-compile the collection vs single entity stuff
                def statement = codeS(
                    '''
                        int expects = 0
                        if( entity.${name} instanceof Collection || entity.${name} instanceof Map ){
                            expects = entity.${name}?.size() ?: 0
                        } else {
                            expects = entity.${name} != null ? 1 : 0
                        }

                        int count = 0
                        def ent = entity

                        jdbcTemplate.update('delete from $assocTable where $tableEntIdName=?', ent.${entityIdName})

                        if( ent.${name} ){
                            if( entity.${name} instanceof Collection ){
                                entity.${name}?.each { itm->
                                    count += jdbcTemplate.update(
                                        'insert into $assocTable ($tableEntIdName,$tableAssocIdName) values (?,?)',
                                        ent.${entityIdName},
                                        itm.${assocIdName}
                                    )
                                }
                            } else if( entity.${name} instanceof Map ){
                                entity.${name}?.each { key,itm->
                                    count += jdbcTemplate.update(
                                        'insert into $assocTable ($tableEntIdName,$tableAssocIdName) values (?,?)',
                                        ent.${entityIdName},
                                        itm.${assocIdName}
                                    )
                                }

                            } else {
                                count += jdbcTemplate.update(
                                    'insert into $assocTable ($tableEntIdName,$tableAssocIdName) values (?,?)',
                                    ent.${entityIdName},
                                    ent.${name}.${assocIdName}
                                )
                            }
                        }

                        if( count != expects ){
                            entity.${entityIdName} = 0
                            throw new RuntimeException(
                                'Insert count for $name (' + count + ') did not match expected count (' + expects + ') - save failed.'
                            )
                        }
                    ''',
                    name: assoc.propertyName,
                    assocTable: assoc.joinTable,
                    tableEntIdName: assoc.entityColumn,
                    tableAssocIdName: assoc.assocColumn,
                    entityIdName: identifier(entityNode).propertyName,
                    assocIdName: identifier(assoc.associatedType).propertyName
                )

                repositoryNode.addMethod(methodN(PROTECTED, methodName, VOID_TYPE, statement, methodParams))

            } catch (ex) {
                log.error 'Unable to inject association save method for entity ({}): {}', entityNode.name, ex.message
                throw ex
            }
        }
    }
}
