/* jSSC (Java Simple Serial Connector) - serial port communication library.
 * © Alexey Sokolov (scream3r), 2010-2014.
 *
 * This file is part of jSSC.
 *
 * jSSC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jSSC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jSSC.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you use jSSC in public project you can inform me about this by e-mail,
 * of course if you want it.
 *
 * e-mail: scream3r.org@gmail.com
 * web-site: http://scream3r.org | http://code.google.com/p/java-simple-serial-connector/
 */
package jssc;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author scream3r
 */
public interface ISerialPort {

    /**
     * Getting port name under operation
     *
     * @return Method returns port name under operation as a String
     */
    String getPortName();

    /**
     * Getting port state
     *
     * @return Method returns true if port is open, otherwise false
     */
    boolean isOpened();

    /**
     * Port opening
     * <br><br>
     * <b>Note: </b>If port busy <b>TYPE_PORT_BUSY</b> exception will be thrown.
     * If port not found <b>TYPE_PORT_NOT_FOUND</b> exception will be thrown.
     *
     * @return If the operation is successfully completed, the method returns true
     *
     * @throws SerialPortException
     */
    boolean openPort() throws SerialPortException;

    /**
     * Setting the parameters of port. RTS and DTR lines are enabled by default
     *
     * @param baudRate data transfer rate
     * @param dataBits number of data bits
     * @param stopBits number of stop bits
     * @param parity parity
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    boolean setParams(int baudRate, int dataBits, int stopBits, int parity) throws SerialPortException;
    /**
     * Setting the parameters of port
     *
     * @param baudRate data transfer rate
     * @param dataBits number of data bits
     * @param stopBits number of stop bits
     * @param parity parity
     * @param setRTS initial state of RTS line(ON/OFF)
     * @param setDTR initial state of DTR line(ON/OFF)
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    boolean setParams(int baudRate, int dataBits, int stopBits, int parity, boolean setRTS, boolean setDTR) throws SerialPortException;

    /**
     * Purge of input and output buffer. Required flags shall be sent to the input. Variables with prefix
     * <b>"PURGE_"</b>, for example <b>"PURGE_RXCLEAR"</b>. Sent parameter "flags" is additive value,
     * so addition of flags is allowed. For example, if input or output buffer shall be purged,
     * parameter <b>"PURGE_RXCLEAR | PURGE_TXCLEAR"</b>.
     * <br><b>Note: </b>some devices or drivers may not support this function
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false.
     *
     * @throws SerialPortException
     */
    boolean purgePort(int flags) throws SerialPortException;

    /**
     * Set events mask. Required flags shall be sent to the input. Variables with prefix
     * <b>"MASK_"</b>, shall be used as flags, for example <b>"MASK_RXCHAR"</b>.
     * Sent parameter "mask" is additive value, so addition of flags is allowed.
     * For example if messages about data receipt and CTS and DSR status changing
     * shall be received, it is required to set the mask - <b>"MASK_RXCHAR | MASK_CTS | MASK_DSR"</b>
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    boolean setEventsMask(int mask) throws SerialPortException;

    /**
     * Getting events mask for the port
     *
     * @return Method returns events mask as int type variable. This variable is an additive value
     *
     * @throws SerialPortException
     */
    int getEventsMask() throws SerialPortException;

    /**
     * Change RTS line state. Set "true" for switching ON and "false" for switching OFF RTS line
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    boolean setRTS(boolean enabled) throws SerialPortException;

    /**
     * Change DTR line state. Set "true" for switching ON and "false" for switching OFF DTR line
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    boolean setDTR(boolean enabled) throws SerialPortException;

    /**
     * Write byte array to port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    boolean writeBytes(byte[] buffer) throws SerialPortException;

    /**
     * Write single byte to port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    boolean writeByte(byte singleByte) throws SerialPortException;

    /**
     * Write String to port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    boolean writeString(String string) throws SerialPortException;

    /**
     * Write String to port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     *
     * @since 2.8.0
     */
    boolean writeString(String string, String charsetName) throws SerialPortException, UnsupportedEncodingException;

    /**
     * Write int value (in range from 0 to 255 (0x00 - 0xFF)) to port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    boolean writeInt(int singleInt) throws SerialPortException;

    /**
     * Write int array (in range from 0 to 255 (0x00 - 0xFF)) to port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    boolean writeIntArray(int[] buffer) throws SerialPortException;

    /**
     * Read byte array from port
     *
     * @param byteCount count of bytes for reading
     *
     * @return byte array with "byteCount" length
     *
     * @throws SerialPortException
     */
    byte[] readBytes(int byteCount) throws SerialPortException;

    /**
     * Read string from port
     *
     * @param byteCount count of bytes for reading
     *
     * @return byte array with "byteCount" length converted to String
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    String readString(int byteCount) throws SerialPortException;

    /**
     * Read Hex string from port (example: FF 0A FF). Separator by default is a space
     *
     * @param byteCount count of bytes for reading
     *
     * @return byte array with "byteCount" length converted to Hexadecimal String
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    String readHexString(int byteCount) throws SerialPortException;

    /**
     * Read Hex string from port with setted separator (example if separator is "::": FF::0A::FF)
     *
     * @param byteCount count of bytes for reading
     *
     * @return byte array with "byteCount" length converted to Hexadecimal String
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    String readHexString(int byteCount, String separator) throws SerialPortException;

    /**
     * Read Hex String array from port
     *
     * @param byteCount count of bytes for reading
     *
     * @return String array with "byteCount" length and Hexadecimal String values
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    String[] readHexStringArray(int byteCount) throws SerialPortException;

    /**
     * Read int array from port
     *
     * @param byteCount count of bytes for reading
     *
     * @return int array with values in range from 0 to 255
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    int[] readIntArray(int byteCount) throws SerialPortException;

    /**
     * Read byte array from port
     *
     * @param byteCount count of bytes for reading
     * @param timeout timeout in milliseconds
     *
     * @return byte array with "byteCount" length
     *
     * @throws SerialPortException
     * @throws SerialPortTimeoutException
     *
     * @since 2.0
     */
    byte[] readBytes(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException;

    /**
     * Read string from port
     *
     * @param byteCount count of bytes for reading
     * @param timeout timeout in milliseconds
     *
     * @return byte array with "byteCount" length converted to String
     *
     * @throws SerialPortException
     * @throws SerialPortTimeoutException
     *
     * @since 2.0
     */
    String readString(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException;

    /**
     * Read Hex string from port (example: FF 0A FF). Separator by default is a space
     *
     * @param byteCount count of bytes for reading
     * @param timeout timeout in milliseconds
     *
     * @return byte array with "byteCount" length converted to Hexadecimal String
     *
     * @throws SerialPortException
     * @throws SerialPortTimeoutException
     *
     * @since 2.0
     */
    String readHexString(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException;

    /**
     * Read Hex string from port with setted separator (example if separator is "::": FF::0A::FF)
     *
     * @param byteCount count of bytes for reading
     * @param timeout timeout in milliseconds
     *
     * @return byte array with "byteCount" length converted to Hexadecimal String
     *
     * @throws SerialPortException
     * @throws SerialPortTimeoutException
     *
     * @since 2.0
     */
    String readHexString(int byteCount, String separator, int timeout) throws SerialPortException, SerialPortTimeoutException;

    /**
     * Read Hex String array from port
     *
     * @param byteCount count of bytes for reading
     * @param timeout timeout in milliseconds
     *
     * @return String array with "byteCount" length and Hexadecimal String values
     *
     * @throws SerialPortException
     * @throws SerialPortTimeoutException
     *
     * @since 2.0
     */
    String[] readHexStringArray(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException;

    /**
     * Read int array from port
     *
     * @param byteCount count of bytes for reading
     * @param timeout timeout in milliseconds
     *
     * @return int array with values in range from 0 to 255
     *
     * @throws SerialPortException
     * @throws SerialPortTimeoutException
     *
     * @since 2.0
     */
    int[] readIntArray(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException;

    /**
     * Read all available bytes from port like a byte array
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    byte[] readBytes() throws SerialPortException;

    /**
     * Read all available bytes from port like a String
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to String
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    String readString() throws SerialPortException;

    /**
     * Read all available bytes from port like a Hex String
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to Hex String
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    String readHexString() throws SerialPortException;

    /**
     * Read all available bytes from port like a Hex String with setted separator
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to Hex String
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    String readHexString(String separator) throws SerialPortException;

    /**
     * Read all available bytes from port like a Hex String array
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to Hex String array
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    String[] readHexStringArray() throws SerialPortException;

    /**
     * Read all available bytes from port like a int array (values in range from 0 to 255)
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to int array
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
     int[] readIntArray() throws SerialPortException;

    /**
     * Get count of bytes in input buffer
     *
     * @return Count of bytes in input buffer or -1 if error occured
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    int getInputBufferBytesCount() throws SerialPortException;

    /**
     * Get count of bytes in output buffer
     *
     * @return Count of bytes in output buffer or -1 if error occured
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    int getOutputBufferBytesCount() throws SerialPortException;

    /**
     * Set flow control mode. For required mode use variables with prefix <b>"FLOWCONTROL_"</b>.
     * Example of hardware flow control mode(RTS/CTS): setFlowControlMode(FLOWCONTROL_RTSCTS_IN | FLOWCONTROL_RTSCTS_OUT);
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    boolean setFlowControlMode(int mask) throws SerialPortException;

    /**
     * Get flow control mode
     *
     * @return Mask of setted flow control mode
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    int getFlowControlMode() throws SerialPortException;

    /**
     * Send Break singnal for setted duration
     *
     * @param duration duration of Break signal
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    boolean sendBreak(int duration)throws SerialPortException;

    /**
     * Getting lines status. Lines status is sent as 0 – OFF and 1 - ON
     *
     * @return Method returns the array containing information about lines in following order:
     * <br><b>element 0</b> - <b>CTS</b> line state</br>
     * <br><b>element 1</b> - <b>DSR</b> line state</br>
     * <br><b>element 2</b> - <b>RING</b> line state</br>
     * <br><b>element 3</b> - <b>RLSD</b> line state</br>
     *
     * @throws SerialPortException
     */
    int[] getLinesStatus() throws SerialPortException;

    /**
     * Get state of CTS line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    boolean isCTS() throws SerialPortException;

    /**
     * Get state of DSR line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    boolean isDSR() throws SerialPortException;

    /**
     * Get state of RING line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    boolean isRING() throws SerialPortException;

    /**
     * Get state of RLSD line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    boolean isRLSD() throws SerialPortException;

    /**
     * Add event listener. Object of <b>"SerialPortEventListener"</b> type shall
     * be sent to the method. This object shall be properly described, as it will
     * be in charge for handling of occurred events. This method will independently
     * set the mask in <b>"MASK_RXCHAR"</b> state if it was not set beforehand
     *
     * @throws SerialPortException
     */
    void addEventListener(SerialPortEventListener listener) throws SerialPortException;

    /**
     * Add event listener. Object of <b>"SerialPortEventListener"</b> type shall be sent
     * to the method. This object shall be properly described, as it will be in
     * charge for handling of occurred events. Also events mask shall be sent to
     * this method, to do it use variables with prefix <b>"MASK_"</b> for example <b>"MASK_RXCHAR"</b>
     *
     * @see #setEventsMask(int) setEventsMask(int mask)
     *
     * @throws SerialPortException
     */
    void addEventListener(SerialPortEventListener listener, int mask) throws SerialPortException;


    /**
     * Delete event listener. Mask is set to 0. So at the next addition of event
     * handler you shall set required event mask again
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    boolean removeEventListener() throws SerialPortException;

    /**
     * Close port. This method deletes event listener first, then closes the port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    boolean closePort() throws SerialPortException;
}
