package burp.ui.apitable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StatusCodeCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        // 检查是否为状态码列且值为200
        if (value instanceof Integer && (Integer) value == 200) {
            component.setForeground(Color.GREEN);
        } else {
            // 恢复默认颜色
            if (isSelected) {
                component.setForeground(table.getSelectionForeground());
            } else {
                component.setForeground(table.getForeground());
            }
        }
        
        return component;
    }
}