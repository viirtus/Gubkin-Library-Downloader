package parser;

import gui.Worker;
import loader.Request;
import org.apache.commons.logging.impl.SimpleLog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Андрей on 04.04.2015.
 */
public class ElibParser implements Constants {
    private final static String LOG = "PARSER";

    private String linkTemplate;
    private String linkPlaceHolder;

    private String bookTitle;
    private int index = 0;
    private SimpleLog logger;

    public ElibParser() {
        logger = new SimpleLog(LOG);
    }

    /**
     * Firstly, search for iframe on lookup page, and get a viewer application location.
     * Next, we try to get a link template.
     * @param url page for lookup. Main page with viewer iframe.
     * @param loginCookie cookie with an active opened session on server
     * @param worker main thread worker
     * @throws IOException if url are broken
     */
    public void doParse (String url, String loginCookie, Worker worker) throws IOException {
        //getting viewer location from iframe
        String viewerSrc = retrieveIframeSrc(url, loginCookie);
        //getting a viewer html-code which contain templates
        String viewerHtml = Request.getPage(viewerSrc, loginCookie);
        try {
            //try to get a templates
            initiateLinkTemplate(viewerHtml);
            logger.info("Ok, start download");
        } catch (RuntimeException e) {
            //if cookie not valid or in case of invalid url
            logger.fatal("SHIT! Cannot to parse a viewer page. Maybe cookies is missing? Or url is broken.");
            worker.doLogin();
        }
    }


    public String getNext() {
        return this.linkTemplate.replaceAll(this.linkPlaceHolder, Integer.toString(++index));
    }

    public int total() {
        return index;
    }

    /**
     *
     * A typical code contain a script which generate <img> element from template:
     *
     * br.getPageURI = function (index, reduce, rotate) {
     *    var tpl = 'bv-19624-%d.jpg';
     *    var url = '/sites/default/files/bookview/19624/' + tpl.replace(/%d/, index + 1);
     *    return url;
     *}
     *
     * We just try to parse it with numbers of RegExp
     *
     * @param html of viewer page
     */
    private void initiateLinkTemplate(String html) {
        Pattern templatePattern = Pattern.compile(".*var tpl = '([^']*?)'");
        Matcher matcher = templatePattern.matcher(html);
        String template = "";
        String url = "";
        String placeHolder = "";
        while(matcher.find()) {
            template = matcher.group(1);
        }
        Pattern urlPattern = Pattern.compile(".*var url = '([^']*?)'");
        matcher = urlPattern.matcher(html);
        while(matcher.find()) {

            url = matcher.group(1);
        }
        Pattern placeHolderPattern = Pattern.compile(".*\\.replace\\(/(.*)/, ");
        matcher = placeHolderPattern.matcher(html);
        while(matcher.find()) {
            placeHolder = matcher.group(1);
        }

        if (url.isEmpty()) throw new RuntimeException("Parse link error");

        logger.info("Now, we steal an image url: " + url);
        logger.info("and an image template: " + template);

        this.linkPlaceHolder = placeHolder;
        this.linkTemplate = Constants.LIBRARY_DOMAIN + url + template;

        logger.info("aaand a template link is: " + this.linkTemplate);
    }

    /**
     * Getting a viewer location from iframe on main book page
     * @param url which contain main book page
     * @param loginCookie cookie for getting a page
     * @return full viewer url
     * @throws IOException
     */
    private String retrieveIframeSrc(String url, String loginCookie) throws IOException {
        String html = Request.getPage(url, loginCookie);
        Document document = Jsoup.parse(html);
        Elements frame = document.select(".bookview");
        String bookStoreUrl = "";
        for (Element e: frame) {
            bookStoreUrl = e.attr("src");
        }

        logger.info("Viewer location is on " + Constants.LIBRARY_DOMAIN + bookStoreUrl);
        //Also, get a title of a book
        Element title = document.select("title").get(0);
        //Example: "Расчет абсолютной энтропии... | что-то еще | еще что-то"
        bookTitle = title.text().split("\\|")[0];
        if (bookTitle.length() > 255) {
            bookTitle = bookTitle.substring(0, 250);
        }
        return Constants.LIBRARY_DOMAIN + bookStoreUrl;
    }

    /**
     * Attempt to parse login form, from library login page.
     * There's a numbers of hidden inputs that contain a required keys
     * @return container with an all form inputs
     * @throws IOException
     */
    public HashMap<String, String> parseLoginForm() throws IOException {
        String html = Request.getPage(Constants.LOGIN_URL, "");
        Document document = Jsoup.parse(html);
        Elements form = document.select("form");
        Elements inputs = form.select("input");
        HashMap<String, String> params = new HashMap<>();
        for(Element e : inputs) {
            params.put(e.attr("name"), e.attr("value"));
        }

        logger.info("Successfully form parse");
        return params;
    }

    public String getBookTitle() {
        return bookTitle;
    }
}
