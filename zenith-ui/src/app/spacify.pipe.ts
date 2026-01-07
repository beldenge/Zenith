import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'spacify',
    standalone: false
})
export class SpacifyPipe implements PipeTransform {
  transform(value: string): string[] {
    return value.split(/\s+/);
  }
}
