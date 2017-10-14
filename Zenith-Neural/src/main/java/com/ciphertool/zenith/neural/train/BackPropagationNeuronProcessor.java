package com.ciphertool.zenith.neural.train;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.activation.HiddenActivationFunction;
import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.Neuron;
import com.ciphertool.zenith.neural.model.ProblemType;
import com.ciphertool.zenith.neural.model.Synapse;

@Component
public class BackPropagationNeuronProcessor {
	private boolean						factorLearningRate;

	@Value("${network.learningRate}")
	private BigDecimal					learningRate;

	@Autowired
	private NeuralNetwork				network;

	@Autowired
	private HiddenActivationFunction	hiddenActivationFunction;

	@PostConstruct
	public void init() {
		if (learningRate != null && learningRate.compareTo(BigDecimal.ONE) != 0) {
			factorLearningRate = true;
		}
	}

	@Async
	public Future<Void> processOutputNeuron(int i, Layer fromLayer, Layer outputLayer, BigDecimal[] errorDerivatives, BigDecimal[] activationDerivatives, BigDecimal[] expectedOutputs, BigDecimal[] allSums) {
		Neuron nextOutputNeuron = outputLayer.getNeurons()[i];

		BigDecimal errorDerivative;

		if (network.getProblemType() == ProblemType.REGRESSION) {
			errorDerivative = derivativeOfCostFunctionRegression(expectedOutputs[i], nextOutputNeuron.getActivationValue());
		} else {
			errorDerivative = derivativeOfCostFunctionClassification(expectedOutputs[i], nextOutputNeuron.getActivationValue());
		}

		errorDerivatives[i] = errorDerivative;

		BigDecimal activationDerivative;

		if (network.getProblemType() == ProblemType.CLASSIFICATION) {
			activationDerivative = BigDecimal.ONE;
		} else {
			activationDerivative = hiddenActivationFunction.calculateDerivative(nextOutputNeuron.getOutputSum());
		}

		activationDerivatives[i] = activationDerivative;

		for (int j = 0; j < fromLayer.getNeurons().length; j++) {
			Neuron nextInputNeuron = fromLayer.getNeurons()[j];

			BigDecimal outputSumDerivative = nextInputNeuron.getActivationValue();

			BigDecimal delta = errorDerivative.multiply(activationDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP).multiply(outputSumDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

			if (factorLearningRate) {
				delta = delta.multiply(learningRate, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
			}

			Synapse nextSynapse = nextInputNeuron.getOutgoingSynapses()[i];
			nextSynapse.setOldWeight(nextSynapse.getWeight());
			nextSynapse.setWeight(nextSynapse.getWeight().subtract(delta));
		}

		return new AsyncResult<>(null);
	}

	@Async
	public Future<Void> processHiddenNeuron(int j, Layer fromLayer, Layer toLayer, BigDecimal[] errorDerivatives, BigDecimal[] activationDerivatives, BigDecimal[] oldErrorDerivatives, BigDecimal[] oldActivationDerivatives) {
		Neuron nextToNeuron = toLayer.getNeurons()[j];

		if (nextToNeuron.isBias()) {
			// There are no synapses going into the bias neuron
			return new AsyncResult<>(null);
		}

		BigDecimal activationDerivative = hiddenActivationFunction.calculateDerivative(nextToNeuron.getOutputSum());
		activationDerivatives[j] = activationDerivative;

		BigDecimal errorDerivative = BigDecimal.ZERO;

		for (int l = 0; l < nextToNeuron.getOutgoingSynapses().length; l++) {
			Synapse nextSynapse = nextToNeuron.getOutgoingSynapses()[l];

			BigDecimal partialErrorDerivative = oldErrorDerivatives[l].multiply(oldActivationDerivatives[l], MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

			BigDecimal weightDerivative = nextSynapse.getOldWeight();

			errorDerivative = errorDerivative.add(partialErrorDerivative.multiply(weightDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP));
		}

		errorDerivatives[j] = errorDerivative;

		BigDecimal errorTimesActivation = errorDerivative.multiply(activationDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

		for (int k = 0; k < fromLayer.getNeurons().length; k++) {
			Neuron nextFromNeuron = fromLayer.getNeurons()[k];

			BigDecimal outputSumDerivative = nextFromNeuron.getActivationValue();

			BigDecimal delta = errorTimesActivation.multiply(outputSumDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

			if (factorLearningRate) {
				delta = delta.multiply(learningRate, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
			}

			Synapse nextSynapse = nextFromNeuron.getOutgoingSynapses()[j];
			nextSynapse.setOldWeight(nextSynapse.getWeight());
			nextSynapse.setWeight(nextSynapse.getWeight().subtract(delta));
		}

		return new AsyncResult<>(null);
	}

	protected static BigDecimal derivativeOfCostFunctionRegression(BigDecimal expected, BigDecimal actual) {
		return expected.subtract(actual).negate();
	}

	protected static BigDecimal derivativeOfCostFunctionClassification(BigDecimal expected, BigDecimal actual) {
		return actual.subtract(expected);
	}
}
