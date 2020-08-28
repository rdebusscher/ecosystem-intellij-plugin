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
package fish.payara.micro.maven;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.idea.maven.execution.MavenRunConfiguration;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;

public class MicroMavenConfiguration extends MavenRunConfiguration {

    protected MicroMavenConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        MavenRunnerParameters parameters = this.getRunnerParameters();
        if (parameters != null && parameters.getGoals().isEmpty()) {
            parameters.getGoals().add("fish.payara.maven.plugins:payara-micro-maven-plugin:RELEASE:start");
            parameters.getGoals().add("-DdeployWar=true");
        }
        return super.getConfigurationEditor();
    }

}
