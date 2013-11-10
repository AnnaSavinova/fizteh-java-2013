package ru.fizteh.fivt.students.vyatkina.database.commands;


import ru.fizteh.fivt.students.vyatkina.database.DatabaseCommand;
import ru.fizteh.fivt.students.vyatkina.database.DatabaseState;
import ru.fizteh.fivt.students.vyatkina.database.WrappedIOException;

import java.util.concurrent.ExecutionException;

public class CommitCommand extends DatabaseCommand {

    public CommitCommand (DatabaseState state) {
        super (state);
        this.name = "commit";
        this.argsCount = 0;

    }

    @Override
    public void execute (String[] args) throws ExecutionException {
        try {
            if (state.getTable () == null) {
                state.getIoStreams ().out.println ("no table");
            } else {
                state.getIoStreams ().out.println (state.getTable ().commit ());
            }
        }
        catch (WrappedIOException e) {
            throw new ExecutionException (e.fillInStackTrace ());
        }
    }

}
