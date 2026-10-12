package tools.dynamia.actions.replay;

import tools.dynamia.ui.ChoicesProvider;
import tools.dynamia.ui.FileTransfer;
import tools.dynamia.ui.MessageDisplayer;
import tools.dynamia.ui.NavigationProvider;
import tools.dynamia.ui.ProgressRunner;
import tools.dynamia.ui.UIEnvironment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The {@link UIEnvironment} of one headless pass: every port answers from the {@link ReplaySession} of the pass instead
 * of a screen. {@link ReplayExecutor} creates one per pass and binds it with {@code UIFacades.with(env, ...)}, so the
 * action sees a single environment whatever the number of ports.
 * <p>
 * It also answers for {@link ReplaySession} itself, which is how {@link ReplaySession#current()} finds the session.
 */
public final class ReplayUIEnvironment implements UIEnvironment {

    private final ReplaySession session;
    private final ReplayInteractions interactions;
    private final Map<Class<?>, Object> ports = new HashMap<>();

    /**
     * @param session      the session of the pass
     * @param contributors modules that add ports (forms, for example); may be empty
     */
    public ReplayUIEnvironment(ReplaySession session, List<ReplayPortContributor> contributors) {
        this.session = session;
        this.interactions = new ReplayInteractions(session);
        ports.put(MessageDisplayer.class, interactions);
        ports.put(FileTransfer.class, new ReplayFileTransfer(session));
        ports.put(ProgressRunner.class, new ReplayProgressRunner(session));
        ports.put(ChoicesProvider.class, new ReplayChoicesProvider(session));
        ports.put(NavigationProvider.class, new ReplayNavigationProvider(session));
        for (ReplayPortContributor contributor : contributors) {
            ports.putAll(contributor.ports(session));
        }
        ports.put(ReplaySession.class, session);
    }

    @Override
    public String name() {
        return "replay";
    }

    @Override
    public <S> Optional<S> port(Class<S> spi) {
        return Optional.ofNullable(ports.get(spi)).map(spi::cast);
    }

    /** @return the session of the pass */
    public ReplaySession session() {
        return session;
    }

    /** @return the messages and questions of the pass, which tell whether it stopped at a question */
    public ReplayInteractions interactions() {
        return interactions;
    }
}
