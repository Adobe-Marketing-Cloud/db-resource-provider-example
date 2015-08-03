package com.adobe.examples.db.resourceprovider.api;

import java.util.Map;

/**
 * The ResourceDataFactory is an abstraction that allows interaction with
 * an underlying (possibly persistent) data source.
 */
public interface ResourceDataFactory {

    /**
     * Create a new ResourceData object that is not connected to the factory's state. Useful for
     * creating transient data that is later persisted when
     * {@link org.apache.sling.api.resource.ModifyingResourceProvider#commit(org.apache.sling.api.resource.ResourceResolver)}
     * is called.
     *
     * @param  path The path of the {@code ResourceData} object.
     * @param properties The data of the {@code ResourceData} object.
     * @return The newly created {@code ResourceData} object.
     */
    ResourceData createResourceData(final String path, final Map<String, Object> properties);

    /**
     * Get a ResourceData instance from the underlying persistence.
     *
     * @param path The path of the resource to fetch.
     * @return A ResourceData instance corresponding to the path.
     */
    ResourceData getResourceData(String path);

    /**
     * Get an iterable of the paths of all direct children of a given path.
     *
     * @param path The path for which to list the child paths.
     * @return An iterable of the child paths. Never null.
     */
    Iterable<String> getChildPaths(String path);

    /**
     * Write back the properties of a ResourceData object to the underlying persistence
     * at a given path.
     *
     * @param path The path where the data should be located.
     * @param resourceData The ResourceData object to be persisted.
     */
    void putResourceData(String path, ResourceData resourceData);

    /**
     * Whether or not the underlying persistence is 'live', i.e. reachable, not closed, etc.
     *
     * @return liveness state of this ResourceDataFactory
     */
    boolean isLive();

    /**
     * Allows this ResourceDataFactory to be closed. After a call to this method
     * {@link #isLive()} must return false. Any underlying resources should be cleaned
     * up in this method.
     */
    void close();
}
