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
import static com.intellij.openapi.fileChooser.FileChooserDescriptorFactory.createSingleFolderDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import static com.intellij.openapi.ui.TextComponentAccessor.STRING_COMBOBOX_WHOLE_TEXT;
import com.intellij.ui.ComboboxWithBrowseButton;
import fish.payara.PayaraBundle;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.jetbrains.annotations.NotNull;

public class PayaraLocalEditor extends JavaeeRunSettingsEditor<PayaraLocalModel> {

    private JPanel panel;
    private ComboboxWithBrowseButton domain;
    private JTextField username;
    private JPasswordField password;
    private JLabel passwordLabel;

    public PayaraLocalEditor(JavaeeIntegration integration) {
        super(integration);
        setupUI();
    }

    @Override
    @NotNull
    protected JComponent getEditor() {
        return panel;
    }

    @Override
    protected void resetEditorFrom(PayaraLocalModel model) {
        JComboBox domainComboBox = this.domain.getComboBox();
        domainComboBox.removeAllItems();
        model.getDomains().forEach(domainComboBox::addItem);
        domainComboBox.setSelectedItem(model.DOMAIN_NAME);
        this.username.setText(model.USERNAME);
        JavaeeRunConfigurationEditorUtil.resetPasswordFrom(model, this.password, this.passwordLabel);
    }

    @Override
    protected void applyEditorTo(PayaraLocalModel model) throws ConfigurationException {
        model.DOMAIN_NAME = (String) this.domain.getComboBox().getEditor().getItem();
        model.USERNAME = this.username.getText();
        JavaeeRunConfigurationEditorUtil.applyPasswordTo(model, this.password);
    }

    private void setupUI() {
        this.domain.addBrowseFolderListener(
                PayaraBundle.message("PayaraLocalEditor.domain.chooser.title", new Object[0]),
                PayaraBundle.message("PayaraLocalEditor.domain.chooser.description", new Object[0]),
                null,
                createSingleFolderDescriptor(), 
                STRING_COMBOBOX_WHOLE_TEXT
        );
        this.domain.getComboBox().setEditable(true);
    }
}
