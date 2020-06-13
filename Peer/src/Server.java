package src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server extends Thread {
    private LinkedList<Socket> peerQueue;
    private ServerSocket serverSocket;
    private int port;

    public Server(int port) {
        peerQueue = new LinkedList<Socket>();
        this.port = port;

    }

    public LinkedList<Socket> getPeerQueue() {
        return peerQueue;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);

        } catch (IOException e) {
            System.out.println("Failed on binding the peer ServerSocket to the given port. Maybe it is already in use");
            return;
        }

        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();

            }
            synchronized (peerQueue) {
                peerQueue.add(socket);
            }
        }

    }
}