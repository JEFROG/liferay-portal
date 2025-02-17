/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.internal.blueprint.search.request.enhancer;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.geolocation.GeoBuilders;
import com.liferay.portal.search.highlight.FieldConfigBuilderFactory;
import com.liferay.portal.search.highlight.HighlightBuilderFactory;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.rescore.RescoreBuilderFactory;
import com.liferay.portal.search.script.Scripts;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.significance.SignificanceHeuristics;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.search.experiences.blueprint.parameter.SXPParameter;
import com.liferay.search.experiences.blueprint.search.request.enhancer.SXPBlueprintSearchRequestEnhancer;
import com.liferay.search.experiences.internal.blueprint.highlight.HighlightConverter;
import com.liferay.search.experiences.internal.blueprint.parameter.SXPParameterData;
import com.liferay.search.experiences.internal.blueprint.parameter.SXPParameterDataCreator;
import com.liferay.search.experiences.internal.blueprint.query.QueryConverter;
import com.liferay.search.experiences.internal.blueprint.script.ScriptConverter;
import com.liferay.search.experiences.internal.blueprint.search.request.body.contributor.AggsSXPSearchRequestBodyContributor;
import com.liferay.search.experiences.internal.blueprint.search.request.body.contributor.GeneralSXPSearchRequestBodyContributor;
import com.liferay.search.experiences.internal.blueprint.search.request.body.contributor.HighlightSXPSearchRequestBodyContributor;
import com.liferay.search.experiences.internal.blueprint.search.request.body.contributor.QuerySXPSearchRequestBodyContributor;
import com.liferay.search.experiences.internal.blueprint.search.request.body.contributor.SXPSearchRequestBodyContributor;
import com.liferay.search.experiences.internal.blueprint.search.request.body.contributor.SortSXPSearchRequestBodyContributor;
import com.liferay.search.experiences.internal.blueprint.search.request.body.contributor.SuggestSXPSearchRequestBodyContributor;
import com.liferay.search.experiences.rest.dto.v1_0.Configuration;
import com.liferay.search.experiences.rest.dto.v1_0.ElementDefinition;
import com.liferay.search.experiences.rest.dto.v1_0.ElementInstance;
import com.liferay.search.experiences.rest.dto.v1_0.Field;
import com.liferay.search.experiences.rest.dto.v1_0.FieldSet;
import com.liferay.search.experiences.rest.dto.v1_0.SXPBlueprint;
import com.liferay.search.experiences.rest.dto.v1_0.SXPElement;
import com.liferay.search.experiences.rest.dto.v1_0.UiConfiguration;
import com.liferay.search.experiences.rest.dto.v1_0.util.ConfigurationUtil;
import com.liferay.search.experiences.rest.dto.v1_0.util.SXPBlueprintUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	enabled = false, immediate = true,
	service = SXPBlueprintSearchRequestEnhancer.class
)
public class SXPBlueprintSearchRequestEnhancerImpl
	implements SXPBlueprintSearchRequestEnhancer {

	@Override
	public void enhance(
		SearchRequestBuilder searchRequestBuilder, String sxpBlueprintJSON) {

		_enhance(
			searchRequestBuilder,
			SXPBlueprintUtil.toSXPBlueprint(sxpBlueprintJSON));
	}

	@Override
	public void enhance(
		SearchRequestBuilder searchRequestBuilder,
		com.liferay.search.experiences.model.SXPBlueprint sxpBlueprint) {

		DTOConverter
			<com.liferay.search.experiences.model.SXPBlueprint, SXPBlueprint>
				dtoConverter = _getDTOConverter();

		try {
			_enhance(searchRequestBuilder, dtoConverter.toDTO(sxpBlueprint));
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Activate
	protected void activate() {
		HighlightConverter highlightConverter = new HighlightConverter(
			_fieldConfigBuilderFactory, _highlightBuilderFactory);
		QueryConverter queryConverter = new QueryConverter(_queries);
		ScriptConverter scriptConverter = new ScriptConverter(_scripts);

		_sxpSearchRequestBodyContributors = Arrays.asList(

			// TODO AdvancedSXPSearchRequestBodyContributor with fetchSource

			new AggsSXPSearchRequestBodyContributor(
				_aggregations, _geoBuilders, highlightConverter, queryConverter,
				scriptConverter, _significanceHeuristics, _sorts),
			new GeneralSXPSearchRequestBodyContributor(),
			new HighlightSXPSearchRequestBodyContributor(highlightConverter),
			new QuerySXPSearchRequestBodyContributor(
				_complexQueryPartBuilderFactory, queryConverter,
				_rescoreBuilderFactory),
			new SuggestSXPSearchRequestBodyContributor(),
			new SortSXPSearchRequestBodyContributor(
				_geoBuilders, queryConverter, scriptConverter, _sorts));
	}

	private void _contributeSXPSearchRequestBodyContributors(
		Configuration configuration, SearchRequestBuilder searchRequestBuilder,
		SXPParameterData sxpParameterData) {

		if (ListUtil.isEmpty(_sxpSearchRequestBodyContributors)) {
			return;
		}

		RuntimeException runtimeException = new RuntimeException();

		for (SXPSearchRequestBodyContributor sxpSearchRequestBodyContributor :
				_sxpSearchRequestBodyContributors) {

			try {
				sxpSearchRequestBodyContributor.contribute(
					configuration, searchRequestBuilder, sxpParameterData);
			}
			catch (Exception exception) {
				runtimeException.addSuppressed(exception);
			}
		}

		if (ArrayUtil.isNotEmpty(runtimeException.getSuppressed())) {
			throw runtimeException;
		}
	}

	private void _enhance(
		ElementInstance elementInstance,
		PropertyExpander.PropertyResolver propertyResolver,
		SearchRequestBuilder searchRequestBuilder,
		SXPParameterData sxpParameterData) {

		SXPElement sxpElement = elementInstance.getSxpElement();

		ElementDefinition elementDefinition = sxpElement.getElementDefinition();

		_contributeSXPSearchRequestBodyContributors(
			_expand(
				elementDefinition.getConfiguration(), propertyResolver,
				(name, options) -> {
					String shortName = StringUtils.substringAfter(
						name, "configuration.");

					if (Validator.isNull(shortName)) {
						return null;
					}

					Map<String, Object> values =
						elementInstance.getUiConfigurationValues();

					return _unpack(
						values.get(shortName),
						_getFieldType(
							shortName, elementDefinition.getUiConfiguration()));
				}),
			searchRequestBuilder, sxpParameterData);
	}

	private void _enhance(
		SearchRequestBuilder searchRequestBuilder, SXPBlueprint sxpBlueprint) {

		if ((sxpBlueprint.getConfiguration() == null) &&
			ArrayUtil.isEmpty(sxpBlueprint.getElementInstances())) {

			return;
		}

		SXPParameterData sxpParameterData = _sxpParameterDataCreator.create(
			searchRequestBuilder.withSearchContextGet(
				searchContext -> searchContext),
			sxpBlueprint);

		PropertyExpander.PropertyResolver propertyResolver =
			(name, options) -> {
				SXPParameter sxpParameter =
					sxpParameterData.getSXPParameterByName(name);

				if ((sxpParameter == null) ||
					!sxpParameter.isTemplateVariable()) {

					return null;
				}

				return sxpParameter.evaluateToString(options);
			};

		if (sxpBlueprint.getConfiguration() != null) {
			_contributeSXPSearchRequestBodyContributors(
				_expand(sxpBlueprint.getConfiguration(), propertyResolver),
				searchRequestBuilder, sxpParameterData);
		}

		ArrayUtil.isNotEmptyForEach(
			sxpBlueprint.getElementInstances(),
			elementInstance -> _enhance(
				elementInstance, propertyResolver, searchRequestBuilder,
				sxpParameterData));
	}

	private Configuration _expand(
		Configuration configuration,
		PropertyExpander.PropertyResolver... propertyResolvers) {

		PropertyExpander propertyExpander = new PropertyExpander(
			propertyResolvers);

		return ConfigurationUtil.toConfiguration(
			propertyExpander.expand(String.valueOf(configuration)));
	}

	private DTOConverter
		<com.liferay.search.experiences.model.SXPBlueprint, SXPBlueprint>
			_getDTOConverter() {

		String dtoClassName =
			com.liferay.search.experiences.model.SXPBlueprint.class.getName();

		return (DTOConverter
			<com.liferay.search.experiences.model.SXPBlueprint, SXPBlueprint>)
				_dtoConverterRegistry.getDTOConverter(dtoClassName);
	}

	private Field _getField(Field[] fields, String name) {
		if (ArrayUtil.isEmpty(fields)) {
			return null;
		}

		for (Field field : fields) {
			if (Objects.equals(field.getName(), name)) {
				return field;
			}
		}

		return null;
	}

	private Field _getField(FieldSet[] fieldSets, String name) {
		if (ArrayUtil.isEmpty(fieldSets)) {
			return null;
		}

		for (FieldSet fieldSet : fieldSets) {
			Field field = _getField(fieldSet.getFields(), name);

			if (field != null) {
				return field;
			}
		}

		return null;
	}

	private String _getFieldType(String name, UiConfiguration uiConfiguration) {
		Field field = _getField(uiConfiguration.getFieldSets(), name);

		if (field != null) {
			return field.getType();
		}

		return null;
	}

	private String _toFieldMappingString(JSONObject jsonObject) {
		StringBundler sb = new StringBundler(5);

		sb.append(jsonObject.get("field"));

		String locale = jsonObject.getString("locale");

		if (Validator.isNotNull(locale)) {
			sb.append(CharPool.UNDERLINE);
			sb.append(locale);
		}

		Object boost = jsonObject.get("boost");

		if (boost != null) {
			sb.append(CharPool.CARET);
			sb.append(boost);
		}

		return sb.toString();
	}

	private Object _unpack(Object value, String type) {
		if ((value instanceof JSONObject) &&
			Objects.equals(type, "fieldMapping")) {

			return _toFieldMappingString((JSONObject)value);
		}

		if ((value instanceof JSONArray) &&
			Objects.equals(type, "fieldMappingList")) {

			List<String> fields = new ArrayList<>();

			for (Object item : (JSONArray)value) {
				fields.add(_toFieldMappingString((JSONObject)item));
			}

			return JSONFactoryUtil.createJSONArray(fields);
		}

		if ((value instanceof String) && Objects.equals(type, "json")) {
			try {
				return JSONFactoryUtil.createJSONObject((String)value);
			}
			catch (JSONException jsonException) {
				return ReflectionUtil.throwException(jsonException);
			}
		}

		return value;
	}

	@Reference
	private Aggregations _aggregations;

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

	@Reference
	private FieldConfigBuilderFactory _fieldConfigBuilderFactory;

	@Reference
	private GeoBuilders _geoBuilders;

	@Reference
	private HighlightBuilderFactory _highlightBuilderFactory;

	@Reference
	private Queries _queries;

	@Reference
	private RescoreBuilderFactory _rescoreBuilderFactory;

	@Reference
	private Scripts _scripts;

	@Reference
	private SignificanceHeuristics _significanceHeuristics;

	@Reference
	private Sorts _sorts;

	@Reference
	private SXPParameterDataCreator _sxpParameterDataCreator;

	private List<SXPSearchRequestBodyContributor>
		_sxpSearchRequestBodyContributors;

}