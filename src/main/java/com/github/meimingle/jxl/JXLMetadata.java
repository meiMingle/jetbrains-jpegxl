package com.github.meimingle.jxl;

import org.w3c.dom.Node;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

public class JXLMetadata extends IIOMetadata {
    public static final String JXL_FORMAT_LOWER_CASE = "jxl";
    public static final String JXL_FORMAT_UPPER_CASE = "JXL";
    public static final String[] JXL_FORMAT_NAMES = new String[]{JXL_FORMAT_UPPER_CASE, JXL_FORMAT_LOWER_CASE};
    public static final String EXT_JXL = JXL_FORMAT_LOWER_CASE;
    public static final String[] JXL_SUFFIXES = new String[]{EXT_JXL};
    public static final String[] JXL_MIME_TYPES = new String[]{"image/jxl"};
    public static final String JXL_VENDOR = "JXL";

    public static final String JXL_LIBRARY_VERSION = "1.0";

    @Override
    public boolean isReadOnly() {
        // TODO:
        return true;
    }

    @Override
    public Node getAsTree(final String formatName) {
        return new IIOMetadataNode(nativeMetadataFormatName);
    }

    @Override
    public void mergeTree(final String formatName, final Node root) throws IIOInvalidTreeException {
        // TODO
    }

    @Override
    public void reset() {
        // TODO
    }
}
