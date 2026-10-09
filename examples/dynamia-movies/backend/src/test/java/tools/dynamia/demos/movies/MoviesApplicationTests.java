package tools.dynamia.demos.movies;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End to end checks against the running application: seeded catalog, the read-only public API that feeds the
 * public site, and the secured endpoints (reports, dashboard) the backoffice uses.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MoviesApplicationTests {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Value("${local.server.port}")
    private int port;

    private final HttpClient anonymous = HttpClient.newHttpClient();
    private HttpClient admin;

    private HttpResponse<String> get(HttpClient client, String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(HttpClient client, String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpClient admin() throws Exception {
        if (admin == null) {
            var client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
            var login = post(client, "/login/json", "{\"username\":\"admin\",\"password\":\"adminadmin\"}");
            assertEquals(200, login.statusCode(), login.body());
            admin = client;
        }
        return admin;
    }

    @Test
    void publicApiListsTheSeededCatalogSortedAndPaged() throws Exception {
        var response = get(anonymous, "/api/public/movies?size=3&_sort=rating&_order=desc");
        assertEquals(200, response.statusCode(), response.body());
        JsonNode body = JSON.readTree(response.body());
        assertEquals(3, body.get("data").size());
        assertTrue(body.get("pageable").get("totalSize").asInt() >= 100, "the demo catalog has 100+ movies");
        assertEquals("The Shawshank Redemption", body.get("data").get(0).get("title").asString());
        assertFalse(body.get("data").get(0).has("searchText"), "the search column is not exposed");
    }

    @Test
    void publicApiSearchesAcrossTitleDirectorAndCast() throws Exception {
        var response = get(anonymous, "/api/public/movies?searchText=nolan&size=50");
        JsonNode data = JSON.readTree(response.body()).get("data");
        assertTrue(data.size() >= 5);
        data.forEach(movie -> assertTrue(movie.get("directorName").asString().contains("Nolan")
                || movie.get("topCast").asString().contains("Nolan")));
    }

    @Test
    void publicApiFiltersByFieldAndByRelation() throws Exception {
        JsonNode year = JSON.readTree(get(anonymous, "/api/public/movies?year=1999&size=50").body());
        year.get("data").forEach(movie -> assertEquals(1999, movie.get("year").asInt()));

        JsonNode credits = JSON.readTree(get(anonymous, "/api/public/credits?movie.id=1&size=100").body());
        assertTrue(credits.get("data").size() >= 2);
        credits.get("data").forEach(credit -> assertEquals(1, credit.get("movie").get("id").asInt()));
    }

    @Test
    void publicApiIsReadOnly() throws Exception {
        assertTrue(post(anonymous, "/api/public/movies", "{\"title\":\"Hacked\"}").statusCode() >= 400);
        var delete = anonymous.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/public/movies/1")).DELETE().build(),
                HttpResponse.BodyHandlers.ofString());
        assertTrue(delete.statusCode() >= 400);
        assertEquals(200, get(anonymous, "/api/public/movies/1").statusCode(), "the movie is still there");
    }

    @Test
    void backofficeApiAndReportsRequireLogin() throws Exception {
        assertEquals(401, get(anonymous, "/api/catalog/movies").statusCode());
        assertEquals(401, get(anonymous, "/api/reports/v2/catalog").statusCode());
        assertEquals(401, get(anonymous, "/api/dashboard/moviesDashboard/widgets/totalMovies").statusCode());
    }

    @Test
    void reportsRunWithFilters() throws Exception {
        var catalog = JSON.readTree(get(admin(), "/api/reports/v2/catalog").body());
        long reports = catalog.valueStream().mapToLong(group -> group.get("reports").size()).sum();
        assertTrue(reports >= 9, "the demo reports were created");

        long id = 0;
        for (var group : catalog) {
            for (var report : group.get("reports")) {
                if ("Top rated movies".equals(report.get("name").asString())) {
                    id = report.get("id").asLong();
                }
            }
        }
        assertTrue(id > 0);

        var run = post(admin(), "/api/reports/v2/" + id + "/run",
                "{\"filters\":{\"fromYear\":\"2010\",\"certification\":\"PG_13\"},\"page\":1,\"size\":5}");
        assertEquals(200, run.statusCode(), run.body());
        JsonNode rows = JSON.readTree(run.body()).get("rows");
        assertEquals(5, rows.size());
        rows.forEach(row -> {
            assertTrue(row.get("Year").asInt() >= 2010);
            assertEquals("PG_13", row.get("Certification").asString());
        });
    }

    @Test
    void dashboardWidgetsAreServed() throws Exception {
        var kpi = JSON.readTree(get(admin(), "/api/dashboard/moviesDashboard/widgets/totalMovies").body());
        assertEquals("kpi", kpi.get("type").asString());
        assertTrue(kpi.get("data").get("value").asInt() >= 100);

        var chart = JSON.readTree(get(admin(), "/api/dashboard/moviesDashboard/widgets/moviesPerDecade").body());
        assertEquals("chart", chart.get("type").asString());

        var feed = JSON.readTree(get(admin(), "/api/dashboard/moviesDashboard/widgets/latestReviews").body());
        assertEquals("reviews-feed", feed.get("type").asString());
        assertEquals(6, feed.get("data").size());
    }

    @Test
    void automaticUpdateReplacesAManyToManyAndKeepsTheRest() throws Exception {
        var client = admin();
        JsonNode movie = JSON.readTree(get(client, "/api/catalog/movies/2").body()).get("data");
        JsonNode genres = JSON.readTree(get(anonymous, "/api/public/genres?size=100&_sort=name").body()).get("data");
        var body = (tools.jackson.databind.node.ObjectNode) movie.deepCopy();
        var chosen = body.putArray("genres");
        chosen.addObject().put("id", genres.get(0).get("id").asLong());   // Action
        chosen.addObject().put("id", genres.get(1).get("id").asLong());   // Adventure

        var put = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/catalog/movies/2"))
                .header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(body.toString())).build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, put.statusCode(), put.body());

        JsonNode after = JSON.readTree(get(anonymous, "/api/public/movies/2").body()).get("data");
        assertEquals(genres.get(0).get("name").asString() + ", " + genres.get(1).get("name").asString(), after.get("genresText").asString());
        assertEquals(movie.get("directorName").asString(), after.get("directorName").asString(), "credits were not touched");
        assertTrue(get(anonymous, "/api/public/credits?movie.id=2").body().contains("\"role\""), "children survive the update");
    }

    @Test
    void navigationHidesTheTechnicalPublicModuleAndExposesExternalPageUrls() throws Exception {
        var nav = get(admin(), "/api/app/metadata/navigation").body();
        assertFalse(nav.contains("\"id\" : \"public\"") || nav.contains("\"path\":\"public\""), "the public module is not a menu entry");
        var tree = JSON.readTree(nav).get("navigation");
        boolean found = false;
        for (var module : tree) {
            if (!"insights".equals(module.get("id").asString())) continue;
            for (var page : module.get("children")) {
                if ("dashboard".equals(page.get("id").asString())) {
                    assertEquals("/insights/insights.js?page=dashboard", page.get("url").asString());
                    found = true;
                }
            }
        }
        assertTrue(found, "the dashboard page carries the url the theme embeds");
        assertFalse(nav.contains("classpath:"), "internal paths are never exposed");
    }

    @Test
    void shellPagesAreServedAsHtml() throws Exception {
        var login = get(anonymous, "/login");
        assertEquals(200, login.statusCode());
        assertTrue(login.headers().firstValue("Content-Type").orElse("").startsWith("text/html"));
    }

    @Test
    void theSameDeleteActionZkRunsAsksAndDeletesThroughTheReplayProtocol() throws Exception {
        var client = admin();
        var action = "/api/app/metadata/entities/Review/action/delete";
        long before = JSON.readTree(get(client, "/api/catalog/reviews?size=1").body()).get("pageable").get("totalSize").asLong();
        JsonNode first = JSON.readTree(get(anonymous, "/api/public/reviews?size=1&_sort=id").body()).get("data").get(0);
        var id = first.get("id").asString();

        // 1. the action asks; the pass is rolled back, nothing is deleted yet
        var question = JSON.readTree(post(client, action, "{\"dataId\":\"" + id + "\",\"dataType\":\"Review\"}").body());
        assertEquals("PENDING", question.get("status").asString());
        assertEquals("CONFIRM", question.get("flow").get("type").asString());
        assertTrue(question.get("flow").get("message").asString().contains("delete"));
        assertEquals(before, JSON.readTree(get(client, "/api/catalog/reviews?size=1").body()).get("pageable").get("totalSize").asLong());
        var token = question.get("flow").get("resumeToken").asString();

        // 2. "no": it runs again and does nothing
        var no = JSON.readTree(post(client, action, "{\"resumeToken\":\"" + token + "\",\"data\":false}").body());
        assertEquals("SUCCESS", no.get("status").asString());
        assertFalse(no.get("data").get("deleted").asBoolean());
        assertEquals(before, JSON.readTree(get(client, "/api/catalog/reviews?size=1").body()).get("pageable").get("totalSize").asLong());

        // 3. "yes": it runs again, the callback of the question deletes and the transaction commits
        var yes = JSON.readTree(post(client, action, "{\"resumeToken\":\"" + token + "\",\"data\":true}").body());
        assertEquals("SUCCESS", yes.get("status").asString());
        assertTrue(yes.get("data").get("deleted").asBoolean());
        assertEquals(before - 1, JSON.readTree(get(client, "/api/catalog/reviews?size=1").body()).get("pageable").get("totalSize").asLong());

        // a token cannot be forged
        assertEquals(401, JSON.readTree(post(client, action, "{\"resumeToken\":\"x" + token + "\",\"data\":true}").body()).get("statusCode").asInt());
    }

    @Test
    void theSameSaveActionSavesAndAnInvalidEntityIsA422() throws Exception {
        var client = admin();
        var action = "/api/app/metadata/entities/Review/action/save";
        var saved = JSON.readTree(post(client, action,
                "{\"data\":{\"id\":3,\"comment\":\"Edited by SaveAction\"},\"dataType\":\"Review\"}").body());
        assertEquals("SUCCESS", saved.get("status").asString());
        assertTrue(saved.get("data").get("saved").asBoolean());
        assertEquals("Edited by SaveAction", saved.get("data").get("entity").get("comment").asString());
        assertEquals("Edited by SaveAction", JSON.readTree(get(anonymous, "/api/public/reviews/3").body()).get("data").get("comment").asString());

        int scoreBefore = JSON.readTree(get(anonymous, "/api/public/reviews/3").body()).get("data").get("score").asInt();
        var invalid = post(client, action, "{\"data\":{\"id\":3,\"score\":99},\"dataType\":\"Review\"}");
        assertEquals(422, invalid.statusCode());
        assertEquals(scoreBefore, JSON.readTree(get(anonymous, "/api/public/reviews/3").body()).get("data").get("score").asInt(),
                "the invalid change was rolled back");
    }

    @Test
    void bulkDeleteAsksOnceForAllTheRecordsAndTheDuplicatedRemoteActionsAreGone() throws Exception {
        var client = admin();
        var meta = JSON.readTree(get(client, "/api/app/metadata/entities/Review").body());
        var ids = new java.util.ArrayList<String>();
        meta.get("actions").forEach(a -> ids.add(a.get("id").asString()));
        assertTrue(ids.containsAll(java.util.List.of("save", "delete")), "the actions of ZK are served: " + ids);
        assertFalse(ids.contains("deleteDirect") || ids.contains("saveDirect"), "no hand written duplicates: " + ids);

        var action = "/api/app/metadata/entities/Review/action/delete";
        long before = JSON.readTree(get(client, "/api/catalog/reviews?size=1").body()).get("pageable").get("totalSize").asLong();
        var question = JSON.readTree(post(client, action, "{\"data\":{\"ids\":[\"10\",\"11\",\"12\"]}}").body());
        assertEquals("CONFIRM", question.get("flow").get("type").asString(), question.toString());
        assertTrue(question.get("flow").get("message").asString().contains("3"), "one question for the three records");
        var done = JSON.readTree(post(client, action, "{\"resumeToken\":\"" + question.get("flow").get("resumeToken").asString() + "\",\"data\":true}").body());
        assertEquals("SUCCESS", done.get("status").asString(), done.toString());
        assertEquals(before - 3, JSON.readTree(get(client, "/api/catalog/reviews?size=1").body()).get("pageable").get("totalSize").asLong());
    }
}
