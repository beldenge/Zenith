package com.ciphertool.zenith.inference.evaluator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class LstmProbability {
    private BigDecimal probability;
    private Float logProbability;
}