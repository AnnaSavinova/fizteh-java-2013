package ru.fizteh.fivt.students.vlmazlov.filemap;

import ru.fizteh.fivt.students.vlmazlov.shell.QuietCloser;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.io.RandomAccessFile;

public class DataBaseWriter {
	private RandomAccessFile dataBaseStorage;
	private final FileMap fileMap;

	public DataBaseWriter(String directory, String file, FileMap fileMap) 
	throws FileNotFoundException {
		File dir = new File(directory);
		if (!dir.exists()) {
			throw new FileNotFoundException("Specified directory doesn't exist");
		}

		dataBaseStorage = new RandomAccessFile(new File(dir, file), "rw");
		this.fileMap = fileMap;
	}

	private int countFirstOffSet() throws IOException {
		int curOffset = 0;

		Iterator<Map.Entry<String, String>> it = fileMap.getEntriesIterator();

		while (it.hasNext()) {
			curOffset += it.next().getKey().getBytes("UTF-8").length + 1 + 4;
		}

		return curOffset;

	}

	private void storeKey(String key, int offSet) throws IOException {
		dataBaseStorage.write(key.getBytes("UTF-8"));
		dataBaseStorage.writeByte('\0');
		dataBaseStorage.writeInt(offSet);
	}

	public void storeInFile() throws IOException {	
		
		try {

			Iterator<Map.Entry<String, String>> it = fileMap.getEntriesIterator();

			long curOffset = countFirstOffSet(), writePosition;

			while (it.hasNext()) {
				Map.Entry<String, String> entry = it.next();
				if (entry.getValue() == null) {
					continue;
				}

				storeKey(entry.getKey(), (int)curOffset);
				writePosition = dataBaseStorage.getFilePointer();

				dataBaseStorage.seek(curOffset);
				dataBaseStorage.write(entry.getValue().getBytes("UTF-8"));
				curOffset = dataBaseStorage.getFilePointer();
				
				dataBaseStorage.seek(writePosition);
			}

			dataBaseStorage.getChannel().truncate(curOffset);
		} finally {
			QuietCloser.closeQuietly(dataBaseStorage);
		}
	}
}