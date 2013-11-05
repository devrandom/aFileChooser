package com.ipaulpro.afilechooser;

import java.io.File;

/**
 * @author devrandom
 */
public class VFile extends File {
    public static final int PHYSICAL_FILE = 1;

    public VFile(String path) {
        super(path);
    }

    public int getType() {
        return PHYSICAL_FILE;
    }
}
