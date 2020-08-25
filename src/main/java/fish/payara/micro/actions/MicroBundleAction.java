/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fish.payara.micro.actions;

import com.intellij.terminal.JBTerminalWidget;
import fish.payara.micro.maven.PayaraMicroProject;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;

/**
 *
 * @author jGauravGupta
 */
public class MicroBundleAction extends MicroAction {

    private static final Logger LOG = Logger.getLogger(MicroBundleAction.class.getName());

    @Override
    public void onAction(PayaraMicroProject project) {
        String projectName;
        projectName = project.getProjectName();
        JBTerminalWidget terminal = getTerminal(project, projectName);
        if (terminal != null) {
            executeCommand(terminal, project.getBundleCommand());
        } else {
            LOG.log(WARNING, "Shell window for {0} is not available.", projectName);
        }
    }

}
