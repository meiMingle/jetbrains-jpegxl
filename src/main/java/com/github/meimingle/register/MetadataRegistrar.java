package com.github.meimingle.register;

import com.github.meimingle.jxl.JXLImageReaderSpi;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;

public class MetadataRegistrar {
    public MetadataRegistrar() {
        ensureJxlRegistered();
    }

    public static void ensureJxlRegistered() {
        IIORegistry defaultInstance = IIORegistry.getDefaultInstance();
        defaultInstance.registerServiceProvider(new JXLImageReaderSpi(), ImageReaderSpi.class);
    }
}
