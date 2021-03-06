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

import java.util.Collections;
import java.util.Optional;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.UpdateMapping;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;
import org.mybatis.dynamic.sql.where.render.WhereSupport;

public class UpdateRenderer {
    private UpdateModel updateModel;
    
    private UpdateRenderer(UpdateModel updateModel) {
        this.updateModel = updateModel;
    }
    
    public UpdateSupport render(RenderingStrategy renderingStrategy) {
        SetPhraseVisitor visitor = new SetPhraseVisitor(renderingStrategy);
        Optional<WhereSupport> whereSupport = renderWhere(renderingStrategy);
        
        return updateModel.columnValues()
                .map(cv -> transform(cv, visitor))
                .collect(UpdateFragmentCollector.toUpdateSupport(updateModel.table(), whereSupport));
    }
    
    private Optional<WhereSupport> renderWhere(RenderingStrategy renderingStrategy) {
        return updateModel.whereModel().flatMap(
                wm -> Optional.of(WhereRenderer.of(wm, renderingStrategy, Collections.emptyMap()).render()));
    }
    
    private FragmentAndParameters transform(UpdateMapping columnAndValue, SetPhraseVisitor visitor) {
        return columnAndValue.accept(visitor);
    }
    
    public static UpdateRenderer of(UpdateModel updateModel) {
        return new UpdateRenderer(updateModel);
    }
}
