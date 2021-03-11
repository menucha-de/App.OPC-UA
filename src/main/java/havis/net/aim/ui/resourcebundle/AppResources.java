package havis.net.aim.ui.resourcebundle;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

public interface AppResources extends ClientBundle {
	
	public static final AppResources INSTANCE = GWT
			.create(AppResources.class);
	
	@Source("resources/CssResources.css")
	CssResources css();
	
	@Source("resources/spec_item_export.png")
	DataResource specItemExport();

	@Source("resources/spec_item_import.png")
	DataResource specItemImport();
}
