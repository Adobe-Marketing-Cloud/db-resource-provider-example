package com.adobe.examples.db.resourceprovider.impl;

import java.io.Closeable;

public interface ResourceDataFactory {

    ResourceData getResourceData(String path);

    Iterable<String> getChildPaths(String path);

    void putResourceData(String path, ResourceData resourceData);

    boolean isLive();

    void close();
}
