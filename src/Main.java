
import gui.GUIHolder;
import gui.Worker;
import loader.Loader;
import loader.LoaderFactory;
import loader.Request;
import org.apache.commons.logging.impl.SimpleLog;
import org.apache.pdfbox.exceptions.COSVisitorException;
import parser.ElibParser;
import util.Constants;
import util.CookieStore;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by root on 03.04.2015.
 * Entry point of programme
 */
public class Main{

    public static void main(String[] args) {
        //Create a worker
        Worker worker = new Worker();
        //and start work
        worker.start();

    }

}
