package ru.fizteh.fivt.students.adanilyak.tools;

import ru.fizteh.fivt.students.adanilyak.commands.Cmd;

import java.io.IOException;
import java.util.*;

/**
 * User: Alexander
 * Date: 20.10.13
 * Time: 22:38
 */
public class CmdParseAndExecute {
    public static List<String> intoCommandsAndArgs(String cmd, String delimetr) {
        cmd.trim();
        String[] tokens = cmd.split(delimetr);
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < tokens.length; i++) {
            if (!tokens[i].equals("") && !tokens[i].matches("\\s+")) {
                result.add(tokens[i]);
            }
        }
        return result;
    }

    public static void execute(String cmdWithArgs, Map<String, Cmd> cmdList) throws IOException {
        List<String> cmdAndArgs = intoCommandsAndArgs(cmdWithArgs, " ");
        try {
            String commandName = cmdAndArgs.get(0);
            if (!cmdList.containsKey(commandName)) {
                throw new NoSuchElementException("Unknown command");
            }

            Cmd command = cmdList.get(commandName);
            if (cmdAndArgs.size() != command.getAmArgs() + 1) {
                throw new IOException("Wrong amount of arguments");
            }

            command.work(cmdAndArgs);
        } catch (Exception exc) {
            System.err.println(cmdAndArgs + ": " + exc.getMessage());
        }
    }
}
