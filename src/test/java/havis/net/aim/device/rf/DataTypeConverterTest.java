package havis.net.aim.device.rf;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

public class DataTypeConverterTest {

	@Test
	public void ubyte() {
		ByteBuffer bb = ByteBuffer.allocate(1024);
		bb.put((byte) 1); // 1 byte
		bb.put((byte) 0xFF);
		bb.flip();

		byte b = bb.get();
		Assert.assertEquals(b, 1);
		Assert.assertEquals(DataTypeConverter.ubyte(b), 1);
		Assert.assertEquals((byte) DataTypeConverter.ubyte(b), 1);

		b = bb.get();
		Assert.assertEquals(b, -1);
		Assert.assertEquals(DataTypeConverter.ubyte(b), 255);
		Assert.assertEquals((byte) DataTypeConverter.ubyte(b), -1);
	}

	@Test
	public void ushort() {
		ByteBuffer bb = ByteBuffer.allocate(1024);
		bb.putShort((short) 1); // 2 bytes
		bb.putShort((short) 0xFFFF);
		bb.flip();

		short s = bb.getShort();
		Assert.assertEquals(s, 1);
		Assert.assertEquals(DataTypeConverter.ushort(s), 1);
		Assert.assertEquals((short) DataTypeConverter.ushort(s), 1);

		s = bb.getShort();
		Assert.assertEquals(s, -1);
		Assert.assertEquals(DataTypeConverter.ushort(s), 65535);
		Assert.assertEquals((short) DataTypeConverter.ushort(s), -1);
	}

	@Test
	public void uint() {
		ByteBuffer bb = ByteBuffer.allocate(1024);
		bb.putInt(1); // 4 bytes
		bb.putInt(0xFFFFFFFF);
		bb.flip();

		int i = bb.getInt();
		Assert.assertEquals(i, 1);
		Assert.assertEquals(DataTypeConverter.uint(i), 1L);
		Assert.assertEquals((int) DataTypeConverter.uint(i), 1);

		i = bb.getInt();
		Assert.assertEquals(i, -1);
		Assert.assertEquals(DataTypeConverter.uint(i), 4294967295L);
		Assert.assertEquals((int) DataTypeConverter.uint(i), -1);
	}

	public void ulong() {
		ByteBuffer bb = ByteBuffer.allocate(1024);
		bb.putLong(1); // 8 bytes
		bb.putLong(0xFFFFFFFFFFFFFFFFL);
		bb.flip();

		long l = bb.getLong();
		Assert.assertEquals(l, 1);
		Assert.assertTrue(DataTypeConverter.ulong(l).equals(new BigInteger("1")));
		Assert.assertEquals(DataTypeConverter.ulong(l).longValue(), 1L);

		l = 0xFFFFFFFFFFFFFFFFL;
		Assert.assertEquals(l, -1);
		Assert.assertTrue(DataTypeConverter.ulong(l).equals(new BigInteger("18446744073709551615"))); // 2^64-1
		Assert.assertEquals(DataTypeConverter.ulong(l).longValue(), -1L);
	}
}
