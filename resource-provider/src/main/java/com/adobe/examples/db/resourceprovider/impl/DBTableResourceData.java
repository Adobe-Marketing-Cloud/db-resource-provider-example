package com.adobe.examples.db.resourceprovider.impl;

import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DBTableResourceData implements ResourceData {

    private static final Logger LOG = LoggerFactory.getLogger(DBTableResourceData.class);

    private final String path;

    private final ValueMap properties;

    public DBTableResourceData(final String path, final Map<String, Object> properties) {
        this.path = path;
        properties.put("sling:resourceType", getResourceType());
        properties.put("sling:resourceSuperType", getResourceSuperType());
        this.properties = new ValueMapDecorator(properties);
    }

    public String getPath() {
        return path;
    }

    public String getResourceType() {
        return "db/table";
    }

    public String getResourceSuperType() {
        return "db";
    }

    public ValueMap getValueMap() {
        return properties;
    }
}
