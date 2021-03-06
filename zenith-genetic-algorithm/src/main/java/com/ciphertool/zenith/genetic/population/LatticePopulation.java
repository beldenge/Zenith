/*
 * Copyright 2017-2020 George Belden
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

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.entities.Parents;
import com.ciphertool.zenith.genetic.operators.selection.Selector;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@NoArgsConstructor
@Component
public class LatticePopulation extends AbstractPopulation {
    private int currentRow = 0;
    private int currentColumn = 0;
    private Genome[][] individuals;
    private int latticeRows;
    private int latticeColumns;
    private boolean wrapAround;
    private int selectionRadius;

    public LatticePopulation(int latticeRows, int latticeColumns, boolean wrapAround, int selectionRadius) {
        this.latticeRows = latticeRows;
        this.latticeColumns = latticeColumns;
        this.wrapAround = wrapAround;
        this.selectionRadius = selectionRadius;
        this.individuals = new Genome[this.latticeRows][this.latticeColumns];
    }

    @Override
    public Population getInstance() {
        return new LatticePopulation(this.latticeRows, this.latticeColumns, this.wrapAround, this.selectionRadius);
    }

    @Override
    public void init(GeneticAlgorithmStrategy strategy) {
        super.init(strategy);
        this.latticeRows = strategy.getLatticeRows();
        this.latticeColumns = strategy.getLatticeColumns();
        this.wrapAround = strategy.getLatticeWrapAround();
        this.selectionRadius = strategy.getLatticeRadius();

        if ((latticeRows * latticeColumns) != strategy.getPopulationSize()) {
            throw new IllegalArgumentException("The population size " + strategy.getPopulationSize() + " for LatticePopulation must be " +
                    "equal to the product of its rows and columns.  Rows=" + latticeRows + ", Columns=" + latticeColumns + ".");
        }
    }

    @Override
    public Callable newSelectionTask(){
        return new SelectionTask();
    }

    private class SelectionTask implements Callable<Parents> {
        public SelectionTask() {}

        @Override
        public Parents call() {
            int row = ThreadLocalRandom.current().nextInt(latticeRows);
            int column = ThreadLocalRandom.current().nextInt(currentColumn - 1);

            List<LatticeIndividual> nearbyLatticeIndividuals = new ArrayList<>();

            // center
            nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[row][column], row, column));

            for (int i = 0; i < selectionRadius; i ++) {
                // top edge
                for (int j = 0 - selectionRadius; j < selectionRadius; j ++) {
                    int rowIndex = row - selectionRadius;
                    int columnIndex = column + j;

                    if (outOfBounds(rowIndex, columnIndex)) {
                        if (!wrapAround) {
                            continue;
                        }

                        rowIndex = wrapRowIndex(rowIndex);
                        columnIndex = wrapColumnIndex(columnIndex);
                    }

                    nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[rowIndex][columnIndex], rowIndex, columnIndex));
                }

                // right edge
                for (int j = 0 - selectionRadius; j < selectionRadius; j ++) {
                    int rowIndex = row + j;
                    int columnIndex = column + selectionRadius;

                    if (outOfBounds(rowIndex, columnIndex)) {
                        if (!wrapAround) {
                            continue;
                        }

                        rowIndex = wrapRowIndex(rowIndex);
                        columnIndex = wrapColumnIndex(columnIndex);
                    }

                    nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[rowIndex][columnIndex], rowIndex, columnIndex));
                }

                // bottom edge
                for (int j = selectionRadius; j > 0 - selectionRadius; j --) {
                    int rowIndex = row + selectionRadius;
                    int columnIndex = column + j;

                    if (outOfBounds(rowIndex, columnIndex)) {
                        if (!wrapAround) {
                            continue;
                        }

                        rowIndex = wrapRowIndex(rowIndex);
                        columnIndex = wrapColumnIndex(columnIndex);
                    }

                    nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[rowIndex][columnIndex], rowIndex, columnIndex));
                }

                // left edge
                for (int j = selectionRadius; j > 0 - selectionRadius; j --) {
                    int rowIndex = row + j;
                    int columnIndex = column - selectionRadius;

                    if (outOfBounds(rowIndex, columnIndex)) {
                        if (!wrapAround) {
                            continue;
                        }

                        rowIndex = wrapRowIndex(rowIndex);
                        columnIndex = wrapColumnIndex(columnIndex);
                    }

                    nearbyLatticeIndividuals.add(new LatticeIndividual(individuals[rowIndex][columnIndex], rowIndex, columnIndex));
                }
            }

            Collections.sort(nearbyLatticeIndividuals);

            List<Genome> nearbyIndividuals = new ArrayList<>();

            nearbyIndividuals.addAll(nearbyLatticeIndividuals.stream()
                    .map(LatticeIndividual::getIndividual)
                    .collect(Collectors.toList()));

            Collections.sort(nearbyIndividuals);
            Selector newSelector = strategy.getSelector().getInstance();
            newSelector.reIndex(nearbyIndividuals);

            int momIndex = newSelector.getNextIndex(nearbyIndividuals, strategy);
            LatticeIndividual momCoordinates = nearbyLatticeIndividuals.get(momIndex);
            Genome mom = individuals[momCoordinates.getRow()][momCoordinates.getColumn()];

            // Ensure that dadIndex is different from momIndex
            nearbyIndividuals.remove(momIndex);
            nearbyLatticeIndividuals.remove(momIndex);
            newSelector.reIndex(nearbyIndividuals);
            int dadIndex = newSelector.getNextIndex(nearbyIndividuals, strategy);
            LatticeIndividual dadCoordinates = nearbyLatticeIndividuals.get(dadIndex);
            Genome dad = individuals[dadCoordinates.getRow()][dadCoordinates.getColumn()];

            return new Parents(mom, dad);
        }
    }

    private boolean outOfBounds(int rowIndex, int columnIndex) {
        return rowIndex < 0 || rowIndex > (latticeRows - 1) || columnIndex < 0 || columnIndex > (latticeColumns - 1) || columnIndex > (currentColumn - 1);
    }

    private int wrapRowIndex(int rowIndex) {
        if (rowIndex < 0) {
            return latticeRows + rowIndex;
        }

        if (rowIndex > latticeRows - 1) {
            return rowIndex - latticeRows;
        }

        return rowIndex;
    }

    private int wrapColumnIndex(int columnIndex) {
        if (columnIndex < 0) {
            return latticeColumns + columnIndex;
        }

        if (columnIndex > latticeColumns - 1) {
            return columnIndex - latticeColumns;
        }

        return columnIndex;
    }

    @AllArgsConstructor
    @Getter
    private class LatticeIndividual implements Comparable<LatticeIndividual> {
        private Genome individual;
        private int row;
        private int column;

        @Override
        public int compareTo(LatticeIndividual other) {
            return this.individual.compareTo(other.individual);
        }
    }

    @Override
    public void clearIndividuals() {
        individuals = new Genome[latticeRows][latticeColumns];
        currentRow = 0;
        currentColumn = 0;
    }

    @Override
    public int size() {
        return latticeRows * latticeColumns;
    }

    @Override
    public List<Genome> getIndividuals() {
        List<Genome> individualsAsList = getIndividualsAsList();

        Collections.sort(individualsAsList);

        return individualsAsList;
    }

    public List<Genome> getIndividualsUnsorted() {
        return getIndividualsAsList();
    }

    private List<Genome> getIndividualsAsList() {
        List<Genome> individualsAsList = new ArrayList<>();

        for (int y = 0; y < latticeColumns; y++) {
            for (int x = 0; x < latticeRows; x++) {
                individualsAsList.add(individuals[x][y]);
            }
        }

        return individualsAsList;
    }

    @Override
    public synchronized boolean addIndividual(Genome individual) {
        if (currentColumn > latticeColumns - 1) {
            throw new IllegalStateException("Attempted to add an individual to LatticePopulation at column " + currentColumn + ", but the population is already full.");
        }

        this.individuals[currentRow][currentColumn] = individual;

        if (currentRow == latticeRows - 1) {
            currentColumn ++;
            currentRow = 0;
        } else {
            currentRow ++;
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
}
