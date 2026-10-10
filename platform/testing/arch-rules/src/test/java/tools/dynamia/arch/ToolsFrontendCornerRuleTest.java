package tools.dynamia.arch;

/** FrontendCornerRule on dynamia-tools. */
class ToolsFrontendCornerRuleTest extends FrontendCornerRule {

    @Override
    protected ArchRulesConfig config() {
        return ToolsArchRules.config();
    }
}
