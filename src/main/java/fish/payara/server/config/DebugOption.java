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
package fish.payara.server.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DebugOption {

    private static final String TRANSPORT_NAME = "transport";
    private static final String ADDRESS_NAME = "address";

    private static final String SERVER_NAME = "server";
    public static final String SERVER_VALUE = "n";

    private static final String SUSPEND_NAME = "suspend";
    public static final String SUSPEND_VALUE = "y";

    public static final DebugOption TRANSPORT_OPTION = new DebugOption(TRANSPORT_NAME, "\\w+");
    public static final DebugOption PORT_OPTION = new DebugOption(ADDRESS_NAME, "\\d+");
    public static final DebugOption SERVER_OPTION = new DebugOption(SERVER_NAME, "[yn]");
    public static final DebugOption SUSPEND_OPTION = new DebugOption(SUSPEND_NAME, "[yn]");
    
    public static final String OPTION_REGEX = "(?:.*\\W)?%s=(%s)(?:,.*)?";

    private final String key;
    private final Pattern pattern;

    private DebugOption(String key, String valuePattern) {
        this.key = key;
        this.pattern = Pattern.compile(String.format(OPTION_REGEX, key, valuePattern));
    }

    public String getValue(String options) {
        if (options != null) {
            Matcher matcher = pattern.matcher(options);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    public String join(String value) {
        return key + '=' + value;
    }
}
