package org.kiirun.beverages.domain;

import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class AccumulatedSalesTest {
    private static final String TERMINAL_1 = "T1";

    @Test
    public void emptySalesYieldsEmptyResult() {
        final AccumulatedSales emptySales = AccumulatedSales.calculatedFrom(Collections.emptyList());

        Assert.assertThat(emptySales.getTotal(), is(BigDecimal.ZERO));
        Assert.assertTrue(emptySales.getSalesFigures().isEmpty());
    }

    @Test
    public void singleCokeSaleYieldsCorrectResult() {
        final AccumulatedSales singleSale = AccumulatedSales.calculatedFrom(Collections.singleton(sellCoke(1L)));

        Assert.assertThat(singleSale.getTotal(), is(BigDecimal.valueOf(1.5).setScale(2)));
        Assert.assertThat(singleSale.getSalesFigures(), hasEntry(coke(), 1L));
    }

    @Test
    public void multipleSalesYieldCorrectResult() {
        final AccumulatedSales multipleSales = AccumulatedSales
                .calculatedFrom(Arrays.asList(sellCoke(2L), sellWater(1L)));

        Assert.assertThat(multipleSales.getTotal(), is(BigDecimal.valueOf(4).setScale(2)));
        Assert.assertThat(multipleSales.getSalesFigures(), hasEntry(coke(), 2L));
        Assert.assertThat(multipleSales.getSalesFigures(), hasEntry(water(), 1L));
    }

    private BeverageSale sellCoke(final Long amount) {
        return BeverageSale.create(coke(), amount, TERMINAL_1);
    }

    private BeverageSale sellWater(final Long amount) {
        return BeverageSale.create(water(), amount, TERMINAL_1);
    }

    private Beverage coke() {
        final Beverage coke = new Beverage("Coke", BigDecimal.valueOf(1.5).setScale(2));
        coke.setId("COKE-ID");
        return coke;
    }

    private Beverage water() {
        final Beverage water = new Beverage("Water", BigDecimal.ONE);
        water.setId("WATER-ID");
        return water;
    }
}
