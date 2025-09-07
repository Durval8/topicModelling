package com.topicmodelling.scraper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/scrapper", produces = "application/json")
public class ScraperController {

    private final String apiURL = "https://content.guardianapis.com/search?q=QUERY&from-date=FROMDATE&to-date=TODATE&page=PAGE&page-size=SIZE&show-fields=bodyText&api-key=" + System.getenv("APIKEY");

    @Autowired
    private DocService docService;

    @GetMapping("/scrape")
    public int scrapeDocs(
            @RequestParam int nDocs,
            @RequestParam String theme,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {

        // Format search term
        String query = theme.replaceAll(" ", "%20");
        if (System.getenv("APIKEY") == null) {
            System.err.println("No API key provided");
            return -1;
        }
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
            docService.addDocument(doc);
        }

        return 200;
    }

    private List<JSONObject> getRawDocs(int nDocs, String query, String fromDate, String toDate) throws Exception {

        HttpClient client = HttpClient.newHttpClient();
        List<String> requests = new ArrayList<>();
        String requestURL = apiURL.replace("QUERY", query);

        if (fromDate != null) {
            requestURL = requestURL.replace("FROMDATE", fromDate);
        } else {
            requestURL = requestURL.replace("from-date=FROMDATE&", "");
        }

        if (toDate != null) {
            requestURL = requestURL.replace("TODATE", toDate);
        } else {
            requestURL = requestURL.replace("to-date=TODATE&", "");
        }


        if (nDocs >= 50) {  // Maximum amount of docs per page for the api
            int iterations = nDocs / 50;
            String iURL;
            for (int page = 1; page < iterations + 2; page++) { // Page numbering starts at 1 and cant forget about remainder
                if (nDocs - 50 >= 0) {
                    iURL = requestURL.replace("PAGE", Integer.toString(page)).replace("SIZE", Integer.toString(50));
                } else {
                    iURL = requestURL.replace("PAGE", Integer.toString(page)).replace("SIZE", Integer.toString(nDocs));
                }
                nDocs -= 50;
                requests.add(iURL);
            }
        } else {
            requestURL = requestURL.replace("SIZE", Integer.toString(nDocs));
            requests.add(requestURL);
        }

        var req = HttpRequest.newBuilder(URI.create(requests.removeFirst()))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
        JSONObject payload = new JSONObject(response.body());

        // Check if we have enough pages in the query
        int pages = Integer.parseInt(payload.getJSONObject("response").get("pages").toString());
        if (pages < requests.size() + 1) {
            throw new Exception("Not enough articles to fulfill the request");
        }
        // Get data for each document
        JSONArray page = new JSONArray(payload.getJSONObject("response").getJSONArray("results"));
        List<JSONObject> documents = new ArrayList<>();

        for (int i = 0; i < page.length(); i++) {
            JSONObject document = page.getJSONObject(i);
            documents.add(document);
        }

        for (String request : requests) {
            req = HttpRequest.newBuilder(URI.create(request))
                    .header("Accept", "application/json")
                    .build();

            response = client.send(req, HttpResponse.BodyHandlers.ofString());
            payload = new JSONObject(response.body());
            page = new JSONArray(payload.getJSONObject("response").getJSONArray("results"));

            for (int i = 0; i < page.length(); i++) {
                JSONObject document = page.getJSONObject(i);
                documents.add(document);
            }
        }

        return documents;
    }
}
