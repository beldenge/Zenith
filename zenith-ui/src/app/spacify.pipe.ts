import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'spacify',
    standalone: false
})
export class SpacifyPipe implements PipeTransform {
  transform(value: String): String {
    let spaced: String = '';

    if (value) {
      let first = true;

      for (let i = 0; i < value.length; i++) {
        if (!first) {
          spaced += ' ';
        }

        first = false;

        spaced += value.charAt(i);
      }
    }

    return spaced;
  }
}
