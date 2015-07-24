package com.adobe.examples.db.resourceprovider.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceProviderFactory;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.util.h2.H2DataType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(
        metatype = true,
        label = "DB Resource Provider Factory",
        description = "OSGi Configuration for DB Resource Providers",
        configurationFactory = true,
        specVersion = "1.1",
        policy = ConfigurationPolicy.REQUIRE
)
@Service
@Properties({
        @Property(name = "datasource.name"),
        @Property(name = ResourceProvider.ROOTS),
        @Property(name = ResourceProvider.OWNS_ROOTS, boolValue = true, propertyPrivate = true)
})
public class DBResourceProviderFactory implements ResourceProviderFactory, ServiceTrackerCustomizer<DataSource, DataSource> {

    private static final Logger LOG = LoggerFactory.getLogger(DBResourceProviderFactory.class);

    private static final String FILTER_TEMPLATE = "(&(datasource.name=%s)(objectClass=javax.sql.DataSource))";

    private DataSource dataSource;
    private ServiceTracker<DataSource, DataSource> dataSourceTracker;
    private String dataSourceName;
    private ServiceReference<DataSource> dataSourceReference;
    private Connection connection;
    private DSLContext dslContext;
    private String rootPath;


    //TODO: temporary in-memory persistence - DB is ignored for now
    private final Map<String, ResourceData> persistedData = new ConcurrentHashMap<String, ResourceData>();

    public ResourceProvider getResourceProvider(Map<String, Object> authenticationInfo) throws LoginException {
        try {
            LOG.info("Getting DBResourceProvider for {}", rootPath);
            return new DBResourceProvider(this, rootPath);
        } catch (SQLException e) {
            throw new LoginException("Database initialization failed", e);
        }
    }

    public ResourceProvider getAdministrativeResourceProvider(Map<String, Object> authenticationInfo) throws LoginException {
        return getResourceProvider(authenticationInfo);
    }

    ResourceData getResourceData(final String path) {
        return persistedData.get(path);
    }

    void putResourceData(final String path, final ResourceData resourceData) {
        if (resourceData == null) {
            persistedData.remove(path);
        } else {
            persistedData.put(path, resourceData);
        }
    }

    Iterable<String> getChildPaths(final String path) {
        final List<String> children = new ArrayList<String>();
        for (final String dataPath : persistedData.keySet()) {
            if (path.equals(ResourceUtil.getParent(dataPath))) {
                children.add(dataPath);
            }
        }
        return children;
    }

    @Activate
    public void activate(BundleContext context, Map<String, Object> properties) throws InvalidSyntaxException {
        final ValueMapDecorator props = new ValueMapDecorator(properties);
        dataSourceName = props.get("datasource.name", String.class);
        rootPath = props.get(ResourceProvider.ROOTS, String.class);
        if (dataSourceName != null) {
            LOG.info("Activating DB Resource Provider Factory for DataSource named [{}]", dataSourceName);
            final String filterExpression = String.format(FILTER_TEMPLATE, dataSourceName);
            final Filter filter = context.createFilter(filterExpression);
            dataSourceTracker = new ServiceTracker<DataSource, DataSource>(context, filter, this);
            dataSourceTracker.open();
        } else {
            throw new IllegalStateException("Configuration is missing datasource.name property");
        }

        final HashMap<String, Object> accountsProperties = new HashMap<String, Object>();
        accountsProperties.put("tableName", "accounts");
        persistedData.put("accounts", new DBTableResourceData(rootPath + "/accounts", accountsProperties));
        LOG.info("DB Resource Provider Factory for DataSource named [{}] activated", dataSourceName);
    }

    @Deactivate
    public void deactivate(BundleContext context, Map<String, Object> properties) {
        LOG.info("Deactivating DB Resource Provider Factory for DataSource [{}]", dataSourceName);
        //dataSourceName = null;
        dataSourceTracker.close();
        dataSourceTracker = null;
    }

    private void initDatabase(final DSLContext ctx) throws SQLException {
        final boolean accountsExists = tableExists(ctx, "accounts");
        LOG.info("Table accounts exists = {}", accountsExists);
        if (!accountsExists) {
            ctx.createTable("accounts")
                    .column("userid", H2DataType.VARCHAR)
                    .column("name", H2DataType.VARCHAR)
                    .column("email", H2DataType.VARCHAR)
                    .column("balance", H2DataType.INT)
                    .execute();
        }
    }

    private boolean tableExists(final DSLContext ctx, final String tableName) {
        final List<Table<?>> tables = ctx.meta().getTables();
        for (final Table<?> table : tables) {
            final String name = table.getName();
            if (tableName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public DataSource addingService(final ServiceReference<DataSource> reference) {
        if (dataSource == null) {
            dataSourceReference = reference;
            final DataSource ds = reference.getBundle().getBundleContext().getService(reference);
            registerDataSource(ds);
            return ds;
        } else {
            LOG.info("A DataSource named [{}] is already bound. Ignoring new DataSource.", dataSourceName);
        }
        return null;
    }

    public void modifiedService(final ServiceReference<DataSource> reference, final DataSource ds) {
        if (dataSourceReference == reference) {
            LOG.info("Updating DataSource named [{}].", dataSourceName);
            unregisterDataSource();
            registerDataSource(ds);
        }
    }

    public void removedService(final ServiceReference<DataSource> reference, final DataSource service) {
        if (dataSourceReference == reference) {
            LOG.info("DataSource named [{}] has disappeared. Deactivating service.", dataSourceName);
            unregisterDataSource();
            dataSourceReference = null;
        }
    }

    private void registerDataSource(final DataSource ds) {
        try {
            connection = ds.getConnection();
            dslContext = DSL.using(connection, SQLDialect.H2);
            dataSource = ds;
            initDatabase(dslContext);
            LOG.info("Bound datasource named [{}]", dataSourceName);
        } catch (SQLException e) {
            LOG.error("Failed to create a DB connection for DataSource named [{}]", dataSourceName, e);
        }
    }

    private void unregisterDataSource() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LOG.error("Failed to close DB connection", e);
        } finally {
            dslContext = null;
            connection = null;
            dataSource = null;
        }
    }
}
