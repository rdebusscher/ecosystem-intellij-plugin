/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fish.payara.micro.maven;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jGauravGupta
 */
public class MavenProject extends PayaraMicroProject {

    private static final Logger LOG = Logger.getLogger(MavenProject.class.getName());

    private static final String PROFILES = "profiles";
    private static final String PROFILE = "profile";
    private static final String BUILD = "build";
    private static final String PLUGINS = "plugins";
    private static final String PLUGIN = "plugin";
    private static final String GROUP_ID = "groupId";
    private static final String ARTIFACT_ID = "artifactId";
    private static final String MICRO_GROUP_ID = "fish.payara.maven.plugins";
    private static final String MICRO_ARTIFACT_ID = "payara-micro-maven-plugin";
    private static final String START_GOAL = "start";
    private static final String RELOAD_GOAL = "reload";
    private static final String STOP_GOAL = "stop";
    private static final String BUNDLE_GOAL = "bundle";
    private static final String DEBUG_PROPERTY = "-Ddebug=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=9009";
    private static final String BUILD_FILE = "pom.xml";

    @Override
    public String getStartCommand() {
        return String.format("mvn %s:%s:%s -f \"%s\"", MICRO_GROUP_ID, MICRO_ARTIFACT_ID, START_GOAL,
                getBuildFile().getVirtualFile().getCanonicalPath());
    }

    @Override
    public String getDebugCommand() {
        return String.format("mvn %s:%s:%s %s -f \"%s\"", MICRO_GROUP_ID, MICRO_ARTIFACT_ID, START_GOAL, DEBUG_PROPERTY,
                getBuildFile().getVirtualFile().getCanonicalPath());
    }

    @Override
    public String getReloadCommand() {
        return String.format("mvn %s:%s:%s -f \"%s\"", MICRO_GROUP_ID, MICRO_ARTIFACT_ID, RELOAD_GOAL,
                getBuildFile().getVirtualFile().getCanonicalPath());
    }

    @Override
    public String getStopCommand() {
        return String.format("mvn %s:%s:%s -f \"%s\"", MICRO_GROUP_ID, MICRO_ARTIFACT_ID, STOP_GOAL,
                getBuildFile().getVirtualFile().getCanonicalPath());
    }

    @Override
    public String getBundleCommand() {
        return String.format("mvn install %s:%s:%s -f \"%s\"", MICRO_GROUP_ID, MICRO_ARTIFACT_ID, BUNDLE_GOAL,
                getBuildFile().getVirtualFile().getCanonicalPath());
    }

    public static MavenProject getInstance(Project project) {
        PsiFile pom = getPomFile(project);
        if (pom != null) {
            return new MavenProject(project, pom);
        }
        return null;
    }

    private MavenProject(Project project, PsiFile pom) {
        super(project, pom);
    }

    /**
     * Return the project name
     *
     * @return
     */
    @Override
    public String getProjectName() {
        try {
            Node pomRoot = getPomRootNode(super.getBuildFile());
            NodeList childNodes = pomRoot.getChildNodes();
            for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++) {
                Node childNode = childNodes.item(childNodeIndex);
                if (childNode.getNodeName().equals("artifactId")) {
                    if (childNode.getTextContent() != null) {
                        return childNode.getTextContent();
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
        return null;
    }

    private static Node getPomRootNode(PsiFile pomFile) throws ParserConfigurationException, SAXException, IOException {
        File inputFile = new File(pomFile.getVirtualFile().getCanonicalPath());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document buildDocument = builder.parse(inputFile);
        buildDocument.getDocumentElement().normalize();
        Node root = buildDocument.getDocumentElement();
        return root;
    }

    private static PsiFile getPomFile(Project project) {
        PsiFile[] poms = FilenameIndex.getFilesByName(project, BUILD_FILE, GlobalSearchScope.projectScope(project));
        for (PsiFile pom : poms) {
            if (isValidPom(pom)) {
                return pom;
            }
        }
        return null;
    }

    private static boolean isValidPom(PsiFile pomFile) {
        try {
            Node pomRoot = getPomRootNode(pomFile);
            return getBuildNodes(pomRoot)
                    .stream()
                    .anyMatch(MavenProject::isMicroPlugin);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return false;

    }

    private static List<Node> getBuildNodes(Node pomRoot) {
        List<Node> buildNodes = new ArrayList<>();
        NodeList childNodes = pomRoot.getChildNodes();
        for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++) {
            Node childNode = childNodes.item(childNodeIndex);

            buildNodes.addAll(
                    getProfileNodes(childNode)
                            .stream()
                            .map(Node::getChildNodes)
                            .map(MavenProject::getBuildNode)
                            .filter(Objects::nonNull)
                            .collect(toList())
            );

            if (childNode.getNodeName().equals(BUILD)) {
                buildNodes.add(childNode);
            }
        }
        return buildNodes;
    }

    private static Node getBuildNode(NodeList childNode) {
        for (int index = 0; index < childNode.getLength(); index++) {
            if (childNode.item(index).getNodeName().equals(BUILD)) {
                return childNode.item(index);
            }
        }
        return null;
    }

    private static List<Node> getProfileNodes(Node childNode) {
        List<Node> profildes = new ArrayList<>();
        if (childNode.getNodeName().equals(PROFILES)) {
            NodeList profileNodes = childNode.getChildNodes();
            for (int profileIndex = 0; profileIndex < profileNodes.getLength(); profileIndex++) {
                Node profile = profileNodes.item(profileIndex);
                if (profile.getNodeName().equals(PROFILE)) {
                    profildes.add(profile);
                }
            }
        }
        return profildes;
    }

    private static List<Node> getPluginNodes(Node childNode) {
        List<Node> plugins = new ArrayList<>();
        if (childNode.getNodeName().equals(PLUGINS)) {
            NodeList pluginNodes = childNode.getChildNodes();
            for (int pluginIndex = 0; pluginIndex < pluginNodes.getLength(); pluginIndex++) {
                Node pluginNode = pluginNodes.item(pluginIndex);
                if (pluginNode.getNodeName().equals(PLUGIN)) {
                    plugins.add(pluginNode);
                }
            }
        }
        return plugins;
    }

    private static boolean isMicroPlugin(Node buildNode) {
        NodeList buildChildNodes = buildNode.getChildNodes();
        for (int buildChildNodeIndex = 0; buildChildNodeIndex < buildChildNodes.getLength(); buildChildNodeIndex++) {
            Node buildChildNode = buildChildNodes.item(buildChildNodeIndex);
            for (Node pluginNode : getPluginNodes(buildChildNode)) {
                NodeList pluginChildNodes = pluginNode.getChildNodes();
                boolean microGroupId = false;
                boolean microArtifactId = false;
                for (int i = 0; i < pluginChildNodes.getLength(); i++) {
                    Node node = pluginChildNodes.item(i);
                    if (node.getNodeName().equals(GROUP_ID)
                            && node.getTextContent().equals(MICRO_GROUP_ID)) {
                        microGroupId = true;
                    } else if (node.getNodeName().equals(ARTIFACT_ID)
                            && node.getTextContent().equals(MICRO_ARTIFACT_ID)) {
                        microArtifactId = true;
                    }
                    if (microGroupId && microArtifactId) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
