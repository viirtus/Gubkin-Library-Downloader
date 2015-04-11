package loader;


import gui.Worker;
import org.apache.commons.logging.impl.SimpleLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

/**
 * Created by Андрей on 05.04.2015.
 */
public class ImagesLoader extends Loader  {
    private final static String LOG = "OLD_SCHOOL_LOADER";
    private SimpleLog logger;

    //List of opened chanel for next downloading
    protected ArrayList<ReadableByteChannel> channels = new ArrayList<>();

    //parent constructor
    protected ImagesLoader(String savePath, String title, Worker worker) {
        super(savePath, title, worker);
        logger = new SimpleLog(LOG);
    }

    /**
     * Open all available channel with data stream
     * and store them in a list
     * @param url of current image for processing
     * @return status of work. If false, then stop opening channel (previous image was last)
     */
    @Override
    public boolean download(String url) {
        try {
            URL website = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            channels.add(rbc);
            ++totalCount;
            logger.info("Downloading an image: " + url);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Save all data on a drive.
     * Iterate through available channels and transfer it into file stream
     * @return state. False, if work interrupt
     * @throws IOException if save path not exist or other
     */
    @Override
    public boolean save() throws IOException {
        int i = 1;
        for (ReadableByteChannel channel : channels) {
            if (!worker.isInterrupted()) {
                FileOutputStream fos = new FileOutputStream(savePath + File.separator + title + "_" + (i < 10 ? ("0" + i) : i) + ".jpg");
                fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
                fos.flush();
                fos.close();
                worker.postProgress(false, i, totalCount - 1);
                logger.info("Save a " + i++ + " image on a drive");
            } else {
                return false;
            }
        }
        //
        worker.complete();
        return true;
    }
}
