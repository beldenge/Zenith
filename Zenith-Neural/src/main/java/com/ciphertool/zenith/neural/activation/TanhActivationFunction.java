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

package com.ciphertool.zenith.neural.activation;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

public class TanhActivationFunction implements ActivationFunction {
    @Override
    public void transformInputSignal(INDArray layer) {
        Transforms.tanh(layer, false);
    }

    @Override
    public void calculateDerivative(INDArray layer) {
        Transforms.tanh(layer, false);
        layer.muli(layer);
        layer.rsubi(1.0);
    }
}
