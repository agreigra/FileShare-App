package utils;

public class PeerInfo {

  public String add;
  public int port;

  public PeerInfo(String add, int port) {
    this.add = add;
    this.port = port;
  }

  public String toString() {
    return add + " " + port;
  }
}
