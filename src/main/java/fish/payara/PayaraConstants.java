/*
 * Copyright (c) 2020 Payara Foundation and/or its affiliates and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package fish.payara;

import com.intellij.openapi.util.IconLoader;
import java.io.File;
import java.util.regex.Pattern;
import javax.swing.Icon;

public interface PayaraConstants {

    Icon PAYARA_ICON = IconLoader.getIcon("/icons/payara.svg");

    Pattern PAYARA_JAR_PATTERN = Pattern.compile("payara-.*\\.jar");

    String PAYARA_MODULES_DIRECTORY_NAME = "glassfish" + File.separator + "modules";

    String PAYARA_BIN_DIRECTORY_NAME = "bin";

    String ADMIN_VIRTUAL_SERVER_ID = "__asadmin";
    int DEFAULT_ADMIN_PORT = 4848;

    String PAYARA_LOG_FILE_ID = "Payara";

    String CONFIGS_TAG = "configs";
    String CONFIG_TAG = "config";
    String SERVER_CONFIG = "server-config";
    String JAVA_CONFIG_TAG = "java-config";
    String NETWORK_CONFIG_TAG = "network-config";
    String NETWORK_LISTENERS_TAG = "network-listeners";
    String NETWORK_LISTENER_TAG = "network-listener";
    String PROTOCOLS_TAG = "protocols";
    String PROTOCOL_TAG = "protocol";
    String HTTP_SERVICE_TAG = "http-service";
    String VIRTUAL_SERVER_TAG = "virtual-server";
    String PORT_UNIFICATION_TAG = "port-unification";
    String PROTOCOL_FINDER_TAG = "protocol-finder";
    String NETWORK_LISTENERS_ATTR = "network-listeners";
    String SECURITY_ENABLED_ATTR = "security-enabled";
    String PROTOCOL_ATTR = "protocol";
    String PORT_ATTR = "port";
    String NAME_ATTR = "name";
    String ID_ATTR = "id";
}
