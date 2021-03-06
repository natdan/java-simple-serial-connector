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
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 *
 * @author scream3r
 */
public class SerialPort {

    private Logger logger = Logger.getLogger("SerialPort");

    private String portName;

    //since 2.2.0 ->
    private Method methodErrorOccurred = null;
    //<- since 2.2.0

    public static final int BAUDRATE_110 = 110;
    public static final int BAUDRATE_300 = 300;
    public static final int BAUDRATE_600 = 600;
    public static final int BAUDRATE_1200 = 1200;
    public static final int BAUDRATE_4800 = 4800;
    public static final int BAUDRATE_9600 = 9600;
    public static final int BAUDRATE_14400 = 14400;
    public static final int BAUDRATE_19200 = 19200;
    public static final int BAUDRATE_38400 = 38400;
    public static final int BAUDRATE_57600 = 57600;
    public static final int BAUDRATE_115200 = 115200;
    public static final int BAUDRATE_128000 = 128000;
    public static final int BAUDRATE_256000 = 256000;


    public static final int DATABITS_5 = 5;
    public static final int DATABITS_6 = 6;
    public static final int DATABITS_7 = 7;
    public static final int DATABITS_8 = 8;


    public static final int STOPBITS_1 = 1;
    public static final int STOPBITS_2 = 2;
    public static final int STOPBITS_1_5 = 3;


    public static final int PARITY_NONE = 0;
    public static final int PARITY_ODD = 1;
    public static final int PARITY_EVEN = 2;
    public static final int PARITY_MARK = 3;
    public static final int PARITY_SPACE = 4;


    public static final int PURGE_RXABORT = 0x0002;
    public static final int PURGE_RXCLEAR = 0x0008;
    public static final int PURGE_TXABORT = 0x0001;
    public static final int PURGE_TXCLEAR = 0x0004;


    public static final int MASK_RXCHAR = 1;
    public static final int MASK_RXFLAG = 2;
    public static final int MASK_TXEMPTY = 4;
    public static final int MASK_CTS = 8;
    public static final int MASK_DSR = 16;
    public static final int MASK_RLSD = 32;
    public static final int MASK_BREAK = 64;
    public static final int MASK_ERR = 128;
    public static final int MASK_RING = 256;


    //since 0.8 ->
    public static final int FLOWCONTROL_NONE = 0;
    public static final int FLOWCONTROL_RTSCTS_IN = 1;
    public static final int FLOWCONTROL_RTSCTS_OUT = 2;
    public static final int FLOWCONTROL_XONXOFF_IN = 4;
    public static final int FLOWCONTROL_XONXOFF_OUT = 8;
    //<- since 0.8

    //since 0.8 ->
    public static final int ERROR_FRAME = 0x0008;
    public static final int ERROR_OVERRUN = 0x0002;
    public static final int ERROR_PARITY = 0x0004;
    //<- since 0.8

    //since 2.6.0 ->
    private static final int PARAMS_FLAG_IGNPAR = 1;
    private static final int PARAMS_FLAG_PARMRK = 2;
    //<- since 2.6.0

    private ISerialPort port;

    public SerialPort(String portName) {
        if (portName.startsWith("serialproxy://")) {
            logger.fine("Setting up network port " + portName);
            port = new NetworkSerialPort(portName);
            logger.fine("Set up network port " + portName);
        } else {
            logger.fine("Setting up local port " + portName);
            port = new LocalSerialPort(portName);
            logger.fine("Set up local port " + portName);
        }
    }

    /**
     * Getting port name under operation
     *
     * @return Method returns port name under operation as a String
     */
    public String getPortName() {
        return port.getPortName();
    }

    /**
     * Getting port state
     *
     * @return Method returns true if port is open, otherwise false
     */
    public boolean isOpened() {
        return port.isOpened();
    }

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
    public boolean openPort() throws SerialPortException {
        return port.openPort();
    }

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
    public boolean setParams(int baudRate, int dataBits, int stopBits, int parity) throws SerialPortException {
        return port.setParams(baudRate, dataBits, stopBits, parity);
    }

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
    public boolean setParams(int baudRate, int dataBits, int stopBits, int parity, boolean setRTS, boolean setDTR) throws SerialPortException {
        return port.setParams(baudRate, dataBits, stopBits, parity, setRTS, setDTR);
    }

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
    public boolean purgePort(int flags) throws SerialPortException {
        return port.purgePort(flags);
    }

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
    public boolean setEventsMask(int mask) throws SerialPortException {
        return port.setEventsMask(mask);
    }

    /**
     * Getting events mask for the port
     *
     * @return Method returns events mask as int type variable. This variable is an additive value
     *
     * @throws SerialPortException
     */
    public int getEventsMask() throws SerialPortException {
        return port.getEventsMask();
    }

    /**
     * Change RTS line state. Set "true" for switching ON and "false" for switching OFF RTS line
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean setRTS(boolean enabled) throws SerialPortException {
        return port.setRTS(enabled);
    }

    /**
     * Change DTR line state. Set "true" for switching ON and "false" for switching OFF DTR line
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean setDTR(boolean enabled) throws SerialPortException {
        return port.setDTR(enabled);
    }

    /**
     * Write byte array to port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean writeBytes(byte[] buffer) throws SerialPortException {
        return port.writeBytes(buffer);
    }

    /**
     * Write single byte to port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    public boolean writeByte(byte singleByte) throws SerialPortException {
        return port.writeByte(singleByte);
    }

    /**
     * Write String to port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    public boolean writeString(String string) throws SerialPortException {
        return port.writeString(string);
    }

    /**
     * Write String to port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     *
     * @since 2.8.0
     */
    public boolean writeString(String string, String charsetName) throws SerialPortException, UnsupportedEncodingException {
        return port.writeString(string, charsetName);
    }

    /**
     * Write int value (in range from 0 to 255 (0x00 - 0xFF)) to port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    public boolean writeInt(int singleInt) throws SerialPortException {
        return port.writeInt(singleInt);
    }

    /**
     * Write int array (in range from 0 to 255 (0x00 - 0xFF)) to port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    public boolean writeIntArray(int[] buffer) throws SerialPortException {
        return port.writeIntArray(buffer);
    }

    /**
     * Read byte array from port
     *
     * @param byteCount count of bytes for reading
     *
     * @return byte array with "byteCount" length
     *
     * @throws SerialPortException
     */
    public byte[] readBytes(int byteCount) throws SerialPortException {
        return port.readBytes(byteCount);
    }

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
    public String readString(int byteCount) throws SerialPortException {
        return port.readString(byteCount);
    }

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
    public String readHexString(int byteCount) throws SerialPortException {
        return port.readHexString(byteCount);
    }

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
    public String readHexString(int byteCount, String separator) throws SerialPortException {
        return port.readHexString(byteCount, separator);
    }

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
    public String[] readHexStringArray(int byteCount) throws SerialPortException {
        return port.readHexStringArray(byteCount);
    }

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
    public int[] readIntArray(int byteCount) throws SerialPortException {
        return port.readIntArray(byteCount);
    }

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
    public byte[] readBytes(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException {
        return port.readBytes(byteCount, timeout);
    }

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
    public String readString(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException {
        return port.readString(byteCount, timeout);
    }

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
    public String readHexString(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException {
        return port.readHexString(byteCount, timeout);
    }

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
    public String readHexString(int byteCount, String separator, int timeout) throws SerialPortException, SerialPortTimeoutException {
        return port.readHexString(byteCount, separator, timeout);
    }

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
    public String[] readHexStringArray(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException {
        return port.readHexStringArray(byteCount, timeout);
    }

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
    public int[] readIntArray(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException {
        return port.readIntArray(byteCount, timeout);
    }

    /**
     * Read all available bytes from port like a byte array
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    public byte[] readBytes() throws SerialPortException {
        return port.readBytes();
    }

    /**
     * Read all available bytes from port like a String
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to String
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    public String readString() throws SerialPortException {
        return port.readString();
    }

    /**
     * Read all available bytes from port like a Hex String
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to Hex String
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    public String readHexString() throws SerialPortException {
        return port.readHexString();
    }

    /**
     * Read all available bytes from port like a Hex String with setted separator
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to Hex String
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    public String readHexString(String separator) throws SerialPortException {
        return port.readHexString(separator);
    }

    /**
     * Read all available bytes from port like a Hex String array
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to Hex String array
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    public String[] readHexStringArray() throws SerialPortException {
        return port.readHexStringArray();
    }

    /**
     * Read all available bytes from port like a int array (values in range from 0 to 255)
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to int array
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    public int[] readIntArray() throws SerialPortException {
        return port.readIntArray();
    }

    /**
     * Get count of bytes in input buffer
     *
     * @return Count of bytes in input buffer or -1 if error occured
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    public int getInputBufferBytesCount() throws SerialPortException {
        return port.getInputBufferBytesCount();
    }

    /**
     * Get count of bytes in output buffer
     *
     * @return Count of bytes in output buffer or -1 if error occured
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    public int getOutputBufferBytesCount() throws SerialPortException {
        return port.getOutputBufferBytesCount();
    }

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
    public boolean setFlowControlMode(int mask) throws SerialPortException {
        return port.setFlowControlMode(mask);
    }

    /**
     * Get flow control mode
     *
     * @return Mask of setted flow control mode
     *
     * @throws SerialPortException
     *
     * @since 0.8
     */
    public int getFlowControlMode() throws SerialPortException {
        return port.getFlowControlMode();
    }

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
    public boolean sendBreak(int duration) throws SerialPortException {
        return port.sendBreak(duration);
    }

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
    public int[] getLinesStatus() throws SerialPortException {
        return port.getLinesStatus();
    }

    /**
     * Get state of CTS line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean isCTS() throws SerialPortException {
        return port.isCTS();
    }

    /**
     * Get state of DSR line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean isDSR() throws SerialPortException {
        return port.isDSR();
    }

    /**
     * Get state of RING line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean isRING() throws SerialPortException {
        return port.isRING();
    }

    /**
     * Get state of RLSD line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean isRLSD() throws SerialPortException {
        return port.isRLSD();
    }

    /**
     * Add event listener. Object of <b>"SerialPortEventListener"</b> type shall
     * be sent to the method. This object shall be properly described, as it will
     * be in charge for handling of occurred events. This method will independently
     * set the mask in <b>"MASK_RXCHAR"</b> state if it was not set beforehand
     *
     * @throws SerialPortException
     */
    public void addEventListener(SerialPortEventListener listener) throws SerialPortException {
        port.addEventListener(listener);
    }

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
    public void addEventListener(SerialPortEventListener listener, int mask) throws SerialPortException {
        port.addEventListener(listener, mask);
    }

    /**
     * Delete event listener. Mask is set to 0. So at the next addition of event
     * handler you shall set required event mask again
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean removeEventListener() throws SerialPortException {
        return port.removeEventListener();
    }

    /**
     * Close port. This method deletes event listener first, then closes the port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean closePort() throws SerialPortException {
        return port.closePort();
    }
}
