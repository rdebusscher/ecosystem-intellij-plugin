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
package fish.payara.micro.project;

import com.intellij.ide.util.projectWizard.*;
import com.intellij.openapi.Disposable;
import static com.intellij.openapi.module.JavaModuleType.JAVA_GROUP;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.RefreshQueue;
import fish.payara.PayaraConstants;
import static fish.payara.micro.project.PayaraMicroConstants.ARCHETYPE_ARTIFACT_ID;
import static fish.payara.micro.project.PayaraMicroConstants.ARCHETYPE_ARTIFACT_ID_KEY;
import static fish.payara.micro.project.PayaraMicroConstants.ARCHETYPE_GENERATE;
import static fish.payara.micro.project.PayaraMicroConstants.ARCHETYPE_GROUP_ID;
import static fish.payara.micro.project.PayaraMicroConstants.ARCHETYPE_GROUP_ID_KEY;
import static fish.payara.micro.project.PayaraMicroConstants.ARCHETYPE_INTERACTIVE_MODE;
import static fish.payara.micro.project.PayaraMicroConstants.ARCHETYPE_VERSION;
import static fish.payara.micro.project.PayaraMicroConstants.ARCHETYPE_VERSION_KEY;
import static fish.payara.micro.project.PayaraMicroConstants.MODULE_DESCRIPTION;
import static fish.payara.micro.project.PayaraMicroConstants.MODULE_TITLE;
import static fish.payara.micro.project.PayaraMicroConstants.PROP_ADD_PAYARA_API;
import static fish.payara.micro.project.PayaraMicroConstants.PROP_ARTIFACT_ID;
import static fish.payara.micro.project.PayaraMicroConstants.PROP_AUTO_BIND_HTTP;
import static fish.payara.micro.project.PayaraMicroConstants.PROP_CONTEXT_ROOT;
import static fish.payara.micro.project.PayaraMicroConstants.PROP_GROUP_ID;
import static fish.payara.micro.project.PayaraMicroConstants.PROP_PAYARA_MICRO_VERSION;
import static fish.payara.micro.project.PayaraMicroConstants.PROP_VERSION;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import javax.swing.Icon;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;

public class PayaraMicroModuleBuilder extends JavaModuleBuilder {

    private final ModuleDescriptor moduleDescriptor = new ModuleDescriptor();

    @Override
    public ModuleType getModuleType() {
        return PayaraMicroModuleType.getModuleType();
    }

    @Override
    public String getParentGroup() {
        return JAVA_GROUP;
    }

    @Override
    public Icon getNodeIcon() {
        return PayaraConstants.PAYARA_ICON;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Override
    public String getDescription() {
        return MODULE_DESCRIPTION;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getPresentableName() {
        return MODULE_TITLE;
    }

    @Override
    public int getWeight() {
        return 50;
    }

    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        return new PayaraMicroProjectWizardStep(moduleDescriptor, context);
    }

    /**
     * Set the folder name default value as the artifactId.
     */
    @Nullable
    @Override
    public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {
        ModuleNameLocationSettings moduleNameLocationSettings = settingsStep.getModuleNameLocationSettings();
        if (moduleNameLocationSettings != null && moduleDescriptor.getArtifactId() != null) {
            moduleNameLocationSettings.setModuleName(StringUtil.sanitizeJavaIdentifier(moduleDescriptor.getArtifactId()));
        }
        return super.modifySettingsStep(settingsStep);
    }

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel rootModel) throws ConfigurationException {
        super.setupRootModel(rootModel);
        generateFromArchetype(rootModel.getProject());
    }

    /**
     * Generate Payara Micro project via
     * fish.payara.maven.archetypes:payara-micro-maven-archetype .
     */
    private void generateFromArchetype(final Project project) {
        final File projectDir = new File(project.getBasePath());

        MavenRunnerParameters params = new MavenRunnerParameters(
                false,
                projectDir.getParentFile().getPath(),
                (String) null,
                Collections.singletonList(ARCHETYPE_GENERATE),
                Collections.emptyList()
        );

        MavenRunner runner = MavenRunner.getInstance(project);
        MavenRunnerSettings settings = runner.getState().clone();
        Map<String, String> props = settings.getMavenProperties();
        props.put(ARCHETYPE_INTERACTIVE_MODE, Boolean.FALSE.toString());
        props.put(ARCHETYPE_GROUP_ID_KEY, ARCHETYPE_GROUP_ID);
        props.put(ARCHETYPE_ARTIFACT_ID_KEY, ARCHETYPE_ARTIFACT_ID);
        props.put(ARCHETYPE_VERSION_KEY, ARCHETYPE_VERSION);
        props.put(PROP_GROUP_ID, moduleDescriptor.getGroupId());
        props.put(PROP_ARTIFACT_ID, moduleDescriptor.getArtifactId());
        props.put(PROP_VERSION, "1.0-SNAPSHOT");
        props.put(PROP_PAYARA_MICRO_VERSION, moduleDescriptor.getVersion().toString());
        props.put(PROP_AUTO_BIND_HTTP, Boolean.toString(moduleDescriptor.isAutoBindHttp()));
        props.put(PROP_CONTEXT_ROOT, moduleDescriptor.getContextRoot());
        props.put(PROP_ADD_PAYARA_API, Boolean.TRUE.toString());

        runner.run(params, settings, () -> {
            File moduleFile = new File(getContentEntryPath());
            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(moduleFile);
            RefreshQueue.getInstance().refresh(true, true, (Runnable)null, new VirtualFile[]{vf});
            System.out.println("Project generated: " + moduleDescriptor.getGroupId() + ":" + moduleDescriptor.getArtifactId());
        });
    }

}
