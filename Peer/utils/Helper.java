package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.ArrayList;

public class Helper {

  /*
   * function that takes a folder as parameter and return a liste of the files
   * exist in the folder with their names, their lengths and keys
   */
  public static ArrayList<FileData> listFiles(File folder, int pieceSize) throws Exception {

    ArrayList<FileData> files = new ArrayList<FileData>();

    for (File fileEntry : folder.listFiles()) {
      if (!fileEntry.isDirectory()) {
        FileData f = new FileData(fileEntry.getName(), (int) fileEntry.length(), md5OfFile(fileEntry), pieceSize);
        int nb_pieces = (int) fileEntry.length() / pieceSize;
        if (fileEntry.length() % pieceSize != 0) {
          nb_pieces++;
        }

        f.setAllMap(nb_pieces, true);
        files.add(f);
      }
    }
    return files;
  }

  /* function that takes a file as parameter and return the md5 key */
  public static String md5OfFile(File file) throws Exception {
    MessageDigest md = MessageDigest.getInstance("MD5");
    FileInputStream fs = new FileInputStream(file);
    BufferedInputStream bs = new BufferedInputStream(fs);
    byte[] buffer = new byte[1024];
    int bytesRead;

    while ((bytesRead = bs.read(buffer, 0, buffer.length)) != -1) {
      md.update(buffer, 0, bytesRead);
    }
    bs.close();
    byte[] digest = md.digest();

    StringBuilder sb = new StringBuilder();
    for (byte bite : digest) {
      sb.append(String.format("%02x", bite & 0xff));
    }
    return sb.toString();
  }

  

  public static void writeToFile(String filePath, byte[] bytes, int size) throws IOException {
    OutputStream out = new FileOutputStream(filePath, true);
    out.write(bytes, 0, size);
    out.close();
  }

  public static byte[] readFromFile(String filePath, int position, int size) throws IOException {

    RandomAccessFile file = new RandomAccessFile(filePath, "r");
    file.seek(position);
    if (file.length() < position + size) {
      size = (int) (file.length() - position);
    }
    byte[] bytes = new byte[size];
    file.read(bytes);
    file.close();
    return bytes;
  }

  public static void deleteDirectory(File file) throws IOException {
    if (file.isDirectory()) {
      File[] entries = file.listFiles();
      if (entries != null) {
        for (File entry : entries) {
          deleteDirectory(entry);
        }
      }
    }
    if (!file.delete()) {
      throw new IOException("Failed to delete " + file);
    }
  }

}
