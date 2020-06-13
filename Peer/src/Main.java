package src;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import utils.Helper;
import java.io.Console;

public class Main {

	public static void main(String[] args) throws Exception {

		/* Tracker information */
		String TrackerAddress;
		int TrackerPort;
		/* shared folder */
		String fld;
		/* peer default port */
		int peerPort;

		/** */

		int pieceSize;

		String ConfigFilePath = args[0];
		try (Stream<String> stream = Files.lines(Paths.get(ConfigFilePath))) {
			List<String> list = stream.collect(Collectors.toList());
			TrackerAddress = list.get(1).trim().split("=")[1];
			TrackerPort = Integer.parseInt(list.get(3).trim().split("=")[1]);
			peerPort = Integer.parseInt(list.get(5).trim().split("=")[1]);
			fld = list.get(7).trim().split("=")[1];
			pieceSize = Integer.parseInt(list.get(9).trim().split("=")[1]);

		} catch (IOException e) {

			System.out.println("verify configuration file");
			return;
		}

		InetAddress addr = InetAddress.getByName(TrackerAddress);

		/* connect to the tracker */
		Socket socket = null;
		try {

			socket = new Socket(addr, TrackerPort);
		} catch (IOException e) {
			System.out.println("Problem on socket binding");
			e.printStackTrace();

			return;
		}

		File folder = new File(fld);
		if (!folder.isDirectory()) {
			System.out.println("Put a valid directory name");
			socket.close();
			return;
		}
		Peer peer = new Peer(peerPort, socket, folder, pieceSize);

		peer.announce();
		/** launch server */

		Server server = new Server(peerPort);
		server.start();

		/* pool de thread */
		PoolThread pt = new PoolThread(3, peer, server);
		pt.start();

		/** update */
		Update update = new Update(peer);
		update.start();
		/**/
		Console cnsl = System.console();
		while (cnsl != null) {

			String command = cnsl.readLine("< ");
			command = command.replaceAll("[\\<]", "").trim();
			String result[] = command.split(" ");

			if ((result[0].equals("look")) && (result.length == 2)) {
				peer.look("look [filename=\"" + result[1] + "\"]");
			}

			else if ((result[0].equals("download")) && (result.length == 2)) {
				peer.interested(result[1]);
			}

			else
				System.out.println("< Unknown Command");

		}

		socket.close();
	}

}
