package ru.fizteh.fivt.students.kochetovnicolai.fileMap;

import ru.fizteh.fivt.storage.strings.TableProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class DistributedTableProvider implements TableProvider {

    HashMap<String, DistributedTable> tables;
    HashMap<String, TableMember> tableMembers;
    File currentPath;

    protected void checkTableDirectory(String name) {
        File tableDirectory = new File(currentPath.getPath() + File.separator + name);
        if (!tableDirectory.exists() || !tableDirectory.isDirectory()) {
            if (tables.containsKey(name)) {
                tables.remove(name);
            }
        }
    }

    public static boolean isValidName(String name) {
        return name != null && !name.equals("") && !name.contains(".") && !name.contains("/") && !name.contains("\\");
        //&& !name.matches(".*[.\\\\/].*");
    }

    protected void loadTable(String name) {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("invalid table name");
        }
        checkTableDirectory(name);
        try {
            if (!tables.containsKey(name) && new File(currentPath + File.separator + name).exists()) {
                tables.put(name, new DistributedTable(currentPath, name));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public boolean existsTable(String name) {
        if (tables.containsKey(name)) {
            return true;
        }
        loadTable(name);
        checkTableDirectory(name);
        return new File(currentPath + File.separator + name).exists();
    }

    public DistributedTableProvider(File workingDirectory) throws IllegalArgumentException {
        currentPath = workingDirectory;
        if (currentPath == null || (currentPath.exists() && !currentPath.isDirectory())
                || (!currentPath.exists() && !currentPath.mkdir())) {
            throw new IllegalArgumentException("couldn't create working directory");
        }
        tables = new HashMap<>();
        tableMembers = new HashMap<>();
    }

    @Override
    public TableMember getTable(String name) throws IllegalArgumentException {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("invalid table name");
        }
        //loadTable(name);
        if (tables.containsKey(name)) {
            /**/
            if (!tableMembers.containsKey(name)) {
                tableMembers.put(name, new TableMember(tables.get(name), this));
            }
            return tableMembers.get(name);
            //return new TableMember(tables.get(name), this);
        } else {
            return null;
        }
    }

    @Override
    public TableMember createTable(String name) throws IllegalArgumentException {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("invalid table name");
        }
        if (tables.containsKey(name)) {
            return null;
        }
        loadTable(name);
        if (!tables.containsKey(name)) {
            try {
                DistributedTable table = new DistributedTable(currentPath, name);
                tables.put(name, table);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
        /**/
        if (!tableMembers.containsKey(name)) {
            tableMembers.put(name, new TableMember(tables.get(name), this));
        }
        return tableMembers.get(name);
        //return new TableMember(tables.get(name), this);
    }

    @Override
    public void removeTable(String name) throws IllegalStateException, IllegalArgumentException {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("invalid argument");
        }
        if (!existsTable(name)) {
            throw new IllegalStateException("table is not exists");
        }
        loadTable(name);
        try {
            tables.get(name).clear();
            File dir = new File(currentPath.getPath() + File.separator + name);
            if (!dir.delete()) {
                throw new IOException(dir.getPath() + ": couldn't delete directory");
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
        tables.remove(name);
        /***/
        if (tableMembers.containsKey(name)) {
            tableMembers.remove(name);
        }
        //
    }
}
