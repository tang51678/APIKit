/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.ui.apitable;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;
import burp.ui.apitable.ApiDetailEntity;
import burp.ui.apitable.ApiDocumentEntity;
import burp.ui.apitable.StatusCodeCellRenderer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

public class ApiDetailTable
        extends JTable {
    private static final List<ApiDetailEntity> EMPTY_LIST = new ArrayList<ApiDetailEntity>(0);
    private final ApiDetailTableModel model;
    private final Consumer<ApiDetailEntity> onSelectedCallback;

    // 上次点击的实体，用于实现单次点击限制
    private ApiDetailEntity lastClickedEntity = null;
    private long lastClickTime = 0;
    private static final long CLICK_DELAY = 1000; // 点击延迟时间，毫秒
    
    public ApiDetailTable(Consumer<ApiDetailEntity> onSelectedCallback) {
        this.onSelectedCallback = onSelectedCallback;
        this.model = new ApiDetailTableModel();
        this.setModel(this.model);
        this.setSelectionMode(2);
        this.setEnabled(true);
        this.setDoubleBuffered(true);
        this.getTableHeader().setReorderingAllowed(true); // 允许标题栏左右拖动
        this.setShowGrid(false);
        this.setRowHeight(20);
        this.setIntercellSpacing(new Dimension(0, 0));
        this.setAutoCreateRowSorter(true);
        
        // 设置状态码列的自定义渲染器
        TableColumn statusCodeColumn = this.getColumnModel().getColumn(1);
        statusCodeColumn.setCellRenderer(new StatusCodeCellRenderer());
        
        // 添加右键菜单支持
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        // 如果没有选中的行，则只选择右击的行
                        if (getSelectedRowCount() == 0) {
                            setRowSelectionInterval(row, row);
                        } else if (!isRowSelected(row)) {
                            // 如果有选中的行，但右击的行不在选中范围内，则将其添加到选择中
                            addRowSelectionInterval(row, row);
                        }
                        showPopupMenu(e);
                    }
                }
            }
        });
        this.getSelectionModel().addListSelectionListener(e -> {
            int modelRow;
            ApiDetailEntity selected;
            int selectedRow;
            if (!e.getValueIsAdjusting() && (selectedRow = this.getSelectedRow()) != -1 && (selected = this.model.getEntityAt(modelRow = this.convertRowIndexToModel(selectedRow))) != null && this.onSelectedCallback != null) {
                
                // 实现单次点击限制功能
                long currentTime = System.currentTimeMillis();
                if (model.getRowCount() == 1) {
                    if (lastClickedEntity == selected && (currentTime - lastClickTime) < CLICK_DELAY) {
                        // 如果只有一个URL且在短时间内再次点击，不执行回调
                        return;
                    }
                    lastClickedEntity = selected;
                    lastClickTime = currentTime;
                }
                
                SwingUtilities.invokeLater(() -> this.onSelectedCallback.accept(selected));
            }
        });
        this.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 67 && (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                    ApiDetailTable.this.copySelectedRows();
                }
            }
        });
    }

    public void setApiDetail(ApiDocumentEntity apiDocument) {
        if (apiDocument == null || apiDocument.details == null) {
            this.model.setEntities(EMPTY_LIST);
            this.setEnabled(true);
            return;
        }
        ArrayList<ApiDetailEntity> details = new ArrayList<ApiDetailEntity>(apiDocument.details);
        SwingUtilities.invokeLater(() -> {
            this.model.setEntities(details);
            this.setEnabled(true);
        });
    }

    public void clear() {
        this.model.clear();
        this.setEnabled(true);
    }

    private void copySelectedRows() {
        int[] rows = this.getSelectedRows();
        if (rows.length == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int col = 0; col < this.getColumnCount(); ++col) {
            sb.append(this.getColumnName(col));
            if (col >= this.getColumnCount() - 1) continue;
            sb.append("\t");
        }
        sb.append("\n");
        for (int row : rows) {
            for (int col = 0; col < this.getColumnCount(); ++col) {
                Object value = this.getValueAt(row, col);
                if (value != null) {
                    sb.append(value.toString());
                }
                if (col >= this.getColumnCount() - 1) continue;
                sb.append("\t");
            }
            sb.append("\n");
        }
        StringSelection selection = new StringSelection(sb.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }
    
    // 复制选中行的完整URL
    private void copySelectedUrls() {
        int[] rows = this.getSelectedRows();
        if (rows.length == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int row : rows) {
            int modelRow = this.convertRowIndexToModel(row);
            ApiDetailEntity entity = this.model.getEntityAt(modelRow);
            if (entity != null && entity.requestResponse != null) {
                String url = getFullUrl(entity.requestResponse);
                sb.append(url).append("\n");
            }
        }
        StringSelection selection = new StringSelection(sb.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }
    
    // 获取完整URL
    private String getFullUrl(IHttpRequestResponse requestResponse) {
        try {
            IRequestInfo requestInfo = BurpExtender.getHelpers().analyzeRequest(requestResponse);
            URL url = requestInfo.getUrl();
            return url.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    // 显示右键菜单
    private void showPopupMenu(MouseEvent e) {
        JPopupMenu popupMenu = new JPopupMenu();
        
        JMenuItem copyUrlItem = new JMenuItem("Copy URL(s)");
        copyUrlItem.addActionListener(ae -> copySelectedUrls());
        popupMenu.add(copyUrlItem);
        
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    public static class ApiDetailTableModel
            extends AbstractTableModel {
        private volatile List<ApiDetailEntity> tableData = new ArrayList<ApiDetailEntity>();

        public synchronized void setEntities(List<ApiDetailEntity> entities) {
            if (entities == null) {
                entities = EMPTY_LIST;
            }
            this.tableData = new ArrayList<ApiDetailEntity>(entities);
            this.fireTableDataChanged();
        }

        public ApiDetailEntity getEntityAt(int rowIndex) {
            try {
                return this.tableData.get(rowIndex);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }

        @Override
        public int getRowCount() {
            return this.tableData.size();
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    return String.class;
                }
                case 1: {
                    return Integer.class;
                }
                case 2: {
                    return Integer.class;
                }
                case 3: {
                    return String.class;
                }
                case 4: {
                    return String.class;
                }
                case 5: {
                    return String.class;
                }
            }
            return null;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    return "Name";
                }
                case 1: {
                    return "Status Code";
                }
                case 2: {
                    return "Content Length";
                }
                case 3: {
                    return "Unauth";
                }
                case 4: {
                    return "API Type";
                }
                case 5: {
                    return "Scan Time";
                }
            }
            return null;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ApiDetailEntity entity = this.tableData.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return entity.name;
                }
                case 1: {
                    return entity.statusCode;
                }
                case 2: {
                    return entity.contentLength;
                }
                case 3: {
                    return entity.unAuth;
                }
                case 4: {
                    return entity.apiType;
                }
                case 5: {
                    return entity.scanTime;
                }
            }
            return null;
        }

        public synchronized void setApiDetail(ApiDocumentEntity apiDocument) {
            if (apiDocument != null && apiDocument.details != null) {
                this.setEntities(apiDocument.details);
            } else {
                this.setEntities(new ArrayList<ApiDetailEntity>(0));
            }
        }

        public synchronized void clear() {
            this.setEntities(new ArrayList<ApiDetailEntity>());
        }
    }
}

