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

package com.liferay.search.experiences.web.internal.blueprint.admin.display.context;

import com.liferay.frontend.taglib.clay.data.set.servlet.taglib.util.ClayDataSetActionDropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.PortletURLUtil;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.search.experiences.constants.SXPActionKeys;
import com.liferay.search.experiences.model.SXPBlueprint;
import com.liferay.search.experiences.web.internal.display.context.helper.SXPRequestHelper;

import java.util.Arrays;
import java.util.List;

import javax.portlet.PortletException;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Kevin Tan
 */
public class ViewSXPBlueprintsDisplayContext {

	public ViewSXPBlueprintsDisplayContext(
		HttpServletRequest httpServletRequest,
		ModelResourcePermission<SXPBlueprint>
			sxpBlueprintModelResourcePermission) {

		_sxpBlueprintModelResourcePermission =
			sxpBlueprintModelResourcePermission;

		_sxpRequestHelper = new SXPRequestHelper(httpServletRequest);
	}

	public String getAPIURL() {
		return "/o/search-experiences-rest/v1.0/sxp-blueprints";
	}

	public List<ClayDataSetActionDropdownItem>
			getClayDataSetActionDropdownItems()
		throws Exception {

		return Arrays.asList(
			new ClayDataSetActionDropdownItem(
				PortletURLBuilder.create(
					getPortletURL()
				).setMVCRenderCommandName(
					"/sxp_blueprint_admin/edit_sxp_blueprint"
				).setParameter(
					"sxpBlueprintId", "{id}"
				).buildString(),
				"pencil", "edit",
				LanguageUtil.get(_sxpRequestHelper.getRequest(), "edit"), "get",
				null, null),
			new ClayDataSetActionDropdownItem(
				getAPIURL() + "/{id}/export", "download", "export",
				LanguageUtil.get(_sxpRequestHelper.getRequest(), "export"),
				"get", null, "blank"),
			new ClayDataSetActionDropdownItem(
				LanguageUtil.get(
					_sxpRequestHelper.getRequest(),
					"are-you-sure-you-want-to-delete-this-entry"),
				getAPIURL() + "/{id}", "trash", "delete",
				LanguageUtil.get(_sxpRequestHelper.getRequest(), "delete"),
				"delete", null, "async"));
	}

	public CreationMenu getCreationMenu() throws Exception {
		CreationMenu creationMenu = new CreationMenu();

		if (!_hasAddSXPBlueprintPermission()) {
			return creationMenu;
		}

		creationMenu.addDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref("addSXPBlueprint");
				dropdownItem.setLabel(
					LanguageUtil.get(
						_sxpRequestHelper.getRequest(), "add-blueprint"));
				dropdownItem.setTarget("event");
			});

		return creationMenu;
	}

	public PortletURL getPortletURL() throws PortletException {
		return PortletURLUtil.clone(
			PortletURLUtil.getCurrent(
				_sxpRequestHelper.getLiferayPortletRequest(),
				_sxpRequestHelper.getLiferayPortletResponse()),
			_sxpRequestHelper.getLiferayPortletResponse());
	}

	private boolean _hasAddSXPBlueprintPermission() {
		PortletResourcePermission portletResourcePermission =
			_sxpBlueprintModelResourcePermission.getPortletResourcePermission();

		return portletResourcePermission.contains(
			_sxpRequestHelper.getPermissionChecker(), null,
			SXPActionKeys.ADD_SXP_BLUEPRINT);
	}

	private final ModelResourcePermission<SXPBlueprint>
		_sxpBlueprintModelResourcePermission;
	private final SXPRequestHelper _sxpRequestHelper;

}