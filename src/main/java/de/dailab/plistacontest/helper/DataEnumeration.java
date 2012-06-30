package de.dailab.plistacontest.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataEnumeration
                implements Enumeration<InputStream> {

    private Logger logger = LoggerFactory.getLogger(DataEnumeration.class);

    String[] listOfFiles;

    int current = 0;

    public DataEnumeration(String[] _listOfFiles) {
        this.listOfFiles = _listOfFiles;
    }

    public boolean hasMoreElements() {
        if (this.current < this.listOfFiles.length) {
            return true;
        }
        else {
            return false;
        }
    }

    public InputStream nextElement() {
        InputStream is = null;

        if (!hasMoreElements()) {
            throw new NoSuchElementException("No more files.");
        }
        else {
            try {
                String nextElement = this.listOfFiles[this.current];
                this.current++;

                File f = new File(nextElement);
                if (f.exists()) {
                    is = new FileInputStream(f);
                }
                else {
                    f.createNewFile();
                    is = new FileInputStream(f);
                }
            }
            catch (FileNotFoundException e) {
                this.logger.error(e.getMessage());
            }
            catch (IOException e) {
                this.logger.error(e.getMessage());
            }
        }
        return is;
    }

}
