package havis.net.aim.ui.client;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ToggleButton;

import havis.net.ui.shared.client.event.MessageEvent.MessageType;

public interface ServerSectionView extends IsWidget {

	void setPresenter(Presenter presenter);
	ToggleButton getEnableButton();
	void showMessage(MessageType messageType, String message);
	public interface Presenter {
		void onLoadState();
		void onSetState(Boolean value);
		void onDownloadLog();
	}
}
