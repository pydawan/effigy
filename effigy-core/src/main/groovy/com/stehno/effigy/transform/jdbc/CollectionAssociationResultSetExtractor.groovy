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

package com.stehno.effigy.transform.jdbc

import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.RowMapper

import java.sql.ResultSet
import java.sql.SQLException

import static java.lang.Integer.MAX_VALUE
import static java.lang.Math.min

/**
 * Spring <code>ResultSetExtractor</code> used internally by the Effigy entity transformers to build extractors for handling entity collections
 * associations. This class is not really intended for use outside of the framework.
 */
abstract class CollectionAssociationResultSetExtractor<T> implements ResultSetExtractor<T> {

    /**
     * The name of the identifier property for the entity.
     */
    String entityIdentifier

    /**
     * Limit the number of results returned by the extractor, leave null to retrieve all results.
     */
    Integer limit

    /**
     * Offset the start of the returned results.
     */
    Integer offset

    @Override
    T extractData(final ResultSet rs) throws SQLException, DataAccessException {
        def entities = [:]

        while (rs.next()) {
            def entity = primaryRowMapper().mapRow(rs, 0)

            if (entities.containsKey(entity[entityIdentifier])) {
                mapAssociations(rs, entities[entity[entityIdentifier]])

            } else {
                entities[entity[entityIdentifier]] = entity
                mapAssociations(rs, entity)
            }
        }

        def results = entities.values() as List

        // TODO: this is not very efficient - should be able to cut the limit before processing all records(?)
        int start = offset ?: 0
        int end = start + min(limit ?: MAX_VALUE, results.size())

        results.subList(start, end)
    }

    /**
     * Performs the association mapping.
     *
     * @param rs the active result set
     * @param entity the entity being populated
     */
    abstract protected void mapAssociations(ResultSet rs, entity)

    /**
     * Used to retrieve the row mapper for the primary entity.
     *
     * @return the primary row mapper instance
     */
    abstract protected RowMapper<T> primaryRowMapper()
}
