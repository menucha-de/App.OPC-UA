package havis.net.aim.opcua;

import havis.net.aim.opcua.Constants.Param;

public interface ParamChangedListener {
	void paramChanged(Object source, Param param, Object newValue);
}
