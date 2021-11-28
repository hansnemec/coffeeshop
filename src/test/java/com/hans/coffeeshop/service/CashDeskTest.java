package com.hans.coffeeshop.service;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hans.coffeeshop.service.CashDesk;

public class CashDeskTest {

	CashDesk coffeeShop;
	
	@Before
	public void setUp() throws Exception {
		coffeeShop = new CashDesk();
	}
	
	@Test
	public void testSimple() throws Exception {
		coffeeShop.order("medium coffee");
		List<String> ls = coffeeShop.done(null);
		
		assertLineMatches("medium coffee", "3.00 CHF", ls.get(0));
		assertLineMatches("total", "3.00 CHF", ls.get(ls.size() - 1));
	}
	
	@Test
	public void testSimpleWithCustomerCard() throws Exception {
		coffeeShop.order("medium coffee");
		List<String> ls = coffeeShop.done("karel");
		
		assertLineMatches("medium coffee", "3.00 CHF", ls.get(0));
		assertLineMatches("total", "3.00 CHF", ls.get(ls.size() - 1));
	}
	
	@Test
	public void testMoreItems() throws Exception {
		coffeeShop.order("medium coffee");
		coffeeShop.order("orange juice");
		List<String> ls = coffeeShop.done(null);
		
		assertLineMatches("medium coffee", "3.00 CHF", ls.get(0));
		assertLineMatches("orange juice", "3.95 CHF", ls.get(1));
		assertLineMatches("total", "6.95 CHF", ls.get(ls.size() - 1));
	}
	
	@Test
	public void testWithExtra() throws Exception {
		coffeeShop.order("medium coffee with extra milk");
		List<String> ls = coffeeShop.done(null);
		
		assertLineMatches("medium coffee", "3.00 CHF", ls.get(0));
		assertLineMatches("extra milk", "0.30 CHF", ls.get(1));
		assertLineMatches("total", "3.30 CHF", ls.get(ls.size() - 1));
	}
	
	@Test
	public void testWithMoreExtras() throws Exception {
		coffeeShop.order("medium coffee with extra milk, special roast coffee");
		List<String> ls = coffeeShop.done(null);
		
		assertLineMatches("medium coffee", "3.00 CHF", ls.get(0));
		assertLineMatches("extra milk", "0.30 CHF", ls.get(1));
		assertLineMatches("special roast coffee", "0.90 CHF", ls.get(2));
		assertLineMatches("total", "4.20 CHF", ls.get(ls.size() - 1));
	}
	
	@Test
	public void testFreeDrinkOneByOne() throws Exception {
		coffeeShop.order("medium coffee");
		coffeeShop.done("hans");
		coffeeShop.order("large coffee");
		coffeeShop.done("hans");
		coffeeShop.order("orange juice");
		coffeeShop.done("hans");
		coffeeShop.order("medium coffee");
		coffeeShop.done("hans");
		coffeeShop.order("medium coffee");
		List<String> ls = coffeeShop.done("hans");
		
		assertLineMatches("medium coffee", "0.00 CHF", ls.get(0));
		assertLineMatches("total", "0.00 CHF", ls.get(ls.size() - 1));
	}
	
	@Test
	public void testFreeDrinkWithBiggerOrders() throws Exception {
		coffeeShop.order("medium coffee");
		coffeeShop.order("large coffee");
		coffeeShop.done("hans");
		coffeeShop.order("orange juice");
		coffeeShop.order("large coffee");
		coffeeShop.order("medium coffee");
		List<String> ls = coffeeShop.done("hans");
		
		assertLineMatches("orange juice", "0.00 CHF", ls.get(0));
		assertLineMatches("large coffee", "3.50 CHF", ls.get(1));
		assertLineMatches("medium coffee", "3.00 CHF", ls.get(2));
		assertLineMatches("total", "6.50 CHF", ls.get(ls.size() - 1));
	}

	@Test
	public void testFreeExtra() throws Exception {
		coffeeShop.order("medium coffee with extra milk");
		coffeeShop.order("bacon roll");
		List<String> ls = coffeeShop.done("hans");
		
		assertLineMatches("medium coffee", "3.00 CHF", ls.get(0));
		assertLineMatches("extra milk", "0.00 CHF", ls.get(1));
		assertLineMatches("bacon roll", "4.50 CHF", ls.get(2));
		assertLineMatches("total", "7.50 CHF", ls.get(ls.size() - 1));
	}

	@Test
	public void testFreeDrinkIsTheMostExpensive() throws Exception {
		coffeeShop.order("small coffee");
		coffeeShop.order("medium coffee");
		coffeeShop.done("pepa");
		coffeeShop.order("orange juice");
		coffeeShop.order("small coffee");
		coffeeShop.order("large coffee");
		List<String> ls = coffeeShop.done("pepa");
		assertLineMatches("orange juice", "0.00 CHF", ls.get(0));
		assertLineMatches("small coffee", "2.50 CHF", ls.get(1));
		assertLineMatches("large coffee", "3.50 CHF", ls.get(2));
		assertLineMatches("total", "6.00 CHF", ls.get(ls.size() - 1));
	}
	
	void assertLineMatches(String expectedStart, String expectedEnd, String actual) {
		actual = actual.trim().toLowerCase();
		
		Assert.assertTrue(actual.startsWith(expectedStart.toLowerCase()));
		Assert.assertTrue(actual.endsWith(expectedEnd.toLowerCase()));
	}
}
