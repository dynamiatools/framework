package tools.dynamia.arch;

/** VocabularyRule on dynamia-tools. */
class ToolsVocabularyRuleTest extends VocabularyRule {

    @Override
    protected ArchRulesConfig config() {
        return ToolsArchRules.config();
    }
}
