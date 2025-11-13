package com.topicmodelling;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/scrapper")
public class ScraperController {

    private static final int MAX_PAGE_SIZE = 50;
    private static final URI GUARDIAN_BASE = URI.create("https://content.guardianapis.com/search");

    //@Autowired
    //private DocService docService;

    @Autowired
    private S3Service s3Service;


    @GetMapping("/scrape")
    public int scrapeDocs(
            @RequestParam int nDocs,
            @RequestParam String theme,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {

        //s3Service = new S3Service(s3Config.amazonS3());

        // Format search term
        String query = theme.replaceAll(" ", "%20");
        List<JSONObject> documents;
        try {
            documents = getRawDocs(nDocs, query, fromDate, toDate);
        } catch (Exception e) {
            System.err.println(e);
            return -1;
        }

        for (JSONObject document: documents) {
            String id = document.getString("id");
            String title = document.getString("webTitle");
            String rawDate = document.getString("webPublicationDate");
            String date = rawDate.substring(0, rawDate.indexOf("T"));
            String body = document.getJSONObject("fields").getString("bodyText");

            Doc doc = new Doc(id, title, date, body);

            s3Service.uploadFile(doc);

   //         if (!docService.containsDocument(doc)) {
     //           docService.addDocument(doc);
       //     }
        }

        return 200;
    }

    private List<JSONObject> getRawDocs(int nDocs, String query, String fromDate, String toDate) throws Exception {
        if (nDocs <= 0) return List.of();

        final String apiKey = System.getenv("APIKEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("API key not configured");
        }

        final HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();

        // Always start with page 1 to learn how many pages exist.
        int pageSize = Math.min(MAX_PAGE_SIZE, nDocs);
        URI firstPageUri = buildGuardianUri(query, fromDate, toDate, /*page=*/1, pageSize, apiKey);

        HttpResponse<String> firstResp = send(client, firstPageUri);
        JSONObject firstJson = new JSONObject(firstResp.body());

        JSONObject resp = firstJson.getJSONObject("response");

        int availablePages = resp.optInt("pages", 1);
        int requiredPages = (int) Math.ceil(nDocs / (double) MAX_PAGE_SIZE);
        int pagesToFetch = Math.min(availablePages, requiredPages);

        if (pagesToFetch == 0) {
            throw new Exception("Not enough articles to fulfill the request");
        }

        List<JSONObject> documents = new ArrayList<>(nDocs);
        // Collect from first page
        appendResults(resp, documents);

        // Fetch remaining pages until we have nDocs (respect remainder on last page)
        int collected = documents.size();
        for (int page = 2; page <= pagesToFetch && collected < nDocs; page++) {
            int remaining = nDocs - collected;
            int thisPageSize = Math.min(MAX_PAGE_SIZE, remaining);
            URI pageUri = buildGuardianUri(query, fromDate, toDate, page, thisPageSize, apiKey);

            HttpResponse<String> r = send(client, pageUri);
            JSONObject j = new JSONObject(r.body()).getJSONObject("response");
            appendResults(j, documents);
            collected = documents.size();
        }

        // Trim in case the API returned more than requested
        if (documents.size() > nDocs) {
            return documents.subList(0, nDocs);
        }
        return documents;
    }

    private static URI buildGuardianUri(String query, String fromDate, String toDate,
                                        int page, int pageSize, String apiKey) {
        StringBuilder sb = new StringBuilder(GUARDIAN_BASE.toString()).append('?');
        sb.append("q=").append(encode(query));
        if (fromDate != null && !fromDate.isBlank()) {
            sb.append("&from-date=").append(encode(fromDate));
        }
        if (toDate != null && !toDate.isBlank()) {
            sb.append("&to-date=").append(encode(toDate));
        }
        sb.append("&page=").append(page);
        sb.append("&page-size=").append(pageSize);
        sb.append("&show-fields=bodyText");
        sb.append("&api-key=").append(encode(apiKey));
        return URI.create(sb.toString());
    }

    private static String encode(String s) {
        return java.net.URLEncoder.encode(s == null ? "" : s, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static HttpResponse<String> send(HttpClient client, URI uri) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(uri)
                .timeout(java.time.Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        int status = resp.statusCode();
        if (status < 200 || status >= 300) {
            throw new RuntimeException("HTTP " + status + " for " + uri + " body=" + resp.body());
        }
        return resp;
    }

    private static void appendResults(JSONObject responseObj, List<JSONObject> out) {
        var results = responseObj.optJSONArray("results");
        if (results == null) return;
        for (int i = 0; i < results.length(); i++) {
            out.add(results.getJSONObject(i));
        }
    }
}
