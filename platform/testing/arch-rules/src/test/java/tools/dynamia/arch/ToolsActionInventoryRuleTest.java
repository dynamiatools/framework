package tools.dynamia.arch;

/** ActionInventoryRule on dynamia-tools. */
class ToolsActionInventoryRuleTest extends ActionInventoryRule {

    @Override
    protected ArchRulesConfig config() {
        return ToolsArchRules.config();
    }
}
