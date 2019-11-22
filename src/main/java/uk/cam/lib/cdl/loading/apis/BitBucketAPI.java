package uk.cam.lib.cdl.loading.apis;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import uk.cam.lib.cdl.loading.model.WebResponse;
import uk.cam.lib.cdl.loading.model.deployment.Tag;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BitBucketAPI {

    private final URL apiURL;
    private final String branch;
    private final URL tagsURL;
    private final URL pipelinesURL;
    private final String username;
    private final String password;
    private final String cacheName = "bitbucketTags";
    private final WebHelper webHelper = new WebHelper();

    public BitBucketAPI(URL apiURL, String branch, String repoURL, String tagsURL, String pipelinesURL, String username,
                        String password) throws MalformedURLException {
        this.apiURL = apiURL;
        this.branch = branch;
        this.tagsURL = new URL(apiURL, repoURL + tagsURL);
        this.pipelinesURL = new URL(apiURL, repoURL + pipelinesURL);
        this.username = username;
        this.password = password;
    }

    @Cacheable(cacheName)
    public List<Tag> getTags() {
        String json = webHelper.requestGET(tagsURL, "application/json", username, password);
        JSONObject parent = new JSONObject(json);
        JSONArray values = parent.getJSONArray("values");

        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < values.length(); i++) {
            JSONObject o = values.getJSONObject(i);
            String name = o.getString("name");
            DateTime date = new DateTime(o.getString("date"));
            String message = o.getString("message");
            Tag t = new Tag(name, date, message);
            tags.add(t);
        }
        return tags;

    }

    /**
     * This assumes the custom pipeline is called 'package'.
     *
     * @return
     */
    public String triggerPipeline() {

        JSONObject jsonObject = new JSONObject("{ 'target': {'type': 'pipeline_ref_target','ref_type': 'branch'," +
            "'ref_name': " +
            "'" + branch + "','selector': { 'type': 'custom', 'pattern': 'package'} } }");
        WebResponse response = webHelper.requestPOSTJSON(pipelinesURL,
            jsonObject, username, password);

        System.out.println("trigger Pipeline response:" + response.getResponse());
        return response.getResponse();
    }

    public PackagingStatus getStatus(String id) {
        try {
            String json = webHelper.requestGET(new URL(pipelinesURL,
                    URLEncoder.encode(id, StandardCharsets.UTF_8)),
                "application/json",
                username,
                password);

            System.out.println("get status response:" + json);

            JSONObject jsonObject = new JSONObject(json);
            JSONObject state = jsonObject.getJSONObject("state");
            if (state.getString("type").equals("pipeline_state_completed")) {
                return new PackagingStatus(
                    state.getString("name"),
                    state.getString("type"),
                    state.getJSONObject("result").getString("name"),
                    state.getJSONObject("result").getString("type"));
            }
            return new PackagingStatus(state.getString("name"), state.getString("type"),
                null, null);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @CacheEvict(allEntries = true, value = {cacheName})
    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 500) // Every 5 mins
    public void cacheEvict() {
        System.out.println("Flush Cache " + DateFormat.getInstance().format(new Date()));
    }

}
