import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'blockify'
})
export class BlockifyPipe implements PipeTransform {
  transform(value: String, columns: number): String {
    let block: String = '';

    for (let i = 0; i < value.length; i ++) {
      block += value[i];

      if ((i + 1) % columns == 0) {
        block += '\n';
      }
    }

    return block;
  }
}
