/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.model;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStorageEditorInput;

/**
 * {@link IContentType} utilities.
 *
 */
public class ContentTypeHelper {

	public static class ContentTypeInfo {

		private final String fileName;
		private final IContentType[] contentTypes;

		public ContentTypeInfo(String fileName, IContentType[] contentTypes) {
			this.fileName = fileName;
			this.contentTypes = contentTypes;
		}

		public String getFileName() {
			return fileName;
		}

		public IContentType[] getContentTypes() {
			return contentTypes;
		}
	}

	/**
	 * Find the content types from the given {@link IDocument} and null
	 * otherwise.
	 * 
	 * @param document
	 * @return the content types from the given {@link IDocument} and null
	 *         otherwise.
	 * @throws CoreException
	 */
	public static ContentTypeInfo findContentTypes(IDocument document) throws CoreException {
		// Find content types from FileBuffers
		ContentTypeInfo contentTypes = findContentTypesFromFileBuffers(document);
		if (contentTypes != null) {
			return contentTypes;
		}
		// Find content types from the IEditorInput
		return findContentTypesFromEditorInput(document);
	}

	// ------------------------- Find content types from FileBuffers

	/**
	 * Find the content types from the given {@link IDocument} by using
	 * {@link ITextFileBufferManager} and null otherwise.
	 * 
	 * @param document
	 * @return the content types from the given {@link IDocument} by using
	 *         {@link ITextFileBufferManager} and null otherwise.
	 * @throws CoreException
	 */
	private static ContentTypeInfo findContentTypesFromFileBuffers(IDocument document) throws CoreException {
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer buffer = bufferManager.getTextFileBuffer(document);
		if (buffer != null) {
			return getContentTypes(buffer);
		}
		return null;
	}

	/**
	 * Returns the content types from the given {@link ITextFileBuffer}.
	 * 
	 * @param buffer
	 * @return the content types from the given {@link ITextFileBuffer}.
	 * @throws CoreException
	 */
	private static ContentTypeInfo getContentTypes(ITextFileBuffer buffer) throws CoreException {
		try {
			String fileName = buffer.getFileStore().getName();
			if (buffer.isDirty()) {
				// Buffer is dirty (content of the filesystem is not synch with
				// the editor content), use IDocument content.
				InputStream input = null;
				try {
					input = new DocumentInputStream(buffer.getDocument());
					IContentType[] contentTypes = Platform.getContentTypeManager().findContentTypesFor(input, fileName);
					if (contentTypes != null) {
						return new ContentTypeInfo(fileName, contentTypes);
					}
				} finally {
					try {
						if (input != null)
							input.close();
					} catch (IOException x) {
					}
				}
			}

			// Buffer is synchronized with filesystem content
			InputStream contents = null;
			try {
				contents = getContents(buffer);
				return new ContentTypeInfo(fileName,
						Platform.getContentTypeManager().findContentTypesFor(contents, fileName));
			} catch (Throwable e) {
				if (contents != null) {
					contents.close();
				}
				e.printStackTrace();
				return null;
			}
		} catch (IOException x) {
			x.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the content of the given buffer.
	 * 
	 * @param buffer
	 * @return the content of the given buffer.
	 * @throws CoreException
	 */
	private static InputStream getContents(ITextFileBuffer buffer) throws CoreException {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = workspaceRoot.getFile(buffer.getLocation());
		if (file.exists() && buffer.isSynchronized()) {
			return file.getContents();
		}
		return buffer.getFileStore().openInputStream(EFS.NONE, null);
	}

	// ------------------------- Find content types from FileBuffers

	/**
	 * Find the content types from the given {@link IDocument} by using
	 * {@link IEditorInput} and null otherwise.
	 * 
	 * @param document
	 * @return the content types from the given {@link IDocument} by using
	 *         {@link IEditorInput} and null otherwise.
	 */
	private static ContentTypeInfo findContentTypesFromEditorInput(IDocument document) {
		IEditorInput editorInput = getEditorInput(document);
		if (editorInput != null) {
			if (editorInput instanceof IStorageEditorInput) {
				InputStream input = null;
				try {
					IStorage storage = ((IStorageEditorInput) editorInput).getStorage();
					String fileName = storage.getName();
					input = storage.getContents();
					return new ContentTypeInfo(fileName, Platform.getContentTypeManager().findContentTypesFor(input, fileName));
				} catch (Exception e) {
					return null;
				} finally {
					try {
						if (input != null)
							input.close();
					} catch (IOException x) {
					}
				}
			} else {
				// TODO: manage other type of IEditorInput
			}
		}
		return null;
	}

	/**
	 * Returns the {@link IEditorInput} from the given document and null
	 * otherwise.
	 * 
	 * @param document
	 * @return the {@link IEditorInput} from the given document and null
	 *         otherwise.
	 */
	private static IEditorInput getEditorInput(IDocument document) {
		try {
			// This utilities class is very ugly, I have not found a clean mean
			// to retrieve the IEditorInput linked to a document.
			// Here the strategy to retrieve the IEditorInput:

			// AbstractDocumentProvider#ElementInfo add a IDocumentListener to
			// the document.
			// ElementInfo contains a fElement which is the IEditorInput (like
			// ISorageEditorInput, see StorageDocumentProvider)

			// get list of IDocumentListener
			ListenerList listeners = getFieldValue(document, "fDocumentListeners");
			if (listeners != null) {
				// Get AbstractDocumentProvider#ElementInfo
				Object[] l = listeners.getListeners();
				for (int i = 0; i < l.length; i++) {
					Object /* AbstractDocumentProvider#ElementInfo */ info = l[i];
					try {
						/* The element for which the info is stored */
						Object input = getFieldValue(info, "fElement");
						if (input instanceof IEditorInput) {
							return (IEditorInput) input;
						}
					} catch (Exception e) {

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static <T> T getFieldValue(Object object, String name) {
		Field f = getDeclaredField(object.getClass(), name);
		if (f != null) {
			try {
				return (T) f.get(object);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	private static Field getDeclaredField(Class clazz, String name) {
		if (clazz == null) {
			return null;
		}
		try {
			Field f = clazz.getDeclaredField(name);
			f.setAccessible(true);
			return f;
		} catch (NoSuchFieldException e) {
			return getDeclaredField(clazz.getSuperclass(), name);
		} catch (SecurityException e) {
			return null;
		}
	}

}
