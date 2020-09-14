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

import com.intellij.execution.configurations.DebuggingRunnerData;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.util.EnvironmentVariable;
import com.intellij.javaee.oss.server.JavaeeParameters;
import com.intellij.javaee.oss.server.JavaeeStartupPolicy;
import com.intellij.javaee.run.localRun.ExecutableObject;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import fish.payara.server.config.PayaraDebugConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PayaraStartupPolicy extends JavaeeStartupPolicy<PayaraLocalInstanceModel> {

    private static final String JAVA_HOME_ENV_KEY = "JAVA_HOME";
    private static final String PATH_ENV_KEY = "PATH";
    private static final String START_DOMAIN_CMD = "start-domain";
    private static final String STOP_DOMAIN_CMD = "stop-domain";
    private static final String DEBUG_OPTION = "--debug";
    private static final String DOMAIN_DIR_OPTION = "--domaindir";

    @Override
    protected List<EnvironmentVariable> getEnvironmentVariables(PayaraLocalInstanceModel model) {
        List<EnvironmentVariable> vars = new ArrayList<>();
        String jrePath = model.getJrePath();
        if (jrePath != null) {
            vars.add(new EnvironmentVariable(JAVA_HOME_ENV_KEY, jrePath, true));
            if (SystemInfo.isWindows) {
                vars.add(new EnvironmentVariable(PATH_ENV_KEY, jrePath + File.separator + "bin;" + System.getenv(PATH_ENV_KEY), true));
            } else {
                vars.add(new EnvironmentVariable(PATH_ENV_KEY, jrePath + File.separator + "bin:" + System.getenv(PATH_ENV_KEY), true));
            }
        }
        return vars;
    }

    @Override
    protected void getStartupParameters(JavaeeParameters params, PayaraLocalInstanceModel model, boolean debug) {
        params.add(model.getAsadminExecutablePath());
        params.add(START_DOMAIN_CMD);
        if (debug) {
            params.add(DEBUG_OPTION);
        }
        addDomainParameters(params, model);
    }

    @Override
    protected void getShutdownParameters(JavaeeParameters params, PayaraLocalInstanceModel model, boolean debug) {
        params.add(model.getAsadminExecutablePath());
        params.add(STOP_DOMAIN_CMD);
        addDomainParameters(params, model);
    }

    private void addDomainParameters(JavaeeParameters params, PayaraLocalInstanceModel model) {
        if (FileUtil.isAbsolute(model.DOMAIN_NAME)) {
            File domain = new File(model.DOMAIN_NAME);
            params.add(DOMAIN_DIR_OPTION, domain.getParent(), domain.getName());
        } else {
            params.add(model.DOMAIN_NAME);
        }
    }

    @Override
    protected void initSettings(final PayaraLocalInstanceModel model, final DebuggingRunnerData runnerData) {
        runnerData.setDebugPort(PayaraDebugConfig.getPort(model));
    }

    @Override
    protected void checkSettings(final PayaraLocalInstanceModel model, final DebuggingRunnerData runnerData) throws RuntimeConfigurationException {
        PayaraDebugConfig.verifyDebugSettings(model, runnerData.getDebugPort());
    }

}
