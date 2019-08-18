package com.ciphertool.zenith.search.model;

import com.ciphertool.zenith.inference.entities.CipherSolution;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class EpochResults {
    private int epoch;
    private int bestSolutionIteration;
    private CipherSolution bestSolution;
    private List<Integer> bestSolutionColumnKey;
}
