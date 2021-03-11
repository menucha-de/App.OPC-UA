package havis.net.aim.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

import havis.net.aim.rest.async.OpcUaServerServiceAsync;
import havis.net.ui.shared.client.ConfigurationSection;
import havis.net.ui.shared.client.event.MessageEvent;
import havis.net.ui.shared.client.event.MessageEvent.Handler;
import havis.net.ui.shared.client.event.MessageEvent.MessageType;
import havis.net.ui.shared.client.widgets.CustomMessageWidget;
import havis.net.ui.shared.resourcebundle.ResourceBundle;

public class ServerSection extends ConfigurationSection implements ServerSectionView, MessageEvent.HasHandlers {

	@UiField
	Button export;
	@UiField
	ToggleButton enable;
	@UiField
	CustomMessageWidget message;

	OpcUaServerServiceAsync service = GWT.create(OpcUaServerServiceAsync.class);

	private ResourceBundle res = ResourceBundle.INSTANCE;
	private Presenter presenter;

	private static ServerSectionUiBinder uiBinder = GWT.create(ServerSectionUiBinder.class);

	interface ServerSectionUiBinder extends UiBinder<Widget, ServerSection> {
	}

	@UiConstructor
	public ServerSection(String name) {
		super(name);
		initWidget(uiBinder.createAndBindUi(this));
		addMessageEventHandler(new MessageEvent.Handler() {

			@Override
			public void onMessage(MessageEvent event) {
				message.showMessage(event.getMessage(), event.getMessageType());
			}

			@Override
			public void onClear(MessageEvent event) {
				message.showMessage(null, MessageEvent.MessageType.CLEAR);
			}
		});
		res.css().ensureInjected();
	}

	@UiHandler("enable")
	void onEnableChange(ValueChangeEvent<Boolean> event) {
		presenter.onSetState(event.getValue());
	}
	
	@UiHandler("export")
	void onExport(ClickEvent event) {
		presenter.onDownloadLog();
	}

	@Override
	public HandlerRegistration addMessageEventHandler(Handler handler) {
		return addHandler(handler, MessageEvent.getType());
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public ToggleButton getEnableButton() {
		return enable;
	}

	@Override
	public void showMessage(MessageType messageType, String message) {
		fireEvent(new MessageEvent(messageType, message));
	}
}
