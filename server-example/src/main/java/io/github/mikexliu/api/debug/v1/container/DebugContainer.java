package io.github.mikexliu.api.debug.v1.container;

import io.github.mikexliu.api.debug.v1.resource.DebugResource;

public class DebugContainer extends DebugResource {

    @Override
    public String getString() {
        return Integer.toHexString(this.hashCode());
    }

    @Override
    public String getJsonException() {
        throw new IllegalStateException("Exception");
    }

    @Override
    public String getStringException() {
        throw new IllegalStateException("Exception");
    }
}
