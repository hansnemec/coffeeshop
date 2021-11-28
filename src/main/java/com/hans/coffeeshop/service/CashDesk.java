package com.hans.coffeeshop.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hans.coffeeshop.domain.AbstractProduct;
import com.hans.coffeeshop.domain.Extra;
import com.hans.coffeeshop.domain.Product;
import com.hans.coffeeshop.util.Assert;

/**
 * Stateful object representing cash desk in a coffee shop.
 * 
 * @author hans
 */
public class CashDesk {

	static final String WITH_KEYWORD = "with";
	static final int RECEIPT_LINE_WIDTH = 60;

	final Comparator<AbstractProduct> comparatorByPrice = new Comparator<AbstractProduct>() {
		@Override
		public int compare(AbstractProduct x, AbstractProduct y) {
			double diff = x.getPrice() - y.getPrice();
			
			if (diff > 0) {
				return -1;
			}
			
			if (diff < 0) {
				return 1;
			}
			
			return 0;
		}
	};
	
	final Comparator<Product> comparatorByExtraPrice = new Comparator<Product>() {
		public int compare(Product x, Product y) {
			List<? extends AbstractProduct> extras1 = x.getExtras();
			List<? extends AbstractProduct> extras2 = y.getExtras();
			
			if (extras1.isEmpty() && extras2.isEmpty()) {
				return 0;
			}
			if (!extras1.isEmpty() && extras2.isEmpty()) {
				return -1;
			}
			if (extras1.isEmpty() && !extras2.isEmpty()) {
				return 1;
			}
			
			extras1 = new ArrayList<>(extras1);
			Collections.sort(extras1, comparatorByPrice);
			extras2 = new ArrayList<>(extras2);
			Collections.sort(extras2, comparatorByPrice);
			
			return comparatorByPrice.compare(extras1.get(0), extras2.get(0));
		}
	};
	
	final List<Product> productDefinitionList = new ArrayList<Product>();
	final List<Product> currentOrder = new ArrayList<>();
	final Map<String, Integer> drinkCountByCustomer = new HashMap<>();
	
	/**
	 * Create new cashdesk and init the available product list.
	 */
	public CashDesk() {
		List<Extra> coffeePossibleExtras = new ArrayList<>();
		coffeePossibleExtras.add(new Extra("extra milk", 0.3));
		coffeePossibleExtras.add(new Extra("foamed milk", 0.5));
		coffeePossibleExtras.add(new Extra("special roast coffee", 0.9));
		
		// treating S/M/L coffees as different products
		productDefinitionList.add(new Product("small coffee", 2.5, coffeePossibleExtras));
		productDefinitionList.add(new Product("medium coffee", 3, coffeePossibleExtras));
		productDefinitionList.add(new Product("large coffee", 3.5, coffeePossibleExtras));
		
		productDefinitionList.add(new Product("bacon roll", 4.5, null));
		productDefinitionList.add(new Product("orange juice", 3.95, null));
	}
	
	/**
	 * Returns the list of avalilable products.
	 * 
	 * @return List of products available on this cashdesk
	 */
	public List<Product> getAvalilableProducts() {
		return Collections.unmodifiableList(productDefinitionList);
	}
	
	/**
	 * Add an item to the current order.
	 * 
	 * @param order Mandatory - the order as said by the customer
	 * @return The answer - "OK" if ok, something else otherwise
	 */
	public String order(String order) throws Exception {
		Assert.notEmpty(order, "Cannot order - argument is null or empty string");
	
		order = order.trim().toLowerCase();
		
		int indexOfWith = order.indexOf(WITH_KEYWORD);
		final String productName = (indexOfWith > 1) ? order.substring(0, indexOfWith - 1) : order;
		
		Product productDefinition = findProduct(productName);
		if (productDefinition == null) {
			return "Sorry, we don't have any '" + productName + "'";
		}
		
		List<Extra> orderedExtras = new ArrayList<>();
		if (indexOfWith > 0) {
			String extras = order.substring(indexOfWith + WITH_KEYWORD.length() + 1);
			String[] extrasArr = extras.split(",", 0);
			for (String extra : extrasArr) {
				Extra ex = productDefinition.findExtra(extra.trim());
				if (ex == null) {
					return "Sorry, cannot make " + productDefinition.getName() + " with '" + extra + "'";
				}
				
				orderedExtras.add(ex);
			}
		}
		
		Product ordered = new Product(productDefinition.getName(), productDefinition.getPrice(), orderedExtras);
		
		currentOrder.add(ordered);
		return "OK :-)";
	}
	
	/**
	 * Finish the current order and return receipt.
	 * 
	 * @param customerName Optional
	 * @return The receipt - one line per list element
	 */
	public List<String> done(String customerName) throws Exception {
		if (currentOrder.isEmpty()) {
			return Collections.emptyList();
		}
		
		updateHistoryAndComputeDiscounts(customerName);
		
		List<String> receipt = new ArrayList<>();
		double total = 0;
		
		for (Product p : currentOrder) {
			addReceiptLine(receipt, p.getName(), p.getPrice());
			total += p.getPrice();
			
			for (Extra ex : p.getExtras()) {
				// indent the extra item a little ;-)
				addReceiptLine(receipt, "   " + ex.getName(), ex.getPrice());
				total += ex.getPrice();
			}
		}
		
		addReceiptLine(receipt, "total", total);
		
		currentOrder.clear();
		return receipt;
	}

	void addReceiptLine(List<String> receipt, String name, double price) {
		String formattedName = name + " ";
		if (Double.valueOf(price).intValue() == 0) {
			formattedName += "*free* ";
		}
		String formattedPrice = String.format(" %.2f CHF", price);
		
		// the compiler may create the stringbuilder anyway..
		// but creating it myself to be absolutely sure ;-)
		StringBuilder receiptLine = new StringBuilder(formattedName);
		for (int i = formattedName.length() + formattedPrice.length(); i < RECEIPT_LINE_WIDTH; i++) {
			receiptLine.append('.');
		}
		receiptLine.append(formattedPrice);
		receipt.add(receiptLine.toString());
	}
	
	Product findProduct(String name) {
		return productDefinitionList.stream().filter(p -> name.equalsIgnoreCase(p.getName())).findAny().orElse(null);
	}
	
	void updateHistoryAndComputeDiscounts(String customer) {
		List<Product> snacks = new ArrayList<>();
		List<Product> drinks = new ArrayList<>();
		for (Product p : currentOrder) {
			// super simple implementation ;-)
			// bacon roll only is the only snack we currently sell
			if ("bacon roll".equals(p.getName())) {
				snacks.add(p);
			}
			else {
				drinks.add(p);
			}
		}
		
		if (!drinks.isEmpty()) {
			int totalDrinkCountUntilNow = (customer != null) ? drinkCountByCustomer.getOrDefault(customer, 0) : 0;
			int currentDrinkCount = drinks.size();
			int freeCount = 0;
			for (int i = totalDrinkCountUntilNow; i <= totalDrinkCountUntilNow + currentDrinkCount; i++) {
				if (i > 0 && i % 5 == 0) {
					freeCount++;
				}
			}
			
			// super customer-friendly implementation ;-)
			List<Product> drinksOrderedByPriceDesc = new ArrayList<>(drinks);
			Collections.sort(drinksOrderedByPriceDesc, comparatorByPrice);
			
			for (int i = 0; i < freeCount; i++) {
				drinksOrderedByPriceDesc.get(i).setPrice(0);
			}
			
			if (customer != null) {
				drinkCountByCustomer.put(customer, totalDrinkCountUntilNow + drinks.size());
			}
			
			if (!snacks.isEmpty()) {
				List<Product> drinksWithExtra = drinks.stream().filter(d -> !d.getExtras().isEmpty()).collect(Collectors.toList());
				if (drinksWithExtra.isEmpty()) {
					return;
				}
				
				// once again super customer-friendly implementation
				Collections.sort(drinksWithExtra, comparatorByExtraPrice);
				int freeExtraCount = Math.min(snacks.size(), drinksWithExtra.size());
				
				for (int i = 0; i < freeExtraCount; i++) {
					List<Extra> extrasOrderedByPriceDesc = new ArrayList<>(drinksWithExtra.get(i).getExtras());
					Collections.sort(extrasOrderedByPriceDesc, comparatorByPrice);
					extrasOrderedByPriceDesc.get(0).setPrice(0);
				}
			}			
		}
	}
}
