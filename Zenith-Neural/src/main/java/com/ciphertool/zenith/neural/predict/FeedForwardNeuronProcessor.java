package com.ciphertool.zenith.neural.predict;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.activation.HiddenActivationFunction;
import com.ciphertool.zenith.neural.activation.OutputActivationFunction;
import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.Neuron;
import com.ciphertool.zenith.neural.model.ProblemType;
import com.ciphertool.zenith.neural.model.Synapse;

@Component
public class FeedForwardNeuronProcessor {
	@Value("${network.bias.weight}")
	private BigDecimal					biasWeight;

	@Autowired
	private NeuralNetwork				network;

	@Autowired
	private HiddenActivationFunction	hiddenActivationFunction;

	@Autowired
	private OutputActivationFunction	outputActivationFunction;

	@Async
	public Future<Void> processNeuron(int j, Layer toLayer, Layer fromLayer) {
		Neuron nextOutputNeuron = toLayer.getNeurons()[j];

		if (nextOutputNeuron.isBias()) {
			// There is no synapse going into a bias neuron
			return new AsyncResult<>(null);
		}

		BigDecimal sum = BigDecimal.ZERO;

		for (int k = 0; k < fromLayer.getNeurons().length; k++) {
			Neuron nextInputNeuron = fromLayer.getNeurons()[k];

			Synapse nextSynapse = nextInputNeuron.getOutgoingSynapses()[j];

			sum = sum.add(nextInputNeuron.getActivationValue().multiply(nextSynapse.getWeight(), MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP));
		}

		nextOutputNeuron.setOutputSum(sum);

		if (network.getProblemType() == ProblemType.REGRESSION || toLayer != network.getOutputLayer()) {
			nextOutputNeuron.setActivationValue(hiddenActivationFunction.transformInputSignal(sum));
		}

		return new AsyncResult<>(null);
	}

	@Async
	public Future<Void> processOutputNeuron(int i, BigDecimal[] allSums) {
		Neuron nextOutputNeuron = network.getOutputLayer().getNeurons()[i];

		nextOutputNeuron.setActivationValue(outputActivationFunction.transformInputSignal(nextOutputNeuron.getOutputSum(), allSums));

		return new AsyncResult<>(null);
	}
}
