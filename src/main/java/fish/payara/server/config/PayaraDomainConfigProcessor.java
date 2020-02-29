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
import java.io.File;
import java.io.IOException;
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
//           </network-config>
//        </config>
//    </configs>
public class PayaraDomainConfigProcessor extends ConfigBase {

    private static final Logger LOGGER = Logger.getInstance(PayaraDomainConfigProcessor.class);

    private static final String CONFIGS_TAG = "configs";
    private static final String CONFIG_TAG = "config";
    private static final String SERVER_CONFIG = "server-config";
    private static final String JAVA_CONFIG_TAG = "java-config";
    private static final String NETWORK_CONFIG_TAG = "network-config";
    private static final String NETWORK_LISTENERS_TAG = "network-listeners";
    private static final String NETWORK_LISTENER_TAG = "network-listener";
    private static final String PROTOCOLS_TAG = "protocols";
    private static final String PROTOCOL_TAG = "protocol";
    private static final String PROTOCOL_ATTR = "protocol";
    private static final String NAME_ATTR = "name";

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
