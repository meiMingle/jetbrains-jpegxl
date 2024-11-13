package com.github.meimingle.jxl;

import com.intellij.openapi.diagnostic.Logger;
import com.traneptora.jxlatte.JXLDecoder;
import com.traneptora.jxlatte.JXLImage;
import com.traneptora.jxlatte.color.*;
import com.traneptora.jxlatte.util.ImageBuffer;
import com.traneptora.jxlatte.util.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.*;

public class JXLImageReaderSpi extends ImageReaderSpi {

    private static final MethodHandle GET_PRIMARIES_HANDLE;
    private static final MethodHandle GET_WHITEPOINT_HANDLE;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(ColorFlags.class, MethodHandles.lookup());
            MethodType mt1 = MethodType.methodType(CIEPrimaries.class, int.class);
            GET_PRIMARIES_HANDLE = lookup.findStatic(ColorFlags.class, "getPrimaries", mt1);
            MethodType mt2 = MethodType.methodType(CIEXY.class, int.class);
            GET_WHITEPOINT_HANDLE = lookup.findStatic(ColorFlags.class, "getWhitePoint", mt2);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static final int MAX_FILE_SIZE = 0x06400000;  // 100 MiBs
    final static byte[] JPEGXL = new byte[]{(byte) 0xff, 0x0a};
    final static byte[] JPEGXL2 = new byte[]{0x00, 0x00, 0x00, 0x0C, 'J', 'X', 'L', ' ', 0x0D, 0x0A, (byte) 0x87, 0x0A};

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
        try {
            stream.mark();
            stream.readFully(b, 0, 2);
            if (equalArrays(b, JPEGXL)) {
                return true;
            }
            stream.readFully(b, 2, 10);
            return equalArrays(b, JPEGXL2);
        } finally {
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
                return asBufferedImage(image);
            }
            return null;
        }

        private BufferedImage asBufferedImage(JXLImage jxlImage) {
            ImageBuffer[] buffer = jxlImage.getBuffer(false);
            int type = buffer[0].getType();
            DataBuffer dataBuffer = switch (type) {
                case ImageBuffer.TYPE_INT -> {
                    final int[][] dataArray = new int[buffer.length][jxlImage.getWidth() * jxlImage.getHeight()];
                    for (int c = 0; c < buffer.length; c++) {
                        for (int y = 0; y < jxlImage.getHeight(); y++) {
                            System.arraycopy(buffer[c].getIntBuffer()[y], 0, dataArray[c], y * jxlImage.getWidth(), jxlImage.getWidth());
                        }
                    }
                    yield new DataBufferInt(dataArray, jxlImage.getWidth() * jxlImage.getHeight());
                }
                case ImageBuffer.TYPE_FLOAT -> {
                    final float[][] dataArray = new float[buffer.length][jxlImage.getWidth() * jxlImage.getHeight()];
                    for (int c = 0; c < buffer.length; c++) {
                        for (int y = 0; y < jxlImage.getHeight(); y++) {
                            System.arraycopy(buffer[c].getFloatBuffer()[y], 0, dataArray[c], y * jxlImage.getWidth(), jxlImage.getWidth());
                        }
                    }
                    yield new DataBufferFloat(dataArray, jxlImage.getWidth() * jxlImage.getHeight());
                }
                default -> throw new IllegalStateException("Unexpected value: " + type);
            };

            SampleModel sampleModel = new BandedSampleModel(type == 1 ? DataBuffer.TYPE_FLOAT : DataBuffer.TYPE_INT, jxlImage.getWidth(), jxlImage.getHeight(), buffer.length);
            WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point());

            boolean alpha = jxlImage.hasAlpha();
            int transparency = alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE;
            ColorSpace cs;
            if (jxlImage.hasICCProfile())
                cs = new ICC_ColorSpace(ICC_Profile.getInstance(jxlImage.getICCProfile()));
            else
                cs = new JXLColorSpace(jxlImage.getCIEPrimaries(), jxlImage.getCIEWhitePoint(), jxlImage.getTransfer(),
                        jxlImage.getColorChannelCount());
            ComponentColorModel colorModel = new ComponentColorModel(cs, alpha, jxlImage.isAlphaPremultiplied(), transparency, type == 1 ? DataBuffer.TYPE_FLOAT : DataBuffer.TYPE_INT);

            return new BufferedImage(colorModel, raster, jxlImage.isAlphaPremultiplied(), null);
        }


        public class JXLColorSpace extends ColorSpace {

            private static final long serialVersionUID = 1L;

            public final CIEPrimaries primaries;
            public final CIEXY whitePoint;
            public final int transfer;
            public final TransferFunction transferFunction;
            private float[][] primariesToXYZD50;
            private float[][] primariesFromXYZD50;
            private float[][] primariesToSRGB;
            private float[][] primariesFromSRGB;

            public JXLColorSpace(CIEPrimaries primaries, CIEXY whitePoint, int transfer, int numComponents) {
                super(Objects.hash(primaries, whitePoint, transfer), numComponents);
                this.primaries = primaries;
                this.whitePoint = whitePoint;
                this.transfer = transfer;
                this.primariesToXYZD50 = ColorManagement.primariesToXYZD50(primaries, whitePoint);
                this.primariesFromXYZD50 = MathHelper.invertMatrix3x3(primariesToXYZD50);
                this.primariesToSRGB = ColorManagement.getConversionMatrix(
                        ColorManagement.PRI_SRGB, ColorManagement.WP_D65, primaries, whitePoint);
                this.primariesFromSRGB = MathHelper.invertMatrix3x3(primariesToSRGB);
                this.transferFunction = ColorManagement.getTransferFunction(transfer);
            }

            public JXLColorSpace(int primaries, int whitePoint, int transfer) throws Throwable {
                this((CIEPrimaries) GET_PRIMARIES_HANDLE.invoke(primaries), (CIEXY) GET_WHITEPOINT_HANDLE.invoke(whitePoint), transfer, 3);
            }

            private float[] transfer(float[] thisSpace) {
                thisSpace[1] = (float) transferFunction.fromLinear(thisSpace[1]);
                if (getNumComponents() < 3) {
                    thisSpace[0] = thisSpace[1];
                } else {
                    thisSpace[0] = (float) transferFunction.fromLinear(thisSpace[0]);
                    thisSpace[2] = (float) transferFunction.fromLinear(thisSpace[2]);
                }
                return thisSpace;
            }

            private float[] linearize(float[] colorvalue) {
                float[] linear = new float[3];
                for (int i = 0; i < getNumComponents(); i++)
                    linear[i] = (float) transferFunction.toLinear(colorvalue[i]);
                if (getNumComponents() < 3)
                    linear[1] = linear[2] = linear[0];
                return linear;
            }

            @Override
            public float[] toRGB(float[] colorvalue) {
                float[] linear = linearize(colorvalue);
                float[] sRGB = MathHelper.matrixMutliply(this.primariesToSRGB, linear);
                TransferFunction sRGBTransfer = ColorManagement.getTransferFunction(ColorFlags.TF_SRGB);
                // clamp here because BufferedImage forbids out of gamut colors
                sRGB[0] = MathHelper.clamp((float) sRGBTransfer.fromLinear(sRGB[0]), 0.0f, 1.0f);
                sRGB[1] = MathHelper.clamp((float) sRGBTransfer.fromLinear(sRGB[1]), 0.0f, 1.0f);
                sRGB[2] = MathHelper.clamp((float) sRGBTransfer.fromLinear(sRGB[2]), 0.0f, 1.0f);
                return sRGB;
            }

            @Override
            public float[] fromRGB(float[] rgbvalue) {
                float[] linear = new float[3];
                TransferFunction inverseSRGB = ColorManagement.getTransferFunction(ColorFlags.TF_SRGB);
                linear[0] = (float) inverseSRGB.fromLinear(rgbvalue[0]);
                linear[1] = (float) inverseSRGB.fromLinear(rgbvalue[1]);
                linear[2] = (float) inverseSRGB.fromLinear(rgbvalue[2]);
                float[] thisSpace = MathHelper.matrixMutliply(this.primariesFromSRGB, linear);
                return transfer(thisSpace);
            }

            @Override
            public float[] toCIEXYZ(float[] colorvalue) {
                float[] linear = linearize(colorvalue);
                float[] linearXYZ = MathHelper.matrixMutliply(this.primariesToXYZD50, linear);
                return linearXYZ;
            }

            @Override
            public float[] fromCIEXYZ(float[] colorvalue) {
                float[] thisSpace = MathHelper.matrixMutliply(this.primariesFromXYZD50, colorvalue);
                return transfer(thisSpace);
            }
        }


        @Override
        public @Nullable Iterator<ImageTypeSpecifier> getImageTypes(final int imageIndex) {
            checkIndex(imageIndex);
            ImageTypeSpecifier specifier = new ImageTypeSpecifier(asBufferedImage(image));
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
