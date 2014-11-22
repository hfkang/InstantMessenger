

import javax.swing.table.AbstractTableModel;

public class UserTable extends AbstractTableModel {
	private String[] columnNames;
	private Object[][] data;

	public UserTable(Object[][] data, String[] columnNames) {
		super();
		this.columnNames = columnNames;
		this.data = data;
	}

	public void changeData(Object[][] data) {
		this.data = data;
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return data.length;
	}

	@Override
	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}
	
	public String getName (int pos){
		if(pos>=data.length || pos < 0 || data[pos][0]==null)
			return null;
		else return (data[pos][0]).toString();
	}

}
