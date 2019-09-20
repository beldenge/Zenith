package com.ciphertool.zenith.genetic.algorithms.mutation;

import com.ciphertool.zenith.genetic.dao.GeneDao;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MandatorySingleMutationAlgorithm implements MutationAlgorithm<Chromosome<Object>> {
    @Autowired
    private GeneDao geneDao;

    @Override
    public boolean mutateChromosome(Chromosome<Object> chromosome) {
        Chromosome original = chromosome.clone();

        List<Object> availableKeys = new ArrayList<>(chromosome.getGenes().keySet());

        int randomIndex = (int) (ThreadLocalRandom.current().nextDouble() * availableKeys.size());
        Object randomKey = availableKeys.get(randomIndex);

        // Replace that map value with a randomly generated Gene
        chromosome.replaceGene(randomKey, geneDao.findRandomGene(chromosome));

        return !original.equals(chromosome);
    }
}
