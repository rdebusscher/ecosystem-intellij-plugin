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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gaurav.gupta@payara.fish
 */
public class GradleProject extends PayaraMicroProject {

    private static final Logger LOG = Logger.getLogger(GradleProject.class.getName());

    private static final String MICRO_PLUGIN_ID = "fish.payara.micro-gradle-plugin";
    private static final String MICRO_GROUP_ID = "fish.payara.gradle.plugins";
    private static final String MICRO_ARTIFACT_ID = "payara-micro-gradle-plugin";
    private static final String START_GOAL = "microStart";
    private static final String RELOAD_GOAL = "microReload";
    private static final String STOP_GOAL = "microStop";
    private static final String BUNDLE_GOAL = "microBundle";
    private static final String WAR_EXPLODE_GOAL = "warExplode";
    private static final String DEBUG_PROPERTY = " -Ddebug=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=9009";
    private static final String SETTINGS_FILE = "settings.gradle";
    private static final String ROOT_PROJECT_NAME = "rootProject.name";
    private static final String BUILD_FILE = "build.gradle";
    private static final String USE_UBER_JAR = "useUberJar";
    private static final String EXPLODED = "exploded";
    private static final String DEPLOY_WAR = "deployWar";
    
    private boolean useUberJar, exploded, deployWar;

    @Override
    public String getStartCommand(boolean debug) {
        String cmd;
        if (useUberJar) {
            cmd = getStartUberJarCommand();
        } else if (exploded) {
            cmd = getStartExplodedWarCommand();
        } else {
            cmd = String.format("gradle %s",
                    START_GOAL
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
        return String.format("gradle %s  %s -DpayaraMicro.exploded=true -DpayaraMicro.deployWar=true",
                WAR_EXPLODE_GOAL,
                START_GOAL
        );
    }

    @Override
    public String getReloadCommand() {
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
        return String.format("gradle install %s",
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
     * Return the project name
     *
     * @return
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
                LOG.log(Level.SEVERE, "Could not read " + settingsFile.toString(), ex);
            }
        }
        return projectName;
    }

    private static boolean isValidBuild(PsiFile buildFile) {
        try {
            return Files.lines(Paths.get(buildFile.getVirtualFile().getPath()))
                    .anyMatch(line -> line.contains(MICRO_PLUGIN_ID) || line.contains(MICRO_ARTIFACT_ID));
        } catch (IOException ex) {
            Logger.getLogger(GradleProject.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private boolean parseBuildFile() {
        String content;
        try {
            content = new String(
                    Files.readAllBytes(
                            Paths.get(
                                    super.getBuildFile().getVirtualFile().getPath()
                            )
                    )
            );
            String regex = "(?<=payaraMicro)(\\s*\\{)([^\\}]+)(?=\\})";
            final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(content);
            matcher.find();
            for (int i = 0; i < matcher.groupCount(); i++) {
                String line = matcher.group(i);
                if (line.contains(USE_UBER_JAR)) {
                    useUberJar = getPropertyValue(line, USE_UBER_JAR);
                }
                if (line.contains(EXPLODED)) {
                    exploded = getPropertyValue(line, EXPLODED);
                }
                if (line.contains(DEPLOY_WAR)) {
                    deployWar = getPropertyValue(line, DEPLOY_WAR);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GradleProject.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private boolean getPropertyValue(String line, String match) {
        String[] pairs = line.split("=");
        if (pairs.length == 2
                && pairs[0].trim().equals(match)
                && pairs[1].trim().equals("true")) {
            return true;
        }
        return false;
    }

}
