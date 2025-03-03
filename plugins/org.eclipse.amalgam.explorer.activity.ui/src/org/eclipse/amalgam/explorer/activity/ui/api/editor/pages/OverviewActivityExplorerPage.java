/*******************************************************************************
 * Copyright (c)  2006, 2016 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.eclipse.amalgam.explorer.activity.ui.api.editor.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.amalgam.explorer.activity.ui.ActivityExplorerActivator;
import org.eclipse.amalgam.explorer.activity.ui.api.actions.OpenActivityExplorerAction2;
import org.eclipse.amalgam.explorer.activity.ui.api.editor.ActivityExplorerEditor;
import org.eclipse.amalgam.explorer.activity.ui.api.editor.input.ActivityExplorerEditorInput;
import org.eclipse.amalgam.explorer.activity.ui.api.editor.pages.helper.FormHelper;
import org.eclipse.amalgam.explorer.activity.ui.api.editor.pages.helper.HTMLHelper;
import org.eclipse.amalgam.explorer.activity.ui.internal.actions.util.FormTextPageLinkAdapter;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Overview page that behaves as a Activity Explorer. That displays navigable
 * links to other pages.
 * 
 */
public class OverviewActivityExplorerPage extends CommonActivityExplorerPage {

	private static final String PAGE_ID = "overview"; //$NON-NLS-1$

	private static final String PAGE_TITLE = Messages.OverviewActivityExplorerPage_1;

	class OverviewPageLinkAdapter extends FormTextPageLinkAdapter {

		/**
		 * @param editor
		 */
		private OverviewPageLinkAdapter(FormEditor editor) {
			super(editor);
		}

		/**
		 * @see org.eclipse.amalgam.explorer.activity.ui.api.actions.util.PageLinkAdapter#linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent)
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void linkEntered(HyperlinkEvent hlevent) {
			super.linkEntered(hlevent);
			// Replace the original image with the one used in hover mode.
			updateImage(hlevent, __hoverHrefImageMappings);
		}

		/**
		 * @see org.eclipse.amalgam.explorer.activity.ui.api.actions.util.FormTextPageLinkAdapter#linkExited(org.eclipse.ui.forms.events.HyperlinkEvent)
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void linkExited(HyperlinkEvent hlevent) {
			super.linkExited(hlevent);
			// Restore original image.
			updateImage(hlevent, __hrefImageMappings);
		}

		private void updateImage(HyperlinkEvent hlevent, Map<String, String> imageMappings) {
			String pageId = (String) hlevent.getHref();
			// Replace image with the one got from specified map.
			String image = imageMappings.get(pageId);
			if (null != image) {
				((FormText) hlevent.widget).setImage(pageId, ActivityExplorerActivator.getDefault().getImage(image));
			}
		}
	}

	/**
	 * Normal HREF image mappings.
	 */
	private static Map<String, String> __hrefImageMappings = new HashMap<String, String>();

	/**
	 * Hover HREF image mappings.
	 */
	private static Map<String, String> __hoverHrefImageMappings = new HashMap<String, String>();

	private IMenuListener2 listener = new IMenuListener2() {

		public void menuAboutToShow(IMenuManager manager) {
			final Session current = ((ActivityExplorerEditorInput) getEditorInput()).getSession();
			Form formWidget = getManagedForm().getForm().getForm();
			manager.removeAll();

			for (Session session : SessionManager.INSTANCE.getSessions()) {
				if (!session.equals(current))
					formWidget.getMenuManager().add(new OpenActivityExplorerAction2(session));
			}

		}

		public void menuAboutToHide(IMenuManager manager) {

		}
	};

	public OverviewActivityExplorerPage() {
		super(null, PAGE_ID, "");
	}

	/**
	 * Constructor.
	 * 
	 * @param editor
	 */
	public OverviewActivityExplorerPage(FormEditor editor) {
		super(editor, PAGE_ID, PAGE_TITLE);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);

		// Set the header title.
		ScrolledForm form = managedForm.getForm();
		form.setText(getHeaderTitle());
		// Install a default layout.
		TableWrapLayout layout = new TableWrapLayout();

		layout.leftMargin = 0;
		layout.rightMargin = 0;
		layout.topMargin = 20;
		layout.bottomMargin = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.numColumns = 2;

		Composite body = form.getBody();
		body.setLayout(layout);

		// add Session Manager On Fly

		final Form formWidget = managedForm.getForm().getForm();

		((ActivityExplorerEditorInput) this.getEditorInput()).getSession();

		// init
		formWidget.getMenuManager().add(new Separator("empty-list")); //$NON-NLS-1$

		// add listener
		formWidget.getMenuManager().addMenuListener(listener);

		// Create the overview content.

		// Set image HREF.
		// FormText richText = null;

		for (CommonActivityExplorerPage page : getContributedPages()) {

			createSubForm(managedForm, body, page);

		}

		form.reflow(true);
	}

	@Override
	public void dispose() {
		if (getManagedForm() != null) {
			getManagedForm().getForm().getForm().getMenuManager().removeMenuListener(listener);
		}
		super.dispose();
	}

	protected String getHeaderTitle() {
		return Messages.OverviewActivityExplorerPage_2 + ((ActivityExplorerEditorInput) getEditorInput()).getName();
	}

	// public Map<String, Composite> contents = new HashMap<String,
	// Composite>();

	public Composite createSubForm(IManagedForm managedForm, Composite body, CommonActivityExplorerPage page) {

		Composite composite = body;

		FormText richText = null;

		if (page.isVisible()) {
			// image

			String txt = HTMLHelper.imageLinkForm(page);
			if (!(txt == null || txt.isEmpty())) {

				richText = FormHelper.createRichText(managedForm.getToolkit(), composite, txt,
						new OverviewPageLinkAdapter(getEditor()));
				richText.setHyperlinkSettings(managedForm.getToolkit().getHyperlinkGroup());
				richText.setImage(page.getId(), page.getOverviewImageOff());

				richText.marginHeight = 0;
				richText.marginWidth = 0;
				TableWrapData layoutData = new TableWrapData();
				layoutData.align = TableWrapData.CENTER;
				layoutData.valign = TableWrapData.MIDDLE;
				richText.setLayoutData(layoutData);

				txt = HTMLHelper.overviewDescForm(page);

				// Set Layout data.

			}
			// text

			if (!(txt == null || txt.isEmpty())) {
				ScrolledFormText descriptionForm = new ScrolledFormText(composite, true);

				descriptionForm.setBackground(new Color(PlatformUI.getWorkbench().getDisplay(), 255, 255, 255));

				descriptionForm.setText(txt);

				descriptionForm.setMinWidth(600);
				TableWrapData layoutData2 = new TableWrapData();

				layoutData2.maxHeight = 100;
				layoutData2.maxWidth = 600;
				layoutData2.align = TableWrapData.CENTER;
				layoutData2.valign = TableWrapData.MIDDLE;
				descriptionForm.setLayoutData(layoutData2);
				
				descriptionForm.pack(false);
			}

			richText.marginHeight = 0;
			richText.marginWidth = 40;
		}
		return composite;
	}

	// public Composite getSubFormComposite(final String id) {
	// return contents.get(id);
	// }

	@Override
	public boolean isVisible() {
		// By default all Overview page are visible (cf. bug 485652)
		boolean result = true;
		if (predicate != null) {
			result &= predicate.isPageOk(this);
		}
		return result;
	}

	private List<CommonActivityExplorerPage> getContributedPages() {
		List<CommonActivityExplorerPage> contributed = new ArrayList<CommonActivityExplorerPage>();
		for (IFormPage page : ((ActivityExplorerEditor) getEditor()).getPages()) {
			if (page instanceof CommonActivityExplorerPage) {
				CommonActivityExplorerPage ap = (CommonActivityExplorerPage) page;
				if (ap.contributeToOverview()) {
					contributed.add(ap);
					__hrefImageMappings.put(ap.getId(), ap.getOverViewImageOffPath());
					__hoverHrefImageMappings.put(ap.getId(), ap.getOverViewImageOnPath());
				}
			}

		}
		return contributed;
	}
}
