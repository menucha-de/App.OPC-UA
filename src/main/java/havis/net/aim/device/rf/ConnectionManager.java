package havis.net.aim.device.rf;

public abstract class ConnectionManager<T> {
	private T connection = null;
	private boolean isClosing = false;
	private int count = 0;

	protected abstract T open() throws Exception;

	protected abstract void close(T connection) throws Exception;

	public synchronized T acquire() throws Exception {
		if (connection == null) {
			connection = open();
		}
		count++;
		return connection;
	}

	public synchronized void release(T connection) throws Exception {
		if (connection != this.connection) {
			return;
		}		
		if (count > 0) {
			count--;
		}
		if (count == 0 && isClosing) {
			close(this.connection);
			this.connection = null;
			isClosing = false;
		}
	}

	public synchronized void requestClosing(T connection) throws Exception {
		if (connection != this.connection) {
			return;
		}
		if (connection != null) {
			if (count == 0) {
				close(this.connection);
				this.connection = null;
			} else {
				isClosing = true;
			}
		}
	}
}
