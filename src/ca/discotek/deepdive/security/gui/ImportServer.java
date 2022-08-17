package ca.discotek.deepdive.security.gui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.SwingUtilities;

public class ImportServer {

    public static final int DEFAULT_PORT = 5678;
    
    ServerSocket serverSocket;
    final DeepDiveGui gui;
    
    ImportServer(int port, DeepDiveGui gui) throws IOException {
        this(null, port, gui);
    }
    
    ImportServer(InetAddress address, int port, DeepDiveGui gui) throws IOException {
        this.gui = gui;
        serverSocket = new ServerSocket(port, Integer.MAX_VALUE, address);
        SocketHandler handler = new SocketHandler();
        handler.start();
    }
    
    public void stop() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
            serverSocket = null;
        }
    }

    public static final String ACCEPT_VERSION = "version-accepted";
    public static final String IMPORT_SUCCESSFUL = "import-successful";
    public static final String IMPORT_FAILED = "import-failed";
    
    class SocketHandler extends Thread {
        
        public void run() {
            while (serverSocket != null && serverSocket.isBound() && !serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    InputStream is = socket.getInputStream();
                    ObjectInputStream ois = new ObjectInputStream(is);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    
                    int version = ois.readInt();
                    switch (version) {
                        case 1:
                            oos.writeObject(ACCEPT_VERSION);
                            SocketHandlerVersion1 sh = new SocketHandlerVersion1(ois, oos);
                            sh.start();
                            break;
                        default:
                            throw new RuntimeException("Bug. Unknown import version: " + version);
                    }
                }
                catch (Exception e) {
//                    e.printStackTrace();
                    System.out.println("Server: " + e.getMessage());
                }
            }
        }
    }
    
    class SocketHandlerVersion1 extends Thread {
        
        ObjectInputStream ois;
        ObjectOutputStream oos;
        
        SocketHandlerVersion1(ObjectInputStream ois, ObjectOutputStream oos) {
            this.ois = ois;
            this.oos = oos;
        }
        
        public void run() {
            String result = null;
            try {
                final String projectName = (String) ois.readObject();
                final String xml[] = (String[]) ois.readObject();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            gui.importNetworkProject(projectName, xml);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                result = IMPORT_SUCCESSFUL;
            }
            catch (Exception e) {
                result = IMPORT_FAILED;
            }
            finally {
                try {
                    oos.writeObject(result);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
