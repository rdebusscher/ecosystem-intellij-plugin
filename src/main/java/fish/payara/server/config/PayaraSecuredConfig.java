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

import static fish.payara.PayaraConstants.ADMIN_VIRTUAL_SERVER_ID;
import static fish.payara.PayaraConstants.PORT_UNIFICATION_TAG;
import static fish.payara.PayaraConstants.PROTOCOL_ATTR;
import static fish.payara.PayaraConstants.PROTOCOL_FINDER_TAG;
import static fish.payara.PayaraConstants.SECURITY_ENABLED_ATTR;
import fish.payara.server.PayaraLocalModel;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jdom.Element;

public class PayaraSecuredConfig extends PayaraConfig {

    private static final PayaraConfigFactory<PayaraSecuredConfig> ADMIN_SERVER_FACTORY;

    private final String virtualServerId;
    private boolean secured;

    private PayaraSecuredConfig(PayaraLocalModel model, String virtualServerId) {
        super(model);
        this.secured = false;
        this.virtualServerId = virtualServerId;
    }

    @Override
    protected void update(PayaraLocalModel model) {
        this.secured = false;
        List<Element> listeners = model.getDomainConfigProcessor()
                .getNetworkListenersFromVirtualServer(this.virtualServerId);
        if (!listeners.isEmpty()) {
            String protocolName = listeners.get(0).getAttributeValue(PROTOCOL_ATTR);
            if (protocolName != null) {
                findSecuredProtocol(model, protocolName);
            }
        }
    }

    private void findSecuredProtocol(
            PayaraLocalModel model,
            String protocolName) {

        Element protocol = model.getDomainConfigProcessor().findProtocol(protocolName);
        if (protocol != null) {
            if (Boolean.parseBoolean(protocol.getAttributeValue(SECURITY_ENABLED_ATTR))) {
                this.secured = true;
            } else {
                Element portUnificaton = protocol.getChild(PORT_UNIFICATION_TAG);
                if (portUnificaton != null) {
                    getChildren(portUnificaton, PROTOCOL_FINDER_TAG)
                            .stream()
                            .map(finder -> finder.getAttributeValue(PROTOCOL_ATTR))
                            .filter(Objects::nonNull)
                            .forEach(subProtocolName -> this.findSecuredProtocol(model, subProtocolName));
                }
            }
        }
    }

    public static boolean isAdminSecured(PayaraLocalModel model) {
        PayaraSecuredConfig config = PayaraSecuredConfig.ADMIN_SERVER_FACTORY.get(model);
        return config != null && config.secured;
    }

    static {
        ADMIN_SERVER_FACTORY = new PayaraConfigFactory<PayaraSecuredConfig>() {
            @NotNull
            @Override
            public PayaraSecuredConfig createConfig(PayaraLocalModel model) {
                return new PayaraSecuredConfig(model, ADMIN_VIRTUAL_SERVER_ID);
            }
        };
    }
}
