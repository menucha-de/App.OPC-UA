package havis.net.aim.device.rf;


/**
 * This enum maps TX levels in dBm and mW to Nur API constants and the
 * corresponding numeric values.
 */

public enum TxLevel {
	/**
	 * Entry for TXLevel 8 dBm = 6 mW
	 *
	 * 10 ^ (8 dBm / 10 dBm) * 1 mW = 6.3 mW,
	 * log (6 mW / 1 mW) * 10 dBm = 7.8 dBm  
	 */
	TxLevel8((short) 8, (short) 6),

	/**
	 * Entry for TXLevel 9 dBm = 8 mW
	 */
	TxLevel9((short) 9, (short) 8),

	/**
	 * Entry for TXLevel 10 dBm = 10 mW
	 */
	TxLevel10((short) 10, (short) 10),

	/**
	 * Entry for TXLevel 11 dBm = 13 mW
	 */
	TxLevel11((short) 11, (short) 13),

	/**
	 * Entry for TXLevel 12 dBm = 16 mW
	 */
	TxLevel12((short) 12, (short) 16),

	/**
	 * Entry for TXLevel 13 dBm = 20 mW
	 */
	TxLevel13((short) 13, (short) 20),

	/**
	 * Entry for TXLevel 14 dBm = 25 mW
	 */
	TxLevel14((short) 14, (short) 25),

	/**
	 * Entry for TXLevel 15 dBm = 32 mW
	 */
	TxLevel15((short) 15, (short) 32),

	/**
	 * Entry for TXLevel 16 dBm = 40 mW
	 */
	TxLevel16((short) 16, (short) 40),

	/**
	 * Entry for TXLevel 17 dBm = 50 mW
	 */
	TxLevel17((short) 17, (short) 50),

	/**
	 * Entry for TXLevel 18 dBm = 63 mW
	 */
	TxLevel18((short) 18, (short) 63),

	/**
	 * Entry for TXLevel 19 dBm = 79 mW
	 */
	TxLevel19((short) 19, (short) 79),

	/**
	 * Entry for TXLevel 20 dBm = 100 mW
	 */
	TxLevel20((short) 20, (short) 100),

	/**
	 * Entry for TXLevel 21 dBm = 126 mW
	 */
	TxLevel21((short) 21, (short) 126),

	/**
	 * Entry for TXLevel 22 dBm = 158 mW
	 */
	TxLevel22((short) 22, (short) 158),

	/**
	 * Entry for TXLevel 23 dBm = 200 mW
	 */
	TxLevel23((short) 23, (short) 200),

	/**
	 * Entry for TXLevel 24 dBm = 251 mW
	 */
	TxLevel24((short) 24, (short) 251),

	/**
	 * Entry for TXLevel 25 dBm = 316 mW
	 */
	TxLevel25((short) 25, (short) 316),

	/**
	 * Entry for TXLevel 26 dBm = 398 mW
	 */
	TxLevel26((short) 26, (short) 398),

	/**
	 * Entry for TXLevel 27 dBm = 500 mW
	 */
	TxLevel27((short) 27, (short) 500),
	
	/**
	 * Entry for TXLevel 0 dBm = 0 mW
	 */
	TxLevelNull((short) 0, (short) 0);

	final short dBm;
	final int mW;

	private TxLevel(short dBm, short mW) {
		this.mW = mW;
		this.dBm = dBm;			
	}

	/**
	 * Returns a {@link TxLevel} instance for a specific dBm value.
	 * 
	 * @param dbm
	 *            a dBm value, valid values range from 8 to 27 dBm (and 0
	 *            for pseudo value TxLevelDefault)
	 * @return a {@link TxLevel} instance
	 * @throws IllegalArgumentException
	 *             if the dBm value passed is not within the valid range.
	 */
	static TxLevel fromDBm(short dbm) {
		switch (dbm) {
		case 0:
			return TxLevelNull;
		case 8:
			return TxLevel8;
		case 9:
			return TxLevel9;
		case 10:
			return TxLevel10;
		case 11:
			return TxLevel11;
		case 12:
			return TxLevel12;
		case 13:
			return TxLevel13;
		case 14:
			return TxLevel14;
		case 15:
			return TxLevel15;
		case 16:
			return TxLevel16;
		case 17:
			return TxLevel17;
		case 18:
			return TxLevel18;
		case 19:
			return TxLevel19;
		case 20:
			return TxLevel20;
		case 21:
			return TxLevel21;
		case 22:
			return TxLevel22;
		case 23:
			return TxLevel23;
		case 24:
			return TxLevel24;
		case 25:
			return TxLevel25;
		case 26:
			return TxLevel26;
		case 27:
			return TxLevel27;

		default:
			throw new IllegalArgumentException(
					"No enum constant found for TX level (dBm): " + dbm);
		}
	}
}	

