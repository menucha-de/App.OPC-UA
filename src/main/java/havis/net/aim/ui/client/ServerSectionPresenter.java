package havis.net.aim.ui.client;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import havis.net.aim.rest.async.OpcUaServerServiceAsync;
import havis.net.aim.rest.data.OpcState;
import havis.net.ui.shared.client.event.MessageEvent.MessageType;
import havis.net.ui.shared.data.HttpMethod;

public class ServerSectionPresenter implements ServerSectionView.Presenter {

	private ServerSectionView view;
	private OpcUaServerServiceAsync service = GWT.create(OpcUaServerServiceAsync.class);

	public ServerSectionPresenter(ServerSectionView view) {
		this.view = view;
		this.view.setPresenter(this);
		onLoadState();
	}

	@Override
	public void onLoadState() {
		service.optionsServerState(new MethodCallback<Void>() {

			@Override
			public void onSuccess(Method method, Void response) {
				view.getEnableButton().setEnabled(HttpMethod.PUT.isAllowed(method.getResponse()));
				service.getServerState(new MethodCallback<OpcState>() {

					@Override
					public void onSuccess(Method method, OpcState response) {
						boolean state = response == OpcState.STARTED;
						view.getEnableButton().setValue(state);
					}

					@Override
					public void onFailure(Method method, Throwable exception) {
						view.showMessage(MessageType.ERROR, exception.getMessage());
					}
				});
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public void onSetState(Boolean value) {
		OpcState state = value ? OpcState.START : OpcState.STOP;
		service.setServerState(state, new MethodCallback<Void>() {

			@Override
			public void onSuccess(Method method, Void response) {

			}

			@Override
			public void onFailure(Method method, Throwable exception) {

			}
		});
	}

	@Override
	public void onDownloadLog() {
		Window.open(GWT.getHostPageBaseURL() + "rest/opc/server/log", "_blank", "");
	}

}
