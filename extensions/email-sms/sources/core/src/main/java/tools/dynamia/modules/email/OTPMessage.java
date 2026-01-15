package tools.dynamia.modules.email;

import tools.dynamia.commons.StringUtils;

import java.io.Serializable;
import java.util.Random;
import java.util.function.Function;

/**
 * Simple OTP (One-Time Password) message
 */
public class OTPMessage implements Serializable {

    /**
     * Generates an OTPMessage for sending an SMS with the specified target phone number, OTP type,
     * and content generator function.
     *
     * @param targetPhoneNumber   The phone number to which the OTP message will be sent.
     * @param type                The type of OTP to be generated (numeric or text).
     * @param contentGenerator    The function that generates the content of the OTP message.
     * @return The generated OTPMessage for sending the SMS.
     */
    public static OTPMessage sms(String targetPhoneNumber, OTPType type, Function<String, String> contentGenerator) {
        var msg = new OTPMessage();
        msg.sendSMS = true;
        msg.targetPhoneNumber = targetPhoneNumber;
        if (type == OTPType.NUMERIC) {
            msg.generateNumericCode();
        } else {
            msg.generateTextCode();
        }
        msg.content = contentGenerator.apply(msg.otp);
        return msg;
    }

    /**
     * Generates an OTPMessage for sending an email with the specified target email address, OTP type, email subject, and content generator function.
     *
     * @param targetEmail         The email address to which the OTP message will be sent.
     * @param type                The type of OTP to be generated (numeric or text).
     * @param emailSubject        The subject of the email.
     * @param contentGenerator    The function that generates the content of the OTP message.
     * @return The generated OTPMessage for sending the email.
     */
    public static OTPMessage email(String targetEmail, OTPType type, String emailSubject, Function<String, String> contentGenerator) {
        var msg = new OTPMessage();
        msg.sendEmail = true;
        msg.targetEmail = targetEmail;
        if (type == OTPType.NUMERIC) {
            msg.generateNumericCode();
        } else {
            msg.generateTextCode();
        }
        msg.emailSubject = emailSubject;
        msg.content = contentGenerator.apply(msg.otp);
        return msg;
    }

    private String otp;
    private String targetEmail;
    private String targetPhoneNumber;
    private boolean sendEmail;
    private boolean sendSMS;
    private String content;
    private String emailSubject;
    private Long accountId;


    /**
     * Represents an OTP (One-Time Password) message that can be sent via SMS or email.
     */
    public OTPMessage() {
    }


    /**
     * Generate 6 length numeric code using random integer
     */
    public void generateNumericCode() {
        int number = 100000 + new Random().nextInt(900000);
        otp = String.valueOf(number);
    }

    /**
     * Generate a 6 length text code using a random string
     */
    public void generateTextCode() {
        otp = StringUtils.randomString().substring(0, 6).toUpperCase();
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getTargetEmail() {
        return targetEmail;
    }

    public void setTargetEmail(String targetEmail) {
        this.targetEmail = targetEmail;
    }

    public String getTargetPhoneNumber() {
        return targetPhoneNumber;
    }

    public void setTargetPhoneNumber(String targetPhoneNumber) {
        this.targetPhoneNumber = targetPhoneNumber;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public boolean isSendSMS() {
        return sendSMS;
    }

    public void setSendSMS(boolean sendSMS) {
        this.sendSMS = sendSMS;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }


    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public OTPMessage withAccountId(Long accountId) {
        this.accountId = accountId;
        return this;
    }

    public enum OTPType {
        NUMERIC, TEXT
    }


}
