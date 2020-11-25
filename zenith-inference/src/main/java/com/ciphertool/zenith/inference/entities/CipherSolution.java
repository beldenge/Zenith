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

package com.ciphertool.zenith.inference.entities;

import com.ciphertool.zenith.genetic.fitness.Fitness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CipherSolution implements Comparable<CipherSolution>, Cloneable {
    private static Logger log = LoggerFactory.getLogger(CipherSolution.class);

    private Cipher cipher;

    private float probability = 0f;
    private float logProbability = 0f;

    private Map<String, Character> mappings;

    private float[] logProbabilities;

    private Fitness[] scores;

    public CipherSolution(Cipher cipher, int numCiphertextKeys) {
        if (cipher == null) {
            throw new IllegalArgumentException("Cannot construct CipherSolution with null cipher.");
        }

        this.cipher = cipher;

        mappings = new HashMap<>(numCiphertextKeys);
        logProbabilities = new float[cipher.getCiphertextCharacters().size()];
        Arrays.fill(this.logProbabilities, 0f);
    }

    public Cipher getCipher() {
        return this.cipher;
    }

    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    public float getProbability() {
        return probability;
    }

    public void setProbability(float score) {
        this.probability = score;
    }

    public float getLogProbability() {
        if (logProbability == 0f) {
            for (int i = 0; i < logProbabilities.length; i ++) {
                logProbability += logProbabilities[i];
            }
        }

        return logProbability;
    }

    public Map<String, Character> getMappings() {
        return Collections.unmodifiableMap(mappings);
    }

    public void putMapping(String key, Character plaintext) {
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

    public float[] getLogProbabilities() {
        return logProbabilities;
    }

    public float getLogProbability(int index) {
        return logProbabilities[index];
    }

    public void clearLogProbabilities() {
        Arrays.fill(this.logProbabilities, 0f);
        this.logProbability = 0f;
    }

    public void addLogProbability(int i, float logProbability) {
        this.logProbabilities[i] = logProbability;
        this.logProbability += logProbability;
    }

    public void replaceLogProbability(int i, float newLogProbability) {
        float oldLogProbability = this.logProbabilities[i];
        this.logProbabilities[i] = newLogProbability;

        this.logProbability -= oldLogProbability;
        this.logProbability += newLogProbability;
    }

    /*
     * This does the same thing as putMapping(), and exists solely for semantic consistency.
     */
    public void replaceMapping(String key, Character newPlaintext) {
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

    @Override
    public CipherSolution clone() {
        CipherSolution copySolution = new CipherSolution(this.cipher, this.mappings.size());

        for (Map.Entry<String, Character> entry : this.mappings.entrySet()) {
            copySolution.putMapping(entry.getKey(), entry.getValue());
        }

        copySolution.logProbability = 0f;
        for (int i = 0; i < this.logProbabilities.length; i ++) {
            copySolution.addLogProbability(i, this.logProbabilities[i]);
        }

        // We need to set these values last to maintain whether evaluation is needed on the clone
        copySolution.setProbability(this.probability);

        Fitness[] newScores = new Fitness[this.scores.length];
        for (int i = 0; i < this.scores.length; i ++) {
            newScores[i] = this.scores[i].clone();
        }
        copySolution.setScores(newScores);

        return copySolution;
    }

    public Fitness[] getScores() {
        return scores;
    }

    public void setScores(Fitness[] scores) {
        this.scores = scores;
    }

    public float evaluateKnownSolution() {
        if (!cipher.hasKnownSolution()) {
            throw new IllegalStateException("Cipher does not have a known solution.");
        }

        float total = 0f;

        if (cipher.getKnownSolutionKey().size() != mappings.size()) {
            log.error("Current solution size of " + mappings.size()
                    + " does not match the known solution size of " + cipher.getKnownSolutionKey().size()
                    + ".  This will cause inaccurate fitness calculations.  Solution: " + this);
        }

        for (String key : cipher.getKnownSolutionKey().keySet()) {
            if (cipher.getKnownSolutionKey().get(key).equals(String.valueOf(mappings.get(key)))) {
                total++;
            }
        }

        float proximityToKnownSolution = (total / (float) mappings.size());

        if (log.isDebugEnabled()) {
            log.debug("Solution has a confidence level of: " + proximityToKnownSolution);
        }

        return proximityToKnownSolution;
    }

    public String asSingleLineString() {
        StringBuilder sb = new StringBuilder();

        if (null == this.getCipher()) {
            throw new IllegalStateException(
                    "Called asSingleLineString(), but found a null Cipher.  Cannot create valid solution string unless the Cipher is properly set.");
        }

        for (Ciphertext ciphertext : this.getCipher().getCiphertextCharacters()) {
            sb.append(this.mappings.get(ciphertext.getValue()));
        }

        return sb.toString();
    }

    @Override
    public int compareTo(CipherSolution other) {
        if (scores.length == 1) {
            return scores[0].compareTo(other.scores[0]);
        }

        int dominating = 0;
        int equivalent = 0;

        // Calculate domination per the pareto front
        for (int i = 0; i < scores.length; i ++) {
            if (scores[i].compareTo(other.scores[i]) > 0) {
                dominating ++;
            } else if (scores[i].compareTo(other.scores[i]) == 0) {
                equivalent ++;
            }
        }

        if (dominating == scores.length) {
            return 1;
        } else if ((dominating + equivalent) > 0) {
            return 0;
        }

        return -1;
    }
}
