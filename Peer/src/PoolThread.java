package src;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoolThread extends Thread {
    private int numThreads;
    Peer peer;
    Server server;

    public PoolThread(int numThreads, Peer p, Server s) {
        this.numThreads = numThreads;
        peer = p;
        server = s;
    }

    public void run() {

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        while (true) {
            if (server.getPeerQueue().peek() == null) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            synchronized (server.getPeerQueue()) {

                Socket s = server.getPeerQueue().poll();
                PeerService peerService = new PeerService(s, peer);
                executor.execute(peerService);
            }
        }
    }
}