package tools.dynamia.ui.testing.contract;

import tools.dynamia.actions.AbstractLocalAction;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.contract.Effects;
import tools.dynamia.ui.contract.Outcome;
import tools.dynamia.ui.contract.PortDriver;
import tools.dynamia.ui.contract.Reply;
import tools.dynamia.ui.testing.ActionTester;
import tools.dynamia.ui.testing.UIInteraction;
import tools.dynamia.ui.testing.UIScript;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Drives an action on the {@code test} platform: callbacks run immediately, as in ZK, and the user is a {@link UIScript}.
 */
public class TestPlatformDriver implements PortDriver {

    @Override
    public Outcome run(Consumer<Effects> action, List<Reply> replies) {
        var effects = new Effects();
        var local = new AbstractLocalAction() {
            {
                setId("contract");
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                action.accept(effects);
            }
        };
        var result = ActionTester.of(local).user(script -> replies.forEach(reply -> play(script, reply))).run();

        var asked = new ArrayList<Outcome.Asked>();
        var notices = new ArrayList<String>();
        for (UIInteraction interaction : result.interactions()) {
            if (interaction.type().asksTheUser() || interaction.type() == UIInteraction.Type.VIEW) {
                boolean repeated = interaction.type() == UIInteraction.Type.DIALOG && interaction.messageType() == MessageType.ERROR;
                asked.add(new Outcome.Asked(interaction.type().name(), interaction.label(), repeated ? interaction.message() : null));
            } else if (interaction.type() == UIInteraction.Type.NOTIFY) {
                notices.add(interaction.messageType() + " " + interaction.message());
            }
        }
        var downloads = result.downloads().stream()
                .map(d -> new Outcome.Download(d.name(), d.contentType(), d.content())).toList();
        Outcome.Redirect redirect = result.interactions().stream()
                .filter(i -> i.type() == UIInteraction.Type.REDIRECT)
                .map(i -> new Outcome.Redirect(String.valueOf(i.payload().get("url")), Boolean.TRUE.equals(i.payload().get("newWindow"))))
                .findFirst().orElse(null);
        return new Outcome(asked, effects.entries(), notices, downloads, redirect, result.exception());
    }

    private static void play(UIScript script, Reply reply) {
        switch (reply) {
            case Reply.Yes yes -> script.confirm(true);
            case Reply.No no -> script.confirm(false);
            case Reply.Cancel cancel -> script.cancel();
            case Reply.Input input -> script.input(input.value());
            case Reply.Form form -> script.fillForm(form.values());
            case Reply.Choose choose -> script.choose(choose.keys().toArray(String[]::new));
            case Reply.Upload upload -> script.upload(upload.files().toArray(tools.dynamia.ui.files.UploadedFile[]::new));
        }
    }
}
