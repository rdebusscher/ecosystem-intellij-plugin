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

import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.openapi.ex.AppServerIntegrationsManager;
import com.intellij.javaee.oss.descriptor.JavaeeDescriptorsManager;
import com.intellij.javaee.oss.glassfish.GlassfishUtil;
import com.intellij.javaee.oss.glassfish.model.GlassfishWebRoot;
import com.intellij.javaee.oss.glassfish.server.GlassfishServerVersionConfig;
import com.intellij.javaee.oss.server.DefaultTemplateMatcher;
import com.intellij.javaee.oss.server.JavaeeIntegration;
import static com.intellij.javaee.oss.server.JavaeeIntegration.checkDir;
import com.intellij.javaee.oss.server.JavaeePersistentData;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import fish.payara.PayaraBundle;
import org.jetbrains.annotations.NotNull;
import static fish.payara.PayaraConstants.PAYARA_BIN_DIRECTORY_NAME;
import static fish.payara.PayaraConstants.PAYARA_JAR_PATTERN;
import static fish.payara.PayaraConstants.PAYARA_MODULES_DIRECTORY_NAME;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.Icon;

public class PayaraIntegration extends JavaeeIntegration {

    public static PayaraIntegration getInstance() {
        return AppServerIntegrationsManager
                .getInstance()
                .getIntegration(PayaraIntegration.class);
    }

    @NotNull
    @Override
    public String getName() {
        return PayaraBundle.message("payara.server.name");
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/icons/payara.svg");
    }

    @NotNull
    @Override
    public Icon getBigIcon() {
        return getIcon();
    }

    @Override
    public String getNameFromTemplate(String template) throws Exception {
        return DefaultTemplateMatcher.getNameFromTemplate(template);
    }

    @Override
    public String getVersionFromTemplate(String template) throws Exception {
        return DefaultTemplateMatcher.getVersionFromTemplate(template);
    }

    @NotNull
    @Override
    public String getServerVersion(JavaeePersistentData persistentData) throws Exception {
        final String value = GlassfishServerVersionConfig.get(persistentData);
        if (value == null) {
            throw new Exception(PayaraBundle.message("error.message.payara.version.not.found"));
        }
        return value;
    }

    @Override
    protected void checkValidServerHome(String home, String version) throws Exception {
        if (StringUtil.isEmptyOrSpaces(home)) {
            throw new Exception(PayaraBundle.message("error.message.payara.home.not.specified"));
        }

        checkDir(new File(home, PAYARA_BIN_DIRECTORY_NAME));
        checkDir(new File(home, PAYARA_MODULES_DIRECTORY_NAME));

        if (!isValidServerPath(home)) {
            throw new FileNotFoundException(PayaraBundle.message("error.message.payara.api.jar.not.found"));
        }
    }

    @Override
    protected void addLibraryLocations(String home, List<File> locations) {
        locations.add(new File(home, PAYARA_MODULES_DIRECTORY_NAME));
    }

    @Override
    public String getContextRoot(JavaeeFacet facet) {
        String contextRoot = null;
        final GlassfishWebRoot web = GlassfishUtil.getWebRoot(facet);
        if (web != null) {
            contextRoot = web.getContextRoot().getValue();
        }
        return contextRoot;
    }

    @Override
    protected void collectDescriptors(JavaeeDescriptorsManager jdm) {
    }

    private boolean isValidServerPath(String home) {
        return findLibByPattern(home, PAYARA_JAR_PATTERN);
    }

    private boolean findLibByPattern(String home, Pattern jarPattern) {
        File libDir = new File(home, PAYARA_MODULES_DIRECTORY_NAME);
        return libDir.isDirectory() && !findFilesByMask(jarPattern, libDir).isEmpty();
    }

    private List<File> findFilesByMask(@NotNull Pattern pattern, @NotNull File dir) {
        final ArrayList<File> found = new ArrayList<>();
        final File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (pattern.matcher(file.getName()).matches()) {
                    found.add(file);
                }
            }
        }
        return found;
    }
}
