package com.github.meimingle.listener;

import com.github.meimingle.jxl.JXLImageReaderSpi;
import com.intellij.ide.ApplicationInitializedListener;
import com.intellij.ide.ApplicationLoadListener;
import com.intellij.openapi.application.Application;
import org.jetbrains.annotations.NotNull;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import java.nio.file.Path;

public class AppLoadListener implements ApplicationLoadListener {
    @Override
    public void beforeApplicationLoaded(@NotNull Application application, @NotNull Path configPath) {
        ensureJxlRegistered();
    }

    public static void ensureJxlRegistered() {
        IIORegistry defaultInstance = IIORegistry.getDefaultInstance();
        defaultInstance.registerServiceProvider(new JXLImageReaderSpi(), ImageReaderSpi.class);
    }
}
