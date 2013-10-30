package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.nio.ByteBuffer;

public class DatabaseContext {
    
    private static final int MAX_KEY_SIZE = 1 << 24;
    private static final int MAX_VALUE_SIZE = 1 << 24;
    
    private HashMap<String, String> dataBase = null;
    private File dbFile; 
    private Path dbDir;

    public String remove(String key) throws IOException {
        String removed = dataBase.remove(key);
        writeOut(dataBase, dbFile);
        return removed;
    }

    public String get(String key) {
        return dataBase.get(key);
    }

    public String put(String key, String value) throws IOException {
        String previousValue = dataBase.put(key, value);
        writeOut(dataBase, dbFile);
        return previousValue;
    }

    public DatabaseContext(Path path) throws Exception {
        dbDir = path;
        if (!dbDir.toFile().isDirectory()) {
            throw new Exception("fizteh.db.dir is not a directory!");
        }
        dbFile = new File(dbDir.toString(), "/db.dat");
        if (!dbFile.exists()) {
            dbFile.createNewFile();
        }
        dataBase = parseDatabase(dbFile);
    } 
    
    private static HashMap<String, String> parseDatabase(File dbFile) 
            throws FileNotFoundException, IOException, Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        FileInputStream fstream = new FileInputStream(dbFile);
        try {
            while (fstream.available() > 0) {
                Map.Entry<String, String> newEntry = parseEntry(fstream);
                map.put(newEntry.getKey(), newEntry.getValue());
            }
        } finally {
            fstream.close();
        }
        return map;
    }
    
    private static void safeRead(FileInputStream fstream, byte[] buf, int readCount) throws Exception {
        int bytesRead = 0;
        if (readCount < 0) {
            throw new Exception("Error: malformed database");
        }
        while (bytesRead < readCount) {
            int readNow = fstream.read(buf, bytesRead, readCount - bytesRead);
            if (readNow < 0) {
                throw new Exception("Error: malformed database");
            }
            bytesRead += readNow;
        }
    }
    
    private static Map.Entry<String, String> parseEntry(FileInputStream fstream) throws Exception {
        byte[] sizeBuf = new byte[4];
        safeRead(fstream, sizeBuf, 4);
        int keySize = ByteBuffer.wrap(sizeBuf).getInt();
        safeRead(fstream, sizeBuf, 4);
        int valueSize = ByteBuffer.wrap(sizeBuf).getInt();
        if (keySize <= 0 || valueSize <= 0 || keySize > MAX_KEY_SIZE || valueSize > MAX_VALUE_SIZE) {
            throw new Exception("Error: malformed database");
        }
        byte[] keyBuf = new byte[keySize];
        safeRead(fstream, keyBuf, keySize);
        byte[] valueBuf = new byte[valueSize];
        safeRead(fstream, valueBuf, valueSize);
        return new AbstractMap.SimpleEntry<String, String>(new String(keyBuf, "UTF-8"), new String(valueBuf, "UTF-8"));
    }
    
    private static void writeOut(HashMap<String, String> map, File dbFile) throws IOException {
        if (dbFile.exists()) {
            dbFile.delete();
        }
        FileOutputStream fstream = new FileOutputStream(dbFile);
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                writeEntry(entry, fstream);
            } 
        } finally {
            try {
                fstream.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    private static void writeEntry(Map.Entry<String, String> e, FileOutputStream fstream) throws IOException {
        byte[] keyBuf = e.getKey().getBytes("UTF-8");
        byte[] valueBuf = e.getValue().getBytes("UTF-8");
        fstream.write(ByteBuffer.allocate(4).putInt(keyBuf.length).array());
        fstream.write(ByteBuffer.allocate(4).putInt(valueBuf.length).array());
        fstream.write(keyBuf);
        fstream.write(valueBuf);
    }
}
