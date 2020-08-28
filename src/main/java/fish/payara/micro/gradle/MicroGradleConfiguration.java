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
package fish.payara.micro.gradle;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.service.execution.GradleRunConfiguration;

import java.util.ArrayList;

public class MicroGradleConfiguration extends GradleRunConfiguration {

    protected MicroGradleConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    public SettingsEditor<ExternalSystemRunConfiguration> getConfigurationEditor() {
        ExternalSystemTaskExecutionSettings settings = super.getSettings();
        if (settings.getTaskNames() == null){
            settings.setTaskNames(new ArrayList<>());
        }
        if(settings.getTaskNames().isEmpty()) {
            settings.getTaskNames().add(GradleProject.BUILD_GOAL);
            settings.getTaskNames().add(GradleProject.START_GOAL);
//            settings.setScriptParameters(GradleProject.DEPLOY_WAR_PROPERTY);
        }
        return super.getConfigurationEditor();
    }

}
