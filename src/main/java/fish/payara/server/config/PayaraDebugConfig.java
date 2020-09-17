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

import com.intellij.debugger.impl.RemoteConnectionBuilder;
import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import fish.payara.PayaraBundle;
import static fish.payara.PayaraConstants.DEFAULT_DEBUG_PORT;
import fish.payara.server.PayaraLocalInstanceModel;
import org.jdom.Element;

import static fish.payara.server.config.DebugOption.*;

public class PayaraDebugConfig extends PayaraConfig {

    private static final Logger LOGGER = Logger.getInstance(PayaraDebugConfig.class);

    private static final String DEBUG_OPTIONS_NAME = "debug-options";
    private static final String DEBUG_OPTIONS_SEPARATOR = ",";

    private static final String DEBUG_ENABLED_NAME = "debug-enabled";
    private static final String DEBUG_ENABLED_VALUE = "false";

    private static final String FLAG_PARAMETER = "-Xdebug";
    private static final String JDWP_PARAMETER_OLD_PREFIX = "-Xrunjdwp:";
    private static final String JDWP_PARAMETER_NEW_PREFIX = "-agentlib:jdwp=";

    private static final PayaraConfigFactory<PayaraDebugConfig> FACTORY = new PayaraConfigFactory<PayaraDebugConfig>() {
        @Override
        public PayaraDebugConfig createConfig(PayaraLocalInstanceModel model) {
            return new PayaraDebugConfig(model);
        }
    };

    private String server;
    private String transport;
    private String port;
    private String suspend;
    private boolean disabled;

    public static boolean isDebugParameter(String vmParameter) {
        return FLAG_PARAMETER.equals(vmParameter)
                || vmParameter.startsWith(JDWP_PARAMETER_NEW_PREFIX)
                || vmParameter.startsWith(JDWP_PARAMETER_OLD_PREFIX);
    }

    private PayaraDebugConfig(PayaraLocalInstanceModel model) {
        super(model);
    }

    @Override
    protected void update(PayaraLocalInstanceModel model) {
        reset();
        Element javaConfig = model.getDomainConfigProcessor().getJavaConfig();
        String debugOptions = javaConfig.getAttributeValue(DEBUG_OPTIONS_NAME);
        String debugEnabled = javaConfig.getAttributeValue(DEBUG_ENABLED_NAME);
        this.server = SERVER_OPTION.getValue(debugOptions);
        this.transport = TRANSPORT_OPTION.getValue(debugOptions);
        this.port = PORT_OPTION.getValue(debugOptions);
        this.suspend = SUSPEND_OPTION.getValue(debugOptions);
        this.disabled = StringUtil.isEmpty(debugEnabled) || DEBUG_ENABLED_VALUE.equals(debugEnabled);
    }

    private void reset() {
        this.server = null;
        this.transport = null;
        this.port = null;
        this.suspend = null;
    }

    public static String getPort(PayaraLocalInstanceModel model) {
        PayaraDebugConfig config = PayaraDebugConfig.FACTORY.get(model);
        return config != null && config.port != null ? config.port : String.valueOf(DEFAULT_DEBUG_PORT);
    }

    public static void verifyDebugSettings(PayaraLocalInstanceModel model, String port) throws RuntimeConfigurationException {
        JavaParameters javaParameters = new JavaParameters();
        try {
            javaParameters.setJdk(model.getJre());
            int transport = DebuggerSettings.getInstance().getTransport();
            RemoteConnectionBuilder builder = new RemoteConnectionBuilder(true, transport, port);
            builder.create(javaParameters);
        } catch (ExecutionException e) {
            LOGGER.error(e);
            return;
        }
        boolean flagPresent = false, newJdwpPrefix = false;
        String transport = null;
        for (String vmParameter : javaParameters.getVMParametersList().getList()) {
            if (FLAG_PARAMETER.equals(vmParameter)) {
                flagPresent = true;
            } else {
                if (!vmParameter.startsWith(JDWP_PARAMETER_NEW_PREFIX)
                        && !vmParameter.startsWith(JDWP_PARAMETER_OLD_PREFIX)) {
                    continue;
                }
                if (vmParameter.startsWith(JDWP_PARAMETER_NEW_PREFIX)) {
                    newJdwpPrefix = true;
                }
                if (transport == null) {
                    transport = TRANSPORT_OPTION.getValue(vmParameter);
                }
            }
        }
        PayaraDebugConfig config = FACTORY.get(model);
        if (config != null
                && !config.isValidDebugSettings(transport, port)) {
            sendWarning(config, model, port, flagPresent, newJdwpPrefix);
        }
    }

    private static void sendWarning(
            PayaraDebugConfig config,
            PayaraLocalInstanceModel model,
            String port,
            boolean flagPresent,
            boolean newJdwpPrefix) throws RuntimeConfigurationException {

        RuntimeConfigurationException warning = new RuntimeConfigurationWarning(
                PayaraBundle.message("PayaraDebugConfig.warning.debug.settings", port)
        );
        if (config.getDomainConfig().canWrite()) {
            String newDebugOptions = config.createDebugOptions(flagPresent, newJdwpPrefix);
            warning.setQuickFix(()
                    -> model
                            .getDomainConfigProcessor()
                            .updateJavaConfig(javaConfig -> {
                                javaConfig.setAttribute(DEBUG_OPTIONS_NAME, newDebugOptions);
                                javaConfig.setAttribute(DEBUG_ENABLED_NAME, DEBUG_ENABLED_VALUE);
                                return true;
                            })
            );
        }
        throw warning;
    }

    private boolean isValidDebugSettings(String transport, String port) {
        return Comparing.equal(this.transport, transport)
                && Comparing.equal(this.port, port)
                && SERVER_VALUE.equals(this.server)
                && SUSPEND_VALUE.equals(this.suspend)
                && this.disabled;
    }

    private String createDebugOptions(boolean flagPresent, boolean newJdwpPrefix) {
        StringBuilder debugOptions = new StringBuilder();
        if (flagPresent) {
            debugOptions.append(FLAG_PARAMETER);
            debugOptions.append(' ');
        }
        return debugOptions
                .append(newJdwpPrefix ? JDWP_PARAMETER_NEW_PREFIX : JDWP_PARAMETER_OLD_PREFIX)
                .append(TRANSPORT_OPTION.join(transport))
                .append(DEBUG_OPTIONS_SEPARATOR)
                .append(PORT_OPTION.join(port))
                .append(DEBUG_OPTIONS_SEPARATOR)
                .append(SERVER_OPTION.join(SERVER_VALUE))
                .append(DEBUG_OPTIONS_SEPARATOR)
                .append(SUSPEND_OPTION.join(SUSPEND_VALUE))
                .toString();
    }

}
