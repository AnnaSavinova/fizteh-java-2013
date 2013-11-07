package ru.fizteh.fivt.students.irinaGoltsman.filemap;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.students.irinaGoltsman.shell.*;

public class DataBase {
    private static TableProvider currentTableProvider = null;
    private static Table currentTable = null;

    public DataBase(TableProvider curTableProvider) {
        currentTableProvider = curTableProvider;
    }

    public static boolean checkTableChosen() {
        if (currentTable == null) {
            System.out.println("no table");
            return false;
        }
        return true;
    }

    public static Code use(String[] args) {
        //TODO: тут нужно проверять, есть ли незакомиченные изменения и выводить сообщение об ошибки, если они есть
        if (currentTable != null) {
            realCommit();
        }
        String inputTableName = args[1];
        Table tmpTable = currentTableProvider.getTable(inputTableName);
        if (tmpTable == null) {
            System.out.println(inputTableName + " not exists");
        } else {
            System.out.println("using " + inputTableName);
            currentTable = tmpTable;
        }
        return Code.OK;
    }

    public static Code get(String[] args) {
        if (!checkTableChosen()) {
            return Code.ERROR;
        }
        String key = args[1];
        String value = currentTable.get(key);
        if (value != null) {
            System.out.println("found");
            System.out.println(value);
        } else {
            System.out.println("not found");
        }
        return Code.OK;
    }

    public static Code put(String[] args) {
        if (!checkTableChosen()) {
            return Code.ERROR;
        }
        String key = args[1];
        String value = args[2];
        String oldValue = currentTable.put(key, value);
        if (oldValue != null) {
            System.out.println("overwrite");
            System.out.println(oldValue);
        } else {
            System.out.println("new");
        }
        return Code.OK;
    }

    public static Code remove(String[] args) {
        if (!checkTableChosen()) {
            return Code.ERROR;
        }
        String key = args[1];
        String removedValue = currentTable.remove(key);
        if (removedValue != null) {
            System.out.println("removed");
            return Code.OK;
        } else {
            System.out.println("not found");
            return Code.ERROR;
        }
    }

    public static int realCommit() {
        if (currentTable == null) {
            return -1;
        }
        return currentTable.commit();
    }

    public static Code commit() {
        int numberOfRecordsWasChanged = realCommit();
        if (numberOfRecordsWasChanged == -1) {
            return Code.ERROR;
        }
        System.out.println(numberOfRecordsWasChanged);
        return Code.OK;
    }

    public static Code createTable(String[] args) {
        String nameTable = args[1];
        Table newTable = null;
        try {
            newTable = currentTableProvider.createTable(nameTable);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            return Code.ERROR;
        }
        if (newTable != null) {
            System.out.println("created");
            return Code.OK;
        } else {
            return Code.ERROR;
        }
    }

    public static Code removeTable(String[] args) {
        String nameTable = args[1];
        if (currentTable != null && currentTable.getName().equals(nameTable)) {
            currentTable = null;
        }
        try {
            currentTableProvider.removeTable(nameTable);
        } catch (IllegalStateException e) {
            return Code.ERROR;
        }
        System.out.println("dropped");
        return Code.OK;
    }

    public static Code closeDB() {
        if (realCommit() == -1) {
            return Code.ERROR;
        }
        return Code.OK;
    }

    public static Code size() {
        if (!checkTableChosen()) {
            return Code.ERROR;
        }
        System.out.println(currentTable.size());
        return Code.OK;
    }

    public static Code rollBack() {
        if (currentTable == null) {
            System.out.println(0);
            return Code.ERROR;
        }
        int countOfChangedKeys = currentTable.rollback();
        System.out.println(countOfChangedKeys);
        return Code.OK;
    }
}
