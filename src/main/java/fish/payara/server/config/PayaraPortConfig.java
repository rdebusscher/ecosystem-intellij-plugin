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

import fish.payara.server.PayaraLocalInstanceModel;
import static fish.payara.PayaraConstants.ADMIN_VIRTUAL_SERVER_ID;
import static fish.payara.PayaraConstants.DEFAULT_ADMIN_PORT;
import static fish.payara.PayaraConstants.PORT_ATTR;
import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class PayaraPortConfig extends PayaraConfig {

    private static final PayaraConfig.PayaraConfigFactory<PayaraPortConfig> ADMIN_SERVER_FACTORY;

    private final String virtualServerId;
    private int port = DEFAULT_ADMIN_PORT;

    private PayaraPortConfig(PayaraLocalInstanceModel model, String virtualServerId) {
        super(model);
        this.virtualServerId = virtualServerId;
    }

    @Override
    protected void update(PayaraLocalInstanceModel model) {
        List<Element> listeners = model.getDomainConfigProcessor()
                .getNetworkListenersFromVirtualServer(this.virtualServerId);
        if (!listeners.isEmpty()) {
            String portValue = listeners.get(0).getAttributeValue(PORT_ATTR);
            if (portValue != null) {
                try {
                    this.port = Integer.parseInt(portValue);
                } catch (NumberFormatException nfe) {
                    this.port = DEFAULT_ADMIN_PORT;
                }
            }
        }
    }

    public static int getAdminPort(PayaraLocalInstanceModel model) {
        PayaraPortConfig config = PayaraPortConfig.ADMIN_SERVER_FACTORY.get(model);
        return config != null ? config.port : DEFAULT_ADMIN_PORT;
    }

    static {
        ADMIN_SERVER_FACTORY = new PayaraConfig.PayaraConfigFactory<PayaraPortConfig>() {
            @NotNull
            @Override
            public PayaraPortConfig createConfig(PayaraLocalInstanceModel model) {
                return new PayaraPortConfig(model, ADMIN_VIRTUAL_SERVER_ID);
            }
        };
    }

}
