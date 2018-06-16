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

package com.ciphertool.zenith.neural.model;

import com.ciphertool.zenith.neural.activation.ActivationFunctionType;

public class LayerConfiguration {
    private LayerType layerType;
    private int numberOfNeurons;
    private ActivationFunctionType activationType;
    private int numberOfRecurrentNeurons;

    public LayerType getLayerType() {
        return layerType;
    }

    public void setLayerType(LayerType layerType) {
        this.layerType = layerType;
    }

    public int getNumberOfNeurons() {
        return numberOfNeurons;
    }

    public void setNumberOfNeurons(int numberOfNeurons) {
        this.numberOfNeurons = numberOfNeurons;
    }

    public ActivationFunctionType getActivationType() {
        return activationType;
    }

    public void setActivationType(ActivationFunctionType activationType) {
        this.activationType = activationType;
    }

    public int getNumberOfRecurrentNeurons() {
        return numberOfRecurrentNeurons;
    }

    public void setNumberOfRecurrentNeurons(int numberOfRecurrentNeurons) {
        this.numberOfRecurrentNeurons = numberOfRecurrentNeurons;
    }
}
