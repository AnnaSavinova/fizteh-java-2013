package ru.fizteh.fivt.students.adanilyak.modernfilemap;

import ru.fizteh.fivt.students.adanilyak.userinterface.GenericShell;

/**
 * User: Alexander
 * Date: 21.10.13
 * Time: 1:02
 */
public class Main {
    public static void main(String[] args) {
        GenericShell myShell = new FileMapShell(args, "db.dat");
    }
}
