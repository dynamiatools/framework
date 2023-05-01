package tools.dynamia.zk;

import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.sys.Attributes;
import org.zkoss.zk.ui.util.ExecutionCleanup;

import java.util.List;

/**
 * This listener just reasign ZK Session again to let Spring Session know that something is changed
 * and new to persisted
 */
public class SpringSessionRedisSupport implements ExecutionCleanup {

    public void cleanup(Execution exec, Execution parent, List<Throwable> errs) {
        var session = exec.getSession();
        var value = session.getAttribute(Attributes.ZK_SESSION);
        session.removeAttribute(Attributes.ZK_SESSION);
        session.setAttribute(Attributes.ZK_SESSION, value);
    }

}
