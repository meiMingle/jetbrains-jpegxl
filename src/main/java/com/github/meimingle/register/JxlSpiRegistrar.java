package com.github.meimingle.register;

import com.github.meimingle.jxl.JXLImageReaderSpi;
import com.intellij.ide.AppLifecycleListener;
import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import org.jetbrains.annotations.NotNull;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import java.util.List;

public class JxlSpiRegistrar implements AppLifecycleListener, DynamicPluginListener {
    private static final ImageReaderSpi PROVIDER = new JXLImageReaderSpi();

    @Override
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        ensureJxlRegistered();
    }

    @Override
    public void pluginLoaded(@NotNull IdeaPluginDescriptor pluginDescriptor) {
        ensureJxlRegistered();
    }

    @Override
    public void beforePluginUnload(@NotNull IdeaPluginDescriptor pluginDescriptor, boolean isUpdate) {
        deRegisterJxl();
    }

    public void ensureJxlRegistered() {
        IIORegistry defaultInstance = IIORegistry.getDefaultInstance();
        if (!defaultInstance.contains(PROVIDER)) {
            defaultInstance.registerServiceProvider(PROVIDER, ImageReaderSpi.class);
        }
    }

    public void deRegisterJxl() {
        IIORegistry defaultInstance = IIORegistry.getDefaultInstance();
        defaultInstance.deregisterServiceProvider(PROVIDER, ImageReaderSpi.class);
    }
}
