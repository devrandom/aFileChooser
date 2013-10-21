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

import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a list of Files in a given path.
 * 
 * @version 2012-10-28
 * 
 * @author paulburke (ipaulpro)
 * 
 */
public class FileListFragment extends ListFragment {

	private static final int FILE_LOADER_ID = 0;
    private static final int VIRTUAL_LOADER_ID = 1;


    private FileListAdapter mAdapter;
	private String mPath;
	private boolean mFolderBrowser ;
    private FileLoaderCallback mFileLoaderCallback;
    private CursorLoaderCallback mCursorLoaderCallback;

    /**
	 * Create a new instance with the given file path.
	 * 
	 * @param path The absolute path of the file (directory) to display.
	 * @return A new Fragment with the given file path. 
	 */
	public static FileListFragment newInstance(String path, boolean folderBrowser) {
		FileListFragment fragment = new FileListFragment();
		Bundle args = new Bundle();
		args.putString(FileChooserActivity.PATH, path);
		args.putBoolean(FileChooserActivity.ARG_FOLDER_BROWSER, folderBrowser);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new FileListAdapter(getActivity());
		mPath = getArguments() != null ? getArguments().getString(
				FileChooserActivity.PATH) : Environment
				.getExternalStorageDirectory().getAbsolutePath();
		Bundle x = getArguments();
		boolean y = x.getBoolean(FileChooserActivity.ARG_FOLDER_BROWSER);
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

        mFileLoaderCallback = new FileLoaderCallback();
		getLoaderManager().initLoader(FILE_LOADER_ID, null, mFileLoaderCallback);
        mCursorLoaderCallback = new CursorLoaderCallback();
		getLoaderManager().initLoader(VIRTUAL_LOADER_ID, null, mCursorLoaderCallback);
		
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

    class FileLoaderCallback implements
            LoaderManager.LoaderCallbacks<List<File>> {
        @Override
        public Loader<List<File>> onCreateLoader(int id, Bundle args) {
            return new FileLoader(getActivity(), mPath, mFolderBrowser);
        }

        @Override
        public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
            mAdapter.setFileItems(data);

            if (isResumed())
                setListShown(true);
            else
                setListShownNoAnimation(true);
        }

        @Override
        public void onLoaderReset(Loader<List<File>> loader) {
            mAdapter.clear();
        }
    }

    class CursorLoaderCallback implements
            LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        		return getFileChooserActivity().getVirtualsCursorLoader( mPath ) ;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        		List<File> list = getList( data ) ;
            mAdapter.setVirtualItems( list );

            if (isResumed())
                setListShown(true);
            else
                setListShownNoAnimation(true);
        }

        /**
		 * @param data
		 * @return
		 */
		private List<File> getList(Cursor aCursor ) {
			List<File> list = new ArrayList<File>();
			aCursor.moveToPosition(-1);
			while( aCursor.moveToNext() ) {
				File file = getFileChooserActivity().createVirtual(aCursor) ;
				list.add(file);
			}
			return list ;
		}

		@Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.setVirtualItems(null);
        }
    }
    
	private FileChooserActivity getFileChooserActivity() {
		return (FileChooserActivity)getActivity() ;
	}
}