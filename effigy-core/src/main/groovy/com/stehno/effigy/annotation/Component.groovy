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

package com.stehno.effigy.annotation

import java.lang.annotation.*

/**
 * Annotation used to denote a property as a one-to-one relationship with the enclosing entity. The one-to-one association expressed by a Component
 * is similar to an <code>@Embedded</code> object; however, this relation data is contained in a separate lookup table, rather than embedded within
 * the same table.
 *
 * The type of the annotated field may be an Entity-annotated class; however, the entity table property as well as any Id, Version annotated fields
 * will be ignored by the Component association functionality.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@Documented
@interface Component {

    /**
     * The database table used to store the component information. If not specified, the default will be a table named as the embedded type name
     * suffixed with an 's'.
     */
    String lookupTable() default ''

    /**
     * The column name of the field used to identify the owning entity in the associated table. If not specified, the default will be the table name
     * of the owning entity suffixed with '_id'.
     */
    String entityColumn() default ''
}
