package gui;

import loader.Loader;
import loader.LoaderFactory;
import loader.Request;
import org.apache.commons.logging.impl.SimpleLog;
import parser.ElibParser;
import util.Constants;
import util.CookieStore;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by root on 07.04.15.
 * Class Worker.
 * Simple controller, that run GUI, retrieving all available data and starting download
 */
public class Worker implements Constants {
    private SimpleLog logger;
    private final static String LOG = "WORKER";

    //View
    private GUIHolder holder;

    //Thread wich used for downloading and saving data
    private Thread workedThread;

    //instance of ElibParser
    private ElibParser elibParser;

    //default save path - current work dir
    private String savePath = System.getProperty("user.dir");

    //By default, save as PDF
    private int saveMode = LoaderFactory.PDF_LOADER;

    public Worker() {
        holder = new GUIHolder(this);
        logger = new SimpleLog(LOG);
        logger.info("running from: " + savePath);
        elibParser = new ElibParser();
    }

    /**
     * Starting download in another Thread
     * Download may be interrupt when progress dialog are close
     */
    public void startDownloading() {
        //Lol. Singleton.
        if (workedThread != null && !workedThread.isInterrupted() && workedThread.isAlive()) {
            return;
        }
        //Reference for Worker instance
        final Worker me = this;
        workedThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Getting cookie from local store
                String cookies = CookieStore.get();
                try {
                    //if we don't have it
                    if (cookies.isEmpty()) {
                        //try to login
                        cookies = doLogin();
                        //and save it in a store
                        CookieStore.save(cookies);
                    }
                    //get book main-page url
                    String url = holder.getUrl();
                    //get user save path
                    String path = holder.getSavedPath();
                    //initiate a page parsing
                    elibParser.doParse(url, cookies, me);
                    //getting a book title (may be a null)
                    String title = elibParser.getBookTitle();
                    //create loader for checked mode (pdf or simple images)
                    Loader loader = LoaderFactory.createLoader(me, path, saveMode, title);
                    showProgressBar();
                    int index = 0;
                    //while we have a valid pages and thread not interrupted, post progress into bar
                    while (loader.download(elibParser.getNext()) && !workedThread.isInterrupted()) {
                        postProgress(true, ++index, 0);
                    }
                    //when all data retrieving, save it
                    loader.save();
                } catch (Exception e) {
                    //show exception
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    handleError(e);
                }
            }
        });

        logger.info("Worker is interrupt: " + workedThread.isInterrupted());

        workedThread.start();
    }

    /**
     * Method invoke, when loading and saving successfully finished
     * Show success Dialog to user
     */
    public void complete() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                holder.showDialog(JOptionPane.DEFAULT_OPTION, "Ура!", "Загрузка успешно завершена!");
                holder.hideProgressBar();
            }
        });
    }

    /**
     * Method used for stop download work
     */
    public void interruptWork() {
        workedThread.interrupt();
    }

    /**
     * Check work status
     *
     * @return is interrupt?
     */
    public boolean isInterrupted() {
        return workedThread.isInterrupted();
    }

    /**
     * Make a login. Get a login form, by parsing login page
     * and post it to back-end with user login data. In next,
     * get a cookie.
     *
     * @return cookie that has been received after login
     * @throws IOException if something wrong
     */
    public String doLogin() throws IOException {
        logger.info("Oh, seems like we don't have a cookies :( ");

        //form container
        HashMap<String, String> formData = elibParser.parseLoginForm();

        //getting login data from gui
        String[] pair = holder.getLoginData();

        //and put it into container
        formData.put(LOGIN_FORM_NAME, pair[0]);
        formData.put(LOGIN_FORM_PASSWORD, pair[1]);

        //return cookie
        return Request.retrieveLoginCookies(formData);
    }

    /**
     * If Exception has been caught, show message in a dialog and work will stop
     *
     * @param e Exception that has been caught
     */
    public void handleError(Exception e) {
        final String message = e.getMessage();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                holder.showDialog(
                        JOptionPane.ERROR_MESSAGE,
                        "Ой...",
                        message
                );
                holder.hideProgressBar();
            }
        });
        //for debug, print it
        e.printStackTrace();
    }

    /**
     * Show GUI progress bar
     */
    public void showProgressBar() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                holder.showProgressBar();
                holder.postProgress(true, 0, 0);
            }
        });
    }

    /**
     * Post progress in a GUI progress bar
     *
     * @param indeterminate status of progress
     * @param current       index of current page or image
     * @param total         total pages count
     */
    public void postProgress(final boolean indeterminate, final int current, final int total) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                holder.postProgress(indeterminate, current, total);
            }
        });

    }

    /**
     * @return save path
     */
    public String getSavePath() {
        return savePath;
    }

    public void setSaveMode(int mode) {
        this.saveMode = mode;
    }

    /**
     * Method invoke after creating Worker
     * Start GUI
     */
    public void start() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                holder.createAndShowGui();
            }
        });
    }

}
