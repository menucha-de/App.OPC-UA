package havis.net.aim.device.rf;

public enum HaIOState {
	LOW(0), //
	HIGH(1), //
	UNKNOWN(2);

	int value;

	HaIOState(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static HaIOState fromValue(int v) {
		for (HaIOState c : values()) {
			if (c.value == v) {
				return c;
			}
		}
		return null;
	}
}
