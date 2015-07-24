package com.adobe.examples.db.resourceprovider.impl;

import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ValueMap;

public interface ResourceData {

    String getPath();

    String getResourceType();

    String getResourceSuperType();

    ValueMap getValueMap();
}
