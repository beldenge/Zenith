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

package com.ciphertool.zenith.neural.predict;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

public class CostFunctions {
    public static void costFunctionRegression(INDArray expectedOutputs, INDArray actualOutputs) {
        actualOutputs.rsubi(expectedOutputs);
        Transforms.pow(actualOutputs, 2, false);
        actualOutputs.divi(2.0f);
    }

    public static void costFunctionClassification(INDArray expectedOutputs, INDArray actualOutputs) {
        Transforms.log(actualOutputs, false);
        actualOutputs.muli(expectedOutputs).negi();
    }

    public static void derivativeOfCostFunctionRegression(INDArray expectedOutputs, INDArray actualOutputs) {
        actualOutputs.rsubi(expectedOutputs);
        actualOutputs.negi();
    }

    public static void derivativeOfCostFunctionClassification(INDArray expectedOutputs, INDArray actualOutputs) {
        actualOutputs.subi(expectedOutputs);
    }
}
