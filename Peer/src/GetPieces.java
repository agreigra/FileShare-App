package src;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

import utils.*;

public class GetPieces extends Thread {

    Peer peer;
    String commande;

    public GetPieces(Peer p, String commande) {
        peer = p;
        this.commande = commande;

    }

    public void run() {

        String[] tokens = commande.split("[\\[\\] ]+");
        int index[] = new int[tokens.length - 2];

        System.out.print("\r< " + commande + "\n< ");
        /* recuperer les index dans la commande */

        for (int i = 2; i < tokens.length; i++) {
            index[i - 2] = Integer.parseInt(tokens[i]);
        }

        /*
         * envoyer la commande à tous les peers
         */
        Socket s;
        if ((s = peer.getPeerQueue().poll()) != null) {

            PrintWriter out;
            try {
                out = new PrintWriter(s.getOutputStream());
                out.println(commande);
                out.flush();

                /* chercher le fichier */
                String key = tokens[1];
                FileData newFile = peer.getFileByKey(key);
                /** creer la fichier */

                String folderName = newFile.getName().replaceFirst("[.][^.]+$", "");
                File tmpFolder = new File(peer.getfolder() + "/" + folderName);
                boolean fld = tmpFolder.mkdir();

                InputStream in = null;

                in = s.getInputStream();

                byte[] rps = new byte[8 + key.length()];

                in.read(rps);
                String response = new String(rps);

                String path = "";
                int count;
                for (int j = 0; j < index.length; j++) {
                    byte[] bytes = new byte[newFile.getPieceSize()];
                    int a = String.valueOf(index[j]).length();
                    in.read(rps, 0, a + 1);
                    response += new String(rps, 0, a + 1);
                    response += "piece" + index[j] + " ";

                    if (index[j] == newFile.getNumberOfPieces()) {
                        int lastPiece = newFile.getLength()
                                - ((newFile.getNumberOfPieces() - 1) * newFile.getPieceSize());
                        count = in.read(bytes, 0, lastPiece);
                    } else {
                        count = in.read(bytes);
                    }
                    path = peer.getfolder() + "/" + folderName + "/" + index[j];
                    Helper.writeToFile(path, bytes, count);
                    newFile.setBit(index[j] - 1);

                }

                response += " ]";
                System.out.print("\r> " + response + "\n< ");

                synchronized (newFile.map) {
                    if ((newFile.map.cardinality() == newFile.getNumberOfPieces()) && (!newFile.getComplet())) {
                        newFile.setComplet(true);
                        gatherFile(peer.getfolder() + "/" + folderName, newFile);
                    }
                }
                out.close();
                in.close();
                s.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

    }

    public void gatherFile(String FolderPath, FileData f) throws IOException {

        String filePath = peer.getfolder() + "/" + f.getName();

        for (int i = 0; i < f.getNumberOfPieces(); i++) {
            String p = FolderPath + "/" + Integer.toString(i + 1);
            byte[] piece = Helper.readFromFile(p, 0, f.getPieceSize());

            Helper.writeToFile(filePath, piece, piece.length);

        }
        File tmp = new File(FolderPath);
        Helper.deleteDirectory(tmp);
    }

}