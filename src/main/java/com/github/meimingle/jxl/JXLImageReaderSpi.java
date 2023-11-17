package com.github.meimingle.jxl;

import com.intellij.openapi.diagnostic.Logger;
import com.thebombzen.jxlatte.JXLDecoder;
import com.thebombzen.jxlatte.JXLImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class JXLImageReaderSpi extends ImageReaderSpi {

    private static final int MAX_FILE_SIZE = 0x06400000;  // 100 MiBs
    final static byte[] JPEGXL = new byte[]{(byte)0xff, 0x0a};
    final static byte[] JPEGXL2 = new byte[]{0x00, 0x00, 0x00, 0x0C, 'J', 'X', 'L', ' ', 0x0D, 0x0A, (byte)0x87, 0x0A};

    public JXLImageReaderSpi() {
        vendorName = JXLMetadata.JXL_VENDOR;
        version = JXLMetadata.JXL_LIBRARY_VERSION;
        suffixes = JXLMetadata.JXL_SUFFIXES;
        names = JXLMetadata.JXL_FORMAT_NAMES;
        MIMETypes = JXLMetadata.JXL_MIME_TYPES;
        pluginClassName = JXLReader.class.getName();
        inputTypes = new Class<?>[]{ImageInputStream.class};
    }

    @Override
    public boolean canDecodeInput(@NotNull Object source) throws IOException {
        if (!(source instanceof ImageInputStream)) {// 80
            source = ImageIO.createImageInputStream(source);
        }
            final ImageInputStream stream = (ImageInputStream) source;
            byte[] b = new byte[12];
            try{
                stream.mark();
                stream.readFully(b,0,2);
                if (equalArrays(b, JPEGXL)) {
                    return true;
                }
                stream.readFully(b,2,10);
                return equalArrays(b, JPEGXL2);
            }finally {
                stream.reset();
            }
    }

    private static boolean equalArrays(byte[] a, byte[] target) {
        if (a == null || target == null || target.length > a.length) {
            return false;
        }
        for (int i = 0; i < target.length; i++) {
            if (a[i] != target[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ImageReader createReaderInstance(final Object extension) {
        return new JXLReader(this);
    }

    @Override
    public @NotNull String getDescription(final Locale locale) {
        return "JXL Image Decoder";
    }

    private static class JXLReader extends ImageReader {
        private JXLImage image = null;

        private JXLReader(final ImageReaderSpi originatingProvider) {
            super(originatingProvider);
        }


        @Override
        public int getNumImages(final boolean allowSearch) {
            return 1;
        }

        /**
         *
         */
        private void checkIndex(int imageIndex) {
            if (imageIndex != 0) {
                Logger.getInstance(this.getClass()).error("bad index");
                throw new IndexOutOfBoundsException("");
            }
        }

        @Override
        public int getWidth(final int imageIndex) throws IOException {
            checkIndex(imageIndex);
            return image.getWidth();
        }

        @Override
        public int getHeight(final int imageIndex) throws IOException {
            checkIndex(imageIndex);
            return image.getHeight();
        }

        @Override
        public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
            super.setInput(input, seekForwardOnly, ignoreMetadata);
            try {
                byte[] bytes = readStreamFully((ImageInputStream) input);
                JXLDecoder jxlDecoder = new JXLDecoder(new ByteArrayInputStream(bytes));
                image = jxlDecoder.decode();
            } catch (Exception e) {
                Logger.getInstance(this.getClass()).error(e);
                image = null;
            }
        }

        private static byte[] readStreamFully(@NotNull ImageInputStream stream) throws IOException {
            if (stream.length() != -1) {
                byte[] bytes = new byte[(int) stream.length()];  // Integer overflow prevented by canDecode check in reader spi above.
                stream.readFully(bytes);
                return bytes;
            }

            // Unknown file size
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(0x100000);  // initialize with 1 MiB to minimize reallocation.
            final int bufferSize = 0x4000;    // 16k
            byte[] bytes = new byte[bufferSize];
            int idx;
            for (idx = 0; idx < MAX_FILE_SIZE / bufferSize; idx++) {  // Just to make sure we don't exceed MAX_FILE_SIZE
                int read = stream.read(bytes, 0, bufferSize);
                buffer.write(bytes, 0, read);
                if (read != bufferSize) {
                    break;
                }
            }
            if (idx == MAX_FILE_SIZE / bufferSize) {
                throw new IOException("jxl image too large");
            }
            return buffer.toByteArray();
        }


        @Override
        public @NotNull BufferedImage read(final int imageIndex, final ImageReadParam param) throws IOException {
            if (image != null) {
                return image.asBufferedImage();
            }
            return null;
        }

        @Override
        public @Nullable Iterator<ImageTypeSpecifier> getImageTypes(final int imageIndex) {
            checkIndex(imageIndex);
            ImageTypeSpecifier specifier = new ImageTypeSpecifier(image.asBufferedImage());
            List<ImageTypeSpecifier> l = new ArrayList<>();
            l.add(specifier);
            return l.iterator();
        }

        @Override
        public @Nullable IIOMetadata getStreamMetadata() {
            return null;
        }

        @Override
        public @Nullable IIOMetadata getImageMetadata(final int imageIndex) {
            checkIndex(imageIndex);
            return null;
        }

        @Override
        public void dispose() {
            super.dispose();
            image = null;
        }
    }
}
