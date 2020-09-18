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

import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.DebuggingRunnerData;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.javaee.oss.server.ServerSupportContributorBase;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.supportProvider.JavaeeFrameworkSupportContributionModel;
import fish.payara.server.config.PayaraDebugConfig;

public class PayaraServerSupportContributor extends ServerSupportContributorBase<PayaraLocalInstanceModel> {

    public PayaraServerSupportContributor() {
        super(PayaraLocalInstanceModel.class);
    }

    @Override
    protected void doSetupServerRunConfiguration(
            PayaraLocalInstanceModel localModel,
            CommonModel commonModel,
            JavaeeFrameworkSupportContributionModel frameworkModel) {

        if (!localModel.getDomains().isEmpty()) {
            localModel.DOMAIN_NAME = localModel.getDomains().get(0);
        }
        String debugPort = PayaraDebugConfig.getPort(localModel);

        ProgramRunner runner = ProgramRunner.getRunner(DefaultDebugExecutor.EXECUTOR_ID, commonModel);
        if (runner != null) {
            RunManager.getInstance(commonModel.getProject())
                    .getConfigurationSettingsList(commonModel.getType())
                    .stream()
                    .filter(settings -> settings.getConfiguration().equals(commonModel))
                    .map(settings -> settings.getRunnerSettings(runner))
                    .filter(DebuggingRunnerData.class::isInstance)
                    .forEach(runnerData -> ((DebuggingRunnerData)runnerData).setDebugPort(debugPort));
        }
    }
}
