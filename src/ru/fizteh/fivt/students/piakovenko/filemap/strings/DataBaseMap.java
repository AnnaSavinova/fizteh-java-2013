package ru.fizteh.fivt.students.piakovenko.filemap.strings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Pavel
 * Date: 12.10.13
 * Time: 23:25
 * To change this template use File | Settings | File Templates.
 */
public class DataBaseMap {
    private Map<String, String> map = new HashMap<String, String>(15);
    private Map<String, String> changedMap = new HashMap<String, String>(15);
    private Map<String, String> overwriteMap = new HashMap<String, String>(15);
    private Map<String, String> removedMap = new HashMap<String, String>(15);

    public String put (String key, String value) {
        String oldValue = null;
        if (!map.containsKey(key)) {
            map.put(key, value);
            System.out.println("new");
        } else {
            System.out.println("overwrite");
            oldValue = map.get(key);
            System.out.println(oldValue);
            map.remove(key);
            map.put(key, value);
        }
        return oldValue;
    }

    public String get(String key) {
        if (!map.containsKey(key)) {
            System.out.println("not found");
        } else {
            System.out.println("found");
            System.out.println(map.get(key));
            return map.get(key);
        }
        return null;
    }

    public String remove (String key) {
        if (!map.containsKey(key)) {
            System.out.println("not found");
        } else {
            String returnValue = map.get(key);
            map.remove(key);
            System.out.println("removed");
            return returnValue;
        }
        return null;
    }

    public void primaryPut (String key, String value) {
            map.put(key, value);
    }

    public Map<String, String> getMap () {
        return map;
    }

    public Map<String, String> getChangedMap() {
        return changedMap;
    }

    public Map<String, String> getOverwriteMap() {
        return overwriteMap;
    }

    public Map<String, String> getRemovedMap() {
        return removedMap;
    }

}
