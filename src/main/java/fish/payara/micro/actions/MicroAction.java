/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fish.payara.micro.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.JBTerminalWidget;
import com.intellij.ui.content.Content;
import com.jediterm.terminal.TtyConnector;
import fish.payara.micro.PayaraMicroProject;
import fish.payara.micro.gradle.GradleProject;
import fish.payara.micro.maven.MavenProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.LocalTerminalDirectRunner;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalTabState;
import org.jetbrains.plugins.terminal.TerminalView;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jGauravGupta
 */
public abstract class MicroAction extends AnAction {

    private static final Logger LOG = Logger.getLogger(MicroAction.class.getName());

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            final Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
            if (project == null) {
                LOG.warning("Unable to resolve project type.");
                return;
            }

            PayaraMicroProject microProject = MavenProject.getInstance(project);
            PayaraMicroProject gradleProject = GradleProject.getInstance(project);
            if (microProject == null && gradleProject == null) {
                LOG.warning("Unable to resolve Payara Micro project type.");
                return;
            }
            if(microProject != null) {
                onAction(microProject);
            } else {
                onAction(gradleProject);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public abstract void onAction(PayaraMicroProject project);

    public JBTerminalWidget getTerminal(Project project, String tabName) {
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        ToolWindow terminalWindow = windowManager.getToolWindow("Terminal");

        JBTerminalWidget widget = findTerminal(terminalWindow, tabName);
        if (widget != null) {
            return widget;
        }
        createTerminal(project, tabName);
        return findTerminal(terminalWindow, tabName);
    }

    private void createTerminal(Project project, String tabName) {
        TerminalTabState tabState = new TerminalTabState();
        tabState.myTabName = tabName;
        tabState.myWorkingDirectory = project.getBasePath();
        TerminalView.getInstance(project)
                .createNewSession(new LocalTerminalDirectRunner(project), tabState);
    }

    private JBTerminalWidget findTerminal(ToolWindow terminalWindow, String tabName) {
        Content[] contents = terminalWindow.getContentManager().getContents();
        for (Content content : contents) {
            if (content.getTabName().equals(tabName)) {
                return TerminalView.getWidgetByContent(content);
            }
        }
        return null;
    }

    public void executeCommand(JBTerminalWidget widget, String command) {
        try {
            widget.setRequestFocusEnabled(true);
            widget.requestFocusInWindow();
            widget.requestFocus(true);
            widget.requestFocus();
            widget.grabFocus();
            TtyConnector connector = widget.getTtyConnector();
            if (connector != null) {
                connector.write(command);
                connector.write(widget.getTerminalStarter().getCode(KeyEvent.VK_ENTER, 0));
            } else if(widget instanceof ShellTerminalWidget) {
                ((ShellTerminalWidget)widget).executeCommand(command);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
