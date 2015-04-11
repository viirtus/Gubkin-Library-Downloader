package loader;

import gui.Worker;
import org.apache.commons.logging.impl.SimpleLog;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by root on 07.04.15.
 */
public class PdfLoader extends Loader {
    private final static String LOG = "PDF_LOADER";
    private SimpleLog logger;

    //List of available streams
    ArrayList<InputStream> streams = new ArrayList<>();

    protected PdfLoader(String savePath, String title, Worker worker) {
        super(savePath, title, worker);
        logger = new SimpleLog(LOG);
    }
    /**
     * Open all available stream
     * and store them in a list
     * @param url_ of current image for processing
     * @return status of work. If false, then stop opening channel (previous image was last)
     */
    @Override
    public boolean download(String url_) {
        try {
            URL url = new URL(url_);
            streams.add(url.openStream());
            logger.info("Downloading an image: " + url_);
            ++totalCount;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Saving all stream into a pdf document
     * @return status. If false, work interrupted
     * @throws IOException file not found, or other
     * @throws COSVisitorException pdf creating exception
     */
    @Override
    public boolean save() throws IOException, COSVisitorException {
        // Create a new empty document
        PDDocument document = new PDDocument();
        int index = 0;
        //Iterate through all stream
        for (InputStream inStream : streams) {
            if (!worker.isInterrupted()) {
                PDPage blankPage = new PDPage(PDPage.PAGE_SIZE_A4);
                document.addPage(blankPage);

                PDJpeg img = new PDJpeg(document, inStream);
                PDPageContentStream stream = new PDPageContentStream(document, blankPage);

                //MAGIC! Constants from PDPage.PAGE_SIZE_A4
                stream.drawXObject(img, 0, 0, 595.27563F, 841.8898F);

                stream.close();
                inStream.close();
                worker.postProgress(false, index, totalCount - 1);
                logger.info("Put a " + index++ + " image into pdf");
            } else {
                document.close();
                return false;
            }
        }
        //write file on disk
        document.save(savePath + File.separator + title + ".pdf");
        document.close();
        //complete task
        worker.complete();
        return true;
    }
}
