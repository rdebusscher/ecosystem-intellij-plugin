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

import com.intellij.javaee.oss.util.ConfigBase;
import com.intellij.javaee.util.JavaeeJdomUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import static fish.payara.PayaraConstants.CONFIGS_TAG;
import static fish.payara.PayaraConstants.CONFIG_TAG;
import static fish.payara.PayaraConstants.HTTP_SERVICE_TAG;
import static fish.payara.PayaraConstants.ID_ATTR;
import static fish.payara.PayaraConstants.JAVA_CONFIG_TAG;
import static fish.payara.PayaraConstants.NAME_ATTR;
import static fish.payara.PayaraConstants.NETWORK_CONFIG_TAG;
import static fish.payara.PayaraConstants.NETWORK_LISTENERS_ATTR;
import static fish.payara.PayaraConstants.NETWORK_LISTENERS_TAG;
import static fish.payara.PayaraConstants.NETWORK_LISTENER_TAG;
import static fish.payara.PayaraConstants.PROTOCOLS_TAG;
import static fish.payara.PayaraConstants.PROTOCOL_ATTR;
import static fish.payara.PayaraConstants.PROTOCOL_TAG;
import static fish.payara.PayaraConstants.SERVER_CONFIG;
import static fish.payara.PayaraConstants.VIRTUAL_SERVER_TAG;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

//    <configs>
//      <config name="server-config">
//         <java-config  debug-options="..." >
//             <jvm-options>-server</jvm-options>
//         </java-config>
//         <network-config>
//             <protocols>
//                  <protocol name="admin-listener">
//                      <http default-virtual-server="__asadmin" />
//                  </protocol>
//             </protocols>
//             <network-listeners>
//                  <network-listener protocol="admin-listener" port="4848" name="admin-listener" thread-pool="admin-thread-pool" transport="tcp"/>
//             </network-listeners>
//          </network-config>
//          <http-service>
//                  <virtual-server network-listeners="http-listener-1,http-listener-2" id="server"/>
//                  <virtual-server network-listeners="admin-listener" id="__asadmin"/>
//          </http-service>
//        </config>
//    </configs>
public class PayaraDomainConfigProcessor extends ConfigBase {

    private static final Logger LOGGER = Logger.getInstance(PayaraDomainConfigProcessor.class);

    protected final File domainConfig;

    public PayaraDomainConfigProcessor(File domainConfig) {
        this.domainConfig = domainConfig;
    }

    public Element getServerConfig() {
        Element serverConfig = null;
        if (!this.domainConfig.exists()) {
            return null;
        }
        try {
            Document doc = JavaeeJdomUtil.loadDocument(this.domainConfig);
            Element root = doc.getRootElement();
            Element configs = root.getChild(CONFIGS_TAG);
            serverConfig = getChild(configs, CONFIG_TAG, NAME_ATTR, SERVER_CONFIG);
        } catch (JDOMException | IOException ex) {
            LOGGER.error(ex);
        }
        return serverConfig;
    }

    public Element getNetworkListener(String id) {
        Element serverConfig = getServerConfig();
        Element networkConfig = serverConfig.getChild(NETWORK_CONFIG_TAG);
        if (networkConfig == null) {
            return null;
        }

        Element networkListeners = networkConfig.getChild(NETWORK_LISTENERS_TAG);
        if (networkListeners == null) {
            return null;
        }

        Element networkListener = getChild(networkListeners, NETWORK_LISTENER_TAG, NAME_ATTR, id);
        if (networkListener == null) {
            networkListener = getChild(networkListeners, NETWORK_LISTENER_TAG, PROTOCOL_ATTR, id);
        }
        return networkListener;
    }

    public Element getVirtualServer(String id) {
        Element virtualServer = null;
        Element serverConfig = getServerConfig();
        Element httpService = serverConfig.getChild(HTTP_SERVICE_TAG);
        if (httpService != null) {
            if (StringUtil.isEmpty(id)) {
                virtualServer = getChild(httpService, VIRTUAL_SERVER_TAG);
            } else {
                virtualServer = getChild(httpService, VIRTUAL_SERVER_TAG, ID_ATTR, id);
            }
        }
        return virtualServer;
    }

    public List<Element> getNetworkListenersFromVirtualServer(String virtualServerId) {
        List<Element> listeners = new ArrayList<>();
        Element virtualServer = getVirtualServer(virtualServerId);
        if (virtualServer != null) {
            String networkListenerIds = virtualServer.getAttributeValue(NETWORK_LISTENERS_ATTR);
            if (networkListenerIds != null) {
                for (String networkListenerId : networkListenerIds.split(",")) {
                    Element listener = getNetworkListener(networkListenerId);
                    if (listener != null) {
                        listeners.add(listener);
                    }
                }
            }
        }
        return listeners;
    }

    public Element findProtocol(String protocolName) {
        Element serverConfig = getServerConfig();
        Element networkConfig = serverConfig.getChild(NETWORK_CONFIG_TAG);
        Element protocols = networkConfig.getChild(PROTOCOLS_TAG);
        if (protocols != null) {
            return getChild(protocols, PROTOCOL_TAG, NAME_ATTR, protocolName);
        }
        return null;
    }

    public Element getJavaConfig() {
        Element serverConfig = getServerConfig();
        return getOrCreateChild(serverConfig, JAVA_CONFIG_TAG);
    }

    public void updateJavaConfig(ConfigProcessorFunction worker) {
        try {
            File config = this.domainConfig;
            Document document = JavaeeJdomUtil.loadDocument(config);
            Element root = document.getRootElement();
            Element configs = root.getChild(CONFIGS_TAG);
            Element serverConfig = getChild(configs, CONFIG_TAG, NAME_ATTR, SERVER_CONFIG);
            Element javaConfig = getOrCreateChild(serverConfig, JAVA_CONFIG_TAG);
            if (worker.apply(javaConfig)) {
                File tmpConfig = FileUtil.createTempFile(config.getParentFile(), config.getName(), null, true);
                JDOMUtil.writeDocument(document, tmpConfig, "\n");
                LOGGER.assertTrue(config.delete());
                LOGGER.assertTrue(tmpConfig.renameTo(config));
            }
        } catch (JDOMException | IOException ex) {
            LOGGER.error(ex);
        }
    }
}
