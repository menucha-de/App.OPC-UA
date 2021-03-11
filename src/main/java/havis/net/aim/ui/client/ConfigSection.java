package havis.net.aim.ui.client;

import havis.net.aim.ui.resourcebundle.AppResources;
import havis.net.aim.xsd.TagSet;
import havis.net.ui.shared.client.ConfigurationSection;
import havis.net.ui.shared.client.event.MessageEvent;
import havis.net.ui.shared.client.event.MessageEvent.Handler;
import havis.net.ui.shared.client.event.MessageEvent.MessageType;
import havis.net.ui.shared.client.widgets.CustomMessageWidget;
import havis.net.ui.shared.resourcebundle.ResourceBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class ConfigSection extends ConfigurationSection implements ConfigSectionView, MessageEvent.HasHandlers {

	@UiField
	FlowPanel configRows;
	
	@UiField
	CustomMessageWidget message;
	
	@UiField
	ListBox tagSet;

	private AppResources appRes = AppResources.INSTANCE;
	private ResourceBundle res = ResourceBundle.INSTANCE;

	private static ConfigSectionUiBinder uiBinder = GWT.create(ConfigSectionUiBinder.class);

	interface ConfigSectionUiBinder extends UiBinder<Widget, ConfigSection> {
	}

	private Presenter presenter;

	@UiConstructor
	public ConfigSection(String name) {
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

		appRes.css().ensureInjected();
		res.css().ensureInjected();
		initializeTagSet();
	}

	private void initializeTagSet() {
		for (TagSet ts : TagSet.values()) {
			tagSet.addItem(ts.name());
		}
	}

	@UiHandler("tagSet")
	void onTagSetChange(ChangeEvent event) {
		presenter.onTagSetChange(TagSet.valueOf(tagSet.getSelectedValue()));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showMessage(MessageType messageType, String message) {
		fireEvent(new MessageEvent(messageType, message));
	}

	@Override
	public ListBox getTagSet() {
		return tagSet;
	}

	@Override
	public HandlerRegistration addMessageEventHandler(Handler handler) {
		return addHandler(handler, MessageEvent.getType());
	}
}
