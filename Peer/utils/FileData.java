package utils;

import java.util.BitSet;

public class FileData {
	/** Nom du fichier */
	private String name;
	/** Taille du fichier */
	private int length;
	/** Taille des pieces du fichier */
	private int pieceSize;
	/** la clé du fichier */
	private String key;
	/* map */
	public BitSet map;

	/** */
	private boolean complet;

	public FileData(String name, int length, String key, int ps) {
		this.name = name;
		this.length = length;
		this.key = key;
		this.pieceSize = ps;
		map = new BitSet();
	}

	public String getName() {
		return name;
	}

	public int getLength() {
		return length;
	}

	public int getPieceSize() {
		return pieceSize;
	}

	public String getKey() {
		return key;
	}

	public boolean getComplet() {
		return complet;
	}

	public void setComplet(Boolean b) {
		complet = b;
	}

	public int getNumberOfPieces() {
		int nbpieces = getLength() / getPieceSize();
		if (getLength() % getPieceSize() != 0)
			nbpieces++;

		return nbpieces;
	}

	public void setAllMap(int size, Boolean value) {
		map.set(0, size, value);
	}

	public void setBit(int index) {
		map.set(index);
	}

	public String getMap() {
		String m = "";
		for (int i = 0; i < getNumberOfPieces(); i++) {
			if (map.get(i) == true) {
				m += "1 ";
			} else {
				m += "0 ";
			}
		}
		return m;
	}

	public String toString() {
		return name + " " + length + " " + pieceSize + " " + key;
	}
}
