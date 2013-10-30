package ru.fizteh.fivt.students.eltyshev.filemap.base;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Set;

public class FilemapWriter implements Closeable {

    private RandomAccessFile file;

    public FilemapWriter(String filePath) throws IOException {
        try {
            file = new RandomAccessFile(filePath, "rw");
        } catch (FileNotFoundException e) {
            throw new IOException(String.format("error while creating file: '%s'", filePath));
        }
        file.setLength(0);
    }

    public static void saveToFile(String filePath, Set<String> keys, HashMap<String, String> data) throws IOException {
        FilemapWriter writer = new FilemapWriter(filePath);
        int offset = FileMapUtils.getKeysLength(keys, AbstractTable.CHARSET);

        for (final String key : keys) {
            writer.writeKey(key);
            writer.writeOffset(offset);
            offset += FileMapUtils.getByteCount(data.get(key), AbstractTable.CHARSET);
        }
        for (final String key : keys) {
            writer.writeValue(data.get(key));
        }
        try {
            writer.close();
        } catch (IOException e) {
            // SAD
        }
    }

    public void close() throws IOException {
        file.close();
    }

    private void writeKey(String key) throws IOException {
        byte[] bytes = key.getBytes(AbstractTable.CHARSET);
        file.write(bytes);
        file.writeByte(0);
    }

    private void writeOffset(int offset) throws IOException {
        file.writeInt(offset);
    }

    private void writeValue(String value) throws IOException {
        byte[] bytes = value.getBytes(AbstractTable.CHARSET);
        file.write(bytes);
    }
}