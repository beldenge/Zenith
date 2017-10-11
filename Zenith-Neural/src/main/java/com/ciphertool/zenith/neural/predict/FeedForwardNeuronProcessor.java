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
import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.Neuron;
import com.ciphertool.zenith.neural.model.ProblemType;
import com.ciphertool.zenith.neural.model.Synapse;

@Component
public class FeedForwardNeuronProcessor {
	@Value("${network.bias.weight}")
	private BigDecimal					biasWeight;

	@Value("${problem.type}")
	private ProblemType					problemType;

	@Autowired
	private NeuralNetwork				network;

	@Autowired
	private HiddenActivationFunction	hiddenActivationFunction;

	@Async
	public Future<Void> processNeuron(int j, Layer toLayer, Layer fromLayer) {
		Neuron nextOutputNeuron = toLayer.getNeurons()[j];

		if (nextOutputNeuron.isBias()) {
			nextOutputNeuron.setActivationValue(biasWeight);

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

		if (problemType == ProblemType.REGRESSION || toLayer != network.getOutputLayer()) {
			nextOutputNeuron.setActivationValue(hiddenActivationFunction.transformInputSignal(sum));
		}

		return new AsyncResult<>(null);
	}
}
