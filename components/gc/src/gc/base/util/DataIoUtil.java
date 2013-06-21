/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */package gc.base.util;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.Timestamp;

/**
 * DataInput/DataOutput utilities.
 * <p/>
 * Intended for reading/writing of objects as binary data.
 * 
 * TODO: Timestamp vs Date
 */
public class DataIoUtil {
		
	/**
	 * Reads a Boolean value.
	 * @param in the source
	 * @return the value
	 * @throws IOException if an i/o exception occurs
	 */
	public static Boolean readBoolean(DataInput in) throws IOException {
  	if (readNullFlag(in)) return null;
  	return new Boolean(in.readBoolean());
  }
	/**
	 * Writes a Boolean value.
	 * @param out the destination
	 * @param v the value
	 * @throws IOException if an i/o exception occurs
	 */
	public static void writeBoolean(DataOutput out, Boolean v) throws IOException {
  	boolean isNull = (v == null);
  	writeNullFlag(out,isNull);
  	if (!isNull) out.writeBoolean(v);
  }
  
	/**
	 * Reads a Byte value.
	 * @param in the source
	 * @return the value 
	 * @throws IOException if an i/o exception occurs
	 */
  public static Byte readByte(DataInput in) throws IOException {
    if (readNullFlag(in)) return null;
    return new Byte(in.readByte());
  }
	/**
	 * Writes a Byte value.
	 * @param out the destination
	 * @param v the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static void writeByte(DataOutput out, Byte v) throws IOException {
  	boolean isNull = (v == null);
  	writeNullFlag(out,isNull);
  	if (!isNull) out.writeByte(v);
  }

	/**
	 * Reads a byte array.
	 * @param in the source
	 * @return the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static byte[] readBytes(DataInput in) throws IOException {
  	int l = in.readInt();
    if (l == -1) return null;
  	byte[] ba = new byte[l];
  	if (l > 0) in.readFully(ba,0,l);
  	return ba;
  }  
	/**
	 * Writes a byte array.
	 * @param out the destination
	 * @param v the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static void writeBytes(DataOutput out, byte[] v) throws IOException {
  	if (v == null) {
  		out.writeInt(-1);
		} else {
      int l = v.length;
			out.writeInt(l);
			if (l > 0) out.write(v,0,l);
		}
  }
  
	/**
	 * Reads a Double value.
	 * @param in the source
	 * @return the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static Double readDouble(DataInput in) throws IOException {
    if (readNullFlag(in)) return null;
    return new Double(in.readDouble());
  }
	/**
	 * Writes a Double value.
	 * @param out the destination
	 * @param v the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static void writeDouble(DataOutput out, Double v) throws IOException {
  	boolean isNull = (v == null);
  	writeNullFlag(out,isNull);
  	if (!isNull) out.writeDouble(v);
  }
  
	/**
	 * Reads a Float value.
	 * @param in the source
	 * @return the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static Float readFloat(DataInput in) throws IOException {
    if (readNullFlag(in)) return null;
    return new Float(in.readFloat());
  }
	/**
	 * Writes a Float value.
	 * @param out the destination
	 * @param v the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static void writeFloat(DataOutput out, Float v) throws IOException {
  	boolean isNull = (v == null);
  	writeNullFlag(out,isNull);
  	if (!isNull) out.writeFloat(v);
  }
  
	/**
	 * Reads an Integer value.
	 * @param in the source
	 * @return the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static Integer readInteger(DataInput in) throws IOException {
    if (readNullFlag(in)) return null;
    return new Integer(in.readInt());
  }
	/**
	 * Writes a Integer value.
	 * @param out the destination
	 * @param v the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static void writeInteger(DataOutput out, Integer v) throws IOException {
  	boolean isNull = (v == null);
  	writeNullFlag(out,isNull);
  	if (!isNull) out.writeInt(v);
  }
  
	/**
	 * Reads a Long value.
	 * @param in the source
	 * @return the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static Long readLong(DataInput in) throws IOException {
    if (readNullFlag(in)) return null;
    return new Long(in.readLong());
  }
	/**
	 * Writes a Long value.
	 * @param out the destination
	 * @param v the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static void writeLong(DataOutput out, Long v) throws IOException {
  	boolean isNull = (v == null);
  	writeNullFlag(out,isNull);
  	if (!isNull) out.writeLong(v);
  }
  
	/**
	 * Reads a null flag.
	 * @param in the source
	 * @return true if null
	 * @throws IOException if an i/o exception occurs
	 */
  private static boolean readNullFlag(DataInput in) throws IOException {
  	return in.readBoolean();
  }
	/**
	 * Writes a null flag.
	 * @param out the destination
	 * @param isNull the flag
	 * @throws IOException if an i/o exception occurs
	 */
  private static void writeNullFlag(DataOutput out, boolean isNull) throws IOException {
  	out.writeBoolean(isNull);
  }
  
	/**
	 * Reads a String value.
	 * @param in the source
	 * @return the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static String readString(DataInput in) throws IOException {
  	int l = in.readInt();
    if (l == -1) return null;
  	byte[] ba = new byte[l];
  	if (l > 0) in.readFully(ba,0,l);
  	return new String(ba,"UTF-8"); 
  }
	/**
	 * Writes a String value.
	 * @param out the destination
	 * @param v the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static void writeString(DataOutput out, String v) throws IOException {
  	if (v == null) {
  		out.writeInt(-1);
		} else {
      byte[] ba = v.getBytes("UTF-8");
      int l = ba.length;
			out.writeInt(l);
			if (l > 0)  out.write(ba,0,l);
		}
  }
  
	/**
	 * Reads a Timestamp value.
	 * @param in the source
	 * @return the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static Timestamp readTimestamp(DataInput in) throws IOException {
    if (readNullFlag(in)) return null;
    return new Timestamp(in.readLong());
  }
	/**
	 * Writes a Timestamp value.
	 * @param out the destination
	 * @param v the value
	 * @throws IOException if an i/o exception occurs
	 */
  public static void writeTimestamp(DataOutput out, Timestamp v) throws IOException {
  	boolean isNull = (v == null);
  	writeNullFlag(out,isNull);
  	if (!isNull) out.writeLong(v.getTime());
  }

}
