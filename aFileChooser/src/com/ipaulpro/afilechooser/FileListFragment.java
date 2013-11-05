/* 
 * Copyright (C) 2012 Paul Burke
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */ 

package com.ipaulpro.afilechooser;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import java.io.File;

/**
 * Fragment that displays a list of Files in a given path.
 * 
 * @version 2012-10-28
 * 
 * @author paulburke (ipaulpro)
 * 
 */
public class FileListFragment extends ListFragment {

	private static final int VFS_LOADER_IDS = 1000;


    private FileListAdapter mAdapter;
	private String mPath;
	private boolean mFolderBrowser ;
    private VFS mVFS;
    private VirtualsFactory mVirtualsFactory;

    /**
     * Create a new instance with the given file path.
     *
     * @param path The absolute path of the file (directory) to display.
     * @return A new Fragment with the given file path.
     */
    public static FileListFragment newInstance(String path, boolean folderBrowser,
                                               VirtualsFactory aVirtualsFactory,
                                               VFS aVFS) {
        FileListFragment fragment = new FileListFragment();
        Bundle args = new Bundle();
        args.putString(FileChooserActivity.PATH, path);
        args.putBoolean(FileChooserActivity.ARG_FOLDER_BROWSER, folderBrowser);
        fragment.setArguments(args);
        fragment.setVirtualsFactory(aVirtualsFactory);
        fragment.setVFS(aVFS);

        return fragment;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new FileListAdapter(getActivity());
		mPath = getArguments() != null ? getArguments().getString(
				FileChooserActivity.PATH) : Environment
				.getExternalStorageDirectory().getAbsolutePath();
		mFolderBrowser = getArguments() == null ? false : getArguments().getBoolean(FileChooserActivity.ARG_FOLDER_BROWSER);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setEmptyText(getString(R.string.empty_directory));
		setListAdapter(mAdapter);
		setListShown(false);
		
		this.getListView().setLongClickable(true);
		this.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                FileChooserActivity.finishWithResult(getActivity(), (File) mAdapter.getItem(position));
                return true;
            }
        });

        mVFS.setObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if (isResumed())
                    setListShown(true);
                else
                    setListShownNoAnimation(true);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                mAdapter.notifyDataSetInvalidated();
            }
        });
        mVFS.setVirtualsFactory(mVirtualsFactory);

        mVFS.onActivityCreated(getActivity(), getLoaderManager(), VFS_LOADER_IDS, mPath);

        mAdapter.setVFS(mVFS);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		FileListAdapter adapter = (FileListAdapter) l.getAdapter();
		if (adapter != null) {
			File file = (File) adapter.getItem(position);
			mPath = file.getAbsolutePath();
			((FileChooserActivity) getActivity()).onFileSelected(file);
		}
	}

    public void setVirtualsFactory(VirtualsFactory virtualsFactory) {
        this.mVirtualsFactory = virtualsFactory;
    }

    public void setVFS(VFS mVFS) {
        this.mVFS = mVFS;
    }

    private FileChooserActivity getFileChooserActivity() {
		return (FileChooserActivity)getActivity() ;
	}
}