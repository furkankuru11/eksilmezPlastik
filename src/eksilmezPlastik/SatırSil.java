package eksilmezPlastik;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SatırSil {

    private JTable salesTable;

    public SatırSil(JTable salesTable) {
        this.salesTable = salesTable;
        initialize();
    }

    private void initialize() {
    	// Sağ tıklama menüsü oluştur
    	JPopupMenu popupMenu = new JPopupMenu();
    	JMenuItem deleteMenuItem = new JMenuItem("Sil");
    	popupMenu.add(deleteMenuItem);

    	// Sağ tıklama olayını dinle
    	salesTable.setComponentPopupMenu(popupMenu);
    	salesTable.addMouseListener(new MouseAdapter() {
    	    @Override
    	    public void mousePressed(MouseEvent e) {
    	        if (e.getButton() == MouseEvent.BUTTON3) { // Sağ tıklama
    	            int row = salesTable.rowAtPoint(e.getPoint());
    	            salesTable.setRowSelectionInterval(row, row);
    	        }
    	    }
    	});
    	// Silme menü öğesine tıklama olayını dinle
    	deleteMenuItem.addActionListener(new ActionListener() {
    	    @Override
    	    public void actionPerformed(ActionEvent e) {
    	        int selectedRow = salesTable.getSelectedRow();
    	        if (selectedRow != -1) {
    	            // Silme işlemi
    	            deleteRowFromDatabase(selectedRow);
    	            ((DefaultTableModel) salesTable.getModel()).removeRow(selectedRow);
    	        }
    	    }

			private void deleteRowFromDatabase(int selectedRow) {
				 int id = (Integer) salesTable.getValueAt(selectedRow, 0); // Satırdan ID'yi al (ID sütununuza bağlı olarak güncelleyin)
				    
				    String sql = "DELETE FROM sales WHERE id = ?";
				    try (Connection conn = DatabaseConnection.getConnection();
				         PreparedStatement pstmt = conn.prepareStatement(sql)) {
				        pstmt.setInt(1, id);
				        pstmt.executeUpdate();
				    } catch (SQLException ex) {
				        ex.printStackTrace();
				    }
				
			}
    	});
    	


    }

   
}
