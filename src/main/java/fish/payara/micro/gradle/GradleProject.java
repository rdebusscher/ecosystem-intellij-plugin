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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import fish.payara.micro.PayaraMicroProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.logging.Level.SEVERE;

/**
 *
 * @author gaurav.gupta@payara.fish
 */
public class GradleProject extends PayaraMicroProject {

    private static final Logger LOG = Logger.getLogger(GradleProject.class.getName());

    private static final String MICRO_PLUGIN_ID = "fish.payara.micro-gradle-plugin";
    private static final String MICRO_ARTIFACT_ID = "payara-micro-gradle-plugin";
    public static final String START_GOAL = "microStart";
    private static final String RELOAD_GOAL = "microReload";
    private static final String STOP_GOAL = "microStop";
    private static final String BUNDLE_GOAL = "microBundle";
    private static final String WAR_EXPLODE_GOAL = "warExplode";
    public static final String BUILD_GOAL = "build";
    private static final String DEBUG_PROPERTY = " -Ddebug=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=9009";
    private static final String SETTINGS_FILE = "settings.gradle";
    private static final String ROOT_PROJECT_NAME = "rootProject.name";
    private static final String BUILD_FILE = "build.gradle";
    private static final String USE_UBER_JAR = "useUberJar";
    private static final String EXPLODED = "exploded";
    private static final String EXPLODED_PROPERTY = "-DpayaraMicro.exploded=true";
    public static final String DEPLOY_WAR_PROPERTY = "-DpayaraMicro.deployWar=true";

    private boolean useUberJar, exploded;

    @Override
    public String getStartCommand(boolean debug) {
        String cmd;
        if (useUberJar) {
            cmd = getStartUberJarCommand();
        } else if (exploded) {
            cmd = getStartExplodedWarCommand();
        } else {
            cmd = String.format("gradle %s %s %s",
                    BUILD_GOAL,
                    START_GOAL,
                    DEPLOY_WAR_PROPERTY
            );
        }
        return debug ? cmd + DEBUG_PROPERTY : cmd;
    }

    public String getStartUberJarCommand() {
        return String.format("gradle %s %s",
                BUNDLE_GOAL,
                START_GOAL
        );
    }

    public String getStartExplodedWarCommand() {
        return String.format("gradle %s %s %s %s",
                WAR_EXPLODE_GOAL,
                START_GOAL,
                DEPLOY_WAR_PROPERTY,
                EXPLODED_PROPERTY
        );
    }

    @Override
    public String getReloadCommand() {
        if (!exploded && useUberJar) {
            throw new IllegalStateException("Reload task is only functional for exploded war artifacts.");
        }
        return String.format("gradle %s %s",
                WAR_EXPLODE_GOAL,
                RELOAD_GOAL
        );
    }

    @Override
    public String getStopCommand() {
        return String.format("gradle %s",
                STOP_GOAL
        );
    }

    @Override
    public String getBundleCommand() {
        return String.format("gradle %s",
                BUNDLE_GOAL
        );
    }

    public static GradleProject getInstance(Project project) {
        PsiFile buildFile = getBuildFile(project);
        if (buildFile != null) {
            return new GradleProject(project, buildFile);
        }
        return null;
    }

    private GradleProject(Project project, PsiFile buildFile) {
        super(project, buildFile);
        parseBuildFile();
    }

    /**
     * @param project
     * @return the build.gradle file
     */
    private static PsiFile getBuildFile(Project project) {
        PsiFile[] buildFiles = FilenameIndex.getFilesByName(project, BUILD_FILE, GlobalSearchScope.projectScope(project));
        for (PsiFile buildFile : buildFiles) {
            if (isValidBuild(buildFile)) {
                return buildFile;
            }
        }
        return null;
    }

    /**
     * @return the project name from settings.properties file
     */
    @Override
    public String getProjectName() {
        String projectName = null;
        VirtualFile parentFolder = super.getBuildFile().getVirtualFile().getParent();
        File settingsFile = Paths.get(parentFolder.getPath(), SETTINGS_FILE).toFile();
        if (settingsFile.exists()) {
            try {
                Properties prop = new Properties();
                prop.load(new FileInputStream(settingsFile));
                String name = prop.getProperty(ROOT_PROJECT_NAME);
                if (name != null) {
                    projectName = name.replaceAll("^[\"']+|[\"']+$", "");
                }
            } catch (IOException ex) {
                LOG.log(SEVERE, settingsFile.getPath(), ex);
            }
        }
        return projectName;
    }

    /**
     * @param buildFile the build.gradle file
     * @return true if build.gradle file includes Payara Micro Gradle plugin
     */
    private static boolean isValidBuild(PsiFile buildFile) {
        try {
            return Files.lines(Paths.get(buildFile.getVirtualFile().getPath()))
                    .anyMatch(line -> line.contains(MICRO_PLUGIN_ID) || line.contains(MICRO_ARTIFACT_ID));
        } catch (IOException ex) {
            LOG.log(SEVERE, buildFile.getVirtualFile().getPath(), ex);
        }
        return false;
    }

    /**
     * Parse the build.gradle to read the configuration of Payara Micro Gradle
     * plugin.
     */
    private void parseBuildFile() {
        String content;
        try {
            content = new String(
                    Files.readAllBytes(
                            Paths.get(
                                    super.getBuildFile().getVirtualFile().getPath()
                            )
                    )
            );
            String regex = "(?<=payaraMicro)(\\s*\\{)([^\\}]+)(\\})";
            final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                for (String line : matcher.group(2).split("\n")) {
                    if (line.contains(USE_UBER_JAR)) {
                        useUberJar = getFlagPropertyValue(line, USE_UBER_JAR);
                    }
                    if (line.contains(EXPLODED)) {
                        exploded = getFlagPropertyValue(line, EXPLODED);
                    }
                }
            }
        } catch (IOException ex) {
            LOG.log(SEVERE, super.getBuildFile().getVirtualFile().getPath(), ex);
        }
    }

    private boolean getFlagPropertyValue(String line, String match) {
        String[] pairs = line.split("=");
        return pairs.length == 2
                && pairs[0].trim().equals(match)
                && pairs[1].trim().equals("true");
    }

}
