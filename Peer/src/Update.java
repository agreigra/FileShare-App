package src;

import java.io.IOException;

import utils.FileData;

public class Update extends Thread {
    Peer peer;

    public Update(Peer p) {
        this.peer = p;
    }

    public void run() {

	while(true){        
	try {
            Thread.sleep(60000);
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
        StringBuffer seed = new StringBuffer();
        StringBuffer leech = new StringBuffer();

        seed.append("[");
        leech.append("[");
        for (FileData s : peer.getFilesInfo()) {
            if (s.map.cardinality() == s.getNumberOfPieces()) {
                seed.append(s.getKey());
                if (s != peer.getFilesInfo().get(peer.getFilesInfo().size() - 1)) {
                    seed.append(" ");
                }
            } else {
                leech.append(s.getKey());
                if (s != peer.getFilesInfo().get(peer.getFilesInfo().size() - 1)) {
                    seed.append(" ");
                }
            }

        }

        seed.append("]");
        leech.append("]");

        String seedResult = seed.toString();
        String leechResult = leech.toString();
        peer.getOutput().print("update seed " + seedResult + " leech " + leechResult);
        peer.getOutput().flush();

        System.out.println("\r< update seed " + seedResult + " leech " + leechResult);
        String response;
        try {
            response = peer.getInput().readLine();
            if (response != null) {
                System.out.print("\r> " + response + "\n< ");
            }
        } catch (IOException e) {
            try {
                peer.getInput().close();
                peer.getOutput().close();
                peer.getSocket().close();
            } catch (IOException e1) {

                e1.printStackTrace();
            }

            System.out.println("\r> Tracker disconnected");
        }
}
    }

}
