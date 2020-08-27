/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fish.payara.micro;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.messages.MessageBus;
import org.picocontainer.PicoContainer;

/**
 *
 * @author jGauravGupta
 */
public abstract class PayaraMicroProject implements Project {

    private final Project project;

    private PsiFile buildFile;

    protected PayaraMicroProject(Project project, PsiFile buildFile) {
        this.project = project;
        this.buildFile = buildFile;
    }

    public Project getProject() {
        return project;
    }

    public PsiFile getBuildFile() {
        return buildFile;
    }

    public abstract String getProjectName();

    public abstract String getStartCommand(boolean debug);

    public abstract String getReloadCommand();

    public abstract String getStopCommand();

    public abstract String getBundleCommand();
    
    @Override
    public String getName() {
        return project.getName();
    }

    @Override
    public VirtualFile getBaseDir() {
        return project.getBaseDir();
    }

    @Override
    public String getBasePath() {
        return project.getBasePath();
    }

    @Override
    public VirtualFile getProjectFile() {
        return project.getProjectFile();
    }

    @Override
    public String getProjectFilePath() {
        return project.getProjectFilePath();
    }

    @Override
    public VirtualFile getWorkspaceFile() {
        return project.getWorkspaceFile();
    }

    @Override
    public String getLocationHash() {
        return project.getLocationHash();
    }

    @Override
    public void save() {
        project.save();
    }

    @Override
    public boolean isOpen() {
        return project.isOpen();
    }

    @Override
    public boolean isInitialized() {
        return project.isInitialized();
    }

    @Override
    public boolean isDefault() {
        return project.isDefault();
    }

    @Override
    public <T> T getComponent(Class<T> type) {
        return project.getComponent(type);
    }

    @Override
    public PicoContainer getPicoContainer() {
        return project.getPicoContainer();
    }

    @Override
    public MessageBus getMessageBus() {
        return project.getMessageBus();
    }

    @Override
    public boolean isDisposed() {
        return project.isDisposed();
    }

    @Override
    public Condition<?> getDisposed() {
        return project.getDisposed();
    }

    @Override
    public <T> T getUserData(Key<T> key) {
        return project.getUserData(key);
    }

    @Override
    public <T> void putUserData(Key<T> key, T t) {
        project.putUserData(key, t);
    }

    @Override
    public void dispose() {
        project.dispose();
    }

}
