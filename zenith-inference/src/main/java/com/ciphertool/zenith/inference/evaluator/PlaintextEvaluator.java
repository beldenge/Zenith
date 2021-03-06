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

package com.ciphertool.zenith.inference.evaluator;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.evaluator.model.SolutionScore;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public interface PlaintextEvaluator {
   SolutionScore evaluate(Map<String, Object> precomputedData, Cipher cipher, CipherSolution solution, String solutionString, String ciphertextKey);

   Map<String, Object> getPrecomputedCounterweightData(Cipher cipher);

   PlaintextEvaluator getInstance(Map<String, Object> data);

   default String getDisplayName() {
      String displayName = getClass().getSimpleName()
              .replace(PlaintextEvaluator.class.getSimpleName(), "");

      return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(displayName), ' ');
   }

   FormlyForm getForm();

   int getOrder();

   String getHelpText();
}
