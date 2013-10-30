package ru.fizteh.fivt.students.vlmazlov.filemap;

import ru.fizteh.fivt.students.vlmazlov.shell.Command;
import ru.fizteh.fivt.students.vlmazlov.shell.CommandFailException;
import ru.fizteh.fivt.students.vlmazlov.shell.UserInterruptionException;
import ru.fizteh.fivt.students.vlmazlov.shell.Shell;
import java.io.OutputStream;

abstract public class FileMapCommand implements Command {
	private final String name;
	private final int argNum;
	protected FileMap fileMap;
	protected final static String separator = System.getProperty("line.separator");

	public String getName() {
		return name;
	}

	public int getArgNum() {
		return argNum;
	}

	FileMapCommand(String name, int argNum, FileMap fileMap) {
		this.name = name;
		this.argNum = argNum;
		this.fileMap = fileMap;
	}

	abstract public void execute(String[] args, Shell.ShellState state, OutputStream out) throws CommandFailException, UserInterruptionException;
}