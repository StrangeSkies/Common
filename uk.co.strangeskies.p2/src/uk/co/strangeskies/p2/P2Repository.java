/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.p2.
 *
 * uk.co.strangeskies.p2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.p2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.p2.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.p2;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * A wrapper for an Eclipse p2 repository.
 * 
 * @author Elias N Vasylenko
 */
public interface P2Repository {
	/**
	 * Property key for repository name.
	 */
	public static final String PROP_NAME = "name";

	/**
	 * Property key for repository location. Setting this property to a value is
	 * equivalent to setting both the {@link #PROP_METADATA_LOCATION} and
	 * {@link #PROP_ARTIFACT_LOCATION} to that value. This property should
	 * therefore not be used in conjunction with those properties.
	 */
	public static final String PROP_LOCATION = "location";

	/**
	 * Property for repository metadata location, to be used alongside the
	 * {@link #PROP_ARTIFACT_LOCATION} property. Typically, this may be the same
	 * as the artifact location, and so {@link #PROP_LOCATION} may be used
	 * instead.
	 */
	public static final String PROP_METADATA_LOCATION = "metadata";

	/**
	 * Property for repository artifact location, to be used alongside the
	 * {@link #PROP_METADATA_LOCATION} property. Typically, this may be the same
	 * as the metadata location, and so {@link #PROP_LOCATION} may be used
	 * instead.
	 */
	public static final String PROP_ARTIFACT_LOCATION = "artifact";

	/**
	 * Property for cache location, with a default given by
	 * {@link #DEFAULT_CACHE_DIR} in the user's home directory.
	 */
	public static final String PROP_CACHE_DIR = "cache";

	/**
	 * Default location for offline caching of repository artifacts.
	 */
	public static final String DEFAULT_CACHE_DIR = ".bnd" + File.separator + "cache" + File.separator + "p2";

	/**
	 * The length of time in seconds that cached artifact downloads will be
	 * retained and remain valid. Defaults to
	 * {@value #DEFAULT_CACHE_TIMEOUT_SECONDS} seconds.
	 */
	public static final String PROP_CACHE_TIMEOUT_SECONDS = "timeout";

	/**
	 * Default cache timeout in seconds.
	 */
	public static final int DEFAULT_CACHE_TIMEOUT_SECONDS = 30;

	/*
	 * Plugin overrides
	 */

	public void setProperties(Map<String, String> map);

	/*
	 * RepositoryPlugin overrides:
	 */

	public PutResult put(InputStream stream, PutOptions options);

	public File get(String bsn, Version version, Map<String, String> properties, DownloadListener... listeners);

	public boolean canWrite();

	public List<String> list(String pattern);

	public SortedSet<Version> versions(String bsn);

	public String getName();

	public String getLocation();

	/*
	 * RemoteRepositoryPlugin overrides:
	 */

	public ResourceHandle getHandle(String bsn, String version, Strategy strategy, Map<String, String> properties);

	public File getCacheDirectory();
}
