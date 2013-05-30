package de.dailab.plistacontest.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsWriter {

	private static Logger logger = LoggerFactory.getLogger(MahoutWriter.class);

	private final String fileName;

	public StatsWriter(final String _fileName, final String _type, final String _numRecItems, final String _recId) {
		this.fileName = _fileName;
		writeStat(_type, _numRecItems, _recId);
	}

	public void writeStat(final String _type, final String _numRecItems, final String _recId) {

		FileWriter fw = null;

		try {
			fw = new FileWriter(this.fileName, true);
			fw.append(_type + ";" + _recId + ";" + _numRecItems + ";" + new Date().getTime());
			fw.append(System.getProperty("line.separator"));
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}

	}

}
