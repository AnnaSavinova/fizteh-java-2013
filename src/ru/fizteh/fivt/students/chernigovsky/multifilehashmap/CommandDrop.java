package ru.fizteh.fivt.students.chernigovsky.multifilehashmap;

import java.io.File;
import java.io.IOException;
import ru.fizteh.fivt.students.chernigovsky.filemap.Command;
import ru.fizteh.fivt.students.chernigovsky.filemap.ExitException;
import ru.fizteh.fivt.students.chernigovsky.filemap.State;


public class CommandDrop implements Command {
    public String getName() {
        return "drop";
    }
    public int getArgumentsCount() {
        return 1;
    }

    public void execute(State state, String[] args) throws IOException, ExitException {
        File table = new File(state.getDbDirectory(), args[1]);
        if (table.exists()) {
            if (table.equals(state.getCurrentTable())) {
                state.changeCurrentTable(null);
            }
            delete(table);
            System.out.println("dropped");

        } else {
            System.out.println(args[1] + " not exists");
        }
    }

    private void delete(File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            if (file.delete()) {
                return;
            } else {
                throw new IOException("Delete error");
            }
        }
        for (File f : file.listFiles()) {
            delete(f);
        }
        if (!file.delete()) {
            throw new IOException("Delete error");
        }
    }
}
