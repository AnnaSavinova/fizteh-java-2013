package ru.fizteh.fivt.students.ermolenko.multifilehashmap;

import ru.fizteh.fivt.students.ermolenko.shell.Command;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CmdUse implements Command<MultiFileHashMapState> {

    @Override
    public String getName() {

        return "use";
    }

    @Override
    public void executeCmd(MultiFileHashMapState inState, String[] args) throws IOException {

        //если мы работали с одной таблицей, а теперь переключились на другую
        //нужно сохранить изменения
        //или таблица не была выбрана
        if (inState.getCurrentTable() != null) {
            if (inState.getCurrentTable().getName() != args[0]) {
                File fileForWrite = ((MultiFileHashMapTable) inState.getCurrentTable()).getDataFile();
                Map<String, String> mapForWrite = ((MultiFileHashMapTable) inState.getCurrentTable()).getDataBase();
                MultiFileHashMapUtils.write(fileForWrite, mapForWrite);
            } else {
                return;
            }
        }


        if (inState.getTable(args[0]) == null) {
            System.out.println(args[0] + " not exists");
            return;
        }

        Map<String, String> tmpDataBase = ((MultiFileHashMapTable) inState.getTable(args[0])).getDataBase();
        File tmpDataFile = ((MultiFileHashMapTable) inState.getTable(args[0])).getDataFile();
        MultiFileHashMapUtils.read(tmpDataFile, tmpDataBase);
        inState.setCurrentTable(args[0]);

        inState.changeCurrentTable(tmpDataBase, tmpDataFile);

        System.out.println("using " + args[0]);
    }
}
