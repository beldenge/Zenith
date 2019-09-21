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

package com.ciphertool.zenith.inference.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CipherSolution {
    private static Logger log = LoggerFactory.getLogger(CipherSolution.class);

    private static final double FIFTH_ROOT = 1d / 5d;

    protected Cipher cipher;

    private Double probability = 0d;
    private Double logProbability = 0d;

    private Map<String, String> mappings = new HashMap<>();

    private List<Double> logProbabilities = new ArrayList<>();

    private Double indexOfCoincidence = 1d;

    public CipherSolution() {
    }

    public CipherSolution(Cipher cipher, int numCiphertext) {
        if (cipher == null) {
            throw new IllegalArgumentException("Cannot construct CipherSolution with null cipher.");
        }

        this.cipher = cipher;

        mappings = new HashMap<>(numCiphertext);
    }

    public Cipher getCipher() {
        return this.cipher;
    }

    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double score) {
        this.probability = score;
    }

    public Double getIndexOfCoincidence() {
        return indexOfCoincidence;
    }

    public void setIndexOfCoincidence(Double indexOfCoincidence) {
        this.indexOfCoincidence = indexOfCoincidence;
    }

    public Double getLogProbability() {
        if (logProbability != null) {
            return logProbability;
        }

        logProbability = logProbabilities.stream().reduce(0d, (a, b) -> a + b);

        return logProbability;
    }

    public Map<String, String> getMappings() {
        return Collections.unmodifiableMap(mappings);
    }

    public void putMapping(String key, String plaintext) {
        if (null == plaintext) {
            log.warn("Attempted to insert a null mapping to CipherSolution.  Returning. " + this);

            return;
        }

        if (this.mappings.get(key) != null) {
            log.warn("Attempted to insert a mapping to CipherSolution with key " + key
                    + ", but the key already exists.  If this was intentional, please use replaceMapping() instead.  Returning. "
                    + this);

            return;
        }

        this.mappings.put(key, plaintext);
    }

    public String removeMapping(Ciphertext key) {
        if (null == this.mappings || null == this.mappings.get(key)) {
            log.warn("Attempted to remove a mapping from CipherSolution with key " + key
                    + ", but this key does not exist.  Returning null.");

            return null;
        }

        return this.mappings.remove(key);
    }

    public List<Double> getLogProbabilities() {
        return Collections.unmodifiableList(logProbabilities);
    }

    public void clearLogProbabilities() {
        this.logProbabilities.clear();
        this.logProbability = 0d;
    }

    public void addLogProbability(Double logProbability) {
        this.logProbabilities.add(logProbability);
        this.logProbability += logProbability;
    }

    public void replaceLogProbability(int i, Double newLogProbability) {
        Double oldLogProbability = this.logProbabilities.remove(i);
        this.logProbabilities.add(i, newLogProbability);
        this.logProbability -= oldLogProbability;
        this.logProbability += newLogProbability;
    }

    /*
     * This does the same thing as putMapping(), and exists solely for semantic consistency.
     */
    public void replaceMapping(String key, String newPlaintext) {
        if (null == newPlaintext) {
            log.warn("Attempted to replace a mapping from CipherSolution, but the supplied mapping was null.  Cannot continue. "
                    + this);

            return;
        }

        if (null == this.mappings || null == this.mappings.get(key)) {
            log.warn("Attempted to replace a mapping from CipherSolution with key " + key
                    + ", but this key does not exist.  Cannot continue.");

            return;
        }

        this.mappings.put(key, newPlaintext);
    }

    public CipherSolution clone() {
        CipherSolution copySolution = new CipherSolution(this.cipher, this.mappings.size());

        for (Map.Entry<String, String> entry : this.mappings.entrySet()) {
            copySolution.putMapping(entry.getKey(), entry.getValue());
        }

        copySolution.logProbability = 0d;
        for (Double logProbability : this.logProbabilities) {
            copySolution.addLogProbability(logProbability.doubleValue());
        }

        // We need to set these values last to maintain whether evaluation is needed on the clone
        copySolution.setProbability(this.probability != null ? this.probability.doubleValue() : null);

        return copySolution;
    }

    public Double getScore() {
        // Scaling down the index of coincidence by its fifth root seems to be the right amount to penalize the sum of log probabilities by
        // This has not been determined empirically but has worked well through experimentation
        return getLogProbability() * Math.pow(indexOfCoincidence, FIFTH_ROOT);
    }

    public Double evaluateKnownSolution() {
        if (!cipher.hasKnownSolution()) {
            throw new IllegalStateException("Cipher does not have a known solution.");
        }

        double total = 0.0;

        if (cipher.getKnownSolutionKey().size() != mappings.size()) {
            log.error("Current solution size of " + mappings.size()
                    + " does not match the known solution size of " + cipher.getKnownSolutionKey().size()
                    + ".  This will cause inaccurate fitness calculations.  Solution: " + this);
        }

        for (String key : cipher.getKnownSolutionKey().keySet()) {
            if (cipher.getKnownSolutionKey().get(key).equals(mappings.get(key))) {
                total++;
            }
        }

        double proximityToKnownSolution = (total / (double) mappings.size());

        if (log.isDebugEnabled()) {
            log.debug("Solution has a confidence level of: " + proximityToKnownSolution);
        }

        return proximityToKnownSolution;
    }

    /**
     * This is currently just used by unit tests.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cipher == null) ? 0 : cipher.hashCode());
        result = prime * result + ((logProbability == null) ? 0 : logProbability.hashCode());
        return result;
    }

    /**
     * This is currently just used by unit tests.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof CipherSolution)) {
            return false;
        }

        CipherSolution other = (CipherSolution) obj;
        if (cipher == null) {
            if (other.cipher != null) {
                return false;
            }
        } else if (!cipher.equals(other.cipher)) {
            return false;
        }

        if (mappings == null) {
            if (other.mappings != null) {
                return false;
            }
        } else if (!mappings.equals(other.mappings)) {
            return false;
        }

        return true;
    }

    public String asSingleLineString() {
        StringBuilder sb = new StringBuilder();

        if (null == this.getCipher()) {
            throw new IllegalStateException(
                    "Called getSolutionAsString(), but found a null Cipher.  Cannot create valid solution string unless the Cipher is properly set.");
        }

        for (Ciphertext ciphertext : this.getCipher().getCiphertextCharacters()) {
            sb.append(this.mappings.get(ciphertext.getValue()));
        }

        return sb.toString();
    }
}
