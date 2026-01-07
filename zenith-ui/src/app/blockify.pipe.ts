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

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'blockify',
    standalone: false
})
export class BlockifyPipe implements PipeTransform {
  transform(value: string[], columns: number): string {
    let block = '';

    if (value?.length) {
      let first = true;

      for (let i = 0; i < value.length; i++) {
        if (!first) {
          block += ' ';
        }

        first = false;

        block += value[i];

        if (i !== value.length - 1 && (i + 1) % columns === 0) {
          block += '\n';
          first = true;
        }
      }
    }

    return block;
  }
}
