package com.adobe.examples.db.resourceprovider.impl;

import com.adobe.examples.db.resourceprovider.api.ResourceData;
import com.adobe.examples.db.resourceprovider.api.ResourceDataFactory;
import org.apache.sling.api.resource.DynamicResourceProvider;
import org.apache.sling.api.resource.ModifyingResourceProvider;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBResourceProvider implements ModifyingResourceProvider, DynamicResourceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DBResourceProvider.class);

    private final ResourceDataFactory factory;

    private Map<String, ResourceData> modifiedResources = new HashMap<String, ResourceData>();

    private Set<String> deletedResources = new HashSet<String>();


    public DBResourceProvider(final ResourceDataFactory factory) throws SQLException {
        this.factory = factory;
    }

    public Resource getResource(final ResourceResolver resourceResolver, final HttpServletRequest httpServletRequest, final String path) {
        return getResource(resourceResolver, path);
    }

    public Resource getResource(final ResourceResolver resourceResolver, final String path) {
        if (deletedResources.contains(path)) {
            return null;
        }

        final ResourceData resourceData;
        if (modifiedResources.containsKey(path)) {
            resourceData = modifiedResources.get(path);
        } else {
            resourceData = factory.getResourceData(path);
        }

        if (resourceData != null) {
            return new DBResource(resourceResolver, this, resourceData);
        }
        return null;
    }

    public Iterator<Resource> listChildren(final Resource resource) {
        final Iterable<String> childPaths = factory.getChildPaths(resource.getPath());
        final List<Resource> children = new ArrayList<Resource>();
        for (final String path : childPaths) {
            children.add(getResource(resource.getResourceResolver(), path));
        }
        return children.iterator();
    }

    public boolean isLive() {
        return factory.isLive();
    }

    public void close() {
        factory.close();
    }

    public Resource create(final ResourceResolver resolver, final String path, final Map<String, Object> properties) throws PersistenceException {
        LOG.info("create resource {}", path);
        final ResourceData resourceData = factory.createResourceData(path, properties);
        deletedResources.remove(path);
        modifiedResources.put(path, resourceData);
        return getResource(resolver, path);
    }

    void modified(final DBResource resource) {
        final String path = resource.getPath();
        LOG.info("modify resource {}", path);
        final ResourceData resourceData = factory.createResourceData(path, resource.getValueMap());
        deletedResources.remove(path);
        modifiedResources.put(path, resourceData);
    }

    public void delete(final ResourceResolver resolver, final String path) throws PersistenceException {
        LOG.info("delete resource {}", path);
        modifiedResources.remove(path);
        deletedResources.add(path);
    }

    public void commit(final ResourceResolver resolver) throws PersistenceException {
        LOG.info("committing {} resources", modifiedResources.size());
        for (final String path : modifiedResources.keySet()) {
            factory.putResourceData(path, modifiedResources.get(path));
        }
        modifiedResources.clear();

        LOG.info("committing {} deleted resources", deletedResources.size());
        for (final String path : deletedResources) {
            factory.putResourceData(path, null);
        }
        deletedResources.clear();
    }

    public void revert(final ResourceResolver resolver) {
        LOG.info("reverting {} changes", modifiedResources.size() + deletedResources.size());
        modifiedResources.clear();
        deletedResources.clear();
    }

    public boolean hasChanges(final ResourceResolver resolver) {
        return !modifiedResources.isEmpty() || !deletedResources.isEmpty();
    }

}
