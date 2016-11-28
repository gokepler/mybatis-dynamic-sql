/**
 *    Copyright 2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.qbe.sql.update;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class combines a "set" clause and a "where" clause into one parameter object
 * that can be sent to a MyBatis3 mapper method.
 * 
 * @author Jeff Butler
 *
 */
public class UpdateSupport {
    private String setClause;
    private String whereClause;
    private Map<String, Object> parameters;

    private UpdateSupport (String setClause, String whereClause, Map<String, Object> parameters) {
        this.setClause = setClause;
        this.whereClause = whereClause;
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
    }

    public String getSetClause() {
        return setClause;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public static UpdateSupport of(String setClause, String whereClause, Map<String, Object> parameters) {
        return new UpdateSupport(setClause, whereClause, parameters);
    }
}
