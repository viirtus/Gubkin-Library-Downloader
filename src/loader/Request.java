package loader;

import util.Constants;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Андрей on 05.04.2015.
 */
public class Request implements Constants {
    /**
     * Method just get a received cookie from login page
     * @param params that will be posted with a request
     * @return received cookies
     * @throws IOException
     */
    public static String retrieveLoginCookies(HashMap<String, String> params) throws IOException {
        String url = Constants.LOGIN_URL;
        HttpURLConnection con = null;
        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();
        } catch (UnknownHostException e) {
            //try to fix broken url
            con = (HttpURLConnection) new URL("http://" + url).openConnection();
        }

        //add request header
        con.setRequestMethod("POST");
        //just for fun
        con.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4,de;q=0.2");

        String urlParameters = "";

        for (Map.Entry<String, String> e : params.entrySet()) {
            urlParameters += e.getKey() + "=" + e.getValue() + "&";
        }

        //send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();


        // temporary to build request cookie header
        String sessionCookie = "";
        // find the cookies in the response header from the first request
        List<String> cookies = con.getHeaderFields().get("Set-Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                // only want the first part of the cookie header that has the value
                String value = cookie.split(";")[0];
                sessionCookie += value + ";";
            }
        }
        return sessionCookie;
    }

    /**
     * Downloading a web-page with cookies
     * @param path web-location
     * @param cookie
     * @return full server output
     * @throws IOException
     */
    public static String getPage(String path, String cookie) throws IOException {
        HttpURLConnection con = null;
        try {
            URL obj = new URL(path);
            con = (HttpURLConnection) obj.openConnection();
        } catch (UnknownHostException e) {
            //try to fix broken url
            con = (HttpURLConnection) new URL("http://" + path).openConnection();
        }
        //setting cookie
        con.setRequestProperty("Cookie", cookie);
        con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        con.connect();

        //getting output
        InputStream stream = con.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder respond = new StringBuilder();

        for (String line; (line = reader.readLine()) != null; ) {
            respond.append(line);
        }
        return respond.toString();
    }
}
