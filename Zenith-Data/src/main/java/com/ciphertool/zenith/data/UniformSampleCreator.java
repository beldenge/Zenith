/**
 * Copyright 2017 George Belden
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

package com.ciphertool.zenith.data;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.model.ModelConstants;
import com.ciphertool.zenith.model.probability.LetterProbability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Validated
@ConfigurationProperties
public class UniformSampleCreator implements SampleCreator {
    private static Logger log	= LoggerFactory.getLogger(UniformSampleCreator.class);

    private static int COMPLETELY_RANDOM = 0;

    private static final int NUM_LETTERS = ModelConstants.LOWERCASE_LETTERS.size();

    private static final List<LetterProbability> RANDOM_LETTER_PROBABILITIES = new ArrayList<>(NUM_LETTERS);

    static {
        for (Character letter : ModelConstants.LOWERCASE_LETTERS) {
            RANDOM_LETTER_PROBABILITIES.add(new LetterProbability(letter, MathConstants.SINGLE_LETTER_RANDOM_PROBABILITY));
        }
    }

    @Min(1)
    @Value("${task.samplesToCreate:-1}")
    private Integer samplesToCreate;

    @Min(1)
    @Value("${training.sequenceLength}")
    private int sequenceLength;

    @Autowired
    private RecordWriter writer;

    @Override
    public int createSamples(int howMany){
        int i = 0;

        for (; i < (samplesToCreate >= 0 ? samplesToCreate : howMany); i ++) {
            generateRandomSample();
        }

        return i;
    }

    private void generateRandomSample() {
        StringBuffer sb = new StringBuffer();

        for (int j = 0; j < sequenceLength; j++) {
            char nextLetter = ModelConstants.LOWERCASE_LETTERS.get(ThreadLocalRandom.current().nextInt(NUM_LETTERS));

            sb.append(nextLetter);
        }

        if (log.isDebugEnabled()) {
            log.debug("Random sample: {}", sb.toString());
        }

        try {
            writer.write(COMPLETELY_RANDOM, sb.toString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
