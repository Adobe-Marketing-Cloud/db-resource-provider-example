package com.adobe.examples.db.resourceprovider.impl;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBResourceProvider implements ModifyingResourceProvider, DynamicResourceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DBResourceProvider.class);

    private static final Map<String, String> HIERARCHY;

    static {
        final HashMap<String, String> hierarchy = new HashMap<String, String>();
        hierarchy.put("/accounts", "accounts");
        HIERARCHY = Collections.unmodifiableMap(hierarchy);
    }

    private final String rootPath;

    private final DBResourceProviderFactory factory;

    private Map<String, ResourceData> modifiedResources = new HashMap<String, ResourceData>();

    private Set<String> deletedResources = new HashSet<String>();


    public DBResourceProvider(final DBResourceProviderFactory factory, final String rootPath) throws SQLException {
        this.factory = factory;
        this.rootPath = rootPath;
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
            return new DBResource(resourceResolver, resourceData);
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
        // TODO: stub impl
        return true;
    }

    public void close() {
        // TODO: stub impl
    }

    public Resource create(final ResourceResolver resolver, final String path, final Map<String, Object> properties) throws PersistenceException {
        final DBRecordResourceData dbRecordResourceData = new DBRecordResourceData(path, properties);
        deletedResources.remove(path);
        modifiedResources.put(path, dbRecordResourceData);
        return getResource(resolver, path);

        /*
        LOG.info("create resource {} {}", path, properties);
        final ValueMapDecorator props = new ValueMapDecorator(properties);
        final String userid = props.get("userid", "[no userid]");
        dslContext.insertInto(table)
                .values(
                        userid,
                        props.get("name", "[no name]"),
                        props.get("email", "[no email]"),
                        props.get("balance", 0)
                )
                .execute();

//                .column("userid", H2DataType.VARCHAR)
//                .column("name", H2DataType.VARCHAR)
//                .column("email", H2DataType.VARCHAR)
//                .column("balance", H2DataType.INT)
//        if (!modifiedResources.containsKey(path)) {
//            modifiedResources.put(path, properties);
//            return new DBRecordResource(dslContext, resolver, path, properties);
//        }
//        throw new PersistenceException("Resource already exists");

        final SelectOptionStep<Record> where = dslContext
                .select()
                .from(table)
                .where("\"PUBLIC\".\"accounts\".\"userid\" = '" + userid + "'")
                .limit(1);
        final Iterator<Record> results = where.iterator();
        if (results.hasNext()) {
            return new DBRecordResource(results.next(), resolver, path);
        }
        throw new PersistenceException("Resource already exists");
        */
    }

    public void delete(final ResourceResolver resolver, final String path) throws PersistenceException {
        LOG.info("remove resource {}", path);
        modifiedResources.remove(path);
        deletedResources.add(path);
    }

    public void commit(final ResourceResolver resolver) throws PersistenceException {
        // TODO: stub impl
        for (final String path : modifiedResources.keySet()) {
            factory.putResourceData(path, modifiedResources.get(path));
        }
        modifiedResources.clear();

        for (final String path : deletedResources) {
            factory.putResourceData(path, null);
        }
        deletedResources.clear();
        LOG.info("commit changes");
    }

    public void revert(final ResourceResolver resolver) {
        modifiedResources.clear();
        deletedResources.clear();
    }

    public boolean hasChanges(final ResourceResolver resolver) {
        return !modifiedResources.isEmpty() || !deletedResources.isEmpty();
    }

}
