package com.adobe.examples.db.resourceprovider.impl;

import com.adobe.examples.db.resourceprovider.api.ResourceData;
import org.apache.sling.api.resource.AbstractResource;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.DeepReadModifiableValueMapDecorator;
import org.apache.sling.api.wrappers.DeepReadValueMapDecorator;

import java.util.Map;

public class DBResource extends AbstractResource {

    private ResourceResolver resourceResolver;

    private DBResourceProvider provider;
    private final ResourceData resourceData;

    private final ResourceMetadata metadata;

    DBResource(final ResourceResolver resourceResolver, final DBResourceProvider provider, final ResourceData resourceData) {
        this.resourceResolver = resourceResolver;
        this.provider = provider;
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
            return (AdapterType) new DeepReadModifiableValueMapDecorator(this, new ChangeableValueMap(this));
        }
        return super.adaptTo(type);
    }

    void modified() {
        provider.modified(this);
    }
}
