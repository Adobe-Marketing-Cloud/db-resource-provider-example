package com.adobe.examples.db.resourceprovider.impl;

import org.apache.sling.api.resource.AbstractResource;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.DeepReadModifiableValueMapDecorator;
import org.apache.sling.api.wrappers.DeepReadValueMapDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DBResource extends AbstractResource {

    private ResourceResolver resourceResolver;

    private final ResourceData resourceData;

    private final ResourceMetadata metadata;

    DBResource(final ResourceResolver resourceResolver, final ResourceData resourceData) {
        this.resourceResolver = resourceResolver;
        this.resourceData = resourceData;
        this.metadata = new ResourceMetadata();
    }

    public String getPath() {
        return resourceData.getPath();
    }

    public String getResourceType() {
        return resourceData.getResourceType();
    }

    public String getResourceSuperType() {
        return resourceData.getResourceSuperType();
    }

    public ResourceMetadata getResourceMetadata() {
        return metadata;
    }

    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <AdapterType> AdapterType adaptTo(final Class<AdapterType> type) {
        if (type == ValueMap.class || type == Map.class) {
            return (AdapterType) new DeepReadValueMapDecorator(this, resourceData.getValueMap());
        } else if (type == ModifiableValueMap.class) {
            return (AdapterType) new DeepReadModifiableValueMapDecorator(this, resourceData.getValueMap());
        }
        return super.adaptTo(type);
    }
}
