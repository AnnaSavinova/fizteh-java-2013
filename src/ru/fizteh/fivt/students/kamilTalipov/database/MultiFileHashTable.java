package ru.fizteh.fivt.students.kamilTalipov.database;


import ru.fizteh.fivt.storage.strings.Table;
import static ru.fizteh.fivt.students.kamilTalipov.database.InputStreamUtils.readInt;
import static ru.fizteh.fivt.students.kamilTalipov.database.InputStreamUtils.readString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class MultiFileHashTable implements Table {
    public MultiFileHashTable(String workingDirectory, String tableName) throws DatabaseException,
                                                                                FileNotFoundException {
        this.tableName = tableName;

        try {
            tableDirectory = FileUtils.makeDir(workingDirectory + File.separator + tableName);
        } catch (IllegalArgumentException e) {
            throw new DatabaseException("Couldn't open table '" + tableName + "'");
        }

        table = new HashMap<String, String>();
        readTable();
    }

    @Override
    public String getName() {
        return tableName;
    }

    @Override
    public String get(String key) {
        return table.get(key);
    }

    @Override
    public String put(String key, String value) {
        return table.put(key, value);
    }

    @Override
    public String remove(String key) {
        return table.remove(key);
    }

    public void removeTable() throws DatabaseException {
        removeDataFiles();
        FileUtils.remove(tableDirectory);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int commit() {
        return 0;
    }

    @Override
    public int rollback() {
        return 0;
    }

    public void exit() {
        try {
            writeTable();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (DatabaseException e) {
            System.err.println(e.getMessage());
        }
    }

    private void readTable() throws DatabaseException, FileNotFoundException {
        File[] innerFiles = tableDirectory.listFiles();
        for (File file : innerFiles) {
            if (!file.isDirectory() || !isCorrectDirectoryName(file.getName())) {
                throw new DatabaseException("At table '" + tableName
                        + "': directory contain redundant files");
            }

            readData(file);
        }
    }

    private void writeTable() throws DatabaseException, IOException {
        removeDataFiles();

        if (table.size() == 0) {
            return;
        }

        for (Map.Entry<String, String> entry : table.entrySet()) {
            byte[] key = entry.getKey().getBytes("UTF-8");
            byte[] value = entry.getValue().getBytes("UTF-8");

            File directory = FileUtils.makeDir(tableDirectory.getAbsolutePath()
                                                + File.separator + getDirectoryName(key[0]));
            File dbFile = FileUtils.makeFile(directory.getAbsolutePath(), getFileName(key[0]));

            FileOutputStream output = new FileOutputStream(dbFile, true);
            try {
                output.write(ByteBuffer.allocate(4).putInt(key.length).array());
                output.write(ByteBuffer.allocate(4).putInt(value.length).array());
                output.write(key);
                output.write(value);
            } finally {
                try {
                    output.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private String getDirectoryName(byte keyByte) {
        if (keyByte < 0) {
            keyByte *= -1;
        }
        return Integer.toString((keyByte % ALL_DIRECTORIES + ALL_DIRECTORIES) % ALL_DIRECTORIES) + ".dir";
    }

    private String getFileName(byte keyByte) {
        if (keyByte < 0) {
            keyByte *= -1;
        }
        return Integer.toString(((keyByte / ALL_DIRECTORIES)
                                + FILES_IN_DIRECTORY) % FILES_IN_DIRECTORY) + ".dat";
    }

    private boolean isCorrectDirectoryName(String name) {
        for (int i = 0; i < ALL_DIRECTORIES; ++i) {
            if (name.equals(Integer.toString(i) + ".dir")) {
                return true;
            }
        }

        return false;
    }

    private void readData(File dbDir) throws DatabaseException, FileNotFoundException {
        for (File dbFile : dbDir.listFiles()) {
            FileInputStream input = new FileInputStream(dbFile);
            try {
                while (input.available() > 0) {
                    int keyLen = readInt(input);
                    int valueLen = readInt(input);
                    if (keyLen > MAX_KEY_LEN || valueLen > MAX_VALUE_LEN) {
                        throw new DatabaseException("Database file have incorrect format");
                    }
                    String key = readString(input, keyLen);
                    if (!getDirectoryName(key.getBytes("UTF-8")[0]).equals(dbDir.getName())
                            || !getFileName(key.getBytes("UTF-8")[0]).equals(dbFile.getName())) {
                        throw new DatabaseException("Database file have incorrect format");
                    }
                    String value = readString(input, valueLen);
                    table.put(key, value);
                }
            } catch (IOException e) {
                throw new DatabaseException("Database file have incorrect format");
            } finally {
                try {
                    input.close();
                }  catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private void removeDataFiles() throws DatabaseException {
        File[] innerFiles = tableDirectory.listFiles();
        for (File file : innerFiles) {
            if (!file.isDirectory() || !isCorrectDirectoryName(file.getName())) {
                throw new DatabaseException("At table '" + tableName
                        + "': directory contain redundant files");
            }
            FileUtils.remove(file);
        }
    }

    private HashMap<String, String> table;

    private final String tableName;
    private final File tableDirectory;

    private static final int ALL_DIRECTORIES = 16;
    private static final int FILES_IN_DIRECTORY = 16;

    private static final int MAX_KEY_LEN = 1 << 24;
    private static final int MAX_VALUE_LEN = 1 << 24;
}
