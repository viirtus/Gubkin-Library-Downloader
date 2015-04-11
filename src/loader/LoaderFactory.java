package loader;

import gui.Worker;

/**
 * Created by root on 06.04.15.
 */
public class LoaderFactory {
    public static final int IMAGE_LOADER = 0;
    public static final int PDF_LOADER = 1;

    /**
     * Factory pattern.
     * @param worker main thread worker
     * @param savePath path for saving data
     * @param what what loader required? Mast be LoaderFactory constants
     * @param title name of file for saving
     * @return instance of required loader
     */
    static public Loader createLoader(Worker worker, String savePath, int what, String title) {
        Loader loader = null;
        switch (what) {
            case IMAGE_LOADER:
                loader = new ImagesLoader(savePath, title, worker);
                break;
            case PDF_LOADER:
                loader = new PdfLoader(savePath, title, worker);
                break;
        }
        return loader;
    }
}
