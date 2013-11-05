package ru.fizteh.fivt.students.belousova.multifilehashmap;

import ru.fizteh.fivt.students.belousova.utils.MultiFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiFileTable implements ChangesCountingTable {

    private Map<String, String> dataBase = new HashMap<String, String>();
    private Map<String, String> addedKeys = new HashMap<String, String>();
    private Set<String> deletedKeys = new HashSet<String>();
    private int changesCounter = 0;
    //private int rollbackChangesCounter = 0;

    private File dataDirectory;

    public MultiFileTable(File data) {
        try {
            dataDirectory = data;
            MultiFileUtils.read(dataDirectory, dataBase);
        } catch (IOException e) {
            throw new IllegalArgumentException("read error", e);
        }
    }

    @Override
    public String getName() {
        return dataDirectory.getName();
    }

    @Override
    public String get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("null key");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("empty key");
        }

        if (addedKeys.containsKey(key)) {
            return addedKeys.get(key);
        }
        if (deletedKeys.contains(key)) {
            return null;
        }
        return dataBase.get(key);
    }

    @Override
    public String put(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("null key");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("empty key");
        }
        if (value == null) {
            throw new IllegalArgumentException("null value");
        }
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("empty value");
        }
        //rollbackChangesCounter++;
        if (dataBase.containsKey(key) && !deletedKeys.contains(key)) {
            deletedKeys.add(key);
            String oldValue = dataBase.get(key);
            addedKeys.put(key, value);
            changesCounter++;
            return oldValue;
        }
        if (!addedKeys.containsKey(key) && !deletedKeys.contains(key)) {
            changesCounter++;
        }
        return addedKeys.put(key, value);
    }

    @Override
    public String remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("null key");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("empty key");
        }

        if (dataBase.containsKey(key) && !deletedKeys.contains(key)) {
            //rollbackChangesCounter++;
            deletedKeys.add(key);
            changesCounter++;
            return dataBase.get(key);
        }
        if (addedKeys.containsKey(key)) {
            //rollbackChangesCounter++;
            changesCounter--;
            return addedKeys.remove(key);
        }
        return null;
    }

    @Override
    public int size() {
        return dataBase.size() + addedKeys.size() - deletedKeys.size();
    }

    @Override
    public int commit() {
        countChanges();
        int counter = changesCounter;
        for (String key : deletedKeys) {
            dataBase.remove(key);
        }
        dataBase.putAll(addedKeys);
        deletedKeys.clear();
        addedKeys.clear();
        changesCounter = 0;
        try {
            MultiFileUtils.write(dataDirectory, dataBase);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return counter;
    }

    private void countChanges() {
        changesCounter = addedKeys.size() + deletedKeys.size();
        for (String key : addedKeys.keySet()) {
            if (deletedKeys.contains(key)) {
                changesCounter--;
                if (dataBase.get(key).equals(addedKeys.get(key))) {
                    changesCounter--;
                }
            }
        }
    }

    @Override
    public int rollback() {
        countChanges();
        int counter = changesCounter;
        deletedKeys.clear();
        addedKeys.clear();
        changesCounter = 0;
        return counter;
    }

    @Override
    public int getChangesCount() {
        countChanges();
        return changesCounter;
    }
}
