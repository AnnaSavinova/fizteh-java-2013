package ru.fizteh.fivt.students.fedoseev.multifilehashmap;

import ru.fizteh.fivt.students.fedoseev.common.AbstractCommand;
import ru.fizteh.fivt.students.fedoseev.common.AbstractFrame;
import ru.fizteh.fivt.students.fedoseev.filemap.AbstractFileMap;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class AbstractMultiFileHashMap extends AbstractFrame<MultiFileHashMapState> {
    private static final int MAX_TABLE_SIZE = 4 * 1024 * 1024;
    private static final int MAX_FILE_SIZE = 1024 * 1024;
    private static final int DIR_COUNT = 16;
    private static final int DIR_FILES_COUNT = 16;

    public AbstractMultiFileHashMap(File dir) {
        state = new MultiFileHashMapState(dir);

        File newDir = state.getCurDir();

        if (!newDir.exists()) {
            newDir.mkdirs();
        }

        if (newDir.listFiles() != null) {
            for (File file : newDir.listFiles()) {
                state.createTable(file.getName());
            }
        }
    }

    @Override
    public Map<String, AbstractCommand> getCommands() {
        final MultiFileHashMapCreateCommand CREATE = new MultiFileHashMapCreateCommand();
        final MultiFileHashMapDropCommand DROP = new MultiFileHashMapDropCommand();
        final MultiFileHashMapUseCommand USE = new MultiFileHashMapUseCommand();
        final MultiFileHashMapPutCommand PUT = new MultiFileHashMapPutCommand();
        final MultiFileHashMapGetCommand GET = new MultiFileHashMapGetCommand();
        final MultiFileHashMapRemoveCommand REMOVE = new MultiFileHashMapRemoveCommand();
        final MultiFileHashMapExitCommand EXIT = new MultiFileHashMapExitCommand();

        return new HashMap<String, AbstractCommand>() {{
            put(CREATE.getCmdName(), CREATE);
            put(DROP.getCmdName(), DROP);
            put(USE.getCmdName(), USE);
            put(PUT.getCmdName(), PUT);
            put(GET.getCmdName(), GET);
            put(REMOVE.getCmdName(), REMOVE);
            put(EXIT.getCmdName(), EXIT);
        }};
    }

    public static void readTable(MultiFileHashMapTable table) throws IOException {
        File curDir = new File(table.getName());

        if (curDir.listFiles() != null) {
            for (File dir : curDir.listFiles()) {
                if (dir.listFiles() != null) {
                    for (File file : dir.listFiles()) {
                        RandomAccessFile raf = new RandomAccessFile(file, "r");

                        checkOpenFile(table, raf);
                    }
                }
            }
        }
    }

    public static void checkOpenFile(MultiFileHashMapTable curTable, RandomAccessFile curFile) throws IOException {
        curTable.setTableSize(curTable.getTableSize() + curFile.length());

        if (curFile.length() > MAX_FILE_SIZE) {
            curFile.close();
            throw new IOException("ERROR: too big file");
        }
        if (curTable.getTableSize() > MAX_TABLE_SIZE) {
            commitTable(curTable);
            curTable.getMapContent().clear();
        }

        Map<String, String> map = AbstractFileMap.readFile(curFile);
        curTable.putMapTable(map);

        curFile.close();
    }

    public static void commitTable(MultiFileHashMapTable curTable) throws IOException {
        if (curTable == null) {
            return;
        }

        curTable.setTableSize(0);

        File[] directories = new File[16];
        Map<Integer, File> files = new HashMap<Integer, File>();
        Map<Integer, RandomAccessFile> RAFiles = new HashMap<Integer, RandomAccessFile>();
        Set<String> keySet = curTable.getMapContent().keySet();
        boolean[] usedDirs = new boolean[16];
        boolean[] usedFiles = new boolean[16];

        for (String key : keySet) {
            usedDirs[curTable.dirHash(key)] = true;
        }

        for (int i = 0; i < DIR_COUNT; i++) {
            directories[i] = curTable.getCurFile().toPath().resolve(i + ".dir").toFile();

            if (!directories[i].exists() && usedDirs[i]) {
                directories[i].mkdirs();
            }

            for (int j = 0; j < DIR_FILES_COUNT; j++) {
                usedFiles[j] = false;
            }

            for (String key : keySet) {
                usedFiles[curTable.fileHash(key)] = true;
            }

            for (int j = 0; j < DIR_FILES_COUNT; j++) {
                if (usedFiles[j] && usedDirs[i]) {
                    File curFile = new File(
                            curTable.getCurFile().toPath().resolve(i + ".dir").resolve(j + ".dat").toFile().toString()
                    );

                    int numb = 16 * i + j;
                    files.put(numb, curFile);

                    if (!files.get(numb).exists()) {
                        files.get(numb).createNewFile();
                    }

                    RandomAccessFile curRAFile = new RandomAccessFile(files.get(numb), "rw");

                    RAFiles.put(numb, curRAFile);
                }
            }
        }

        Set<String> curFileKeySet = new HashSet<String>();
        Set<Integer> usedKeys = new HashSet<Integer>();

        for (String key : keySet) {
            if (!usedKeys.contains(curTable.keyHashFunction(key))) {
                usedKeys.add(curTable.keyHashFunction(key));

                curFileKeySet.clear();

                RandomAccessFile raf = RAFiles.get(curTable.keyHashFunction(key));

                for (String curFileKey : keySet) {
                    if (curTable.keyHashFunction(curFileKey) ==
                            curTable.keyHashFunction(key)) {
                        curFileKeySet.add(curFileKey);
                    }
                }

                AbstractFileMap.commitFile(raf, curFileKeySet, curTable.getMapContent());
            }
        }

        for (int i : RAFiles.keySet()) {
            RAFiles.get(i).close();
        }

        for (int i : files.keySet()) {
            if (files.get(i).length() == 0) {
                files.get(i).delete();
            }
        }

        for (File dir : directories) {
            if (dir.exists()) {
                if (dir.listFiles().length == 0) {
                    dir.delete();
                }
            }
        }
    }
}
