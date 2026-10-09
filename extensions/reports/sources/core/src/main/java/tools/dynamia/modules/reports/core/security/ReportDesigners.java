package tools.dynamia.modules.reports.core.security;

import tools.dynamia.integration.Containers;

import java.util.Collection;
import java.util.List;

/**
 * Entry point to check the designer permission, see {@link ReportDesignerPolicy}. Fails closed.
 */
public final class ReportDesigners {

    private ReportDesigners() {
    }

    /**
     * @return true if any registered policy allows the current user to design reports
     */
    public static boolean canDesign() {
        return canDesign(policies());
    }

    /**
     * @param policies the policies to ask
     * @return true if any of them allows the current user to design reports
     */
    public static boolean canDesign(Collection<ReportDesignerPolicy> policies) {
        return policies.stream().anyMatch(ReportDesignerPolicy::canDesign);
    }

    /**
     * @throws ReportAccessDeniedException if the current user is not a designer
     */
    public static void check() {
        if (!canDesign()) {
            throw new ReportAccessDeniedException("designer");
        }
    }

    private static Collection<ReportDesignerPolicy> policies() {
        try {
            return Containers.get().findObjects(ReportDesignerPolicy.class);
        } catch (RuntimeException e) {
            return List.of();
        }
    }
}
