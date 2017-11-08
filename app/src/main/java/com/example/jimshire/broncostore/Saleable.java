package com.example.jimshire.broncostore;

import java.math.BigDecimal;

/**
 * Created by Jimshire on 10/4/17.
 */

public interface Saleable {
    BigDecimal getPrice();

    String getName();
}
