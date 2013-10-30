package ru.fizteh.fivt.students.irinapodorozhnaya.shell;

import java.io.File;
import java.io.IOException;


public class CommandChangeDirectory extends AbstractCommand {
	CommandChangeDirectory (StateShell st){
		super(1, st);
	}
	
	public void execute(String[] args) throws IOException {	
		File f = getFileByName(args[1]);
		if (!f.isDirectory()){
			throw new IOException("cd: '" + args[1] + "' is not an exicting directory");
		} else {
			getState().setCurrentDir(f);
		}
	}
	
	public String getName() {
		return "cd";
	}
}
