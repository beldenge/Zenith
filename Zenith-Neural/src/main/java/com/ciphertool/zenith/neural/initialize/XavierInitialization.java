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

package com.ciphertool.zenith.neural.initialize;

import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.LayerType;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import org.nd4j.linalg.factory.Nd4j;

public class XavierInitialization implements Initialization {
    @Override
    public void initialize(NeuralNetwork network) {
        for (int i = 0; i < network.getLayers().length - 1; i++) {
            Layer layer = network.getLayers()[i];

            float inputCount = layer.getActivations().size(1);

            if(i < network.getLayers().length - 1 && LayerType.RECURRENT == network.getLayers()[i + 1].getType()) {
                inputCount += network.getLayers()[i + 1].getActivations().size(1);
            }

            layer.setOutgoingWeights(Nd4j.randn(layer.getOutgoingWeights().shape()));
            layer.getOutgoingWeights().divi(inputCount);

            if(i < network.getLayers().length - 1 && LayerType.RECURRENT == network.getLayers()[i + 1].getType()) {
                layer.setRecurrentOutgoingWeights(Nd4j.randn(layer.getRecurrentOutgoingWeights().shape()));
                layer.getRecurrentOutgoingWeights().divi(inputCount);
            }
        }
    }
}
