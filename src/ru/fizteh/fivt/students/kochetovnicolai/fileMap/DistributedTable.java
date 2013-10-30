package ru.fizteh.fivt.students.kochetovnicolai.fileMap;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.students.kochetovnicolai.shell.FileManager;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DistributedTable extends FileManager implements Table {

    protected File currentFile;
    protected String tableName;
    protected HashMap<String, String> changes;
    protected int recordNumber;
    protected int oldRecordNumber;

    protected final int partsNumber = 16;
    protected File[] directoriesList = new File[partsNumber];
    protected File[][] filesList = new File[partsNumber][partsNumber];

    @Override
    public String getName() {
        return tableName;
    }

    private byte getFirstByte(String s) {
        try {
            return (byte) Math.abs(s.getBytes("UTF-8")[0]);
        } catch (UnsupportedEncodingException e) {
            return 0;
        }
    }

    DistributedTable(File tableDirectory, String name) throws IOException {
        currentPath = new File(tableDirectory.getPath() + File.separator + name);
        tableName = name;
        if (!currentPath.exists()) {
            if (!currentPath.mkdir()) {
                throw new IOException(currentPath.getAbsolutePath() + ": couldn't create directory");
            }
        }
        oldRecordNumber = 0;
        for (int i = 0; i < partsNumber; i++) {
            directoriesList[i] = new File(currentPath.getPath() + File.separator + Integer.toString(i) + ".dir");
            for (int j = 0; j < partsNumber; j++) {
                currentFile = new File(directoriesList[i].getPath() + File.separator + Integer.toString(j) + ".dat");
                filesList[i][j] = currentFile;
                if (directoriesList[i].exists() && currentFile.exists()) {
                    DataInputStream inputStream = new DataInputStream(new FileInputStream(currentFile));
                    String[] pair;
                    while ((pair = readNextPair(inputStream)) != null) {
                        byte firstByte = getFirstByte(pair[0]);
                        if (firstByte % partsNumber != i || (firstByte / partsNumber) % partsNumber != j) {
                            throw new IOException("Invalid key in file " + currentFile.getAbsolutePath());
                        }
                        oldRecordNumber++;
                    }
                    if (inputStream.read() != -1) {
                        throw new IOException("invalid file " + currentFile.getAbsolutePath());
                    }
                    inputStream.close();
                }
            }
        }
        changes = new HashMap<>();
        rollback();
    }

    @Override
    public int rollback() {
        int canceled = recordNumber - oldRecordNumber;
        recordNumber = oldRecordNumber;
        changes.clear();
        return canceled;
    }

    @Override
     public String get(String key) throws IllegalArgumentException {
        byte firstByte = getFirstByte(key);
        currentFile = filesList[firstByte % partsNumber][(firstByte / partsNumber) % partsNumber];
        currentPath = directoriesList[firstByte % partsNumber];
        if (key == null) {
            throw new IllegalArgumentException();
        }
        if (!changes.containsKey(key)) {
            changes.put(key, readValue(key));
        }
        return changes.get(key);
    }

    @Override
    public String put(String key, String value) throws IllegalArgumentException {
        byte firstByte = getFirstByte(key);
        currentFile = filesList[firstByte % partsNumber][(firstByte / partsNumber) % partsNumber];
        currentPath = directoriesList[firstByte % partsNumber];
        if (key == null || value == null) {
            throw new IllegalArgumentException();
        }
        if (get(key) == null) {
            recordNumber++;
        }
        return changes.put(key, value);
    }

    @Override
    public int commit() {
        int updated = changes.size();
        DataInputStream[][] inputStreams = new DataInputStream[partsNumber][partsNumber];
        DataOutputStream[][] outputStreams = new DataOutputStream[partsNumber][partsNumber];
        try {
            for (int i = 0; i < partsNumber; i++) {
                if (!directoriesList[i].exists()) {
                    if (!directoriesList[i].mkdir()) {
                        throw new IOException("couldn't create directory " + directoriesList[i].getAbsolutePath());
                    }
                }
                for (int j = 0; j < partsNumber; j++) {
                    if (!filesList[i][j].exists()) {
                        if (!filesList[i][j].createNewFile()) {
                            throw new IOException("couldn't create file " + filesList[i][j].getAbsolutePath());
                        }
                    }
                    if (!filesList[i][j].renameTo(new File(filesList[i][j].getPath() + "~"))) {
                        throw new IOException("couldn't rename file " + filesList[i][j].getAbsolutePath());
                    }
                    inputStreams[i][j] = new DataInputStream(new FileInputStream(filesList[i][j].getPath() + "~"));
                    outputStreams[i][j] = new DataOutputStream(new FileOutputStream(filesList[i][j]));
                    DataInputStream inputStream = inputStreams[i][j];
                    DataOutputStream outputStream = outputStreams[i][j];
                    String nextKey;
                    String nextValue;
                    String[] pair;
                    while ((pair = readNextPair(inputStream)) != null) {
                        nextKey = pair[0];
                        nextValue = pair[1];
                        if (changes.containsKey(nextKey)) {
                            nextValue = changes.get(nextKey);
                            changes.remove(nextKey);
                        }
                        if (nextValue != null) {
                            writeNextPair(outputStream, nextKey, nextValue);
                        }
                    }
                }
            }
            Set<Map.Entry<String, String>> entries = changes.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                if (entry.getValue() != null) {
                    byte firstByte = getFirstByte(entry.getKey());
                    DataOutputStream outputStream = outputStreams[firstByte % partsNumber]
                            [(firstByte / partsNumber) % partsNumber];
                    writeNextPair(outputStream, entry.getKey(), entry.getValue());
                }
            }
            for (int i = 0; i < partsNumber; i++) {
                for (int j = 0; j < partsNumber; j++) {
                    inputStreams[i][j].close();
                    outputStreams[i][j].close();
                    if (!(new File(filesList[i][j].getPath() + "~")).delete()) {
                        throw new IOException("couldn't delete file " + filesList[i][j].getPath() + "~");
                    }
                    if (filesList[i][j].length() == 0) {
                        if (!filesList[i][j].delete()) {
                            throw new IOException("couldn't delete file " + filesList[i][j].getPath());
                        }
                    }
                }
            }
            changes.clear();
            oldRecordNumber = recordNumber;
            for (File directory : directoriesList) {
                if (directory.list().length == 0) {
                    if (!directory.delete()) {
                        throw new IOException("couldn't delete directory " + directory.getAbsolutePath());
                    }
                }
            }
            return updated;
        } catch (IOException e) {
            for (int i = 0; i < partsNumber; i++) {
                for (int j = 0; j < partsNumber; j++) {
                    try {
                        if (inputStreams[i][j] != null) {
                            inputStreams[i][j].close();
                        }
                    } catch (IOException exception) {
                        printMessage(tableName + exception.getMessage());
                    }
                    try {
                        if (outputStreams[i][j] != null) {
                            outputStreams[i][j].close();
                        }
                    } catch (IOException exception) {
                        printMessage(tableName + exception.getMessage());
                    }
                }
            }
        }
        printMessage(tableName + ": cannot commit changes: i/o error occurred");
        return 0;
    }

    @Override
    public String remove(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        if (get(key) != null) {
            recordNumber--;
        }
        return changes.put(key, null);
    }

    @Override
    public int size() {
        return recordNumber;
    }

    protected void writeNextPair(DataOutputStream outputStream, String key, String value) throws IOException {
        byte[] keyBytes = key.getBytes("UTF-8");
        byte[] valueBytes = value.getBytes("UTF-8");
        outputStream.writeInt(keyBytes.length);
        outputStream.writeInt(valueBytes.length);
        outputStream.write(keyBytes);
        outputStream.write(valueBytes);
    }

    protected String[] readNextPair(DataInputStream inputStream) throws IOException {
        int keySize;
        int valueSize;
        try {
            keySize = inputStream.readInt();
            valueSize = inputStream.readInt();
            if (keySize < 1 || valueSize < 1 || inputStream.available() < keySize
                    || inputStream.available() < valueSize || inputStream.available() < keySize + valueSize) {
                throw new IOException("invalid string size");
            }
        } catch (IOException e) {
            return null;
        }
        byte[] keyBytes = new byte[keySize];
        byte[] valueBytes = new byte[valueSize];
        if (inputStream.read(keyBytes) != keySize || inputStream.read(valueBytes) != valueSize) {
            throw new IOException("unexpected end of file");
        }
        String[] pair = new String[2];
        pair[0] = new String(keyBytes, "UTF-8");
        pair[1] = new String(valueBytes, "UTF-8");
        return pair;
    }

    protected String readValue(String key) {
        if (currentFile == null) {
            return null;
        }
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(currentFile))) {
            String[] pair;
            while ((pair = readNextPair(inputStream)) != null) {
                if (pair[0].equals(key)) {
                    inputStream.close();
                    return pair[1];
                }
            }
            inputStream.close();
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
