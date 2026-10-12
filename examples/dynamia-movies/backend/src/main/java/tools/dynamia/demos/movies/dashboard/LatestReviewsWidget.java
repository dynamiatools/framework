package tools.dynamia.demos.movies.dashboard;

import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.modules.dashboard.AbstractDashboardWidgetDefinition;
import tools.dynamia.modules.dashboard.InstallDashboardWidget;
import tools.dynamia.modules.dashboard.WidgetContext;

import java.util.List;
import java.util.Map;

/**
 * Custom, UI-agnostic dashboard widget: the latest community reviews. Its type ({@code reviews-feed}) is not one of
 * the built-in ones, so the Vue side registers its own renderer for it (see {@code insights/main.ts}).
 */
@InstallDashboardWidget
public class LatestReviewsWidget extends AbstractDashboardWidgetDefinition {

    public static final String TYPE = "reviews-feed";

    private final CrudService crudService;
    private int limit = 6;

    public LatestReviewsWidget(CrudService crudService) {
        this.crudService = crudService;
        setTitle("Latest reviews");
        setTitleVisible(true);
    }

    @Override
    public String getId() {
        return "latest-reviews";
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void init(WidgetContext context) {
        var configured = context.getField() != null ? context.getField().getParams().get("limit") : null;
        if (configured != null) {
            limit = Integer.parseInt(configured.toString());
        }
    }

    @Override
    public Object getData(WidgetContext context) {
        List<Object[]> rows = crudService.executeQuery(
                "select r.author, r.score, r.comment, m.title, r.reviewDate "
                        + "from Review r join r.movie m order by r.reviewDate desc, r.id desc",
                new QueryParameters().setMaxResults(limit));
        return rows.stream()
                .map(r -> Map.of(
                        "author", String.valueOf(r[0]),
                        "score", r[1],
                        "comment", String.valueOf(r[2]),
                        "movie", String.valueOf(r[3]),
                        "date", String.valueOf(r[4])))
                .toList();
    }
}
