package ru.fizteh.fivt.students.vyatkina.database.commands;

import ru.fizteh.fivt.students.vyatkina.database.DatabaseCommand;
import ru.fizteh.fivt.students.vyatkina.database.DatabaseState;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ExitDatabaseCommand extends DatabaseCommand {

    public ExitDatabaseCommand (DatabaseState state) {
        super (state);
        this.name = "exit";
        this.argsCount = 0;
    }

    @Override
    public void execute (String[] args) throws ExecutionException {
        try {
            if (state.getTable () == null) {
                System.exit (0);
            } else {
                state.getTable ().commit ();
            }
        }
        catch (IllegalArgumentException e) {
            throw new ExecutionException (e.fillInStackTrace ());
        }
        System.exit (0);
    }
}
