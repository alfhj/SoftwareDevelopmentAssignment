package gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import stock.Item;
import store.Store;

/**
 * Model for the table. It takes data from the store's inventory.
 * It keeps a stored version of the Stock by adding the entries to an ArrayList
 * and sorting it each time the data is updated.
 * 
 * @author alfhj
 *
 */
public class StoreTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -907552824456549873L;
	private String[] columnNames = new String[] {"Name", "Amount", "Cost", "Price", "Re.point", "Re.amount", "Temp."};
	private Store store = Store.getInstance();
	private ArrayList<Entry<Item, Integer>> sortedInventory;
	
	@Override
	public int getRowCount() {
		return store.getInventory().getItems().size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Entry<Item, Integer> entry = sortedInventory.get(rowIndex);
		Item item = entry.getKey();
		int amount = entry.getValue();
		switch (columnIndex) {
			case 0: return item.getName();
			case 1: return amount;
			case 2: return item.getCost();
			case 3: return item.getPrice();
			case 4: return item.getReorderPoint();
			case 5: return item.getReorderAmount();
			case 6: return item.getTemperature() != null ? item.getTemperature() : "";
		default: return "";
		}
	}

	@Override
	public void fireTableDataChanged() {
		sortedInventory = new ArrayList<Entry<Item, Integer>>(store.getInventory().getItems().entrySet());
		sortedInventory.sort(new Comparator<Entry<Item, Integer>>() {
			@Override
			public int compare(Entry<Item, Integer> entry1, Entry<Item, Integer> entry2) {
				String name1 = entry1.getKey().getName();
				String name2 = entry2.getKey().getName();
				return name1.compareTo(name2);
			}
		});
		fireTableChanged(new TableModelEvent(this, 0, Integer.MAX_VALUE));
	}

	/**
	 * Checks if an item needs reordering.
	 * 
	 * @param row the row number of the amount column
	 * @return true if row item's amount is less than or equal it's reorder point
	 */
	public boolean getReorderNeed(int row) {
		Item item = sortedInventory.get(row).getKey();
		int amount = sortedInventory.get(row).getValue();
		return amount <= item.getReorderPoint();
	}

}
