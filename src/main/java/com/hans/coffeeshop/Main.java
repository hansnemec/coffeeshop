package com.hans.coffeeshop;

import java.util.List;
import java.util.Scanner;

import com.hans.coffeeshop.domain.Extra;
import com.hans.coffeeshop.domain.Product;
import com.hans.coffeeshop.service.CashDesk;

/**
 * Main class with the executable method.
 * 
 * @author hans
 */
public class Main {

	public static void main(String[] args) throws Exception {
		System.out.println("Welcome to Charlene's Coffee Corner");
		System.out.println("Type 'help' to get help");
		printPrompt();
		
		CashDesk coffeeShop = new CashDesk();
		
		try (Scanner scanner = new Scanner(System.in)) {
			String line = null;
			
			while ((line = scanner.nextLine()) != null) {
				line = line.trim().toLowerCase(); // to be absolutely sure
				
				if ("close".equals(line)) {
					System.out.println("Bye ;-)");
					return;
				}
				
				if ("help".equals(line)) {
					printHelp(coffeeShop);
				}
				else if (line.startsWith("done")) {
					String[] splitByWhiteSpace = line.split("\\W", 2);
					String customer = (splitByWhiteSpace.length > 1) ? splitByWhiteSpace[1].trim() : null;
					List<String> receipt = coffeeShop.done(customer);
										
					if (receipt.isEmpty()) {
						System.out.println("Eeh, the current order is empty :-/");
					}
					
					if (customer != null) {
						System.out.println("Dear " + customer + ", here's your order:");
					}
					else {
						System.out.println("Here's your order:");	
					}
					
					for (String s : receipt) {
						System.out.println(s);
					}
				}
				else {
					String reply = coffeeShop.order(line);
					System.out.println(reply);
				}
				printPrompt();
			}
		}
	}

	static void printPrompt() {
		System.out.print("coffeeshop> ");
	}
	
	static void printHelp(CashDesk desk) {
		System.out.println("Recognized commands:");
		System.out.println("\t<product name> [with extra1, [extra2], ...]");
		System.out.println("\t\tadd product to the current order - optionally with extras");
		System.out.println("\t\tsee the product list and possible extras below");
		System.out.println("\t\tnote that 'with' is a reserved keyword ;-)");
		System.out.println("\tdone [customer name]");
		System.out.println("\t\t finish the current order and print receipt - provide customer name to record the order on his discount card ;-)");
		System.out.println("\tclose");
		System.out.println("\t\tclose the shop (for today ;-) and return to the shell");
		System.out.println("Available products:");
		List<Product> ls = desk.getAvalilableProducts();
		for (Product p : ls) {
			System.out.println("\t" + p.getName() + String.format(" (%.2f CHF)", p.getPrice()));
			List<Extra> extras = p.getExtras();
			if (!extras.isEmpty()) {
				System.out.println("\t\tpossible extras:");
				for (Extra e : extras) {
					System.out.println("\t\t" + e.getName() + String.format(" (%.2f CHF)", e.getPrice()));
				}
			}
		}
	}
}
