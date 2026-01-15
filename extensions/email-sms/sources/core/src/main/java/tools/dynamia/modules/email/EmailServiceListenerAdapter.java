package tools.dynamia.modules.email;

public class EmailServiceListenerAdapter implements EmailServiceListener{
    @Override
    public void onMailProcessing(EmailMessage message) {

    }

    @Override
    public void onMailSending(EmailMessage message) {

    }

    @Override
    public void onMailSended(EmailMessage message) {

    }

    @Override
    public void onMailSendFail(EmailMessage message, Throwable cause) {

    }
}
