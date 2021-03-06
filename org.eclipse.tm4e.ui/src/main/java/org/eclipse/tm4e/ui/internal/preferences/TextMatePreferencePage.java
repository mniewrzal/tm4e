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
package org.eclipse.tm4e.ui.internal.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * TextMate Global preferences page.
 * 
 */
public class TextMatePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public TextMatePreferencePage() {

	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);

		// Add link to grammar preference page
		addRelatedLink(composite, GrammarPreferencePage.PAGE_ID,
				TMUIMessages.TextMatePreferencePage_GrammarRelatedLink);

		// Add link to theme preference page
		addRelatedLink(composite, ThemePreferencePage.PAGE_ID, TMUIMessages.TextMatePreferencePage_ThemeRelatedLink);

		applyDialogFont(composite);
		return composite;

	}

	private void addRelatedLink(Composite parent, String pageId, String message) {
		PreferenceLinkArea contentTypeArea = new PreferenceLinkArea(parent, SWT.NONE, pageId, message,
				(IWorkbenchPreferenceContainer) getContainer(), null);

		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		contentTypeArea.getControl().setLayoutData(data);
	}

	@Override
	public void init(IWorkbench workbench) {

	}
}
