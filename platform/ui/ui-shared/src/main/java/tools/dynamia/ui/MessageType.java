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
package tools.dynamia.ui;

/**
 * Enumeration representing different types of messages that can be displayed in the UI.
 * This enum can be used to categorize messages based on their severity or purpose, such as normal informational messages,
 * warnings, errors, critical issues, or special notifications.
 *
 * @author Mario A. Serrano Leones
 */
public enum MessageType {

    NORMAL, ERROR, WARNING, INFO, CRITICAL, SPECIAL
}
