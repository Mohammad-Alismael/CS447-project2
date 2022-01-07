package client;

import java.net.DatagramSocket;
import java.net.SocketException;

public class DatagramSocketSingleton extends DatagramSocket {
    private static DatagramSocket instance = null;
    private DatagramSocket datagramSocket;
    private DatagramSocketSingleton() throws SocketException {
        datagramSocket = new DatagramSocket();
    }

    public static DatagramSocket getInstance() throws SocketException {
        if (instance == null){
            instance = new DatagramSocket();
        }
        return instance;
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }
}

//public class SocketDevice{
//
//    private static final SocketDevice INSTANCE = new SocketDevice();
//
//    private Socket socket;
//
//    //Private constructor prevents instantiating and subclassing
//    private SocketDevice(){
//        // instanciates the socket ...
//    }
//
//    //Static 'instance' method
//    public static SocketDevice getInstance( ) {
//        return INSTANCE;
//    }
//public void open(){
//    // ...
//    socket.open()
//    // ...
//}
//
//    public void close(){
//        // ...
//        socket.close()
//        // ...
//    }



