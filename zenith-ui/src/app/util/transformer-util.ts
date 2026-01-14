/*
 * Copyright 2017-2026 George Belden
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

import { FormComponent } from "../models/FormComponent";

export class TransformerUtil {
  static transformersAreEqual(first: FormComponent[], second: FormComponent[]) {
    if (!first && !second) {
      return true;
    }

    if ((!first && second) || (first && !second)) {
      return false;
    }

    if (first.length !== second.length) {
      return false;
    }

    for (let i = 0; i < first.length; i++) {
      if (first[i].name !== second[i].name) {
        return false;
      }
    }

    return true;
  }
}
