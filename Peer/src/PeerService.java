package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import utils.*;

public class PeerService extends Thread {
	private Socket socket;
	private Peer peer;
	private PrintWriter output;
	private BufferedReader input;
	FileData file;

	public PeerService(Socket socket, Peer p) {
		this.socket = socket;
		this.peer = p;
		try {
			output = new PrintWriter(this.socket.getOutputStream());
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		String commande = null;
		while (true) {
			try {
				commande = input.readLine();
				if (commande != null) {
					System.out.print("\r> " + commande + "\n< ");
					commande = commande.replaceAll("[\\<]", "").trim();
					String[] tokens = commande.split("[\\[\\] ]+");
					if (tokens[0].equals("interested")) {
						this.have(tokens);
					} else if (tokens[0].equals("getpieces")) {
						this.data(tokens);
					}
				}
			} catch (IOException e) {

				try {
					input.close();
					output.close();
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				break;
			}

		}
	}

	public void have(String[] commande) throws IOException {
		String key = commande[1];
		ArrayList<FileData> fd = peer.getFilesInfo();

		if (!fd.isEmpty()) {
			for (int i = 0; i < fd.size(); i++) {
				if (fd.get(i).getKey().equals(key)) {
					file = fd.get(i);
					break;
				}
			}

			System.out.print("\r< have " + file.getKey() + " " + file.getMap() + "\n< ");
			output.println("have " + file.getKey() + " " + file.getMap());
			output.flush();
		}
	}

	public void data(String[] commande) throws IOException {

		/* récuperer les index des pieces à partir de la commande réçue */
		int index[] = new int[commande.length - 2];
		for (int i = 2; i < commande.length; i++) {
			index[i - 2] = Integer.parseInt(commande[i]);
		}

		OutputStream out = socket.getOutputStream();
		String dt = "data " + file.getKey() + " [ ";
		String responseToPrint = dt;
		byte[] byteArr = dt.getBytes();
		out.write(byteArr);

		String filePath = peer.getfolder() + "/";

		if (file.map.cardinality() == file.getNumberOfPieces()) {
			filePath += file.getName();

			for (int j = 0; j < index.length; j++) {
				byte[] data = Helper.readFromFile(filePath, (index[j] - 1) * file.getPieceSize(), file.getPieceSize());
				String indx = index[j] + ":";
				responseToPrint += indx + "piece" + index[j] + " ";
				out.write(indx.getBytes());
				out.write(data);
				out.flush();
			}
		}

		else {
			String folderName = file.getName().replaceFirst("[.][^.]+$", "");
			filePath += folderName + "/";

			for (int j = 0; j < index.length; j++) {
				byte[] data = Helper.readFromFile(filePath + "/" + index[j], 0, file.getPieceSize());
				String indx = index[j] + ":";
				responseToPrint += indx + "piece" + index[j] + " ";
				out.write(indx.getBytes());
				out.write(data);
				out.flush();

			}
		}

		String fin = " ]";
		responseToPrint += fin;
		System.out.print("\r< " + responseToPrint + "\n< ");
		out.write(fin.getBytes());

	}

}