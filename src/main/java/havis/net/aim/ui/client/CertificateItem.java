package havis.net.aim.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.adapters.SimpleEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

import havis.net.aim.ui.client.SecuritySectionView.Presenter;
import havis.net.ui.shared.resourcebundle.ResourceBundle;

public class CertificateItem extends Composite {

	SimpleEditor<String> id = SimpleEditor.of();

	private ResourceBundle res = ResourceBundle.INSTANCE;

	@UiField
	Label name;

	@UiField
	Anchor export;

	@UiField
	Label delete;

	@UiField
	ToggleButton extend;

	@UiField
	FlowPanel innerButton;

	private Presenter presenter;

	private CertificateType type;

	private static CertificateItemUiBinder uiBinder = GWT.create(CertificateItemUiBinder.class);

	interface CertificateItemUiBinder extends UiBinder<Widget, CertificateItem> {
	}

	public CertificateItem(String name, Presenter presenter, CertificateType type) {
		initWidget(uiBinder.createAndBindUi(this));
		this.name.setText(name);
		this.presenter = presenter;
		this.type = type;
	}

	@UiHandler("extend")
	void onChangeExtend(ValueChangeEvent<Boolean> event) {
		export.setStyleName(res.css().closed(), !event.getValue());
		delete.setStyleName(res.css().closed(), !event.getValue());
		extend.setStyleName(res.css().closed(), !event.getValue());
	}

	@UiHandler("focus")
	void onMouseOver(MouseOverEvent event) {
		extend.setValue(true, true);
	}

	@UiHandler("focus")
	void onMouseOut(MouseOutEvent event) {
		extend.setValue(false, true);
	}

	@UiHandler("delete")
	void onDeleteSpecClick(ClickEvent event) {
		presenter.onDeleteCertificate(name.getText(), type);
	}

	@UiHandler({ "name", "export" })
	void onExportClick(ClickEvent event) {
		presenter.onDownloadCertificate(name.getText(), type);
	}
}
