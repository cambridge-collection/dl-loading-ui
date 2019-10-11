package uk.cam.lib.cdl.loading.apis;


import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public abstract class WebAPI {

    boolean requestPOSTJSON(URL url, JSONObject json) {
        return requestPOSTJSON(url, json, null, null);
    }

    boolean requestPOSTJSON(URL url, JSONObject json, String username, String password) {

        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = json.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            if (username != null && password != null) {
                String userpass = username + ":" + password;
                String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
                con.setRequestProperty("Authorization", basicAuth);
            }

            Integer code = con.getResponseCode();
            String response = getContent(con, code);

            if (code > 299) {
                System.err.println("Error getting content. ");
                System.err.println(response);
                return false;
            } else {
                return true;
            }

        } catch (IOException e) {
            System.err.println("Problem connecting to the API");
            e.printStackTrace();
        } finally {
            try {
                con.disconnect();
            } catch (Exception e) {
            }
        }

        return false;
    }


    String requestGET(URL url) {
        return requestGET(url, "text/plain; charset=\"utf-8\"", null, null);
    }

    String requestGET(URL url, String mimeType) {
        return requestGET(url, mimeType, null, null);
    }

    String requestGET(URL url, String mimeType, String username, String password) {

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

            Integer code = con.getResponseCode();
            String response = getContent(con, code);

            if (code > 299) {
                System.err.println("Error getting content. ");
                System.err.println(response);
                return null;
            } else {
                return response;
            }

        } catch (IOException e) {
            System.err.println("Problem connecting to the API");
            e.printStackTrace();
        } finally {
            try {
                con.disconnect();
            } catch (Exception e) {
            }
        }

        return null;
    }

    private String getContent(HttpURLConnection con, Integer code) throws IOException {
        Reader streamReader;

        if (code > 299) {
            System.err.println("Error in API: Response Code: " + code);
            System.err.println("Con: " + con.getErrorStream());
            streamReader = new InputStreamReader(con.getInputStream());
        } else {
            streamReader = new InputStreamReader(con.getInputStream());
        }

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
