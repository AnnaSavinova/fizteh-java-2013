package ru.fizteh.fivt.students.anastasyev.filemap;

import ru.fizteh.fivt.students.anastasyev.shell.Command;

import java.io.IOException;

public class GetCommand implements Command<FileMapTable> {
    @Override
    public boolean exec(FileMapTable state, String[] command) {
        if (command.length != 2) {
            System.err.println("get: Usage - get key");
            return false;
        }
        try {
            FileMap db = null;
            try {
                db = state.getMyState(command[1].hashCode());
            } catch (IOException e) {
                if (e.getMessage().equals("no table")) {
                    System.out.println("no table");
                    return false;
                }
                System.err.println(e.getMessage());
                return false;
            }
            if (db == null) {
                System.out.println("not found");
                return true;
            }
            String str = db.get(command[1]);
            if (str.equals("not found")) {
                System.out.println("not found");

            } else {
                System.out.println("found");
                System.out.println(str);
            }
        } catch (Exception e) {
            System.err.println("get: Can't get the key");
            return false;
        }
        return true;
    }

    @Override
    public String commandName() {
        return "get";
    }
}
