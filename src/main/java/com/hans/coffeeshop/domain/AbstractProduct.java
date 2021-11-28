package com.hans.coffeeshop.domain;

import com.hans.coffeeshop.util.Assert;

/**
 * Base class for domain objects represening some kind of product.
 * 
 * @author hans
 */
public abstract class AbstractProduct {

	final String name;
	double price;

	public AbstractProduct(String name, double price) {
		Assert.notEmpty(name, "Cannot create " + getClass().getSimpleName() + " - name is null or empty string");
		Assert.isTrue(price > 0, "Cannot create " + getClass().getSimpleName() + " - price must be greater than zero");
		
		this.name = name;
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public double getPrice() {
		return price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
}
