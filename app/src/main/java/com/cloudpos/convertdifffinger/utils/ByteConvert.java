package com.cloudpos.convertdifffinger.utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.zip.GZIPOutputStream;

/*
 * Created on 2004-5-11
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/*
 * updated by Hans_j on 2004-6-15
 *
 * 1, overload the method buf2String to print string in one line
 * 2, update the method to deal with the bigIndian
 */
/*
 * update start by hans_j at 2004-9-2 11:24:54
 * add the byteString2Bytes, convert the byte String in hex "02 03 af a4 " to byte array
 */
/*
 * updated by Hans_j on 2005-6-8
 *
 * 1, the using of big-endian is wrong in int <--> byte 
 */

/**
 * @author Minye_L
 * @version 0.2 2003/03/19
 *          <p>
 *          The ByteConvert class used for convert byte stream to primary data,
 *          such as int, short, ...
 */

final public class ByteConvert {
	final static String lineSeperate = "\r\n";
	final public static int DEFAULT_BUFFERLENGTH = 1024 * 3;
	final public static int DEFAULT_TABLELENGTH = 256;
	// update start by hans_j at 2004-8-15 21:08:49
	// private static StringBuffer sBuf;
	// update end by hans_j at 2004-8-15
	private static String[] convertTable;

	static {
		convertTable = new String[DEFAULT_TABLELENGTH];
		// update start by hans_j at 2004-8-15 21:09:04
		// sBuf = new StringBuffer(DEFAULT_BUFFERLENGTH);
		// update end by hans_j at 2004-8-15

		int i = 0;
		for (i = 0; i < 16; i++) {
			convertTable[i] = "0" + Integer.toHexString(i) + " ";
		}
		for (; i < 256; i++) {
			convertTable[i] = Integer.toHexString(i) + " ";
		}
	}

	/**
	 * convert two byte to int, in fact return int for represent unsigned short.
	 *
	 * @param convertByte byte stream
	 * @return int
	 */
	public static int byte2int2(byte[] convertByte) {
		return byte2int2(convertByte, 0, true);
	}

	// add by jhq at 2004-6-15 for bigIndian
	public static int byte2int2(byte[] convertByte, boolean bigIndian) {
		return byte2int2(convertByte, 0, bigIndian);
	}

	// add end at 2004-6-15

	/**
	 * convert two byte to int, in fact return int for represent unsigned short.
	 *
	 * @param convertByte byte stream
	 * @param offset      offset of byte stream to be converted
	 * @return int
	 */
	public static int byte2int2(byte[] convertByte, int offset) {
		// update by jhq at 2004-6-15
		// int value=0;
		// int byte0,byte1;
		//
		// byte0=convertByte[0+offset]<0?convertByte[0+offset]+256:convertByte[0+offset];
		// byte1=convertByte[1+offset]<0?convertByte[1+offset]+256:convertByte[1+offset];
		//
		// value=byte1*256+byte0;
		// return value;
		return byte2int2(convertByte, offset, true);
		// update end at 2004-6-15
	}

	// add by jhq at 2004-6-15
	public static int byte2int2(byte[] convertByte, int offset,
								boolean bigIndian) {
		int value = 0;
		int byte0, byte1;

		byte0 = convertByte[0 + offset] < 0 ? convertByte[0 + offset] + 256
				: convertByte[0 + offset];
		byte1 = convertByte[1 + offset] < 0 ? convertByte[1 + offset] + 256
				: convertByte[1 + offset];
		// update start by Hans_j at 2005-6-8 9:52:19
		// if (bigIndian)
		if (!bigIndian)
			// update end by Hans_j at 2005-6-8
			value = byte1 * 256 + byte0;
		else
			value = byte0 * 256 + byte1;

		return value;
	}

	// add end at 2004-6-15
	public static int byte2int2sign(byte[] convertByte) {
		return byte2int2sign(convertByte, 0, true);
	}

	public static int byte2int2sign(byte[] convertByte, int offset,
									boolean bigIndian) {
		int value = 0;
		if (!bigIndian)
			value = (convertByte[1 + offset] << 8)
					| (convertByte[0 + offset] & 0xff);
		else
			value = (convertByte[0 + offset] << 8)
					| (convertByte[1 + offset] & 0xff);
		return value;
	}

	/**
	 * convert four byte to int.
	 *
	 * @param convertByte byte stream
	 * @return int
	 */
	public static int byte2int4(byte[] convertByte) {
		return byte2int4(convertByte, 0, true);
	}

	// add by jhq at 2004-6-15
	public static int byte2int4(byte[] convertByte, boolean bigIndian) {
		return byte2int4(convertByte, 0, bigIndian);
	}

	// add end 2004-6-15

	/**
	 * convert four byte to int.
	 *
	 * @param convertByte byte stream
	 * @param offset      offset of byte stream to be converted
	 * @return int
	 */
	public static int byte2int4(byte[] convertByte, int offset) {
		// update by jhq at 2004-6-15
		// int value = 0;
		// int byte0, byte1, byte2, byte3;
		//
		// byte0 =
		// convertByte[0 + offset] < 0
		// ? convertByte[0 + offset] + 256
		// : convertByte[0 + offset];
		// byte1 =
		// convertByte[1 + offset] < 0
		// ? convertByte[1 + offset] + 256
		// : convertByte[1 + offset];
		// byte2 =
		// convertByte[2 + offset] < 0
		// ? convertByte[2 + offset] + 256
		// : convertByte[2 + offset];
		// byte3 =
		// convertByte[3 + offset] < 0
		// ? convertByte[3 + offset] + 256
		// : convertByte[3 + offset];
		//
		// value = (byte3 << 24) + (byte2 << 16) + (byte1 << 8) + byte0;
		// return value;
		return byte2int4(convertByte, offset, true);
		// update end at 2004-6-15
	}

	// add by jhq at 2004-6-15
	public static int byte2int4(byte[] convertByte, int offset,
								boolean bigIndian) {
		int value = 0;
		int byte0, byte1, byte2, byte3;

		byte0 = convertByte[0 + offset] < 0 ? convertByte[0 + offset] + 256
				: convertByte[0 + offset];
		byte1 = convertByte[1 + offset] < 0 ? convertByte[1 + offset] + 256
				: convertByte[1 + offset];
		byte2 = convertByte[2 + offset] < 0 ? convertByte[2 + offset] + 256
				: convertByte[2 + offset];
		byte3 = convertByte[3 + offset] < 0 ? convertByte[3 + offset] + 256
				: convertByte[3 + offset];
		// update start by Hans_j at 2005-6-8 9:54:21
		// if (bigIndian)
		if (!bigIndian)
			// update end by Hans_j at 2005-6-8
			value = (byte3 << 24) + (byte2 << 16) + (byte1 << 8) + byte0;
		else
			value = (byte0 << 24) + (byte1 << 16) + (byte2 << 8) + byte3;
		return value;
	}

	// add end at 2004-6-15
	public static int byte2int4sign(byte[] convertByte, int offset,
									boolean bigIndian) {
		int value = 0;
		if (!bigIndian)
			value = ((convertByte[3 + offset] << 24) & 0xff000000)
					| ((convertByte[2 + offset] << 16) & 0xff0000)
					| ((convertByte[1 + offset] << 8) & 0xff00)
					| (convertByte[0 + offset] & 0xff);
		else
			value = ((convertByte[0 + offset] << 24) & 0xff000000)
					| ((convertByte[1 + offset] << 16) & 0xff0000)
					| ((convertByte[2 + offset] << 8) & 0xff00)
					| (convertByte[3 + offset] & 0xff);
		return value;
	}

	/**
	 * convert short to two byte.
	 *
	 * @param value int value represent unsigned short
	 * @return byte[]
	 */
	public static byte[] int2byte2(int value) {
		// update by jhq at 2004-6-15
		// byte[] byteValue = new byte[2];
		//
		// byteValue[0] = (byte) (value & 0xff);
		// byteValue[1] = (byte) ((value & 0xff00) >>> 8);
		// return byteValue;
		return int2byte2(value, true);
		// update end at 2004-6-15
	}

	// add by jhq at 2004-6-15
	public static byte[] int2byte2(int value, boolean bigIndian) {
		byte[] byteValue = new byte[2];
		// update start by Hans_j at 2005-6-8 10:15:35
		// if (bigIndian) {
		if (!bigIndian) {
			// update end by Hans_j at 2005-6-8
			byteValue[0] = (byte) (value & 0xff);
			byteValue[1] = (byte) ((value & 0xff00) >>> 8);
		} else {
			byteValue[1] = (byte) (value & 0xff);
			byteValue[0] = (byte) ((value & 0xff00) >>> 8);
		}
		return byteValue;
	}

	// add end at 2004-6-15

	/**
	 * convert short to two byte.
	 *
	 * @param value    int value represent unsigned short
	 * @param fillByte byte stream to set
	 * @return void
	 */
	public static void int2byte2(int value, byte[] fillByte) {
		// update by jhq at 2004-6-15
		// int2byte2(value, fillByte, 0);
		int2byte2(value, fillByte, 0, true);
		// update end at 2004-6-15
	}

	// add by jhq at 2004-6-15
	public static void int2byte2(int value, byte[] fillByte, boolean bigIndian) {
		int2byte2(value, fillByte, 0, bigIndian);
	}

	// add end at 2004-6-15

	/**
	 * convert short to two byte.
	 *
	 * @param value    int value represent unsigned short
	 * @param fillByte byte stream to set
	 * @param fillByte at the offset of byte stream to set
	 * @return void
	 */
	public static void int2byte2(int value, byte[] fillByte, int offset) {
		// update by jhq at 2004-6-15
		// fillByte[0 + offset] = (byte) (value & 0xff);
		// fillByte[1 + offset] = (byte) ((value & 0xff00) >>> 8);
		int2byte2(value, fillByte, offset, true);
		// update end at 2004-6-15
	}

	// add by jhq at 2004-6-15
	public static void int2byte2(int value, byte[] fillByte, int offset,
								 boolean bigIndian) {
		// update start by Hans_j at 2005-6-8 10:15:35
		// if (bigIndian) {
		if (!bigIndian) {
			// update end by Hans_j at 2005-6-8
			fillByte[0 + offset] = (byte) (value & 0xff);
			fillByte[1 + offset] = (byte) ((value & 0xff00) >>> 8);
		} else {
			fillByte[1 + offset] = (byte) (value & 0xff);
			fillByte[0 + offset] = (byte) ((value & 0xff00) >>> 8);
		}
	}

	// add at 2004-6-15

	/**
	 * convert int to four byte.
	 *
	 * @param value int
	 * @return byte[]
	 * <p>
	 * No exceptions thrown
	 */
	public static byte[] int2byte4(int value) {
		// update by jhq at 2004-6-15
		// byte[] byteValue = new byte[4];
		//
		// byteValue[0] = (byte) (value & 0xff);
		// byteValue[1] = (byte) ((value & 0xff00) >>> 8);
		// byteValue[2] = (byte) ((value & 0xff0000) >>> 16);
		// byteValue[3] = (byte) ((value & 0xff000000) >>> 24);
		// return byteValue;
		return int2byte4(value, true);
		// update end at 2004-6-15
	}

	// add by jhq at 2004-6-15
	public static byte[] int2byte4(int value, boolean bigIndian) {
		byte[] byteValue = new byte[4];
		// update start by Hans_j at 2005-6-8 10:09:12
		// if (bigIndian) {
		if (!bigIndian) {
			// update end by Hans_j at 2005-6-8
			byteValue[0] = (byte) (value & 0xff);
			byteValue[1] = (byte) ((value & 0xff00) >>> 8);
			byteValue[2] = (byte) ((value & 0xff0000) >>> 16);
			byteValue[3] = (byte) ((value & 0xff000000) >>> 24);
		} else {
			byteValue[3] = (byte) (value & 0xff);
			byteValue[2] = (byte) ((value & 0xff00) >>> 8);
			byteValue[1] = (byte) ((value & 0xff0000) >>> 16);
			byteValue[0] = (byte) ((value & 0xff000000) >>> 24);
		}
		return byteValue;
	}

	// add end at 2004-6-15

	/**
	 * convertint to four byte.
	 *
	 * @param value    int value represent unsigned short
	 * @param fillByte byte stream to set
	 * @return void
	 * <p>
	 * No exceptions thrown
	 */
	public static void int2byte4(int value, byte[] fillByte) {
		// update by jhq at 2004-6-15
		// int2byte4(value, fillByte, 0);
		int2byte4(value, fillByte, 0, true);
		// update end at 2004-6-15
	}

	// add by jhq at 2004-6-15
	public static void int2byte4(int value, byte[] fillByte, boolean bigIndian) {
		int2byte4(value, fillByte, 0, bigIndian);
	}

	// add end at 2004-6-15

	/**
	 * convertint to four byte.
	 *
	 * @param value    int value represent unsigned short
	 * @param fillByte byte stream to set
	 * @param fillByte at the offset of byte stream to set
	 * @return void
	 * <p>
	 * No exceptions thrown
	 */
	public static void int2byte4(int value, byte[] fillByte, int offset) {
		// update by jhq at 2004-6-15
		// fillByte[0 + offset] = (byte) (value & 0xff);
		// fillByte[1 + offset] = (byte) ((value & 0xff00) >>> 8);
		// fillByte[2 + offset] = (byte) ((value & 0xff0000) >>> 16);
		// fillByte[3 + offset] = (byte) ((value & 0xff000000) >>> 24);
		int2byte4(value, fillByte, offset, true);
		// update end at 2004-6-15
	}

	// add by jhq at 20074-6-15
	public static void int2byte4(int value, byte[] fillByte, int offset,
								 boolean bigIndian) {
		// update start by Hans_j at 2005-6-8 10:15:35
		// if (bigIndian) {
		if (!bigIndian) {
			// update end by Hans_j at 2005-6-8
			fillByte[0 + offset] = (byte) (value & 0xff);
			fillByte[1 + offset] = (byte) ((value & 0xff00) >>> 8);
			fillByte[2 + offset] = (byte) ((value & 0xff0000) >>> 16);
			fillByte[3 + offset] = (byte) ((value & 0xff000000) >>> 24);
		} else {
			fillByte[3 + offset] = (byte) (value & 0xff);
			fillByte[2 + offset] = (byte) ((value & 0xff00) >>> 8);
			fillByte[1 + offset] = (byte) ((value & 0xff0000) >>> 16);
			fillByte[0 + offset] = (byte) ((value & 0xff000000) >>> 24);
		}
	}

	// add end at 2004-6-15
	public static String buf2String(String info, byte[] buf) {
		// update by jhq at 2004-6-15
		// return buf2String(info, buf, 0, buf.length);
		return buf2String(info, buf, 0, buf.length, true);
		// update end at 2004-6-15
	}

	// add by jhq at 2004-6-15
	public static String buf2String(String info, byte[] buf, boolean oneLine16) {
		return buf2String(info, buf, 0, buf.length, oneLine16);
	}

	// add end at 2004-6-15
	public static String buf2String(String info, byte[] buf, int offset,
                                    int length) {
		// update by jhq at 2004-6-15
		// int i, index;
		//
		// sBuf.delete(0, sBuf.length());
		// sBuf.append(info);
		//
		// for (i = 0 + offset; i < length + offset; i++) {
		// if (i % 16 == 0) {
		// sBuf.append(lineSeperate);
		// } else if (i % 8 == 0) {
		// sBuf.append(" ");
		// }
		// index = buf[i] < 0 ? buf[i] + DEFAULT_TABLELENGTH : buf[i];
		// sBuf.append(convertTable[index]);
		// }
		//
		// return sBuf.toString();
		return buf2String(info, buf, offset, length, true);
		// update end at 2004-6-15
	}

	// add by jhq at 2004-6-15 for no return String
	public static String buf2String(String info, byte[] buf, int offset,
                                    int length, boolean oneLine16) {
		int i, index;
		// update start by hans_j at 2004-8-15 21:08:15
		// sBuf.delete(0, sBuf.length());
		// update start by hans_j at 2004-9-24 15:47:58
		// if(Config.ENABLE_MEMORY_INFO)com.shera.cldc.util.MemMonitor.printInfo("bBuf2Str");
		// update end by hans_j at 2004-9-24

		StringBuffer sBuf = new StringBuffer();
		// update end by hans_j at 2004-8-15
		sBuf.append(info);

		for (i = 0 + offset; i < length + offset; i++) {
			if (i % 16 == 0) {
				if (oneLine16) {
					sBuf.append(lineSeperate);
				}
			} else if (i % 8 == 0) {
				if (oneLine16) {
					sBuf.append("   ");
				}
			}
			index = buf[i] < 0 ? buf[i] + DEFAULT_TABLELENGTH : buf[i];
			sBuf.append(convertTable[index]);
		}

		// update start by hans_j at 2004-9-24 15:47:58
		// if(Config.ENABLE_MEMORY_INFO)com.shera.cldc.util.MemMonitor.printInfo("eBuf2Str");
		// update end by hans_j at 2004-9-24
		return sBuf.toString();
	}

	// add end at 2004-6-15
	public static String buf2StringCompact(byte[] buf) {
		int i, index;
		StringBuffer sBuf = new StringBuffer();
		for (i = 0; i < buf.length; i++) {
			index = buf[i] < 0 ? buf[i] + DEFAULT_TABLELENGTH : buf[i];
			if (index < 16) {
				sBuf.append("0" + Integer.toHexString(index));
			} else {
				sBuf.append(Integer.toHexString(index));
			}
		}
		return sBuf.toString();
	}


	public static byte[] getBytesSHA256BASE64(String str) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			digest.update(str.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return digest.digest();
	}

	/**
	 * Convert Hex to Binary
	 *
	 * @param hexStr
	 * @return
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
					16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	/**
	 * Convert Binary to Hex
	 *
	 * @param buf
	 * @return
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * Compress by gzip
	 */
	public static byte[] gZip(String primStr) {
		if (primStr == null || primStr.length() == 0) {
			return null;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		GZIPOutputStream gzip = null;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(primStr.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (gzip != null) {
				try {
					gzip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return out.toByteArray();
	}


	/**
	 * @param src       first byte array
	 * @param srcOffset the start position of the first byte array
	 * @param tag       the second byte array
	 * @param tagOffset the start position of the second byte array
	 * @param length    the length of the byte to be compared
	 * @return
	 * @author pengli by 2016.11.14
	 * check if the two byte array is same.
	 */
	public static boolean compareByteArray(byte[] src, int srcOffset,
										   byte[] tag, int tagOffset, int length) {
		int compareLength = length;
		// check if the length is out of the array's length
		int srcRealLen = src.length - srcOffset;
		int tagRealLen = tag.length - tagOffset;
		if ((srcRealLen < compareLength || tagRealLen < compareLength)) {
			// if the given length is too long
			if (srcRealLen == tagRealLen) {
				// if the two array's rest length is same, so continue comparing to the rest length
				compareLength = srcRealLen;
			} else {
				// if the two array's rest length is different, so must return false
				return false;
			}
		}
		// only compare to the compare length for safe operator in array
		for (int i = 0; i < compareLength; i++) {
			// compare the byte
			Log.i("compareByteArray", "src[srcOffset + i]  = " + src[srcOffset + i] + ", tag[tagOffset + i] = " + tag[tagOffset + i]);
			if (src[srcOffset + i] != tag[tagOffset + i])
				return false;
		}
		return true;
	}

	public static String getBestStringBySpace(byte[] buffer) {
		StringBuilder sb = new StringBuilder();
		for (byte b : buffer) {
			sb.append(String.format("%02X ", b));
		}
		return sb.toString();
	}

	public static String getBestString(byte[] buffer) {
		if (buffer != null && buffer.length > 0) {
			String str = new String();
			for (byte b : buffer) {
				str = str + String.format("%02X", b);
			}
			return str;
		}
		return null;
	}

	public static int longToString(long number, byte[] buf, int offset, int length, boolean bcdFlag) {
		if (buf == null
				|| offset < 0
				|| length < 1
				|| buf.length < (offset + length)
				) {
			return -1;
		}
		length = offset + length;
		int charPos = length - 1;
		int numberLength = charPos;
		boolean numberLengthFlag = true;
		int index = 0;
		int radix = (bcdFlag ? 100 : 10);

		if (number > 0) {
			number = -number;
		}
		for (; charPos >= offset; charPos--) {
			if (0 != number) {
				index = (int) (-(number % radix));
				number = number / radix;
			} else {
				index = 0;
			}
			if (numberLengthFlag && 0 == number) {
				numberLengthFlag = !numberLengthFlag;
				numberLength = length - charPos;
			}
			if (bcdFlag) {
				buf[charPos] = BCD_Digit[index];
			} else {
				buf[charPos] = (byte) digits[index];
			}
		}
		return numberLength;
	}

	public static byte[] longToString(long number, boolean bcdFlag, int bufferSize) {
		int size = (bufferSize > 0 ? bufferSize : 20);
		byte[] buffer = new byte[size];
		int length = longToString(number, buffer, 0, size, bcdFlag);
		if (bufferSize > 0) {
			return buffer;
		}
		byte[] buf = new byte[length];
		System.arraycopy(buffer, size - length, buf, 0, length);
		buffer = null;
		return buf;
	}

	final static char[] digits = {
			'0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b',
			'c', 'd', 'e', 'f', 'g', 'h',
			'i', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', 's', 't',
			'u', 'v', 'w', 'x', 'y', 'z'
	};
	final static char[] DigitTens = {
			'0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
			'1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
			'2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
			'3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
			'4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
			'5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
			'6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
			'7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
			'8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
			'9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
	};
	final static char[] DigitOnes = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	};
	public final static byte[] BCD_Digit = {
			(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09,
			(byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19,
			(byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29,
			(byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
			(byte) 0x40, (byte) 0x41, (byte) 0x42, (byte) 0x43, (byte) 0x44, (byte) 0x45, (byte) 0x46, (byte) 0x47, (byte) 0x48, (byte) 0x49,
			(byte) 0x50, (byte) 0x51, (byte) 0x52, (byte) 0x53, (byte) 0x54, (byte) 0x55, (byte) 0x56, (byte) 0x57, (byte) 0x58, (byte) 0x59,
			(byte) 0x60, (byte) 0x61, (byte) 0x62, (byte) 0x63, (byte) 0x64, (byte) 0x65, (byte) 0x66, (byte) 0x67, (byte) 0x68, (byte) 0x69,
			(byte) 0x70, (byte) 0x71, (byte) 0x72, (byte) 0x73, (byte) 0x74, (byte) 0x75, (byte) 0x76, (byte) 0x77, (byte) 0x78, (byte) 0x79,
			(byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x88, (byte) 0x89,
			(byte) 0x90, (byte) 0x91, (byte) 0x92, (byte) 0x93, (byte) 0x94, (byte) 0x95, (byte) 0x96, (byte) 0x97, (byte) 0x98, (byte) 0x99,
	};


	public static byte[] bcdToAscii(byte[] bcdByte) {
		byte[] returnByte = new byte[bcdByte.length * 2];
		byte value;
		for (int i = 0; i < bcdByte.length; i++) {
			value = (byte) (bcdByte[i] >> 4 & 0xF);
			if (value > 9) {
				returnByte[i * 2] = (byte) (value + (byte) 0x37);
			} else {
				returnByte[i * 2] = (byte) (value + (byte) 0x30);
			}
			value = (byte) (bcdByte[i] & 0xF);
			if (value > 9) {
				returnByte[i * 2 + 1] = (byte) (value + (byte) 0x37);
			} else {
				returnByte[i * 2 + 1] = (byte) (value + (byte) 0x30);
			}
		}
		return returnByte;
	}

	public static byte[] contactBuff(byte[]... arrs) {
		if (arrs == null || arrs.length < 1) {
			return new byte[0];
		}
		int tlength = 0;
		for (byte[] arr : arrs) {
			tlength += arr.length;
		}
		byte[] result = new byte[tlength];
		int startPos = 0;
		for (byte[] arr : arrs) {
			System.arraycopy(arr, 0, result, startPos, arr.length);
			startPos += arr.length;
		}
		return result;
	}

	public static String int2BitString(int value, int length) {

		byte b = (byte) (value & 0xff);
		StringBuilder sb = new StringBuilder();
		for (int i = length - 1; i >= 0; i--) {
			sb.append("" + (byte) ((b >> i) & 0x1));
		}
		return sb.toString();
	}

}
