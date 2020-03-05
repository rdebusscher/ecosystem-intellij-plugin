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

import fish.payara.server.config.PayaraPortConfig;
import fish.payara.server.config.PayaraSecuredConfig;
import com.intellij.javaee.oss.glassfish.server.GlassfishServerModel;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import static fish.payara.PayaraConstants.PAYARA_LOG_FILE_ID;
import fish.payara.server.config.PayaraDomainConfigProcessor;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import static java.util.Collections.singletonList;
import java.util.List;
import static java.util.stream.Collectors.toList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public class PayaraLocalInstanceModel extends GlassfishServerModel {

    @NonNls
    public String DOMAIN_NAME = "domain1";

    private PayaraDomainConfigProcessor configProcessor;

    @Override
    protected boolean isSecured() {
        return PayaraSecuredConfig.isAdminSecured(this);
    }

    @Override
    protected int getServerPort() {
        return PayaraPortConfig.getAdminPort(this);
    }

    @Override
    public SettingsEditor getEditor() {
        return new PayaraLocalInstanceConfigurationEditor(PayaraServerIntegration.getInstance());
    }

    public File getAsadminExecutablePath() {
        return new File(
                getHome() + File.separator
                + "bin" + File.separator
                + (SystemInfo.isWindows ? "asadmin.bat" : "asadmin")
        );
    }

    public String getBaseDir() {
        return getHome() + File.separator + "glassfish";
    }

    public String getDomainsDir() {
        return getBaseDir() + File.separator + "domains";
    }

    public String getDomainDir() {
        return FileUtil.isAbsolute(DOMAIN_NAME) ? DOMAIN_NAME : getDomainsDir() + File.separator + DOMAIN_NAME;
    }

    @Nullable
    protected String getLogFilePath() {
        return new File(getDomainDir() + File.separator + "logs", "server.log").getAbsolutePath();
    }

    public File getDomainConfig() {
        return new File(getDomainDir() + File.separator + "config", "domain.xml");
    }

    public List<String> getDomains() {
        List<String> domains = Collections.emptyList();
        File[] files = new File(getDomainsDir()).listFiles();
        if (files != null) {
            domains = Arrays.stream(files)
                    .filter(File::isDirectory)
                    .map(File::getName)
                    .collect(toList());
        }
        return domains;
    }

    public PayaraDomainConfigProcessor getDomainConfigProcessor() {
        if (configProcessor == null) {
            this.configProcessor = new PayaraDomainConfigProcessor(getDomainConfig());
        }
        return configProcessor;
    }

    @Override
    protected List<LogFileFactory> getLogFileFactories() {
        return singletonList(new PayaraLogFileFactory());
    }

    private class PayaraLogFileFactory extends DefaultLogFileFactory {

        @Override
        protected String getPath() {
            return PayaraLocalInstanceModel.this.getLogFilePath();
        }

        @Override
        protected String getName() {
            return PAYARA_LOG_FILE_ID;
        }
    }

}
