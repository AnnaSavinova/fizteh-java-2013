package ru.fizteh.fivt.students.valentinbarishev.shell;

/**
 * Created with IntelliJ IDEA.
 * User: Valik
 * Date: 03.10.13
 * Time: 0:26
 * To change this template use File | Settings | File Templates.
 */

import java.io.IOException;
import java.util.ArrayList;



public class Shell {
    private ArrayList<ShellCommand> allCommands;

    public Shell() {
        allCommands = new ArrayList<ShellCommand>();
    }

    public void addCommand(ShellCommand command) {
        allCommands.add(command);
    }

    public void executeCommand(String[] command) {
        for (int i = 0; i < allCommands.size(); ++i) {
            if (allCommands.get(i).isMyCommand(command)) {
                allCommands.get(i).run();
                return;
            }
        }
        throw new InvalidCommandException(command[0]);
    }
}
