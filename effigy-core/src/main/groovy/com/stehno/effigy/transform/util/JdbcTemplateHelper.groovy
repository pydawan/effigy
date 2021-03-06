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

package com.stehno.effigy.transform.util

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.Statement
import org.springframework.jdbc.core.SingleColumnRowMapper

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE
import static org.codehaus.groovy.ast.tools.GeneralUtils.*
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafe
import static org.codehaus.groovy.ast.tools.GenericsUtils.newClass

/**
 * AST helper for building interactions with the Spring JdbcTemplate.
 */
class JdbcTemplateHelper {

    private static final String JDBC_TEMPLATE = 'jdbcTemplate'
    private static final String COLLECTION_ASSOCIATION_EXTRACTOR = 'collectionAssociationExtractor'
    private static final String QUERY = 'query'
    private static final String QUERY_FOR_OBJECT = 'queryForObject'
    private static final String UPDATE = 'update'

    /**
     * Expression used to access the RowMapper accessor method for an entity.
     *
     * @param entityNode the entity node
     * @return the call expression
     */
    static Expression entityRowMapper(ClassNode entityNode) {
        callX(classX(newClass(entityNode)), 'rowMapper')
    }

    static Expression entityCollectionExtractor(ClassNode entityNode, Expression offsetX = null, Expression limitX = null) {
        if (limitX && offsetX) {
            callX(classX(newClass(entityNode)), COLLECTION_ASSOCIATION_EXTRACTOR, args(offsetX, limitX))
        } else if (limitX) {
            callX(classX(newClass(entityNode)), COLLECTION_ASSOCIATION_EXTRACTOR, args(constX(null), limitX))
        } else if (offsetX) {
            callX(classX(newClass(entityNode)), COLLECTION_ASSOCIATION_EXTRACTOR, args(offsetX, constX(null)))
        } else {
            callX(classX(newClass(entityNode)), COLLECTION_ASSOCIATION_EXTRACTOR)
        }
    }

    static Expression entityExtractor(ClassNode entityNode) {
        callX(classX(newClass(entityNode)), 'associationExtractor')
    }

    @SuppressWarnings('ConfusingMethodName')
    static Statement query(String sql, Expression handler, List<Expression> params = []) {
        returnS(
            callX(varX(JDBC_TEMPLATE), QUERY, queryArgs(sql, handler, params))
        )
    }

    static Expression queryX(String sql, Expression handler, List<Expression> params = []) {
        callX(varX(JDBC_TEMPLATE), QUERY, queryArgs(sql, handler, params))
    }

    static Expression queryX(String sql, Expression handler, Expression setter) {
        callX(varX(JDBC_TEMPLATE), QUERY, args(constX(sql), setter, handler))
    }

    static Statement queryForObject(String sql, Expression handler, List<Expression> params = []) {
        returnS(
            callX(varX(JDBC_TEMPLATE), QUERY_FOR_OBJECT, queryArgs(sql, handler, params))
        )
    }

    static Expression queryForObjectX(String sql, Expression handler, List<Expression> params = []) {
        callX(varX(JDBC_TEMPLATE), QUERY_FOR_OBJECT, queryArgs(sql, handler, params))
    }

    static Expression updateX(String sql, List<Expression> params = []) {
        callX(varX(JDBC_TEMPLATE), UPDATE, updateArgs(sql, params))
    }

    static Expression updateX(String sql, Expression setter) {
        callX(varX(JDBC_TEMPLATE), UPDATE, args(constX(sql), setter))
    }

    static Expression singleColumnRowMapper(ClassNode requiredType = OBJECT_TYPE) {
        ctorX(makeClassSafe(SingleColumnRowMapper), args(classX(requiredType.typeClass)))
    }

    private static ArgumentListExpression updateArgs(String sql, List<Expression> params) {
        ArgumentListExpression arguments = args(constX(sql))

        params.each { pex ->
            arguments.addExpression(pex)
        }

        arguments
    }

    private static ArgumentListExpression queryArgs(String sql, Expression handler, List<Expression> params) {
        ArgumentListExpression arguments = args(
            constX(sql),
            handler
        )

        params.each { pex ->
            arguments.addExpression(pex)
        }

        arguments
    }
}
