/* 
 * Copyright (C) 2013 Paul Burke
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * Main Activity that handles the FileListFragments 
 * 
 * @version 2013-06-25
 * 
 * @author paulburke (ipaulpro)
 * 
 */
public class FileChooserActivity extends FragmentActivity implements
		OnBackStackChangedListener {
	
    public static final String ACTION_FOLDER_BROWSER = "FolderBrowser";
    public static final String ACTION_FILE_BROWSER = "FileBrowser";
    public static final String EXTRA_BASE_PATH = "BasePath";

    private boolean mFolderBrowser = false ;

    public static final String ARG_FOLDER_BROWSER = "FolderBrowser";
    public static final String PATH = "path";

	private FragmentManager mFragmentManager;
	private BroadcastReceiver mStorageListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, R.string.storage_removed, Toast.LENGTH_LONG).show();
			finishWithResult(null);
		}
	};

    private String mPath;
	protected String mBasePath; // start browsing here
    protected String mAppRootPath; // app root path - hide from title

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String action = getIntent().getAction() ;
		if( ACTION_FOLDER_BROWSER.equals(action) ) {
			mFolderBrowser = true ;
		}
        if( getIntent().getStringExtra(EXTRA_BASE_PATH) != null ) {
            mBasePath = getIntent().getStringExtra(EXTRA_BASE_PATH);
        }
        mPath = mBasePath ;

		setContentView(R.layout.chooser);

		mFragmentManager = getSupportFragmentManager();
		mFragmentManager.addOnBackStackChangedListener(this);

		if (savedInstanceState == null) {
			addFragment();
		} else {
			mPath = savedInstanceState.getString(PATH);
			mFolderBrowser = savedInstanceState.getBoolean(ARG_FOLDER_BROWSER);
		}
		// TODO handle path inside the geebox
		setPathTitle(mPath);
	}

    public void setPathTitle(String aPath ) {
        String title = aPath ;
        if( aPath.startsWith(mAppRootPath)) {
            title = aPath.substring( mAppRootPath.length()-1 );
        }
        setTitle(title);
    }

	@Override
	protected void onPause() {
		super.onPause();
		
		unregisterStorageListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		registerStorageListener();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString(PATH, mPath);
		outState.putBoolean(ARG_FOLDER_BROWSER, mFolderBrowser);
	}

	@Override
	public void onBackStackChanged() {
		
		int count = mFragmentManager.getBackStackEntryCount();
		if (count > 0) {
            BackStackEntry fragment = mFragmentManager.getBackStackEntryAt(count - 1);
            mPath = fragment.getName();
		} else {
		    mPath = mBasePath;
		}
		
		setPathTitle(mPath);
	}
	
	/**
	 * Add the initial Fragment with given path.
	 */
	private void addFragment() {
		FileListFragment fragment = FileListFragment.newInstance(mPath, mFolderBrowser, getVFS());
		mFragmentManager.beginTransaction()
				.add(R.id.explorer_fragment, fragment).commit();
	}

	/**
	 * "Replace" the existing Fragment with a new one using given path.
	 * We're really adding a Fragment to the back stack.
	 * 
	 * @param file The file (directory) to display.
	 */
	private void replaceFragment(File file) {
        mPath = file.getAbsolutePath();

        FileListFragment fragment = FileListFragment.newInstance(mPath, mFolderBrowser, getVFS());
		mFragmentManager.beginTransaction()
				.replace(R.id.explorer_fragment, fragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(mPath).commit();
	}

	/**
	 * Finish this Activity with a result code and URI of the selected file.
	 * 
	 * @param file The file selected.
	 */
	private void finishWithResult(final File file) {
		if( file == null ) {
			setResult(RESULT_CANCELED);	
			finish();
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Select '" + file.getName() + "' ?")
			.setPositiveButton("Yes", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Uri uri = Uri.fromFile(file);
					setResult(RESULT_OK, new Intent().setData(uri));
					finish();
				}
			})
		    .setNegativeButton("No", null)
		    .show();
	}
	
	public static void finishWithResult(final Activity aActivity, final File file) {
		if( file == null ) {
			aActivity.setResult(RESULT_CANCELED);	
			aActivity.finish();
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(aActivity);
		builder.setMessage("Select '" + file.getName() + "' ?")
			.setPositiveButton("Yes", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Uri uri = Uri.fromFile(file);
					aActivity.setResult(RESULT_OK, new Intent().setData(uri));
					aActivity.finish();
				}
			})
		    .setNegativeButton("No", null)
		    .show();
	}
	
	/**
	 * Called when the user selects a File
	 * 
	 * @param file The file that was selected
	 */
	protected void onFileSelected(File file) {
		if (file != null) {
			// folder browser - recurse only if has children directories
			if( mFolderBrowser ) {
				if( FileUtils.hasChildDirectories( file ) ) {
					replaceFragment(file);
				} else {
					finishWithResult(file);	
				}
				return ;
			}
			
			if (file.isDirectory()) {
				replaceFragment(file);
			} else {
				finishWithResult(file);	
			}
		} else {
			Toast.makeText(FileChooserActivity.this, R.string.error_selecting_file, Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Register the external storage BroadcastReceiver.
	 */
	private void registerStorageListener() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		registerReceiver(mStorageListener, filter);
	}

	/**
	 * Unregister the external storage BroadcastReceiver.
	 */
	private void unregisterStorageListener() {
		unregisterReceiver(mStorageListener);
	}

    public VFS getVFS() {
        return new VFS() {
            @Override
            public void setObserver(DataSetObserver aObserver) {
                // TODO
            }

            @Override
            public void onActivityCreated(Context aContext, LoaderManager loaderManager, int startLoaderId, String aPath) {
                // TODO

            }

            @Override
            public List<VFile> getVFiles() {
                // TODO
                return null;
            }
        };
    }
}