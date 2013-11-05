package com.ipaulpro.afilechooser;

import android.database.Cursor;
import android.support.v4.content.Loader;

import java.util.List;

/**
* @author devrandom
*/
public interface VirtualsFactory {
    public VFile createVirtual(Cursor aCursor);
    public List<VFile> createVirtualList(Cursor aCursor);
    public Loader<Cursor> getVirtualsCursorLoader(String mPath);
}
