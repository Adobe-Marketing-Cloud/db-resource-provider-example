package com.adobe.examples.db.resourceprovider.impl;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.Map;

public class ChangeableValueMap extends ValueMapDecorator implements ModifiableValueMap {

    private final DBResource resource;

    public ChangeableValueMap(DBResource resource) {
        super(resource.getValueMap());
        this.resource = resource;
    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
        throw new UnsupportedOperationException("clear");
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(final String name, final Object value) {
        final Object oldValue = super.put(name, value);
        resource.modified();
        return oldValue;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(final Map<? extends String, ? extends Object> m) {
        for(final Map.Entry<? extends String, ? extends Object> e : m.entrySet() ) {
            this.put(e.getKey(), e.getValue());
        }
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(final Object name) {
        final Object oldValue = super.remove(name);
        resource.modified();
        return oldValue;
    }
}
