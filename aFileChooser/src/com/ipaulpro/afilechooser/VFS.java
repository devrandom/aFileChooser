package com.ipaulpro.afilechooser;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.app.LoaderManager;

import java.util.List;

/**
 * @author devrandom
 */
public interface VFS {
    void setObserver(DataSetObserver aObserver);

    void onActivityCreated(Context aContext, LoaderManager loaderManager, int startLoaderId, String aPath);

    void setVirtualsFactory(VirtualsFactory virtualsFactory);

    List<VFile> getVFiles();
}
