package havis.net.aim.device.rf;

public enum HaIODirection {
	OUTPUT(0), //
	INPUT(1);

	int value;

	HaIODirection(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static HaIODirection fromValue(int v) {
		for (HaIODirection c : values()) {
			if (c.value == v) {
				return c;
			}
		}
		return null;
	}
}
