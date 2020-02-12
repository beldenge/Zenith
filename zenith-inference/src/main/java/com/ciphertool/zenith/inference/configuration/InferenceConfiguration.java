/**
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

package com.ciphertool.zenith.inference.configuration;

import com.ciphertool.zenith.inference.dao.CipherDao;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.transformer.ciphertext.CiphertextTransformationManager;
import com.ciphertool.zenith.inference.transformer.ciphertext.CiphertextTransformationStep;
import com.ciphertool.zenith.inference.transformer.plaintext.*;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.validation.constraints.Min;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@ComponentScan(basePackages = {
        "com.ciphertool.zenith.genetic.algorithms.crossover",
        "com.ciphertool.zenith.genetic.algorithms.mutation",
        "com.ciphertool.zenith.genetic.algorithms",
        "com.ciphertool.zenith.genetic.algorithms.selection",
        "com.ciphertool.zenith.genetic.population",
        "com.ciphertool.zenith.model.dao",
        "com.ciphertool.zenith.model.archive"
})
public class InferenceConfiguration {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${task-executor.pool-size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int corePoolSize;

    @Value("${task-executor.pool-size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int maxPoolSize;

    @Value("${task-executor.queue-capacity}")
    private int queueCapacity;

    @Value("${cipher.name}")
    private String cipherName;

    @Value("${markov.letter.order}")
    private int markovOrder;

    @Min(1)
    @Value("${language-model.max-ngrams-to-keep}")
    private int maxNGramsToKeep;

    @Value("${decipherment.evaluator.plaintext}")
    private String plaintextEvaluatorName;

    @Value("${decipherment.transformers.ciphertext}")
    private List<String> cipherTransformersToUse;

    @Value("${four-square-transformer.key.top-left}")
    protected String fourSquareKeyTopLeft;

    @Value("${four-square-transformer.key.top-right}")
    protected String fourSquareKeyTopRight;

    @Value("${four-square-transformer.key.bottom-left}")
    protected String fourSquareKeyBottomLeft;

    @Value("${four-square-transformer.key.bottom-right}")
    protected String fourSquareKeyBottomRight;

    @Value("${one-time-pad-transformer.key}")
    protected String oneTimePadKey;

    @Value("${vigenere-transformer.key}")
    protected String vigenereKey;

    @Value("${decipherment.transformers.plaintext}")
    protected List<String> plaintextTransformersToUse;

    @Bean
    public Cipher cipher(CipherDao cipherDao, CiphertextTransformationManager ciphertextTransformationManager) {
        Cipher cipher = cipherDao.findByCipherName(cipherName);

        List<CiphertextTransformationStep> transformationSteps = new ArrayList<>(cipherTransformersToUse.size());

        for (String transformerName : cipherTransformersToUse) {
            String transformerNameBeforeParenthesis = transformerName.contains("(") ? transformerName.substring(0, transformerName.indexOf('(')) : transformerName;

            if (transformerName.contains("(") && transformerName.endsWith(")")) {
                String argument = transformerName.substring(transformerName.indexOf('(') + 1, transformerName.length() - 1);

                transformationSteps.add(new CiphertextTransformationStep(transformerNameBeforeParenthesis, argument));
            } else {
                transformationSteps.add(new CiphertextTransformationStep(transformerNameBeforeParenthesis, null));
            }
        }

        return ciphertextTransformationManager.transform(cipher, transformationSteps);
    }

    @Bean
    public ArrayMarkovModel letterMarkovModel(LetterNGramDao letterNGramDao) {
        long startFindAll = System.currentTimeMillis();
        log.info("Beginning retrieval of all n-grams.");

        /*
         * Begin setting up letter n-gram model
         */
        List<TreeNGram> nGramNodes = letterNGramDao.findAll();

        log.info("Finished retrieving {} n-grams in {}ms.", nGramNodes.size(), (System.currentTimeMillis() - startFindAll));

        ArrayMarkovModel letterMarkovModel = new ArrayMarkovModel(markovOrder);

        long startAdding = System.currentTimeMillis();
        log.info("Adding nodes to the model.");

        nGramNodes.stream()
                .filter(node -> node.getOrder() == 1)
                .forEach(letterMarkovModel::addNode);

        nGramNodes.stream()
                .filter(node -> node.getOrder() == markovOrder)
                .sorted(Comparator.comparing(TreeNGram::getCount).reversed())
                .limit(maxNGramsToKeep)
                .forEach(letterMarkovModel::addNode);

        log.info("Finished adding {} nodes to the letter n-gram model in {}ms.", letterMarkovModel.getMapSize(), (System.currentTimeMillis() - startAdding));

        float unknownLetterNGramProbability = 1f / (float) letterMarkovModel.getTotalNGramCount();
        letterMarkovModel.setUnknownLetterNGramProbability(unknownLetterNGramProbability);
        letterMarkovModel.setUnknownLetterNGramLogProbability((float) Math.log(unknownLetterNGramProbability));

        return letterMarkovModel;
    }

    @Bean
    public PlaintextEvaluator plaintextEvaluator(List<PlaintextEvaluator> plaintextEvaluators) {
        for (PlaintextEvaluator evaluator : plaintextEvaluators) {
            if (evaluator.getClass().getSimpleName().equals(plaintextEvaluatorName)) {
                return evaluator;
            }
        }

        List<String> existentPlaintextEvaluators = plaintextEvaluators.stream()
                .map(evaluator -> evaluator.getClass().getSimpleName())
                .collect(Collectors.toList());

        log.error("The PlaintextEvaluator with name {} does not exist.  Please use a name from the following: {}", plaintextEvaluatorName, existentPlaintextEvaluators);
        throw new IllegalArgumentException("The PlaintextEvaluator with name " + plaintextEvaluatorName + " does not exist.");
    }

    @Bean
    public List<PlaintextTransformationStep> plaintextTransformationSteps() {
        List<PlaintextTransformationStep> plaintextTransformationSteps = new ArrayList<>();

        for (String toUse : plaintextTransformersToUse) {
            Map<String, Object> data = new HashMap<>();

            if (toUse.contains("OneTimePad")) {
                data.put(AbstractOneTimePadPlaintextTransformer.KEY, oneTimePadKey);
            } else if (toUse.contains("FourSquare")) {
                data.put(AbstractFourSquarePlaintextTransformer.KEY_TOP_LEFT, fourSquareKeyTopLeft);
                data.put(AbstractFourSquarePlaintextTransformer.KEY_TOP_RIGHT, fourSquareKeyTopRight);
                data.put(AbstractFourSquarePlaintextTransformer.KEY_BOTTOM_LEFT, fourSquareKeyBottomLeft);
                data.put(AbstractFourSquarePlaintextTransformer.KEY_BOTTOM_RIGHT, fourSquareKeyBottomRight);
            } else if (toUse.contains("Vigenere")) {
                data.put(AbstractVigenerePlaintextTransformer.VIGENERE_SQUARE, null);
                data.put(AbstractVigenerePlaintextTransformer.KEY, vigenereKey);
            }

            plaintextTransformationSteps.add(new PlaintextTransformationStep(toUse, data));
        }

        return plaintextTransformationSteps;
    }

    @Bean
    @Primary
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.setKeepAliveSeconds(5);
        taskExecutor.setAllowCoreThreadTimeOut(true);

        return taskExecutor;
    }
}
