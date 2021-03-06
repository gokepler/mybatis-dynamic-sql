/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql.update.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.mybatis.dynamic.sql.AbstractSqlSupport;
import org.mybatis.dynamic.sql.where.render.WhereSupport;

/**
 * This class combines a "set" clause and a "where" clause into one parameter object
 * that can be sent to a MyBatis3 mapper method.
 * 
 * @author Jeff Butler
 *
 */
public class UpdateSupport extends AbstractSqlSupport {
    private String setClause;
    private Optional<String> whereClause;
    private Map<String, Object> parameters;

    private UpdateSupport(String tableName) {
        super(tableName);
    }

    public String getSetClause() {
        return setClause;
    }

    public String getWhereClause() {
        return whereClause().orElse(EMPTY_STRING);
    }

    public Optional<String> whereClause() {
        return whereClause;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String getFullUpdateStatement() {
        return "update " //$NON-NLS-1$
                + tableName()
                + ONE_SPACE
                + getSetClause()
                + whereClause().map(w -> ONE_SPACE + w).orElse(EMPTY_STRING);
    }
    
    public static class Builder {
        private String tableName;
        private String setClause;
        private Optional<WhereSupport> whereSupport = Optional.empty();
        private Map<String, Object> parameters = new HashMap<>();
        
        public Builder(String tableName) {
            this.tableName = tableName;
        }
        
        public Builder withSetClause(String setClause) {
            this.setClause = setClause;
            return this;
        }
        
        public Builder withWhereSupport(Optional<WhereSupport> whereSupport) {
            this.whereSupport = whereSupport;
            return this;
        }
        
        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }
        
        public UpdateSupport build() {
            UpdateSupport updateSupport = new UpdateSupport(tableName);
            updateSupport.setClause = setClause;
            updateSupport.whereClause = whereSupport.flatMap(ws -> Optional.of(ws.getWhereClause()));
            whereSupport.ifPresent(ws -> parameters.putAll(ws.getParameters()));
            updateSupport.parameters = parameters;
            return updateSupport;
        }
    }
}
