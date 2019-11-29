package uk.cam.lib.cdl.loading.apis;


import org.json.JSONObject;
import uk.cam.lib.cdl.loading.model.WebResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Class for help with web requests
 * Package-private
 */
class WebHelper {

    public WebResponse requestPOSTJSON(URL url, JSONObject json) {
        return requestPOSTJSON(url, json, null, null);
    }

    public WebResponse requestPOSTJSON(URL url, JSONObject json, String username, String password) {

        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            if (username != null && password != null) {
                String userpass = username + ":" + password;
                String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
                con.setRequestProperty("Authorization", basicAuth);
            }

            try (OutputStream os = con.getOutputStream()) {
                OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                writer.write(json.toString());
                writer.flush();
                os.flush();
            }

            String response = getContent(con);
            int code = con.getResponseCode();

            WebResponse output = new WebResponse(code, response);
            if (code > 299) {
                System.err.println("Error getting content. ");
                System.err.println(con.getResponseMessage());
                System.err.println(response);
                return output;
            } else {
                return output;
            }

        } catch (IOException e) {
            System.err.println("Problem connecting to the API");
            e.printStackTrace();
        } finally {
            try {
                assert con != null;
                con.disconnect();
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    public WebResponse requestGET(URL url) {
        return requestGET(url, "text/plain; charset=\"utf-8\"", null, null);
    }

    public WebResponse requestGET(URL url, String mimeType) {
        return requestGET(url, mimeType, null, null);
    }

    public WebResponse requestGET(URL url, String mimeType, String username, String password) {

        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", mimeType);

            if (username != null && password != null) {
                String userpass = username + ":" + password;
                String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
                con.setRequestProperty("Authorization", basicAuth);
            }

            String response = getContent(con);
            int code = con.getResponseCode();

            if (code > 299) {
                System.err.println("Error getting content. ");
                System.err.println(response);
                return null;
            } else {
                return new WebResponse(code, response);
            }

        } catch (IOException e) {
            System.err.println("Problem connecting to the API");
            e.printStackTrace();
        } finally {
            try {
                assert con != null;
                con.disconnect();
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String getContent(HttpURLConnection con) throws IOException {
        Reader streamReader;

        streamReader = new InputStreamReader(con.getInputStream());
        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();

        return content.toString();
    }
}
