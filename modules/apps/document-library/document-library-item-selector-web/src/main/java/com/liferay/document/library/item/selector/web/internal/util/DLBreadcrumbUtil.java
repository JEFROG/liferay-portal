/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.document.library.item.selector.web.internal.util;

import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortletURLUtil;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.GroupServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Collections;
import java.util.List;

import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Adolfo Pérez
 */
public class DLBreadcrumbUtil {

	public static void addPortletBreadcrumbEntries(
			String displayStyle, Folder folder,
			HttpServletRequest httpServletRequest,
			LiferayPortletResponse liferayPortletResponse,
			PortletURL portletURL, long repositoryId, boolean showGroupSelector)
		throws Exception {

		if (showGroupSelector) {
			_addGroupSelectorBreadcrumbEntry(
				httpServletRequest, liferayPortletResponse, portletURL);
		}

		portletURL.setParameter("displayStyle", displayStyle);

		_addPortletBreadcrumbEntry(
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID, httpServletRequest,
			portletURL,
			_getRepositoryId(folder, httpServletRequest, repositoryId),
			_getRootFolderName(
				httpServletRequest,
				_getRepositoryId(folder, httpServletRequest, repositoryId),
				showGroupSelector));

		if (folder != null) {
			List<Folder> ancestorFolders = folder.getAncestors();

			Collections.reverse(ancestorFolders);

			for (Folder ancestorFolder : ancestorFolders) {
				_addPortletBreadcrumbEntry(
					ancestorFolder.getFolderId(), httpServletRequest,
					portletURL, ancestorFolder.getRepositoryId(),
					ancestorFolder.getName());
			}

			_addPortletBreadcrumbEntry(
				folder.getFolderId(), httpServletRequest, portletURL,
				folder.getRepositoryId(), folder.getName());
		}
	}

	private static void _addGroupSelectorBreadcrumbEntry(
			HttpServletRequest httpServletRequest,
			LiferayPortletResponse liferayPortletResponse,
			PortletURL portletURL)
		throws Exception {

		PortalUtil.addPortletBreadcrumbEntry(
			httpServletRequest,
			LanguageUtil.get(httpServletRequest, "sites-and-libraries"),
			PortletURLBuilder.create(
				PortletURLUtil.clone(portletURL, liferayPortletResponse)
			).setParameter(
				"groupType", "site"
			).setParameter(
				"showGroupSelector", true
			).buildString());
	}

	private static void _addPortletBreadcrumbEntry(
		long folderId, HttpServletRequest httpServletRequest,
		PortletURL portletURL, long repositoryId, String title) {

		portletURL.setParameter("repositoryId", String.valueOf(repositoryId));
		portletURL.setParameter("folderId", String.valueOf(folderId));

		PortalUtil.addPortletBreadcrumbEntry(
			httpServletRequest, title, portletURL.toString());
	}

	private static long _getRepositoryId(
		Folder folder, HttpServletRequest httpServletRequest,
		long repositoryId) {

		if (repositoryId != 0) {
			return repositoryId;
		}

		if (folder != null) {
			return folder.getRepositoryId();
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return themeDisplay.getScopeGroupId();
	}

	private static String _getRootFolderName(
			HttpServletRequest httpServletRequest, long repositoryId,
			boolean showGroupSelector)
		throws Exception {

		if (!showGroupSelector) {
			return LanguageUtil.get(httpServletRequest, "home");
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		Group group = themeDisplay.getScopeGroup();

		if (repositoryId != 0) {
			group = GroupServiceUtil.getGroup(repositoryId);
		}

		return group.getDescriptiveName(themeDisplay.getLocale());
	}

}