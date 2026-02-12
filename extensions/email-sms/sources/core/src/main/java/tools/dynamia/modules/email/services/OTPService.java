package tools.dynamia.modules.email.services;

import tools.dynamia.modules.email.OTPMessage;
import tools.dynamia.modules.email.OTPSendResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Service interface to dispatch One-Time Password (OTP) messages.
 * <p>
 * Implementations may deliver OTP codes via email, SMS, or both, depending on configuration and
 * the contents of the provided {@link OTPMessage}. The service abstracts transport selection,
 * formatting, and delivery tracking, returning an asynchronous handle to inspect the result.
 * </p>
 * <p>
 * Typical usage: build an {@link OTPMessage} with target channels and the OTP value, then call {@link #send(OTPMessage)}.
 * </p>
 */
public interface OTPService {

    /**
     * Sends an OTP message using email, SMS, or both channels.
     * <p>
     * The selected transport(s) depend on implementation and the {@link OTPMessage} fields (e.g., presence of
     * email address, phone number, or explicit channel selection). Delivery may happen asynchronously; use the
     * returned {@link Future} to check completion and obtain the {@link OTPSendResult}.
     * </p>
     *
     * @param message Fully populated {@link OTPMessage} containing the OTP code, target recipient(s), preferred
     *                channel(s), expiration and metadata as required by the implementation. Must not be null.
     * @return a {@link Future} that completes with {@link OTPSendResult} indicating success, failure, and relevant
     * details (e.g., which channels were used). The future may complete exceptionally if an unrecoverable error occurs.
     */
    CompletableFuture<OTPSendResult> send(OTPMessage message);

}
