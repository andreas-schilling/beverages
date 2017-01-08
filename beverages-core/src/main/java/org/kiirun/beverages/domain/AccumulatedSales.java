package org.kiirun.beverages.domain;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class AccumulatedSales {
    private final Map<Beverage, Long> salesFigures;

    private final BigDecimal total;

    private AccumulatedSales(final Collection<BeverageSale> sales) {
        this.salesFigures = sales.stream().collect(
                Collectors.groupingBy(BeverageSale::getBeverage, Collectors.summingLong(BeverageSale::getAmount)));
        this.total = sales.stream().map(BeverageSale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static final AccumulatedSales calculatedFrom(final Collection<BeverageSale> sales) {
        return new AccumulatedSales(sales);
    }

    @JsonSerialize(keyUsing = BeverageNameSerializer.class)
    public Map<Beverage, Long> getSalesFigures() {
        return salesFigures;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
