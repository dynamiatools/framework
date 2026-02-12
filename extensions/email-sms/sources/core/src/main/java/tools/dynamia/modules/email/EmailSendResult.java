/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tools.dynamia.modules.email;
/**
 * Email sending process result.
 */
public class EmailSendResult {

    private EmailMessage message;
    private boolean sended;
    private Exception exception;
    private String cause;

    public EmailSendResult(EmailMessage message, boolean sended, String reason) {
        this.message = message;
        this.sended = sended;
        this.cause = reason;
    }

    public EmailSendResult(EmailMessage message, Exception exception) {
        this.message = message;
        this.sended = false;
        this.exception = exception;
        this.cause = exception.getMessage();
    }

    public Exception getException() {
        return exception;
    }


    public EmailMessage getMessage() {
        return message;
    }

    public boolean isSended() {
        return sended;
    }

    public String getCause() {
        return cause;
    }
}
