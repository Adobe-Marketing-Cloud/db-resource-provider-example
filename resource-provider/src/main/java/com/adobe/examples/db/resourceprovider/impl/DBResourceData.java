package com.adobe.examples.db.resourceprovider.impl;

import com.adobe.examples.db.resourceprovider.api.ResourceData;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.Map;

public class DBResourceData implements ResourceData {

    private static final String SLING_RESOURCE_TYPE = "sling:resourceType";
    private static final String SLING_RESOURCE_SUPER_TYPE = "sling:resourceSuperType";

    private final String path;

    private final ValueMapDecorator properties;

    public DBResourceData(final String path, final String resourceType, final String resourceSuperType, final Map<String, Object> properties) {
        this.path = path;
        properties.put(SLING_RESOURCE_TYPE, resourceType);
        properties.put(SLING_RESOURCE_SUPER_TYPE, resourceSuperType);
        this.properties = new ValueMapDecorator(properties);
    }

    public String getPath() {
        return path;
    }

    public String getResourceType() {
        return properties.get(SLING_RESOURCE_TYPE, "sling/servlet/default");
    }

    public String getResourceSuperType() {
        return properties.get(SLING_RESOURCE_SUPER_TYPE, String.class);
    }

    public ValueMap getValueMap() {
        return properties;
    }
}
