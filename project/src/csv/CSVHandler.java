package csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import stock.*;
import delivery.*;
import store.*;

/**
 * Handles the reading and writing of relevant csv files.
 * The csv are of three different formats: item properties, sales logs, and manifests
 * Attempting to read a csv file of the wrong format will result in an exception.
 * 
 * @author alfhj
 *
 */
public final class CSVHandler {
	
	private static Pattern csvExt = Pattern.compile("\\.[Cc][Ss][Vv]$");
	
	private CSVHandler() {}
	
	/**
	 * Reads a file using BufferedReader and returns the contents as a String.
	 * It converts the newlines to "\n" 
	 * 
	 * @param file the file to be read
	 * @return the file's content
	 * @throws IOException
	 */
	public static String readCSV(File file) throws IOException {
		FileReader reader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(reader);
		String output = "";
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			output += line + "\n";
		}
		bufferedReader.close();
		return output;
	}
	
	/**
	 * Writes a String to a file by using FileWriter.
	 * If the file's extension is not ".csv", it adds this extension to the end of the filename.
	 * 
	 * @param file the file to be written to
	 * @param output the String to be written to the file
	 * @throws IOException
	 */
	public static void writeCSV(File file, String output) throws IOException {
		if (! csvExt.matcher(file.getPath()).find()) {
			file = new File(file.getPath() + ".csv");
		}
		FileWriter writer = new FileWriter(file);
		writer.write(output);
		writer.close();
	}
	
	/**
	 * Reads an item property csv file and returns the Stock containing
	 * Item objects corresponding to these items. The amounts are set to 0.
	 * 
	 * @param file the item property csv file
	 * @return the Stock containing the Item
	 * @throws IOException
	 * @throws CSVFormatException if the csv file is not a item property file
	 * @throws StockException via Item and Stock
	 */
	public static Stock readItemProperties(File file) throws IOException, CSVFormatException, StockException {
		String input = readCSV(file);
		Stock stock = new Stock();
		for (String line: input.split("\n")) {
			String[] fields = line.split(",");
			if (fields.length != 5 && fields.length != 6) throw new CSVFormatException("This is not a valid item properties file");
			String name = fields[0];
			int cost = Integer.parseInt(fields[1]);
			int price = Integer.parseInt(fields[2]);
			int repoint = Integer.parseInt(fields[3]);
			int reamount = Integer.parseInt(fields[4]);
			
			if (fields.length == 5) {
				Item item = new Item(name, cost, price, repoint, reamount);
				stock.addItems(item, 0);
			} else if (fields.length == 6) {
				int temp = Integer.parseInt(fields[5]);
				Item item = new Item(name, cost, price, repoint, reamount, temp);
				stock.addItems(item, 0);
			}
		}
		return stock;
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws CSVFormatException
	 * @throws StockException
	 */
	public static Stock readSalesLog(File file) throws IOException, CSVFormatException, StockException {
		String input = readCSV(file);
		Stock stock = new Stock();
		for (String line: input.split("\n")) {
			String[] fields = line.split(",");
			if (fields.length != 2) throw new CSVFormatException("This is not a valid sales log");
			String name = fields[0];
			int amount = Integer.parseInt(fields[1]);
			Item item = Store.getInstance().getInventory().searchItem(name).getKey();
			stock.addItems(item, amount);
		}
		return stock;
	}
	
	public static Manifest readManifest(File file) throws IOException, CSVFormatException, DeliveryException, StockException {
		String input = readCSV(file);
		Manifest manifest = new Manifest();
		Stock currentStock = null;
		for (String line: input.split("\n")) {
			if (line.charAt(0) == '>') {
				if (currentStock != null) manifest.addTruck(TruckFactory.getTruck(currentStock));
				if (line.substring(1).equals("Ordinary") || line.substring(1).equals("Refrigerated")) {
					currentStock = new Stock();
				} else {
					throw new CSVFormatException("Expected Ordinary or Refrigerated truck");
				}
				continue;
			} else {
				// if stock is not initialised, top line is not a truck type
				if (currentStock == null) throw new CSVFormatException("This is not a valid manifest");
				String[] fields = line.split(",");
				if (fields.length != 2) throw new CSVFormatException("This is not a valid manifest");
				String name = fields[0];
				int amount = Integer.parseInt(fields[1]);
				Item item = Store.getInstance().getInventory().searchItem(name).getKey();
				currentStock.addItems(item, amount);
			}
		}
		if (currentStock != null) manifest.addTruck(TruckFactory.getTruck(currentStock));
		
		return manifest;
	}
	
	public static void writeManifest(File file, Manifest manifest) throws IOException {
		String output = "";
		for (Truck truck: manifest.getFleet()) {
			if (truck instanceof OrdinaryTruck) {
				output += ">Ordinary\r\n";
			} else if (truck instanceof RefrigeratedTruck) {
				output += ">Refrigerated\r\n";
			}
			for (Entry<Item, Integer> entry: truck.getCargo().getItems().entrySet()) {
				output += entry.getKey().getName() + "," + entry.getValue() + "\r\n";
			}
		}
		writeCSV(file, output);
	}
	
}
