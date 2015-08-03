package com.adobe.examples.db.resourceprovider.api;

import org.apache.sling.api.resource.ValueMap;

/**
 * ResourceData is an abstraction that is meant to represent the inner state of
 * Resource. Unlike a Resource, it is not bound to a ResourceResolver instance.
 */
public interface ResourceData {

    /**
     * @see org.apache.sling.api.resource.Resource#getPath()
     */
    String getPath();

    /**
     * @see org.apache.sling.api.resource.Resource#getResourceType()
     */
    String getResourceType();

    /**
     * @see org.apache.sling.api.resource.Resource#getResourceSuperType()
     */
    String getResourceSuperType();

    /**
     * @see org.apache.sling.api.resource.Resource#getValueMap()
     */
    ValueMap getValueMap();
}
