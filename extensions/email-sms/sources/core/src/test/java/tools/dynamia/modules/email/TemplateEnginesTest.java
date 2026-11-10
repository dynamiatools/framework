package tools.dynamia.modules.email;

import org.junit.jupiter.api.Test;
import tools.dynamia.modules.email.domain.EmailAccount;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.domain.EmailTemplateEngine;
import tools.dynamia.modules.email.services.SMSService;
import tools.dynamia.modules.email.services.impl.EmailServiceImpl;
import tools.dynamia.templates.TemplateEngine;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An email template renders with the engine it is written in: Velocity templates reach the platform engine exactly as
 * before the block editor, Dynamia templates use the safe engine, and a child and its parent each use their own.
 */
class TemplateEnginesTest {

    /** Stands for Velocity: replaces {@code $key} by the value and marks the text with "V:" so its use is visible. */
    static class FakeVelocity implements TemplateEngine {
        final List<String> calls = new ArrayList<>();

        @Override
        public String evaluate(String content, Map<String, Object> params) {
            calls.add(content);
            var out = content;
            for (var e : params.entrySet()) {
                out = out.replace("$" + e.getKey(), String.valueOf(e.getValue()));
            }
            return "V:" + out;
        }

        @Override
        public String evaluate(String content, Object target) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void evaluate(Reader reader, Writer writer, Map<String, Object> params) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void evaluate(Reader reader, Writer writer, Object target) {
            throw new UnsupportedOperationException();
        }
    }

    private final FakeVelocity velocity = new FakeVelocity();
    private final EmailTemplateRenderer renderer = new EmailTemplateRenderer(velocity);
    private final EmailServiceImpl service = new EmailServiceImpl(renderer, null, null);

    private static EmailTemplate template(EmailTemplateEngine engine, boolean escape, String subject, String content) {
        var t = new EmailTemplate();
        t.setName("t");
        t.setEngine(engine);
        t.setEscapeValues(escape);
        t.setSubject(subject);
        t.setContent(content);
        return t;
    }

    private static Map<String, Object> model(Object... pairs) {
        var m = new HashMap<String, Object>();
        for (int i = 0; i < pairs.length; i += 2) {
            m.put((String) pairs[i], pairs[i + 1]);
        }
        return m;
    }

    private EmailMessage process(EmailTemplate t, Map<String, Object> model) {
        var message = new EmailMessage(t);
        message.setTemplateModel(model);
        service.processTemplate(message);
        return message;
    }

    @Test
    void aLegacyTemplateGoesThroughTheVelocityEngineInEveryField() {
        var t = template(null, false, "Hi $name", "<p>$name</p>");
        t.setTo("$mail");
        t.setCc("$mail");
        t.setBcc("$mail");
        t.setReplyTo("$mail");
        var message = process(t, model("name", "Ada", "mail", "ada@x.com"));

        assertEquals("V:Hi Ada", message.getSubject());
        assertEquals("V:<p>Ada</p>", message.getContent());
        assertTrue(message.getTos().contains("V:ada@x.com"));
        assertTrue(message.getCcs().contains("V:ada@x.com"));
        assertTrue(message.getBccs().contains("V:ada@x.com"));
        assertEquals("V:ada@x.com", message.getReplyTo());
        assertEquals(6, velocity.calls.size());
    }

    @Test
    void aLegacyTemplateNeverEscapesEvenIfTheFlagIsSet() {
        var t = template(EmailTemplateEngine.VELOCITY, true, "s", "<p>$name</p>");
        assertEquals("V:<p><b></p>", process(t, model("name", "<b>")).getContent());
    }

    @Test
    void aDynamiaTemplateUsesTheSafeEngineAndNeverCallsVelocity() {
        var t = template(EmailTemplateEngine.DYNAMIA, false, "Hi ${name}", "<p>{{name}}</p>");
        t.setTo("${mail}");
        var message = process(t, model("name", "Ada", "mail", "ada@x.com"));

        assertEquals("Hi Ada", message.getSubject());
        assertEquals("<p>Ada</p>", message.getContent());
        assertTrue(message.getTos().contains("ada@x.com"));
        assertTrue(velocity.calls.isEmpty());
    }

    @Test
    void escapingAppliesToTheBodyOnlyNotToTheSubjectOrTheAddresses() {
        var t = template(EmailTemplateEngine.DYNAMIA, true, "Re: ${name}", "<p>${name}</p><div>${html|raw}</div>");
        t.setReplyTo("${name}");
        var message = process(t, model("name", "A&B <x>", "html", "<i>ok</i>"));

        assertEquals("Re: A&B <x>", message.getSubject());
        assertEquals("<p>A&amp;B &lt;x&gt;</p><div><i>ok</i></div>", message.getContent());
        assertEquals("A&B <x>", message.getReplyTo());
    }

    @Test
    void aTemplateIsNotRenderedTwiceOrWithTheWrongEngineWhenBlank() {
        var t = template(EmailTemplateEngine.DYNAMIA, true, "s", "x");
        assertEquals(null, renderer.render(t, null, Map.of()));
        assertEquals("  ", renderer.renderHtml(t, "  ", Map.of()));
        assertTrue(velocity.calls.isEmpty());
    }

    @Test
    void childAndParentEachUseTheirOwnEngine() {
        // child Velocity, parent Dynamia (escaping): the child HTML must be passed with raw
        var child = template(EmailTemplateEngine.VELOCITY, false, "s", "<p>$name</p>");
        var parentRaw = template(EmailTemplateEngine.DYNAMIA, true, "s", "<div>{{ TEMPLATE_CONTENT | raw }}</div>");
        child.setParent(parentRaw);
        assertEquals("<div>V:<p>Ada</p></div>", process(child, model("name", "Ada")).getContent());

        var parentForgotRaw = template(EmailTemplateEngine.DYNAMIA, true, "s", "<div>{{ TEMPLATE_CONTENT }}</div>");
        child.setParent(parentForgotRaw);
        assertEquals("<div>V:&lt;p&gt;Ada&lt;/p&gt;</div>", process(child, model("name", "Ada")).getContent());

        // child Dynamia, parent Velocity
        var dChild = template(EmailTemplateEngine.DYNAMIA, true, "s", "<p>${name}</p>");
        var vParent = template(EmailTemplateEngine.VELOCITY, false, "s", "<div>$TEMPLATE_CONTENT</div>");
        dChild.setParent(vParent);
        assertEquals("V:<div><p>A&amp;B</p></div>", process(dChild, model("name", "A&B")).getContent());

        // Dynamia and Dynamia, Velocity and Velocity
        var dParent = template(EmailTemplateEngine.DYNAMIA, true, "s", "<main>{{TEMPLATE_CONTENT|raw}}</main>");
        dChild.setParent(dParent);
        assertEquals("<main><p>A&amp;B</p></main>", process(dChild, model("name", "A&B")).getContent());

        var vChild = template(null, false, "s", "<p>$name</p>");
        vChild.setParent(vParent);
        assertEquals("V:<div>V:<p>Ada</p></div>", process(vChild, model("name", "Ada")).getContent());
    }

    @Test
    void theServiceRendersATemplateForApplicationsWithoutSending() {
        var t = template(EmailTemplateEngine.DYNAMIA, true, "s", "<p>${n}</p>");
        assertEquals("<p>&lt;1&gt;</p>", service.render(t, model("n", "<1>")));
        assertEquals("<p>${n}</p>", service.render(t, null));
        assertEquals("<p>${n}</p>", t.getContent(), "the template is not changed");
    }

    @Test
    void theSmsTextAndNumberUseTheEngineOfTheTemplate() {
        var sent = new ArrayList<SMSMessage>();
        var smsService = (SMSService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{SMSService.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("send")) {
                        sent.add((SMSMessage) args[0]);
                        return "id";
                    }
                    return null;
                });
        var listener = new AutoSendSMSFromEmailServiceListener(smsService, renderer);

        for (var engine : new EmailTemplateEngine[]{EmailTemplateEngine.DYNAMIA, null}) {
            sent.clear();
            var dynamia = engine == EmailTemplateEngine.DYNAMIA;
            var t = template(engine, true, "s", "x");
            t.setSendSMS(true);
            t.setSmsNumber(dynamia ? "${phone}" : "$phone");
            t.setSmsText(dynamia ? "Hi ${name} {{#if vip}}VIP{{/if}}" : "Hi $name");
            var account = new EmailAccount();
            account.setSmsEnabled(true);
            account.setSmsUsername("u");
            account.setSmsPassword("p");
            var message = new EmailMessage(t);
            message.setMailAccount(account);
            message.setTemplateModel(model("phone", "+573001", "name", "A&B", "vip", true));

            listener.onMailSended(message);

            assertEquals(1, sent.size());
            assertEquals(dynamia ? "+573001" : "V:+573001", sent.get(0).getPhoneNumber());
            assertEquals(dynamia ? "Hi A&B VIP" : "V:Hi A&B", sent.get(0).getText());
        }
    }
}
