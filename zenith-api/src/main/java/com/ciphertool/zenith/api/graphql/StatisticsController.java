package com.ciphertool.zenith.api.graphql;

import com.ciphertool.zenith.api.model.*;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.statistics.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class StatisticsController {
    @Autowired
    private CiphertextUniqueSymbolsEvaluator uniqueSymbolsEvaluator;

    @Autowired
    private CiphertextMultiplicityEvaluator multiplicityEvaluator;

    @Autowired
    private CiphertextEntropyEvaluator entropyEvaluator;

    @Autowired
    private CiphertextIndexOfCoincidenceEvaluator indexOfCoincidenceEvaluator;

    @Autowired
    private CiphertextRepeatingBigramEvaluator bigramEvaluator;

    @Autowired
    private CiphertextCycleCountEvaluator cycleCountEvaluator;

    @Autowired
    private CiphertextNgramEvaluator ciphertextNgramEvaluator;

    @QueryMapping
    @Cacheable(value = "uniqueSymbols", key = "#cipher.name")
    public DoubleResponse uniqueSymbols(@Argument CipherRequest cipher) {
        return new DoubleResponse(uniqueSymbolsEvaluator.evaluate(cipher.asCipher()));
    }

    @QueryMapping
    @Cacheable(value = "multiplicities", key = "#cipher.name")
    public DoubleResponse multiplicity(@Argument CipherRequest cipher) {
        return new DoubleResponse(multiplicityEvaluator.evaluate(cipher.asCipher()));
    }

    @QueryMapping
    @Cacheable(value = "entropies", key = "#cipher.name")
    public DoubleResponse entropy(@Argument CipherRequest cipher) {
        return new DoubleResponse(entropyEvaluator.evaluate(cipher.asCipher()));
    }

    @QueryMapping
    @Cacheable(value = "indexesOfCoincidence", key = "#cipher.name")
    public DoubleResponse indexOfCoincidence(@Argument CipherRequest cipher) {
        return new DoubleResponse(indexOfCoincidenceEvaluator.evaluate(cipher.asCipher()));
    }

    @QueryMapping
    @Cacheable(value = "bigramRepeats", key = "#cipher.name")
    public IntResponse bigramRepeats(@Argument CipherRequest cipher) {
        return new IntResponse(bigramEvaluator.evaluate(cipher.asCipher()));
    }

    @QueryMapping
    @Cacheable(value = "cycleScores", key = "#cipher.name")
    public IntResponse cycleScore(@Argument CipherRequest cipher) {
        return new IntResponse(cycleCountEvaluator.evaluate(cipher.asCipher()));
    }

    @QueryMapping
    public NGramStatistics nGramStatistics(@Argument CipherRequest request, @Argument int statsPage) {
        Cipher cipher = request.asCipher();

        List<NGramCount> firstNGramCounts = getNGramCounts(cipher, statsPage, 1);
        List<NGramCount> secondNGramCounts = getNGramCounts(cipher, statsPage, 2);
        List<NGramCount> thirdNGramCounts = getNGramCounts(cipher, statsPage, 3);

        return new NGramStatistics(firstNGramCounts, secondNGramCounts, thirdNGramCounts);
    }

    private List<NGramCount> getNGramCounts(Cipher cipher, int statsPage, int offset) {
        Map<String, Integer> nGramCountMap = ciphertextNgramEvaluator.evaluate(cipher, (statsPage * 3) + offset);
        List<NGramCount> nGramCounts = new ArrayList<>(nGramCountMap.size());

        for (Map.Entry<String, Integer> entry : nGramCountMap.entrySet()) {
            nGramCounts.add(new NGramCount(entry.getKey(), entry.getValue()));
        }

        return nGramCounts;
    }

    @SchemaMapping
    public double value(DoubleResponse doubleResponse) {
        return doubleResponse.getValue();
    }

    @SchemaMapping
    public int value(IntResponse intResponse) {
        return intResponse.getValue();
    }

    @SchemaMapping
    public List<NGramCount> firstNGramCounts(NGramStatistics ngramStatistics) {
        return ngramStatistics.getFirstNGramCounts();
    }

    @SchemaMapping
    public List<NGramCount> secondNGramCounts(NGramStatistics ngramStatistics) {
        return ngramStatistics.getSecondNGramCounts();
    }

    @SchemaMapping
    public List<NGramCount> thirdNGramCounts(NGramStatistics ngramStatistics) {
        return ngramStatistics.getThirdNGramCounts();
    }

    @SchemaMapping
    public String ngram(NGramCount ngramCount) {
        return ngramCount.getNgram();
    }

    @SchemaMapping
    public Integer count(NGramCount ngramCount) {
        return ngramCount.getCount();
    }
}
