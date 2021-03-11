package havis.net.aim.ui.client;

import havis.net.aim.rest.async.OpcUaConfigurationServiceAsync;
import havis.net.aim.ui.client.ConfigSectionView.Presenter;
import havis.net.aim.xsd.Configuration;
import havis.net.aim.xsd.TagSet;
import havis.net.ui.shared.client.event.MessageEvent.MessageType;
import havis.net.ui.shared.client.widgets.LoadingSpinner;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.GWT;

public class ConfigSectionPresenter implements Presenter {

	private ConfigSectionView view;
	private OpcUaConfigurationServiceAsync service = GWT.create(OpcUaConfigurationServiceAsync.class);
	private LoadingSpinner spinner = new LoadingSpinner(); 

	public ConfigSectionPresenter(ConfigSectionView view) {
		this.view = view;
		this.view.setPresenter(this);
		loadConfiguration();
	}

	@Override
	public void onTagSetChange(TagSet tagset) {
		Configuration config = new Configuration();
		config.setTagSet(tagset);
		service.setConfiguration(config, new MethodCallback<Void>() {
			
			@Override
			public void onSuccess(Method method, Void response) {
				
			}
			
			@Override
			public void onFailure(Method method, Throwable exception) {
				view.showMessage(MessageType.ERROR, "Failed to set configuration");
			}
		});
	}

	@Override
	public void loadConfiguration() {
		service.getConfiguration(new MethodCallback<Configuration>() {
			
			@Override
			public void onSuccess(Method method, Configuration response) {
				TagSet ts = response.getTagSet();
				view.getTagSet().setSelectedIndex(ts.ordinal());
			}
			
			@Override
			public void onFailure(Method method, Throwable exception) {
				view.showMessage(MessageType.ERROR, "Failed to load configuration");
			}
		});
	}

}
