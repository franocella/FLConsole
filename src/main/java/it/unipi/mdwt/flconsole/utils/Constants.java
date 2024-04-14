package it.unipi.mdwt.flconsole.utils;

public class Constants {


    public static final String LOG_FILE = "applicationLog.txt";
    public static final String DIR = "logs";
    public static final int LOG_SIZE_LIMIT = 2048 * 1024; // 2MB max size per file
    public static final int LOG_FILE_COUNT = 5; // 5 files max
    public static final int PAGE_SIZE = 2;
    public static final String DIRECTOR_NODE_NAME = "director@10.2.1.125";
    public static final String DIRECTOR_MAILBOX = "mboxDirector";
    public static final String COOKIE = "cookie_123456789";
    public static final String PROJECT_PATH = System.getProperty("user.dir");

}
