/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.framework.context;
import org.apache.tiles.TilesApplicationContext;
import org.apache.tiles.TilesContainer;
import org.apache.tiles.compat.definition.digester.CompatibilityDigesterDefinitionsReader;
import org.apache.tiles.context.TilesRequestContextFactory;
import org.apache.tiles.definition.DefinitionsReader;
import org.apache.tiles.evaluator.AttributeEvaluatorFactory;
import org.apache.tiles.factory.BasicTilesContainerFactory;
import org.apache.tiles.renderer.impl.BasicRendererFactory;
import org.apache.tiles.renderer.impl.TemplateAttributeRenderer;

/**
 * Apache Tiles container factory.
 */
public class ATilesContainerFactory extends BasicTilesContainerFactory {

	@Override
	protected DefinitionsReader createDefinitionsReader(
			TilesApplicationContext applicationContext,
			TilesRequestContextFactory contextFactory) {
		return new CompatibilityDigesterDefinitionsReader();
	}

	@Override
	protected void registerAttributeRenderers(
			BasicRendererFactory rendererFactory,
			TilesApplicationContext applicationContext,
			TilesRequestContextFactory contextFactory, TilesContainer container,
			AttributeEvaluatorFactory attributeEvaluatorFactory) {
		// TODO Auto-generated method stub
		super.registerAttributeRenderers(rendererFactory, applicationContext,
				contextFactory, container, attributeEvaluatorFactory);
    TemplateAttributeRenderer templateRenderer = new TemplateAttributeRenderer();
    templateRenderer.setApplicationContext(applicationContext);
    templateRenderer.setRequestContextFactory(contextFactory);
    templateRenderer.setAttributeEvaluatorFactory(attributeEvaluatorFactory);
    rendererFactory.registerRenderer("page",templateRenderer);
	}

	
	
}
