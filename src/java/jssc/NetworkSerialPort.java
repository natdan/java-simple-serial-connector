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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scream3r
 */
public class NetworkSerialPort implements ISerialPort {

    private Logger logger = Logger.getLogger("NetworkSerialPort");

    private SerialPortEventListener eventListener;
    private String portName;
    private boolean portOpened = false;
    private boolean maskAssigned = false;
    private boolean eventListenerAdded = false;

    public static final int MASK_RXCHAR = 1;
    public static final int MASK_RXFLAG = 2;
    public static final int MASK_TXEMPTY = 4;
    public static final int MASK_CTS = 8;
    public static final int MASK_DSR = 16;
    public static final int MASK_RLSD = 32;
    public static final int MASK_BREAK = 64;
    public static final int MASK_ERR = 128;
    public static final int MASK_RING = 256;

    private String address;
    private int port;
    private Socket socket;
    private InputStream socketIn;
    private OutputStream socketOut;

    public NetworkSerialPort(String portName) {
        this.portName = portName;
        if (portName.startsWith("serialproxy://")) {
            String[] sp = portName.substring(14).split(":");
            address = sp[0];
            port = Integer.parseInt(sp[1]);
            logger.fine("NetworkSerialPort " + address + ":" + port);
        } else {
            logger.fine("NetworkSerialPort port name doesn't start with 'serialproxy:' but is " + portName);
        }
    }

    /**
     * Getting port name under operation
     *
     * @return Method returns port name under operation as a String
     */
    public String getPortName() {
        return portName;
    }

    /**
     * Getting port state
     *
     * @return Method returns true if port is open, otherwise false
     */
    public boolean isOpened() {
        return portOpened;
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
        if (portOpened) {
            throw new SerialPortException(portName, "openPort()", SerialPortException.TYPE_PORT_ALREADY_OPENED);
        }

        try {
            logger.fine("Opening socket to " + address + ":" + port);
            socket = new Socket(address, port);
            socketIn = socket.getInputStream();
            socketOut = socket.getOutputStream();
            logger.fine("Opened socket to " + address + ":" + port);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error: " + e.getMessage(), e);
            throw new SerialPortException(portName, "openPort()", SerialPortException.TYPE_PORT_NOT_FOUND);
        }
        portOpened = true;
        return true;
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
        return setParams(baudRate, dataBits, stopBits, parity, true, true);
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
        checkPortOpened("setParams()");
        if(stopBits == 1){
            stopBits = 0;
        }
        else if(stopBits == 3){
            stopBits = 1;
        }
        int flags = 0;
        // TODO
        return true;
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
        checkPortOpened("purgePort()");
        try {
            socketOut.flush();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error purgin port", e);
        }
        return true;
    }

    /**
     * Events mask for Linux OS
     *
     * @since 0.8
     */
    private int linuxMask;

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
        checkPortOpened("setEventsMask()");
        if(SerialNativeInterface.getOsType() == SerialNativeInterface.OS_LINUX ||
           SerialNativeInterface.getOsType() == SerialNativeInterface.OS_SOLARIS ||
           SerialNativeInterface.getOsType() == SerialNativeInterface.OS_MAC_OS_X){//since 0.9.0
            linuxMask = mask;
            if(mask > 0){
                maskAssigned = true;
            }
            else {
                maskAssigned = false;
            }
            return true;
        }
        boolean returnValue = true;
        // TODO
        return returnValue;
    }

    /**
     * Getting events mask for the port
     *
     * @return Method returns events mask as int type variable. This variable is an additive value
     *
     * @throws SerialPortException
     */
    public int getEventsMask() throws SerialPortException {
        checkPortOpened("getEventsMask()");
        if (SerialNativeInterface.getOsType() == SerialNativeInterface.OS_LINUX ||
           SerialNativeInterface.getOsType() == SerialNativeInterface.OS_SOLARIS ||
           SerialNativeInterface.getOsType() == SerialNativeInterface.OS_MAC_OS_X) {//since 0.9.0
            return linuxMask;
        }
        return linuxMask;
    }

    /**
     * Getting events mask for the port is Linux OS (for internal use)
     *
     * @since 0.8
     */
    private int getLinuxMask() {
        return linuxMask;
    }

    /**
     * Change RTS line state. Set "true" for switching ON and "false" for switching OFF RTS line
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean setRTS(boolean enabled) throws SerialPortException {
        checkPortOpened("setRTS()");
        // TODO
        return true;
    }

    /**
     * Change DTR line state. Set "true" for switching ON and "false" for switching OFF DTR line
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean setDTR(boolean enabled) throws SerialPortException {
        checkPortOpened("setDTR()");
        // TODO
        return true;
    }

    /**
     * Write byte array to port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean writeBytes(byte[] buffer) throws SerialPortException {
        checkPortOpened("writeBytes()");
        try {
            socketOut.write(buffer);
            if (logger.isLoggable(Level.FINEST)) { logger.finest(">(" + buffer.length + ")"); }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error writing bytes", e);
            return false;
        }
        return true;
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
        checkPortOpened("writeByte()");
        return writeBytes(new byte[]{singleByte});
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
        checkPortOpened("writeString()");
        return writeBytes(string.getBytes());
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
        checkPortOpened("writeString()");
        return writeBytes(string.getBytes(charsetName));
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
        checkPortOpened("writeInt()");
        return writeBytes(new byte[]{(byte)singleInt});
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
        checkPortOpened("writeIntArray()");
        byte[] byteArray = new byte[buffer.length];
        for(int i = 0; i < buffer.length; i++){
            byteArray[i] = (byte)buffer[i];
        }
        return writeBytes(byteArray);
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
        checkPortOpened("readBytes()");

        try {
            byte[] buf = new byte[byteCount];

            int r = 0;
            while (r < byteCount) {
                int rr = socketIn.read(buf, r, byteCount - r);
                if (rr > 0) {
                    r = r + rr;
                } else {
                    logger.warning("Error readBytes(): read " + rr + " and now got " + r);
                    throw new SerialPortException(portName, "readBytes(count)", "Cannot read socket");
                }
            }

            if (logger.isLoggable(Level.FINEST)) { logger.finest("<(" + byteCount + ")"); }

            return buf;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error read bytes", e);
            throw new SerialPortException(portName, "readBytes(count)", e.getMessage());
        }
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
        checkPortOpened("readString()");
        return new String(readBytes(byteCount));
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
        checkPortOpened("readHexString()");
        return readHexString(byteCount, " ");
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
        checkPortOpened("readHexString()");
        String[] strBuffer = readHexStringArray(byteCount);
        String returnString = "";
        boolean insertSeparator = false;
        for(String value : strBuffer){
            if(insertSeparator){
                returnString += separator;
            }
            returnString += value;
            insertSeparator = true;
        }
        return returnString;
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
        checkPortOpened("readHexStringArray()");
        int[] intBuffer = readIntArray(byteCount);
        String[] strBuffer = new String[intBuffer.length];
        for(int i = 0; i < intBuffer.length; i++){
            String value = Integer.toHexString(intBuffer[i]).toUpperCase();
            if(value.length() == 1) {
                value = "0" + value;
            }
            strBuffer[i] = value;
        }
        return strBuffer;
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
        checkPortOpened("readIntArray()");
        byte[] buffer = readBytes(byteCount);
        int[] intBuffer = new int[buffer.length];
        for(int i = 0; i < buffer.length; i++){
            if(buffer[i] < 0){
                intBuffer[i] = 256 + buffer[i];
            }
            else {
                intBuffer[i] = buffer[i];
            }
        }
        return intBuffer;
    }

    private void waitBytesWithTimeout(String methodName, int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException {
        checkPortOpened("waitBytesWithTimeout()");
        boolean timeIsOut = true;
        long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime) < timeout){
            if(getInputBufferBytesCount() >= byteCount){
                timeIsOut = false;
                break;
            }
            try {
                Thread.sleep(0, 100);//Need to sleep some time to prevent high CPU loading
            }
            catch (InterruptedException ex) {
                //Do nothing
            }
        }
        if(timeIsOut){
            throw new SerialPortTimeoutException(portName, methodName, timeout);
        }
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
        checkPortOpened("readBytes()");
        waitBytesWithTimeout("readBytes()", byteCount, timeout);
        return readBytes(byteCount);
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
        checkPortOpened("readString()");
        waitBytesWithTimeout("readString()", byteCount, timeout);
        return readString(byteCount);
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
        checkPortOpened("readHexString()");
        waitBytesWithTimeout("readHexString()", byteCount, timeout);
        return readHexString(byteCount);
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
        checkPortOpened("readHexString()");
        waitBytesWithTimeout("readHexString()", byteCount, timeout);
        return readHexString(byteCount, separator);
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
        checkPortOpened("readHexStringArray()");
        waitBytesWithTimeout("readHexStringArray()", byteCount, timeout);
        return readHexStringArray(byteCount);
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
        checkPortOpened("readIntArray()");
        waitBytesWithTimeout("readIntArray()", byteCount, timeout);
        return readIntArray(byteCount);
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
        checkPortOpened("readBytes()");
        int byteCount = getInputBufferBytesCount();
        if(byteCount <= 0){
            return null;
        }
        return readBytes(byteCount);
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
        checkPortOpened("readString()");
        int byteCount = getInputBufferBytesCount();
        if(byteCount <= 0){
            return null;
        }
        return readString(byteCount);
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
        checkPortOpened("readHexString()");
        int byteCount = getInputBufferBytesCount();
        if(byteCount <= 0){
            return null;
        }
        return readHexString(byteCount);
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
        checkPortOpened("readHexString()");
        int byteCount = getInputBufferBytesCount();
        if(byteCount <= 0){
            return null;
        }
        return readHexString(byteCount, separator);
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
        checkPortOpened("readHexStringArray()");
        int byteCount = getInputBufferBytesCount();
        if(byteCount <= 0){
            return null;
        }
        return readHexStringArray(byteCount);
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
        checkPortOpened("readIntArray()");
        int byteCount = getInputBufferBytesCount();
        if(byteCount <= 0){
            return null;
        }
        return readIntArray(byteCount);
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
        checkPortOpened("getInputBufferBytesCount()");
        try {
            int available = socketIn.available();
            if (logger.isLoggable(Level.FINEST)) { logger.finest("?<(" + available + ")"); }
            return available;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error checking count", e);
            throw new SerialPortException(portName, "getInputBufferBytesCount()", e.getMessage());
        }
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
        checkPortOpened("getOutputBufferBytesCount()");
        return 0;
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
        checkPortOpened("setFlowControlMode()");
        // TODO
        return true;
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
        checkPortOpened("getFlowControlMode()");
        // TODO
        return 0;
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
    public boolean sendBreak(int duration)throws SerialPortException {
        checkPortOpened("sendBreak()");
        // TODO
        return true;
     }

    private int[][] waitEvents() {
        return new int[0][];
    }

    /**
     * Check port opened (since jSSC-0.8 String "EMPTY" was replaced with "portName" variable)
     *
     * @param methodName method name
     *
     * @throws SerialPortException
     */
    private void checkPortOpened(String methodName) throws SerialPortException {
        if (!portOpened) {
            throw new SerialPortException(portName, methodName, SerialPortException.TYPE_PORT_NOT_OPENED);
        }
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
        checkPortOpened("getLinesStatus()");
        return new int[4];
    }

    /**
     * Get state of CTS line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean isCTS() throws SerialPortException {
        checkPortOpened("isCTS()");
        // TODO
        return false;
    }

    /**
     * Get state of DSR line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean isDSR() throws SerialPortException {
        checkPortOpened("isDSR()");
        // TODO
        return false;
    }

    /**
     * Get state of RING line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean isRING() throws SerialPortException {
        checkPortOpened("isRING()");
        // TODO
        return false;
    }

    /**
     * Get state of RLSD line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean isRLSD() throws SerialPortException {
        checkPortOpened("isRLSD()");
        // TODO
        return false;
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
        addEventListener(listener, MASK_RXCHAR, false);
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
        addEventListener(listener, mask, true);
    }

    /**
     * Internal method. Add event listener. Object of <b>"SerialPortEventListener"</b> type shall be sent
     * to the method. This object shall be properly described, as it will be in
     * charge for handling of occurred events. Also events mask shall be sent to
     * this method, to do it use variables with prefix <b>"MASK_"</b> for example <b>"MASK_RXCHAR"</b>. If
     * <b>overwriteMask == true</b> and mask has been already assigned it value will be rewrited by <b>mask</b>
     * value, if <b>overwriteMask == false</b> and mask has been already assigned the new <b>mask</b> value will be ignored,
     * if there is no assigned mask to this serial port the <b>mask</b> value will be used for setting it up in spite of
     * <b>overwriteMask</b> value
     *
     * @see #setEventsMask(int) setEventsMask(int mask)
     *
     * @throws SerialPortException
     */
    private void addEventListener(SerialPortEventListener listener, int mask, boolean overwriteMask) throws SerialPortException {
        checkPortOpened("addEventListener()");
        if(!eventListenerAdded){
            if((maskAssigned && overwriteMask) || !maskAssigned) {
                setEventsMask(mask);
            }
            eventListener = listener;
            eventThread = getNewEventThread();
            eventThread.setName("EventThread " + portName);
            //since 2.2.0 ->
            try {
                Method method = eventListener.getClass().getMethod("errorOccurred", new Class[]{SerialPortException.class});
                method.setAccessible(true);
                // methodErrorOccurred = method;
            }
            catch (SecurityException ex) {
                //Do nothing
            }
            catch (NoSuchMethodException ex) {
                //Do nothing
            }
            //<- since 2.2.0
            eventThread.start();
            eventListenerAdded = true;
        }
        else {
            throw new SerialPortException(portName, "addEventListener()", SerialPortException.TYPE_LISTENER_ALREADY_ADDED);
        }
    }

    /**
     * Create new EventListener Thread depending on the type of operating system
     *
     * @since 0.8
     */
    private EventThread getNewEventThread() {
//        if(SerialNativeInterface.getOsType() == SerialNativeInterface.OS_LINUX ||
//           SerialNativeInterface.getOsType() == SerialNativeInterface.OS_SOLARIS ||
//           SerialNativeInterface.getOsType() == SerialNativeInterface.OS_MAC_OS_X){//since 0.9.0
//            return new LinuxEventThread();
//        }
        return new EventThread();
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
        checkPortOpened("removeEventListener()");
        if(!eventListenerAdded){
            throw new SerialPortException(portName, "removeEventListener()", SerialPortException.TYPE_CANT_REMOVE_LISTENER);
        }
        eventThread.terminateThread();
        setEventsMask(0);
        if(Thread.currentThread().getId() != eventThread.getId()){
            if(eventThread.isAlive()){
                try {
                    eventThread.join(5000);
                }
                catch (InterruptedException ex) {
                    throw new SerialPortException(portName, "removeEventListener()", SerialPortException.TYPE_LISTENER_THREAD_INTERRUPTED);
                }
            }
        }
        // methodErrorOccurred = null;
        eventListenerAdded = false;
        return true;
    }

    /**
     * Close port. This method deletes event listener first, then closes the port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException
     */
    public boolean closePort() throws SerialPortException {
        checkPortOpened("closePort()");
        if(eventListenerAdded){
            removeEventListener();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private EventThread eventThread;

    private class EventThread extends Thread {

        private boolean threadTerminated = false;

        @Override
        public void run() {
            while(!threadTerminated){
                int[][] eventArray = waitEvents();
                for(int i = 0; i < eventArray.length; i++){
                    if(eventArray[i][0] > 0 && !threadTerminated){
                        eventListener.serialEvent(new SerialPortEvent(portName, eventArray[i][0], eventArray[i][1]));
                        //FIXME
                        /*if(methodErrorOccurred != null){
                            try {
                                methodErrorOccurred.invoke(eventListener, new Object[]{new SerialPortException("port", "method", "exception")});
                            }
                            catch (Exception ex) {
                                System.out.println(ex);
                            }
                        }*/
                    }
                }
            }
        }

        private void terminateThread(){
            threadTerminated = true;
        }
    }
}