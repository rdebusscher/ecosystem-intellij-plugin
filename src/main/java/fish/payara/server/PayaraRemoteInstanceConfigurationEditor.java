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

import com.intellij.javaee.oss.server.JavaeeIntegration;
import com.intellij.javaee.oss.server.JavaeeRunSettingsEditor;
import com.intellij.javaee.run.configuration.view.JavaeeRunConfigurationEditorUtil;
import com.intellij.openapi.options.ConfigurationException;
import static fish.payara.PayaraConstants.DEFAULT_ADMIN_PORT;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.jetbrains.annotations.NotNull;


public class PayaraRemoteInstanceConfigurationEditor extends JavaeeRunSettingsEditor<PayaraRemoteInstanceModel> {

    private JPanel panel;
    private JTextField username;
    private JPasswordField password;
    private JLabel passwordLabel;
    private JTextField adminServerPort;
    private JRadioButton domainRadioButton;
    private JRadioButton clusterRadioButton;
    private JTextField clusterName;
    private JTextField adminServerHost;
    private JCheckBox securedCheckBox;
    private JLabel clusterNameLabel;
    private JLabel adminServerHostLabel;

    public PayaraRemoteInstanceConfigurationEditor(JavaeeIntegration integration) {
        super(integration);
        setupUI();
    }

    @Override
    @NotNull
    protected JComponent getEditor() {
        return panel;
    }

    @Override
    protected void resetEditorFrom(PayaraRemoteInstanceModel model) {
        this.username.setText(model.USERNAME);
        JavaeeRunConfigurationEditorUtil.resetPasswordFrom(model, this.password, this.passwordLabel);

        (model.DEPLOY_ON_CLUSTER ? this.clusterRadioButton : this.domainRadioButton).doClick();
        this.clusterName.setText(model.CLUSTER_NAME);
        this.adminServerHost.setText(model.ADMIN_SERVER_HOST);
        this.adminServerPort.setText(String.valueOf(model.ADMIN_PORT));
        this.securedCheckBox.setSelected(model.isSecured());
    }

    @Override
    protected void applyEditorTo(PayaraRemoteInstanceModel model) throws ConfigurationException {
        model.USERNAME = this.username.getText();
        JavaeeRunConfigurationEditorUtil.applyPasswordTo(model, this.password);
        model.DEPLOY_ON_CLUSTER = this.clusterRadioButton.isSelected();
        model.CLUSTER_NAME = this.clusterName.getText();
        model.ADMIN_SERVER_HOST = this.adminServerHost.getText();
        int adminPort;
        try {
            adminPort = Integer.valueOf(this.adminServerPort.getText());
        } catch (NumberFormatException nfe) {
            adminPort = DEFAULT_ADMIN_PORT;
        }
        model.ADMIN_PORT = adminPort;
        model.setSecured(this.securedCheckBox.isSelected());
    }

    private void setupUI() {
        final ActionListener listener = e -> {
            boolean enable = this.clusterRadioButton.isSelected();
            this.clusterName.setVisible(enable);
            this.adminServerHost.setVisible(enable);
            this.clusterNameLabel.setVisible(enable);
            this.adminServerHostLabel.setVisible(enable);
        };
        this.domainRadioButton.addActionListener(listener);
        this.clusterRadioButton.addActionListener(listener);
        listener.actionPerformed(null);
    }
}
