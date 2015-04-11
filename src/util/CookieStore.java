package util;

import loader.Request;
import parser.ElibParser;

import java.util.prefs.Preferences;

/**
 * Created by Андрей on 05.04.2015.
 */
public class CookieStore {
    static Preferences prefs = Preferences.userNodeForPackage(util.CookieStore.class);
    static final String PREF_NAME = "elib_cookie";
    public static String get() {
        return prefs.get(PREF_NAME, "");
    }
    public static void save(String cookie) {
        System.out.println("All right, cookie now in safety. We did it!");
        prefs.put(PREF_NAME, cookie);
    }
}
