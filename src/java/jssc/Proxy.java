package jssc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Proxy {

    private static Logger logger;

    private ISerialPort serialPort;

    private String serialPortString;

    private int socketPort;

    private ServerSocket serverSocket;

    private File devFile;

    public static void main(String[] args) throws Exception {
        Formatter formatter = new Formatter() {
            private final Date dat = new Date();

            @Override
            public synchronized String format(LogRecord record) {
                dat.setTime(record.getMillis());
                String source;
                if (record.getSourceClassName() != null) {
                    source = record.getSourceClassName();
                    if (record.getSourceMethodName() != null) {
                       source += " " + record.getSourceMethodName();
                    }
                } else {
                    source = record.getLoggerName();
                }
                String message = formatMessage(record);
                String throwable = "";
                if (record.getThrown() != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    pw.println();
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    throwable = sw.toString();
                }
                return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$6s %2$s %5$s%6$s%n",
                                     dat,
                                     source,
                                     record.getLoggerName(),
                                     record.getLevel(),
                                     message,
                                     throwable);
            }
        };

        Handler consoleHandler = new ConsoleHandler() {
            { setOutputStream(System.out); }
        };
        consoleHandler.setFormatter(formatter);
        consoleHandler.setLevel(Level.FINEST);

        Logger rootLogger = LogManager.getLogManager().getLogger("");

        for (Handler h : rootLogger.getHandlers()) {
            rootLogger.removeHandler(h);
        }
        rootLogger.addHandler(consoleHandler);
        Logger.getAnonymousLogger().addHandler(consoleHandler);

        logger = Logger.getLogger("NetworkSerialPort");

        boolean portFlag = false;
        String serialPortString = null;
        int socketPort = 9990;
        for (String a : args) {
            if (portFlag) {
                try {
                    socketPort = Integer.parseInt(a);
                    portFlag = false;
                } catch (NumberFormatException e) {
                    System.err.println("Wrong port number " + a);
                    System.exit(6);
                }
            } else if (a.equals("-v")) {
                consoleHandler.setLevel(Level.FINE);
                rootLogger.setLevel(Level.FINE);
                logger.fine("Log level set to FINE");
            } else if (a.equals("-vv")) {
                consoleHandler.setLevel(Level.FINER);
                rootLogger.setLevel(Level.FINER);
                logger.fine("Log level set to FINER");
            } else if (a.equals("-vvv")) {
                consoleHandler.setLevel(Level.FINEST);
                rootLogger.setLevel(Level.FINEST);
                logger.fine("Log level set to FINEST");
            } else if (a.equals("-h")) {
                printHelp();
                System.exit(1);
            } else if (a.equals("-p")) {
                portFlag = true;
            } else if (a.startsWith("-")) {
                System.err.println("Unknown option " + a);
                System.exit(2);
            } else {
                if (serialPortString != null) {
                    System.err.println("You can only have one port string!");
                }
                serialPortString = a;
            }
        }
        if (portFlag) {
            System.err.println("Socket port number is missing");
            System.err.println();
            printHelp();
            System.exit(7);
        }
        if (serialPortString == null) {
            System.err.println("Port string is not supplied");
            System.err.println();
            printHelp();
            System.exit(3);
        }

        System.out.println("Serial port: " + serialPortString);
        System.out.println("Socket port: " + socketPort);

        Proxy client = new Proxy(serialPortString, socketPort);
        client.start();
    }

    private static void printHelp() {
        System.err.println("Usage: Proxy [options] <serial>");
        System.err.println("    -h        - this page");
        System.err.println("    -v        - verbose output");
        System.err.println("    -vv       - more verbose output");
        System.err.println("    -vvv      - the most verbose output");
        System.err.println("    -p <port> - socket port number");
    }

    public Proxy(String serialPortString, int socketPort) throws IOException {
        this.serialPortString = serialPortString;
        this.socketPort = socketPort;
    }

    public void start() {
        int serialError = 0;

        devFile = new File(serialPortString);

        while (true) {
            if (serialPort == null && devFile.exists()) {
                logger.info("Opening serial port " + serialPortString);
                try {
                    serialPort = new LocalSerialPort(serialPortString);
                    serialPort.openPort();
                    serialPort.setParams(115200, 8, 1, 0);
                    logger.info("Opened serial port");
                    serialError = 0;
                } catch (Exception e) {
                    if (serialError == 5) {
                        logger.severe("Cannot open serial port " + serialPortString);
                        // logger.log(Level.SEVERE, "Cannot open serial port " + serialPortString, e);
                    }
                    serialError = serialError + 1;
                    if (serialError > 10) { serialError = 10; }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) { }
                }
            } else if (serialPort != null && !devFile.exists()) {
                logger.info("Closing serial port " + serialPortString);
                closeSerialPort();
            } else if (serialPort != null) {
                if (serverSocket == null) {
                    logger.info("Opening socket port " + socketPort);
                    try {
                        serverSocket = new ServerSocket(socketPort);
                        serverSocket.setSoTimeout(1000);
                        logger.info("Opened socket port");
                    } catch (Exception e) {
                        System.err.println("Cannot open port " + socketPort);
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Socket socket = serverSocket.accept();
                        logger.fine("Got connection " + socket.getRemoteSocketAddress());
                        Client client = new Client(serialPort, socket);
                        client.start();
                    } catch (SocketTimeoutException ignore) {

                    } catch (IOException e) {
                        closeServerSocket();
                    }
                }
            }
        }
    }

    protected synchronized void closeServerSocket() {
        if (serverSocket != null) {
            logger.info("Closing server socket");
            try {
                serverSocket.close();
            } catch (Exception ignore) { }
            serverSocket = null;
        }
    }

    protected synchronized void closeSerialPort() {
        if (serialPort != null) {
            logger.info("Closing serial port");
            try {
                serialPort.closePort();
            } catch (SerialPortException ignore) { }
            serialPort = null;
            closeServerSocket();
        }
    }

    private class Client {

        private ISerialPort port;

        private Thread readerThread;
        private Thread writerThread;
        private Socket socket;
        private InputStream socketIn;
        private OutputStream socketOut;

        private boolean startWriterThread = true;

        public Client(ISerialPort port, Socket socket) {
            this.port = port;
            this.socket = socket;
        }

        public void start() {

            try {
                socketIn = socket.getInputStream();
                socketOut = socket.getOutputStream();
            } catch (IOException e) {
                close();
            }

            if (!socket.isClosed()) {

                readerThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (socket != null && !socket.isClosed()) {
                            try {
                                int r = socketIn.read();

                                if (r > 0) {
                                    if (startWriterThread) {
                                        writerThread.start();
                                        startWriterThread = false;
                                    }
                                    int a = socketIn.available();
                                    logger.finer("<(" + a + ")");
                                    byte[] readBuffer = new byte[a + 1];
                                    readBuffer[0] = (byte)r;
                                    socketIn.read(readBuffer, 1, a);

                                    a = a + 1;

                                    port.writeBytes(readBuffer);
                                } else {
                                    logger.finer("<(EOF)");
                                    close();
                                }
                            } catch (SerialPortException e) {
                                close();
                                logger.log(Level.SEVERE, "Problem reading writing serial port", e);
                                closeSerialPort();
                            } catch (IOException e) {
                                close();
                            }
                        }
                    }
                });

                writerThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (socket != null && !socket.isClosed()) {
                            try {
                                int bytes = port.getInputBufferBytesCount();
                                if (bytes > 0) {
                                    logger.finer(">(" + bytes + ")");
                                    byte[] buffer = port.readBytes(bytes);
                                    socketOut.write(buffer);
                                } else {
                                    try {
                                        byte[] buffer = port.readBytes(1, 1000);
                                        logger.finer(">(1.)");
                                        socketOut.write(buffer, 0, 1);
                                    } catch (SerialPortTimeoutException ignore) {
                                    }
                                }
                            } catch (SerialPortException e) {
                                close();
                                logger.log(Level.SEVERE, "Problem reading to serial port", e);
                                closeSerialPort();
                            } catch (IOException e) {
                                close();
                            }
                        }
                    }
                });

                readerThread.start();
                // writerThread.start();
            }
        }

        protected synchronized void close() {
            if (socket != null) {
                try {
                    socket.close();
                    logger.fine("Closing connection " + socket.getRemoteSocketAddress());
                } catch (Exception ignore) { }

                try { readerThread.interrupt(); } catch (Exception ignore) {}
                try { writerThread.interrupt(); } catch (Exception ignore) {}

                readerThread = null;
                writerThread = null;

                socket = null;
            }
        }
    }
}
