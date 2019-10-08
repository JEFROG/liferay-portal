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

package com.liferay.document.library.kernel.store;

import com.liferay.portal.kernel.exception.PortalException;

import java.io.File;
import java.io.InputStream;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The interface for all file store implementations. Most, if not all
 * implementations should extend from the class {@link BaseStore}.
 *
 * @author Brian Wing Shun Chan
 * @author Edward Han
 * @see    BaseStore
 */
@ProviderType
public interface Store {

	public static final String VERSION_DEFAULT = "1.0";

	/**
	 * Adds a directory.
	 *
	 * @param companyId the primary key of the company
	 * @param repositoryId the primary key of the data repository (optionally
	 *        {@link com.liferay.portal.kernel.model.CompanyConstants#SYSTEM})
	 * @param dirName the directory's name
	 */
	public void addDirectory(long companyId, long repositoryId, String dirName);

	/**
	 * Adds a file based on an {@link InputStream} object.
	 *
	 * @param companyId the primary key of the company
	 * @param repositoryId the primary key of the data repository (optionally
	 *        {@link com.liferay.portal.kernel.model.CompanyConstants#SYSTEM})
	 * @param fileName the file name
	 * @param is the files's data
	 */
	public void addFile(
			long companyId, long repositoryId, String fileName, InputStream is)
		throws PortalException;

	/**
	 * Ensures company's root directory exists.
	 *
	 * @deprecated As of Mueller (7.2.x), with no direct replacement
	 * @param companyId the primary key of the company
	 */
	@Deprecated
	public void checkRoot(long companyId);

	public void copyFileVersion(
			long companyId, long repositoryId, String fileName,
			String fromVersionLabel, String toVersionLabel)
		throws PortalException;

	/**
	 * Deletes a directory.
	 *
	 * @param companyId the primary key of the company
	 * @param repositoryId the primary key of the data repository (optionally
	 *        {@link com.liferay.portal.kernel.model.CompanyConstants#SYSTEM})
	 * @param dirName the directory's name
	 */
	public void deleteDirectory(
		long companyId, long repositoryId, String dirName);

	/**
	 * Deletes a file. If a file has multiple versions, all versions will be
	 * deleted.
	 *
	 * @param companyId the primary key of the company
	 * @param repositoryId the primary key of the data repository (optionally
	 *        {@link com.liferay.portal.kernel.model.CompanyConstants#SYSTEM})
	 * @param fileName the file's name
	 */
	public void deleteFile(long companyId, long repositoryId, String fileName);

	/**
	 * Deletes a file at a particular version.
	 *
	 * @param companyId the primary key of the company
	 * @param repositoryId the primary key of the data repository (optionally
	 *        {@link com.liferay.portal.kernel.model.CompanyConstants#SYSTEM})
	 * @param fileName the file's name
	 * @param versionLabel the file's version label
	 */
	public void deleteFile(
		long companyId, long repositoryId, String fileName,
		String versionLabel);

	public File getFile(
			long companyId, long repositoryId, String fileName,
			String versionLabel)
		throws PortalException;

	public byte[] getFileAsBytes(
			long companyId, long repositoryId, String fileName)
		throws PortalException;

	public byte[] getFileAsBytes(
			long companyId, long repositoryId, String fileName,
			String versionLabel)
		throws PortalException;

	public InputStream getFileAsStream(
			long companyId, long repositoryId, String fileName)
		throws PortalException;

	/**
	 * Returns the file as an {@link InputStream} object.
	 *
	 * @param  companyId the primary key of the company
	 * @param  repositoryId the primary key of the data repository (optionally
	 *         {@link com.liferay.portal.kernel.model.CompanyConstants#SYSTEM})
	 * @param  fileName the file's name
	 * @param  versionLabel the file's version label
	 * @return Returns the {@link InputStream} object with the file's name
	 */
	public InputStream getFileAsStream(
			long companyId, long repositoryId, String fileName,
			String versionLabel)
		throws PortalException;

	public String[] getFileNames(long companyId, long repositoryId);

	/**
	 * Returns all files of the directory.
	 *
	 * @param  companyId the primary key of the company
	 * @param  repositoryId the primary key of the data repository (optionally
	 *         {@link com.liferay.portal.kernel.model.CompanyConstants#SYSTEM})
	 * @param  dirName the directory's name
	 * @return Returns all files of the directory
	 */
	public String[] getFileNames(
		long companyId, long repositoryId, String dirName);

	/**
	 * Returns the size of the file.
	 *
	 * @param  companyId the primary key of the company
	 * @param  repositoryId the primary key of the data repository (optionally
	 *         {@link com.liferay.portal.kernel.model.CompanyConstants#SYSTEM})
	 * @param  fileName the file's name
	 * @return Returns the size of the file
	 */
	public long getFileSize(long companyId, long repositoryId, String fileName)
		throws PortalException;

	/**
	 * Returns <code>true</code> if the directory exists.
	 *
	 * @param  companyId the primary key of the company
	 * @param  repositoryId the primary key of the data repository (optionally
	 *         {@link com.liferay.portal.kernel.model.CompanyConstants#SYSTEM})
	 * @param  dirName the directory's name
	 * @return <code>true</code> if the directory exists; <code>false</code>
	 *         otherwise
	 */
	public boolean hasDirectory(
		long companyId, long repositoryId, String dirName);

	public boolean hasFile(long companyId, long repositoryId, String fileName);

	/**
	 * Returns <code>true</code> if the file exists.
	 *
	 * @param  companyId the primary key of the company
	 * @param  repositoryId the primary key of the data repository (optionally
	 *         {@link com.liferay.portal.kernel.model.CompanyConstants#SYSTEM})
	 * @param  fileName the file's name
	 * @param  versionLabel the file's version label
	 * @return <code>true</code> if the file exists; <code>false</code>
	 *         otherwise
	 */
	public boolean hasFile(
		long companyId, long repositoryId, String fileName,
		String versionLabel);

	/**
	 * @deprecated As of Mueller (7.2.x), with no direct replacement
	 */
	@Deprecated
	public void move(String srcDir, String destDir);

	/**
	 * Moves a file to a new data repository.
	 *
	 * @param companyId the primary key of the company
	 * @param repositoryId the primary key of the data repository
	 * @param newRepositoryId the primary key of the new data repository
	 * @param fileName the file's name
	 */
	public void updateFile(
			long companyId, long repositoryId, long newRepositoryId,
			String fileName)
		throws PortalException;

	public void updateFile(
			long companyId, long repositoryId, String fileName,
			String newFileName)
		throws PortalException;

	public void updateFile(
			long companyId, long repositoryId, String fileName,
			String versionLabel, byte[] bytes)
		throws PortalException;

	public void updateFile(
			long companyId, long repositoryId, String fileName,
			String versionLabel, File file)
		throws PortalException;

	/**
	 * Updates a file based on an {@link InputStream} object.
	 *
	 * @param companyId the primary key of the company
	 * @param repositoryId the primary key of the data repository (optionally
	 *        {@link com.liferay.portal.kernel.model.CompanyConstants#SYSTEM})
	 * @param fileName the file name
	 * @param versionLabel the file's new version label
	 * @param is the new file's data
	 */
	public void updateFile(
			long companyId, long repositoryId, String fileName,
			String versionLabel, InputStream is)
		throws PortalException;

	public void updateFileVersion(
			long companyId, long repositoryId, String fileName,
			String fromVersionLabel, String toVersionLabel)
		throws PortalException;

}