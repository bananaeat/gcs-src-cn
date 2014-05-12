/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.gcs.common;

import com.trollworks.gcs.library.LibraryFile;
import com.trollworks.gcs.menu.DataMenuProvider;
import com.trollworks.gcs.template.TemplateWindow;
import com.trollworks.toolkit.collections.Stack;
import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.ui.App;
import com.trollworks.toolkit.utility.PathUtils;

import java.awt.EventQueue;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/** A thread that periodically updates the set of available list files. */
public class ListCollectionThread extends Thread implements FileVisitor<Path> {
	private static final ListCollectionThread	INSTANCE;
	private Path								mListDir;
	private List<Object>						mLists;
	private List<Object>						mCurrent;
	private Stack<List<Object>>					mStack;
	private boolean								mRunning;

	static {
		INSTANCE = new ListCollectionThread();
		INSTANCE.start();
	}

	/** @return The one and only instance of this thread. */
	public static final ListCollectionThread get() {
		return INSTANCE;
	}

	private ListCollectionThread() {
		super("List Collection"); //$NON-NLS-1$
		setPriority(NORM_PRIORITY);
		setDaemon(true);
		mListDir = App.getHomePath().resolve("data"); //$NON-NLS-1$
	}

	/** @return The current list of lists. */
	public List<Object> getLists() {
		try {
			while (mLists == null) {
				sleep(100);
			}
		} catch (InterruptedException outerIEx) {
			// Someone is trying to terminate us... let them.
		}
		return mLists == null ? new ArrayList<>() : mLists;
	}

	@Override
	public void run() {
		if (mRunning) {
			DataMenuProvider.update();
		} else {
			mRunning = true;
			try {
				while (true) {
					List<Object> lists = collectLists();
					if (!lists.equals(mLists)) {
						mLists = lists;
						EventQueue.invokeLater(this);
					}
					sleep(5000);
				}
			} catch (InterruptedException outerIEx) {
				// Someone is trying to terminate us... let them.
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<Object> collectLists() {
		mCurrent = new ArrayList<>();
		mStack = new Stack<>();
		try {
			Files.walkFileTree(mListDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 5, this);
		} catch (Exception exception) {
			Log.error(exception);
		}
		List<Object> result = mCurrent;
		mCurrent = null;
		mStack = null;
		return result.isEmpty() ? new ArrayList<>() : (List<Object>) result.get(0);
	}

	private static boolean shouldSkip(Path path) {
		return path.getFileName().toString().startsWith("."); //$NON-NLS-1$
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if (shouldSkip(dir)) {
			return FileVisitResult.SKIP_SUBTREE;
		}
		mStack.push(mCurrent);
		mCurrent = new ArrayList<>();
		mCurrent.add(dir.getFileName().toString());
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if (!shouldSkip(file)) {
			String ext = PathUtils.getExtension(file.getFileName());
			if (LibraryFile.EXTENSION.equalsIgnoreCase(ext) || TemplateWindow.EXTENSION.equalsIgnoreCase(ext)) {
				mCurrent.add(file);
			}
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exception) throws IOException {
		Log.error(exception);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
		if (exception != null) {
			Log.error(exception);
		}
		List<Object> restoring = mStack.pop();
		if (mCurrent.size() > 1) {
			restoring.add(mCurrent);
		}
		mCurrent = restoring;
		return FileVisitResult.CONTINUE;
	}
}
