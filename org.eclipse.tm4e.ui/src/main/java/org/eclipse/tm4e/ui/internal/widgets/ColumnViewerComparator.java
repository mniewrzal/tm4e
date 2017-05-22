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
package org.eclipse.tm4e.ui.internal.widgets;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

/**
 * Viewer compoarator which sort a given column.
 *
 */
public class ColumnViewerComparator extends ViewerComparator {

	private int fSortColumn;

	private int fSortOrder; // 1 = asc, -1 = desc

	public ColumnViewerComparator() {
		fSortColumn = 0;
		fSortOrder = 1;
	}

	/**
	 * Returns the {@linkplain SWT} style constant for the sort direction.
	 * 
	 * @return {@link SWT#DOWN} for asc sorting, {@link SWT#UP} otherwise
	 */
	public int getDirection() {
		return fSortOrder == 1 ? SWT.DOWN : SWT.UP;
	}

	/**
	 * Sets the sort column. If the newly set sort column equals the previous
	 * set sort column, the sort direction changes.
	 * 
	 * @param column
	 *            New sort column
	 */
	public void setColumn(int column) {
		if (column == fSortColumn) {
			fSortOrder *= -1;
		} else {
			fSortColumn = column;
			fSortOrder = 1;
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {

		if (viewer instanceof TableViewer) {
			IBaseLabelProvider baseLabel = ((TableViewer) viewer).getLabelProvider();

			String left = ((ITableLabelProvider) baseLabel).getColumnText(e1, fSortColumn);
			String right = ((ITableLabelProvider) baseLabel).getColumnText(e2, fSortColumn);
			int sortResult = getComparator().compare(left != null ? left : "", right != null ? right : "");
			return sortResult * fSortOrder;
		}

		return super.compare(viewer, e1, e2);
	}

}