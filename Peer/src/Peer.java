package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import utils.*;
import java.util.LinkedList;

public class Peer {

  private int port;
  private Socket socket;
  private File folder;
  ArrayList<FileData> filesInfo = new ArrayList<FileData>();
  private PrintWriter output;
  private BufferedReader input;
  private static LinkedList<Socket> peerQueue;
  private int pieceSize;

  public Peer(int port, Socket socket, File folder, int pieceSize) throws IOException {
    this.port = port;
    this.socket = socket;
    this.folder = folder;
    this.pieceSize = pieceSize;
    peerQueue = new LinkedList<Socket>();
    output = new PrintWriter(this.socket.getOutputStream());
    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    try {
      filesInfo = Helper.listFiles(this.folder, pieceSize);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public Socket getSocket() {
    return socket;
  }

  public ArrayList<FileData> getFilesInfo() {
    return filesInfo;
  }

  public File getfolder() {
    return folder;
  }

  public FileData getFileByKey(String key) {
    for (int k = 0; k < filesInfo.size(); k++) {
      if (filesInfo.get(k).getKey().equals(key)) {
        return filesInfo.get(k);

      }
    }
    return null;

  }

  public BufferedReader getInput() {
    return input;
  }

  public PrintWriter getOutput() {
    return output;
  }

  public int getPort() {
    return port;
  }

  public LinkedList<Socket> getPeerQueue() {
    return peerQueue;
  }

  public void closeSocket() throws IOException {
    socket.close();
  }

  public void announce() throws Exception {

    StringBuffer sb = new StringBuffer();
    sb.append("[");
    for (FileData s : filesInfo) {
      sb.append(s.toString());
      sb.append(" ");
    }
    sb.append("]");
    String f = sb.toString();

    output.print("announce listen " + port + " seed ");
    output.println(f);
    output.flush();
    System.out.println("\r< announce listen " + port + " seed " + f + " leech [ ]");
    String response = input.readLine();
    if (response != null) {
      System.out.println("\r> " + response);
    }

  }

  public void look(String commande) throws IOException {

    // String cmd = String.join(" ", commande);
    System.out.println("\r< " + commande );
    output.println(commande);
    output.flush();
    String response = null;
    try {
      response = input.readLine();
      if (response != null)
        System.out.println("\r> " + response);

      response = response.replaceAll("[\\<]", "").trim();
      String[] tokens = response.split("[\\[\\] ]+");

      if ((tokens[0].equals("list")) && (tokens.length > 1)) {

        FileData f = new FileData(tokens[1], (int) Long.parseLong(tokens[2]), tokens[4], pieceSize);
        f.setAllMap(f.getNumberOfPieces(), false);
        f.setComplet(false);
        filesInfo.add(f);

        getfile("getfile " + tokens[4]);
      }

    } catch (IOException e) {
      System.out.println("< Tracker response is not well synated");
    }
  }

  /**
   * Envoie la commande getfile au tracker et attend la reponse.
   *
   * @param commande tableau de string contenant les mots de la commande qui va
   *                 être envoyé.
   * @throws IOException
   */
  public void getfile(String commande) throws IOException {

    System.out.print("\r< " + commande + "\n< ");
    output.println(commande);
    output.flush();
    String response = null;
    try {
      response = input.readLine();
      if (response != null) {
        System.out.println("\r> " + response);

        String delimiters = "[\\[\\] ]+";
        String[] tokens = response.split(delimiters);
        int length = tokens.length;
        ArrayList<PeerInfo> peerInfo = new ArrayList<PeerInfo>();
        if (length > 1) {
          for (int i = 2; i < length; i++) {
            String[] info = tokens[i].split(":");
            try {
              if (Integer.parseInt(info[1]) != port)
                peerInfo.add(new PeerInfo(info[0], Integer.parseInt(info[1])));
            } catch (Exception e) {
              System.out.println("< Invalid information about peers");
            }
          }
        }

        if (!peerInfo.isEmpty()) {
          for (int i = 0; i < peerInfo.size(); i++) {
            PeerInfo info = peerInfo.get(i);
            Socket s = new Socket(info.add, info.port);
            peerQueue.add(s);
          }
        }

      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void interested(String commande) throws IOException {
    System.out.print("\r< " + "interested " + commande + "\n< ");
    int[][] mapResultant = new int[peerQueue.size()][];

    for (int i = 0; i < peerQueue.size(); i++) {
      Socket s = peerQueue.get(i);

      PrintWriter out = new PrintWriter(s.getOutputStream());
      BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
      out.println("interested " + commande);
      out.flush();
      String response = null;
      try {
        response = in.readLine();
        if (response != null) {
          System.out.println("\r> " + response);
          mapResultant[i] = extractMap(response);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    ArrayList<ArrayList<Integer>> map = choosePieces(mapResultant);
    for (int i = 0; i < peerQueue.size(); i++) {
      String pieces = "[ ";
      for (int j = 0; j < map.get(i).size(); j++) {
        pieces += map.get(i).get(j) + " ";
      }
      pieces += "]";
      GetPieces gp = new GetPieces(this, "getpieces " + commande + " " + pieces);
      gp.start();
    }

  }

  public int[] extractMap(String cmd) {
    String[] tokens = cmd.split("[ ]+");
    int[] map = Arrays.stream(Arrays.copyOfRange(tokens, 2, tokens.length)).mapToInt(Integer::parseInt).toArray();
    return map;
  }

  public ArrayList<ArrayList<Integer>> choosePieces(int[][] pairs) {

    ArrayList<ArrayList<Integer>> r = new ArrayList<ArrayList<Integer>>();
    int j = 0;

    int peersNumber = pairs.length;

    if (peersNumber == 1) {
      ArrayList<Integer> r1 = new ArrayList<Integer>();
      r.add(r1);

      for (int i = 0; i < pairs[0].length; i++) {
        if (pairs[j][i] == 1)
          r.get(j).add(i + 1);
      }
    } else if (peersNumber == 2) {
      ArrayList<Integer> r1 = new ArrayList<Integer>();
      ArrayList<Integer> r2 = new ArrayList<Integer>();
      r.add(r1);
      r.add(r2);

      for (int i = 0; i < pairs[0].length; i++) {
        if (pairs[j][i] == 1)
          r.get(j).add(i + 1);
        else if (pairs[(j + 1) % 2][i] == 1)
          r.get((j + 1) % 2).add(i + 1);
        j = (j + 1) % 2;
      }

    } else if (peersNumber == 3) {
      ArrayList<Integer> r1 = new ArrayList<Integer>();
      ArrayList<Integer> r2 = new ArrayList<Integer>();
      ArrayList<Integer> r3 = new ArrayList<Integer>();
      r.add(r1);
      r.add(r2);
      r.add(r3);

      for (int i = 0; i < pairs[0].length; i++) {
        if (pairs[j][i] == 1)
          r.get(j).add(i + 1);

        else if (pairs[(j + 1) % 3][i] == 1)
          r.get((j + 1) % 3).add(i + 1);

        else if (pairs[(j + 2) % 3][i] == 1)
          r.get((j + 1) % 3).add(i + 1);
        j = (j + 1) % 3;

      }

    }
    return r;

  }
}
