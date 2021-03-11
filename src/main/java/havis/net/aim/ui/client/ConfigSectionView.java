package havis.net.aim.ui.client;

import havis.net.aim.xsd.TagSet;
import havis.net.ui.shared.client.event.MessageEvent.MessageType;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;

public interface ConfigSectionView extends IsWidget {
	void setPresenter(Presenter presenter);
	ListBox getTagSet();

	void showMessage(MessageType messageType, String message);

	public interface Presenter {
		void onTagSetChange(TagSet tagset);
		void loadConfiguration();
	}
}
