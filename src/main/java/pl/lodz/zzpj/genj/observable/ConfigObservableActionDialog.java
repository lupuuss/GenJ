package pl.lodz.zzpj.genj.observable;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ConfigObservableActionDialog extends DialogWrapper {

    private JFormattedTextField textField;
    private JCheckBox addCheck;
    private JCheckBox removeCheck;

    public ConfigObservableActionDialog() {
        super(true);
        init();
        setTitle("Configure Observable...");
    }


    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new GridLayout(0, 1));

        JLabel label = new JLabel("Event name:");
        textField = new JFormattedTextField();

        dialogPanel.add(label);
        dialogPanel.add(textField);

        addCheck = new JCheckBox();
        addCheck.setSelected(true);
        addCheck.setText("Generate add method");

        removeCheck = new JCheckBox();
        removeCheck.setSelected(true);
        removeCheck.setText("Generate remove method");

        dialogPanel.add(addCheck);
        dialogPanel.add(removeCheck);

        return dialogPanel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return textField;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {

        boolean isJavaIdentifier = textField
                .getText()
                .chars()
                .allMatch(Character::isJavaIdentifierPart);

        if (!isJavaIdentifier) {
            return new ValidationInfo("Invalid name", textField);
        }

        return null;
    }

    public String getEventName() {
        return textField.getText();
    }

    public boolean generateAddMethod() {
        return addCheck.isSelected();
    }

    public boolean generateRemoveMethod() {
        return removeCheck.isSelected();
    }
}
