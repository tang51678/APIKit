/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.ui.apitable;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;
import burp.ui.apitable.ApiDocumentEntity;
import burp.ui.apitable.StatusCodeCellRenderer;

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

public class ApiDocumentTable
        extends JTable {
    private final ApiDocumentTableModel model;
    private final Consumer<ApiDocumentEntity> onSelectedCallback;
    
    // 上次点击的实体，用于实现单次点击限制
    private ApiDocumentEntity lastClickedEntity = null;
    private long lastClickTime = 0;
    private static final long CLICK_DELAY = 1000; // 点击延迟时间，毫秒

    public ApiDocumentTable(Consumer<ApiDocumentEntity> onSelectedCallback) {
        this.onSelectedCallback = onSelectedCallback;
        this.model = new ApiDocumentTableModel();
        this.setModel(this.model);
        this.setSelectionMode(2);
        this.setEnabled(true);
        this.setAutoCreateRowSorter(true);
        this.getColumnModel().getColumn(0).setMaxWidth(30);
        this.getTableHeader().setReorderingAllowed(true); // 允许标题栏左右拖动
        
        // 设置状态码列的自定义渲染器
        TableColumn statusCodeColumn = this.getColumnModel().getColumn(2); // 假设状态码在第3列
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
            ApiDocumentEntity selected;
            int selectedRow;
            if (!e.getValueIsAdjusting() && (selectedRow = this.getSelectedRow()) != -1 && (selected = this.model.getEntityAt(this.convertRowIndexToModel(selectedRow))) != null && this.onSelectedCallback != null) {
                
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
                
                this.onSelectedCallback.accept(selected);
            }
        });
        this.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 67 && (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                    ApiDocumentTable.this.copySelectedRows();
                }
            }
        });
    }

    public void append(ApiDocumentEntity apiDocument) {
        this.model.append(apiDocument);
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
            ApiDocumentEntity entity = this.model.getEntityAt(modelRow);
            if (entity != null) {
                if (entity.url != null && !entity.url.isEmpty()) {
                    sb.append(entity.url).append("\n");
                } else if (entity.requestResponse != null) {
                    String url = getFullUrl(entity.requestResponse);
                    sb.append(url).append("\n");
                }
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

    public static class ApiDocumentTableModel
            extends AbstractTableModel {
        private final List<ApiDocumentEntity> tableData = new ArrayList<ApiDocumentEntity>();

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void append(ApiDocumentEntity entity) {
            ApiDocumentTableModel apiDocumentTableModel;
            ApiDocumentTableModel apiDocumentTableModel2 = apiDocumentTableModel = this;
            synchronized (apiDocumentTableModel2) {
                this.tableData.add(entity);
                int _id = this.tableData.size();
                this.fireTableRowsInserted(_id - 1, _id - 1);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public synchronized void clear() {
            ApiDocumentTableModel apiDocumentTableModel;
            ApiDocumentTableModel apiDocumentTableModel2 = apiDocumentTableModel = this;
            synchronized (apiDocumentTableModel2) {
                int size = this.tableData.size();
                if (size > 0) {
                    this.tableData.clear();
                    this.fireTableRowsDeleted(0, size - 1);
                }
            }
        }

        public ApiDocumentEntity getEntityAt(int rowIndex) {
            return this.tableData.get(rowIndex);
        }

        @Override
        public int getRowCount() {
            return this.tableData.size();
        }

        @Override
        public int getColumnCount() {
            return 7;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    return Integer.class;
                }
                case 1: {
                    return String.class;
                }
                case 2: {
                    return Integer.class;
                }
                case 3: {
                    return Integer.class;
                }
                case 4: {
                    return String.class;
                }
                case 5: {
                    return String.class;
                }
                case 6: {
                    return String.class;
                }
            }
            return null;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    return "#";
                }
                case 1: {
                    return "URL";
                }
                case 2: {
                    return "Status Code";
                }
                case 3: {
                    return "Content Length";
                }
                case 4: {
                    return "Unauth";
                }
                case 5: {
                    return "API Type";
                }
                case 6: {
                    return "Scan Time";
                }
            }
            return null;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ApiDocumentEntity entity = this.tableData.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return entity.id;
                }
                case 1: {
                    return entity.url;
                }
                case 2: {
                    return entity.statusCode;
                }
                case 3: {
                    return entity.contentLength;
                }
                case 4: {
                    return entity.unAuth;
                }
                case 5: {
                    return entity.apiType;
                }
                case 6: {
                    return entity.scanTime;
                }
            }
            return null;
        }

        public ApiDocumentEntity getApiDocument(int rowIndex) {
            return this.getEntityAt(rowIndex);
        }
    }
}

