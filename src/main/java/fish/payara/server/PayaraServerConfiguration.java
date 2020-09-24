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
package fish.payara.server;

import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.javaee.appServers.run.configuration.ServerModel;
import com.intellij.javaee.appServers.run.localRun.ExecutableObjectStartupPolicy;
import com.intellij.javaee.oss.server.JavaeeConfigurationType;
import com.intellij.javaee.oss.server.JavaeeIntegration;
import org.jetbrains.annotations.NotNull;

public class PayaraServerConfiguration extends JavaeeConfigurationType {

    public static PayaraServerConfiguration getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(PayaraServerConfiguration.class);
    }

    public PayaraServerConfiguration() {
        super(PayaraServerConfiguration.class.getSimpleName());
    }

    @NotNull
    @Override
    public JavaeeIntegration getIntegration() {
        return PayaraServerIntegration.getInstance();
    }

    @Override
    @NotNull
    protected ServerModel createLocalModel() {
        return new PayaraLocalInstanceModel();
    }

    @Override
    @NotNull
    protected ServerModel createRemoteModel() {
        return new PayaraRemoteInstanceModel();
    }

    @Override
    @NotNull
    protected ExecutableObjectStartupPolicy createStartupPolicy() {
        return new PayaraStartupPolicy();
    }

}
