package ru.fizteh.fivt.students.eltyshev.multifilemap;

import ru.fizteh.fivt.storage.strings.*;

import javax.swing.plaf.multi.MultiInternalFrameUI;
import java.io.File;
import java.util.HashMap;

public class DatabaseTableProvider implements TableProvider {
    HashMap<String, MultifileTable> tables = new HashMap<String, MultifileTable>();
    private String databaseDirectoryPath;
    private MultifileTable activeTable = null;

    public DatabaseTableProvider(String databaseDirectoryPath) {
        this.databaseDirectoryPath = databaseDirectoryPath;
        File databaseDirectory = new File(databaseDirectoryPath);
        for (final File tableFile : databaseDirectory.listFiles()) {
            if (tableFile.isFile()) {
                continue;
            }
            MultifileTable table = new MultifileTable(databaseDirectoryPath, tableFile.getName());
            tables.put(table.getName(), table);
        }
    }

    public Table getTable(String name) throws IllegalArgumentException, IllegalStateException {
        if (name == null) {
            throw new IllegalArgumentException("table's name cannot be null");
        }
        MultifileTable table = tables.get(name);

        if (table == null) {
            throw new IllegalStateException(String.format("%s not exists", name));
        }

        if (activeTable != null && activeTable.getUncommitedChangesCount() > 0) {
            throw new IllegalStateException(String.format("%d unsaved changes", activeTable.getUncommitedChangesCount()));
        }

        activeTable = table;
        return table;
    }

    public Table createTable(String name) throws IllegalArgumentException, IllegalStateException {
        if (name == null) {
            throw new IllegalArgumentException("table's name cannot be null");
        }

        if (tables.containsKey(name)) {
            throw new IllegalStateException(String.format("%s exists", name));
        }

        MultifileTable table = new MultifileTable(databaseDirectoryPath, name);
        tables.put(name, table);
        return table;
    }

    public void removeTable(String name) throws IllegalArgumentException, IllegalStateException {
        if (name == null) {
            throw new IllegalArgumentException("table's name cannot be null");
        }

        if (!tables.containsKey(name)) {
            throw new IllegalStateException(String.format("%s not exists", name));
        }

        tables.remove(name);

        File tableFile = new File(databaseDirectoryPath, name);
        MultifileMapUtils.deleteFile(tableFile);
    }
}
