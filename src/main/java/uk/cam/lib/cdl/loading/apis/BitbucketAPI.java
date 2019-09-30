package uk.cam.lib.cdl.loading.apis;

import org.joda.time.DateTime;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import uk.cam.lib.cdl.loading.model.Tag;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BitbucketAPI extends WebAPI {

    private URL apiURL;
    private URL tagsURL;
    private String username;
    private String password;

    public BitbucketAPI(URL apiURL, String tagsURL, String username, String password) throws MalformedURLException {
        this.apiURL = apiURL;
        this.tagsURL = new URL(apiURL, tagsURL);
        this.username = username;
        this.password = password;
    }

    public List<Tag> getTags() {
        try {
            URL url = new URL(tagsURL.toString());

            String json = this.requestGETJSON(url, username, password);
            JSONObject parent = new JSONObject(json);
            JSONArray values = parent.getJSONArray("values");

            List<Tag> tags = new ArrayList<Tag>();
            for (int i = 0; i < values.length(); i++) {
                JSONObject o = values.getJSONObject(i);
                String name = o.getString("name");
                DateTime date = new DateTime(o.getString("date"));
                String message = o.getString("message");
                Tag t = new Tag(name, date, message);
                tags.add(t);
            }
            return tags;

        } catch (MalformedURLException e) {
            System.err.println("Invalid URL.  Look at your application.properties.");
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}
