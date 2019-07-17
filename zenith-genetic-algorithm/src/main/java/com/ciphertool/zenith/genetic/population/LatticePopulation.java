/**
 * Copyright 2017-2019 George Belden
 *
 * This file is part of Zenith.
 *
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.genetic.population;

import com.ciphertool.zenith.genetic.algorithms.selection.Selector;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Parents;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class LatticePopulation extends AbstractPopulation {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${genetic-algorithm.population.lattice.rows}")
    private int latticeRows;

    @Value("${genetic-algorithm.population.lattice.columns}")
    private int latticeColumns;

    private int currentRow = 0;
    private int nextColumn = 0;
    private Chromosome[][] individuals;
    private Selector selector;

    @Override
    public Callable newSelectionTask(){
        return new SelectionTask();
    }

    private class SelectionTask implements Callable<Parents> {
        public SelectionTask() {
        }

        @Override
        public Parents call() {
            int row = ThreadLocalRandom.current().nextInt(latticeRows);
            int column = ThreadLocalRandom.current().nextInt(latticeColumns);

            List<LatticeIndividual> nearbyLatticeIndividuals = new ArrayList<>();

            // center
            nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[row][column], row, column));

            // immediately above
            if (row > 0) {
                nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[row - 1][column], row - 1, column));
            }

            // top-right diagonal
            if (row > 0 && column < individuals[row - 1].length - 1) {
                nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[row - 1][column + 1], row - 1, column + 1));
            }

            // immediate right
            if (column < individuals[row].length - 1) {
                nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[row][column + 1], row, column + 1));
            }

            // bottom-right diagonal
            if (row < individuals.length - 1 && column < individuals[row + 1].length - 1) {
                nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[row + 1][column + 1], row + 1, column + 1));
            }

            // immediately below
            if (row < individuals.length - 1) {
                nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[row + 1][column], row + 1, column));
            }

            // bottom-left diagonal
            if (row < individuals.length - 1 && column > 0) {
                nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[row + 1][column - 1], row + 1, column - 1));
            }

            // immediate left
            if (column > 0) {
                nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[row][column - 1], row, column - 1));
            }

            // top-left diagonal
            if (row > 0 && column > 0) {
                nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[row - 1][column - 1], row - 1, column - 1));
            }

            Collections.sort(nearbyLatticeIndividuals);

            List<Chromosome> nearbyIndividuals = new ArrayList<>();

            nearbyIndividuals.addAll(nearbyLatticeIndividuals.stream()
                    .map(LatticeIndividual::getIndividual)
                    .collect(Collectors.toList()));

            Collections.sort(nearbyIndividuals);

            int momIndex = selector.getNextIndexThreadSafe(nearbyIndividuals);
            LatticeIndividual momCoordinates = nearbyLatticeIndividuals.get(momIndex);
            Chromosome mom = individuals[momCoordinates.getRow()][momCoordinates.getColumn()];

            // Ensure that dadIndex is different from momIndex
            nearbyIndividuals.remove(momIndex);
            nearbyLatticeIndividuals.remove(momIndex);
            int dadIndex = selector.getNextIndexThreadSafe(nearbyIndividuals);
            LatticeIndividual dadCoordinates = nearbyLatticeIndividuals.get(dadIndex);
            Chromosome dad = individuals[dadCoordinates.getRow()][dadCoordinates.getColumn()];

            return new Parents(mom, dad);
        }
    }

    @AllArgsConstructor
    @Getter
    private class LatticeIndividual implements Comparable<LatticeIndividual> {
        private Chromosome individual;
        private int row;
        private int column;

        @Override
        public int compareTo(LatticeIndividual other) {
            return this.individual.compareTo(other.individual);
        }
    }

    @Override
    public int breed() {
        if ((latticeRows * latticeColumns) != targetSize) {
            throw new IllegalArgumentException("The target size " + targetSize + " for LatticePopulation must be " +
                    "equal to the product its rows and columns.  Rows=" + latticeRows + ", Columns=" + latticeColumns + ".");
        }

        return super.breed();
    }

    @Override
    public void clearIndividuals() {
        individuals = new Chromosome[latticeRows][latticeColumns];
        currentRow = 0;
        nextColumn = 0;
    }

    @Override
    public void printAscending() {
        List<Chromosome> sortedIndividuals = this.getIndividuals();

        int size = sortedIndividuals.size();

        for (int i = 0; i < size; i++) {
            log.info("Chromosome {}: {}", (i + 1), sortedIndividuals.get(i));
        }
    }

    @Override
    public int size() {
        return latticeRows * latticeColumns;
    }

    @Override
    public List<Chromosome> getIndividuals() {
        List<Chromosome> individualsAsList = new ArrayList<>();

        for (int x = 0; x < latticeRows; x++) {
            for (int y = 0; y < latticeColumns; y++) {
                individualsAsList.add(individuals[x][y]);
            }
        }

        Collections.sort(individualsAsList);

        return individualsAsList;
    }

    @Override
    public synchronized boolean addIndividual(Chromosome individual) {
        if (currentRow > latticeRows - 1) {
            throw new IllegalStateException("Attempted to add an individual to LatticePopulation at row " + currentRow + ", but the population is already full.");
        }

        this.individuals[currentRow][nextColumn] = individual;

        if (nextColumn == latticeColumns - 1) {
            currentRow ++;
            nextColumn = 0;
        } else {
            nextColumn ++;
        }

        individual.setPopulation(this);

        this.totalFitness += individual.getFitness() == null ? 0d : individual.getFitness();

        return individual.isEvaluationNeeded();
    }

    @Override
    public void sortIndividuals() {
        // Nothing to do
    }

    @Override
    public void reIndexSelector() {
        // Nothing to do
    }

    @Override
    public void setSelector(Selector selector) {
        this.selector = selector;
    }
}
