package uk.cam.lib.cdl.loading.apis;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.util.UriUtils;
import uk.cam.lib.cdl.loading.model.WebResponse;
import uk.cam.lib.cdl.loading.model.packaging.PackagingStatus;
import uk.cam.lib.cdl.loading.model.packaging.Pipeline;
import uk.cam.lib.cdl.loading.model.packaging.PipelineStatus;
import uk.cam.lib.cdl.loading.utils.WebHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is used for calls to the Bitbucket API
 * Package-private for use by APIs
 */
class BitBucketAPI {

    private final String branch;
    //private URL tagsURL = null;
    private URL pipelinesURL = null;
    private final String username;
    private final String password;
    private final String cacheName = "bitbucketTags";
    private final WebHelper webHelper = new WebHelper();

    public BitBucketAPI(URL apiURL, String branch, String repoURL, String tagsURL, String pipelinesURL, String username,
                        String password) {

        this.branch = branch;
        this.username = username;
        this.password = password;

        try {
            //this.tagsURL = new URL(apiURL, repoURL + tagsURL);
            this.pipelinesURL = new URL(apiURL, repoURL + pipelinesURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }
/*
    @Cacheable(cacheName)
    public List<Tag> getTags() {
        String json = webHelper.requestGET(tagsURL, "application/json", username, password).getResponse();
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
*/

    /**
     * Gets the last 10 pipelines that have been run.
     *
     * @return List of last 10 pipelines.
     */
    @PreAuthorize("@roleService.canBuildPackages(authentication)")
    public List<Pipeline> getPipelines() {

        List<Pipeline> output = new ArrayList<>();
        try {
            WebResponse response = webHelper.requestGET(new URL(pipelinesURL, "?sort=-created_on"),
                "application/json",
                username,
                password);

            JSONObject json = new JSONObject(response.getResponse());
            JSONArray pipelines = json.getJSONArray("values");

            for (int i = 0; i < pipelines.length(); i++) {
                JSONObject pipeline = pipelines.getJSONObject(i);
                String id = pipeline.getString("uuid");
                int buildNumber = pipeline.getInt("build_number");

                DateTimeFormatter parser = ISODateTimeFormat.dateTime();
                Date created = parser.parseDateTime(pipeline.getString("created_on")).toDate();
                Date completed = null;
                if (pipeline.has("completed_on")) {
                    completed = parser.parseDateTime(pipeline.getString("completed_on")).toDate();
                }

                JSONObject stateJSON = pipeline.getJSONObject("state");
                String stateName = stateJSON.getString("name");
                String stateType = stateJSON.getString("type");
                // Assume max one result

                String resultName = null;
                String resultType = null;
                if (stateJSON.has("result")) {
                    JSONObject result = stateJSON.getJSONObject("result");
                    resultName = result.getString("name");
                    resultType = result.getString("type");
                }
                PipelineStatus pipelineStatus = new PipelineStatus(stateName, stateType, resultName, resultType);
                output.add(new Pipeline(id, buildNumber, created, pipelineStatus, completed));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return output;

    }

    /**
     * This assumes the custom pipeline is called 'package'.
     *
     * @return Pipeline response
     */
    @PreAuthorize("@roleService.canBuildPackages(authentication)")
    public WebResponse triggerPipeline() {

        JSONObject jsonObject = new JSONObject("{ 'target': {'type': 'pipeline_ref_target','ref_type': 'branch'," +
            "'ref_name': " +
            "'" + branch + "','selector': { 'type': 'custom', 'pattern': 'package'} } }");
        WebResponse response = webHelper.requestPOSTJSON(pipelinesURL,
            jsonObject, username, password);

        return response;
    }

    /**
     * Get Pipeline status
     *
     * @param id for Pipeline build
     * @return PackagingStatus object for this pipeline build.
     */
    @PreAuthorize("@roleService.canBuildPackages(authentication)")
    public PackagingStatus getStatus(String id) {
        try {
            String json = webHelper.requestGET(new URL(pipelinesURL,
                    UriUtils.encodeQueryParam(id, StandardCharsets.UTF_8)),
                "application/json",
                username,
                password).getResponse();

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

