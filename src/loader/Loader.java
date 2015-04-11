package loader;

import gui.Worker;
import org.apache.pdfbox.exceptions.COSVisitorException;

import java.io.IOException;

/**
 * Created by Андрей on 05.04.2015.
 *
 */
public abstract class Loader {
    protected String savePath;
    protected String title;
    protected Worker worker;
    protected int totalCount = 0;

    /**
     * Default constructor.
     * @param savePath path for saving data
     * @param title of the book
     * @param worker thread worker
     */
    protected Loader(String savePath, String title, Worker worker) {
        this.savePath = savePath;
        this.title = title;
        this.worker = worker;
    }

    /**
     * Make a download ONLY ONE image
     * @param url image location
     * @return status
     */
    public abstract boolean download(String url);
    public abstract boolean save() throws IOException, COSVisitorException;
}
