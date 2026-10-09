package tools.dynamia.demos.movies.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tools.dynamia.demos.movies.domain.enums.Certification;
import tools.dynamia.domain.jpa.RootTenantIdentifierResolver;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportChart;
import tools.dynamia.modules.reports.core.domain.ReportFilter;
import tools.dynamia.modules.reports.core.domain.ReportGroup;
import tools.dynamia.modules.reports.core.domain.enums.DataType;

/**
 * Reports are data in DynamiaReports: SQL stored in the database with filters and charts. This loader creates the
 * ones the movies dashboard and the reports page use. They run against the application database.
 */
@Component
@Order(200)
public class ReportsDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ReportsDataLoader.class);

    /** Single tenant demo: everything lives in the root tenant, like the admin user. */
    private static final Long ACCOUNT_ID = RootTenantIdentifierResolver.ROOT_TENANT_ID;

    private final CrudService crudService;
    private final java.util.List<Report> pending = new java.util.ArrayList<>();

    public ReportsDataLoader(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public void run(String... args) {
        if (crudService.count(ReportGroup.class) > 0) {
            return;
        }

        var kpis = group("Key figures", "kpis");
        report(kpis, "Total movies", "Number of movies in the catalog",
                "select count(*) as \"total\" from movies");
        report(kpis, "Average rating", "Average rating of the whole catalog",
                "select round(avg(rating), 2) as \"avg_rating\" from movies");
        report(kpis, "Total reviews", "Community reviews written",
                "select count(*) as \"total\" from reviews");
        report(kpis, "Total box office", "Worldwide box office of the catalog, millions of USD",
                "select round(sum(box_office)) as \"total\" from movies");

        var catalog = group("Catalog", "catalog");

        // JPQL report: the ENUM filter is bound as a Java enum, which only JPQL understands
        var topRated = report(catalog, "Top rated movies", "Best rated movies, filter by year and certification",
                """
                        select m.title as Title, m.year as Year, m.directorName as Director,
                               m.rating as Rating, m.votes as Votes, m.certification as Certification
                        from Movie m
                        order by m.rating desc, m.votes desc""");
        topRated.setQueryLang("jpql");
        filter(topRated, "fromYear", "From year", "m.year >= :fromYear", DataType.NUMBER, 0);
        filter(topRated, "toYear", "To year", "m.year <= :toYear", DataType.NUMBER, 1);
        var cert = filter(topRated, "certification", "Certification", "m.certification = :certification", DataType.ENUM, 2);
        cert.setEnumClassName(Certification.class.getName());

        var perDecade = report(catalog, "Movies per decade", "How many movies and which average rating per decade",
                """
                        select (release_year / 10) * 10 as "decade", count(*) as "movies",
                               round(avg(rating), 2) as "avg_rating", round(sum(box_office)) as "box_office"
                        from movies
                        group by (release_year / 10) * 10
                        order by 1""");
        chart(perDecade, "Movies per decade", "decade", "movies", "bar");
        chart(perDecade, "Average rating per decade", "decade", "avg_rating", "line");

        var byGenre = report(catalog, "Movies by genre", "Movies and average rating per genre",
                """
                        select g.name as "genre", count(*) as "movies", round(avg(m.rating), 2) as "avg_rating"
                        from genres g
                          join movies_genres mg on mg.genres_id = g.id
                          join movies m on m.id = mg.movie_id
                        group by g.name
                        order by 2 desc""");
        chart(byGenre, "Movies by genre", "genre", "movies", "bar");

        var directors = report(catalog, "Top directors", "Directors with more movies in the catalog",
                """
                        select p.name as "Director", count(*) as "Movies",
                               round(avg(m.rating), 2) as "Avg rating", round(sum(m.box_office)) as "Box office"
                        from credits c
                          join people p on p.id = c.person_id
                          join movies m on m.id = c.movie_id
                        where c.role = 'DIRECTOR'
                        group by p.name
                        order by 2 desc, 3 desc""");
        chart(directors, "Movies per director", "Director", "Movies", "bar");

        report(catalog, "Return on investment", "Box office divided by budget (budget and box office in millions of USD)",
                """
                        select title as "Title", release_year as "Year", budget as "Budget",
                               box_office as "Box office", round(box_office / budget, 1) as "ROI"
                        from movies
                        where budget > 0
                        order by 5 desc""");

        var community = group("Community", "community");
        var scores = report(community, "Reviews by score", "How the community scores movies",
                """
                        select score as "score", count(*) as "reviews"
                        from reviews
                        group by score
                        order by score""");
        chart(scores, "Reviews by score", "score", "reviews", "bar");

        pending.forEach(crudService::save);
        log.info("Demo reports created: {}", pending.size());
    }

    private ReportGroup group(String name, String endpoint) {
        var group = new ReportGroup();
        group.setName(name);
        group.setEndpointName(endpoint);
        group.setAccountId(ACCOUNT_ID);
        return crudService.save(group);
    }

    private Report report(ReportGroup group, String name, String description, String sql) {
        var report = new Report();
        report.setGroup(group);
        report.setName(name);
        report.setTitle(name);
        report.setDescription(description);
        report.setQueryLang("sql");
        report.setQueryScript(sql);
        report.setAccountId(ACCOUNT_ID);
        pending.add(report);
        return report;
    }

    private ReportFilter filter(Report report, String name, String label, String condition, DataType type, int order) {
        var filter = new ReportFilter();
        filter.setReport(report);
        filter.setName(name);
        filter.setLabel(label);
        filter.setCondition(condition);
        filter.setDataType(type);
        filter.setOrder(order);
        filter.setAccountId(ACCOUNT_ID);
        report.getFilters().add(filter);
        return filter;
    }

    private void chart(Report report, String title, String labelField, String valueField, String type) {
        var chart = new ReportChart();
        chart.setReport(report);
        chart.setTitle(title);
        chart.setLabelField(labelField);
        chart.setValueField(valueField);
        chart.setType(type);
        chart.setOrder(report.getCharts().size());
        chart.setAccountId(ACCOUNT_ID);
        report.getCharts().add(chart);
        report.setChartable(true);
    }
}
