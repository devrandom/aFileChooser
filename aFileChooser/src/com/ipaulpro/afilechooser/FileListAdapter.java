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

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * List adapter for Files.
 * 
 * @version 2013-06-25
 * 
 * @author paulburke (ipaulpro)
 *
 */
public class FileListAdapter extends BaseAdapter {

	private final static int ICON_FOLDER_WITH_FOLDERS = R.drawable.ic_folder_with_folders;
	private final static int ICON_FOLDER = R.drawable.ic_folder;
	private final static int ICON_FILE = R.drawable.ic_file;

	private List<File> mFiles = new ArrayList<File>();
	private Set<File> mVirtuals = new HashSet<File>();
    private Set<File> mLocals = new HashSet<File>();

	private final LayoutInflater mInflater;

	public FileListAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	public ArrayList<File> getListItems() {
		return (ArrayList<File>) mFiles;
	}

    public void setVirtualItems(List<File> list) {
        mVirtuals = new HashSet<File>();
        if( list != null) {
            mVirtuals.addAll(list);
        }
        merge() ;
		notifyDataSetChanged();
    }

    public void setFileItems(List<File> list) {
        mLocals = new HashSet<File>();
        if( list != null ) {
            mLocals.addAll(list);
        }
        merge() ;
		notifyDataSetChanged();
	}

    private void merge() {
        mFiles.clear();
        if( mLocals != null )
            mFiles.addAll(mLocals);
        if( mVirtuals != null )
            mFiles.addAll(mVirtuals);
        sort() ;
    }

    private void sort() {
        // TODO add more sorts
        // alpha sort for now
        Collections.sort(mFiles, new Comparator<File>() {
            @Override
            public int compare(File data1, File data2) {
                return data1.compareTo(data2);
            }
        });
    }

    private boolean isVirtual( int aPosition ) {
        if( mVirtuals == null ) return false ;
        return mVirtuals.contains( getItem( aPosition ) ) ;
    }

	@Override
    public int getCount() {
		return mFiles.size();
	}

	public void add(File file) {
		mFiles.add(file);
		notifyDataSetChanged();
	}

	public void clear() {
		mFiles.clear();
		notifyDataSetChanged();
	}

	@Override
    public Object getItem(int position) {
		return mFiles.get(position);
	}

	@Override
    public long getItemId(int position) {
		return position;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;

		if (row == null) {
			row = mInflater.inflate(R.layout.file, parent, false);
			holder = new ViewHolder(row);
			row.setTag(holder);
		} else {
			// Reduce, reuse, recycle!
			holder = (ViewHolder) row.getTag();
		}

		// Get the file at the current position
		final File file = (File) getItem(position);

		// Set the TextView as the file name
		holder.nameView.setText(file.getName());

        // set color for virtuals
        int color = isVirtual(position) ? Color.GRAY : Color.BLACK ;
        holder.nameView.setTextColor(color);

		// If the item is not a directory, use the file icon
//		holder.iconView.setImageResource(file.isDirectory() ? ICON_FOLDER : ICON_FILE);
		int resId = file.isDirectory() ? ( FileUtils.hasChildDirectories(file) ? ICON_FOLDER_WITH_FOLDERS : ICON_FOLDER) : ICON_FILE ;
		holder.iconView.setImageResource( resId );

		return row;
	}

    static class ViewHolder {
		TextView nameView;
		ImageView iconView;

		ViewHolder(View row) {
			nameView = (TextView) row.findViewById(R.id.file_name);
			iconView = (ImageView) row.findViewById(R.id.file_icon);
		}
	}
}
