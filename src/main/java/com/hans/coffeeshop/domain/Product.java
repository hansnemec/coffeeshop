package com.hans.coffeeshop.domain;

import java.util.Collections;
import java.util.List;

import com.hans.coffeeshop.util.Assert;

/**
 * Domain object that represents a coffeeshop product.
 * Can be either a product "definition" or a product "instance".
 * 
 * @author hans
 */
public class Product extends AbstractProduct {
	
	final List<Extra> extras;
	
	public Product(String name, double price, List<Extra> extras) {
		super(name, price);
		this.extras = (extras != null) ? Collections.unmodifiableList(extras) : Collections.emptyList();
	}

	public List<Extra> getExtras() {
		return extras;
	}
	
	public Extra findExtra(String extraName) {
		Assert.notEmpty(extraName, "Cannot findExtra - argument is null or empty string");
		
		return extras.stream().filter(e -> extraName.equalsIgnoreCase(e.getName())).findAny().orElse(null);
	}
}
