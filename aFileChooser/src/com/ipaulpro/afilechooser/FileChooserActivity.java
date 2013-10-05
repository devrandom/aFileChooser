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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

import com.ipaulpro.afilechooser.utils.FileUtils;

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
    
    private boolean mFolderBrowser = false ;

    public static final String ARG_FOLDER_BROWSER = "FolderBrowser";
    public static final String PATH = "path";
	public static final String EXTERNAL_BASE_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath();

	private static final boolean HAS_ACTIONBAR = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

	private FragmentManager mFragmentManager;
	private BroadcastReceiver mStorageListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, R.string.storage_removed, Toast.LENGTH_LONG).show();
			finishWithResult(null);
		}
	};
	
	private String mPath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String action = getIntent().getAction() ;
		if( ACTION_FOLDER_BROWSER.equals(action) ) {
			mFolderBrowser = true ;
		}

		setContentView(R.layout.chooser);

		mFragmentManager = getSupportFragmentManager();
		mFragmentManager.addOnBackStackChangedListener(this);

		if (savedInstanceState == null) {
			mPath = EXTERNAL_BASE_PATH;
			addFragment();
		} else {
			mPath = savedInstanceState.getString(PATH);
			mFolderBrowser = savedInstanceState.getBoolean(ARG_FOLDER_BROWSER);
		}
		// TODO handle path inside the geebox
		setTitle(mPath);
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
		    mPath = EXTERNAL_BASE_PATH;
		}
		
		setTitle(mPath);
		if (HAS_ACTIONBAR) invalidateOptionsMenu();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        if (HAS_ACTIONBAR) {
            boolean hasBackStack = mFragmentManager.getBackStackEntryCount() > 0;
            
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(hasBackStack);
            actionBar.setHomeButtonEnabled(hasBackStack);
        }
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            mFragmentManager.popBackStack();
	            return true;
	    }

	    return super.onOptionsItemSelected(item);
	}

	/**
	 * Add the initial Fragment with given path.
	 */
	private void addFragment() {
		FileListFragment fragment = FileListFragment.newInstance(mPath,mFolderBrowser);
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

        FileListFragment fragment = FileListFragment.newInstance(mPath, mFolderBrowser);
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
}
