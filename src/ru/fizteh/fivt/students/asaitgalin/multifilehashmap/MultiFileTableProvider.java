package ru.fizteh.fivt.students.asaitgalin.multifilehashmap;

import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.students.asaitgalin.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class MultiFileTableProvider implements TableProvider {
    private MultiFileTable currentTable;
    private File dbDirectory;

    public MultiFileTableProvider(File dbDirectory) {
        this.dbDirectory = dbDirectory;
    }

    public MultiFileTable getCurrentTable() {
        return currentTable;
    }

    public void setCurrentTable(MultiFileTable currentTable) {
        this.currentTable = currentTable;
    }

    public void saveCurrentTable() {
        if (currentTable != null) {
            try {
                currentTable.save(new File(dbDirectory, currentTable.getName()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public MultiFileTable getTable(String name) {
        if (name == null) {
            throw new IllegalArgumentException("failed to get table: invalid name");
        }
        File tableDir = new File(dbDirectory, name);
        if (!tableDir.exists()) {
            return null;
        }
        MultiFileTable table = new MultiFileTable(name);
        try {
            table.load(tableDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return table;
    }

    @Override
    public MultiFileTable createTable(String name) {
        if (name == null) {
            throw new IllegalArgumentException("failed to create table: invalid name");
        }
        File tableDir = new File(dbDirectory, name);
        if (tableDir.exists()) {
            return null;
        }
        tableDir.mkdir();
        return new MultiFileTable(name);
    }

    @Override
    public void removeTable(String name) {
        if (name == null) {
            throw new IllegalArgumentException("failed to remove table: invalid name");
        }
        File tableDir = new File(dbDirectory, name);
        if (!tableDir.exists()) {
            throw new IllegalStateException("failed to remove table: table does not exist");
        }
        try {
            FileUtils.deleteRecursively(tableDir);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
