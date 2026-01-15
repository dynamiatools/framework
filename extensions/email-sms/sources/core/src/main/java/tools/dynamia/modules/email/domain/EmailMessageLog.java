package tools.dynamia.modules.email.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tools.dynamia.modules.email.EmailMessage;
import tools.dynamia.modules.saas.jpa.BaseEntitySaaS;

@Entity
@Table(name = "email_log")
public class EmailMessageLog extends BaseEntitySaaS {

    @Column(length = 1000)
    private String subject;
    @Column(length = 1000, name = "log_to")
    private String to;
    @Column(length = 1000)
    private String additionalAddress;

    @Column(name = "log_sended")
    private boolean sended;
    @Column(length = 1000, name = "log_result")
    private String result;
    private boolean notification;
    private String notificationId;
    @Column(name = "log_from")
    private String from;

    public EmailMessageLog() {
    }

    public EmailMessageLog(EmailMessage message) {
        this.subject = message.getSubject();
        this.to = message.getTo();
        var otherAddress = message.loadAllAddresses();
        if (!otherAddress.isEmpty()) {
            this.additionalAddress = String.join(",", message.loadAllAddresses());
        }
        this.notification = message.isNotification();
        this.notificationId = message.getNotificationUuid();
        this.sended = message.isSended();
        this.from = message.getMailAccount() != null ? message.getMailAccount().getFromAddress() : null;
        setAccountId(message.getAccountId());
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getAdditionalAddress() {
        return additionalAddress;
    }

    public void setAdditionalAddress(String additionalAddress) {
        this.additionalAddress = additionalAddress;
    }


    public boolean isSended() {
        return sended;
    }

    public void setSended(boolean sended) {
        this.sended = sended;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return subject;
    }
}
