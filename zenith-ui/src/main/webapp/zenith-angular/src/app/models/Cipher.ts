export class Cipher {
  name: string;
  rows: number;
  columns: number;
  ciphertext: string;
  multiplicity: number;
  entropy: number;
  indexOfCoincidence: number;
  chiSquared: number;
  bigramRepeats: number;
  cycleScore: number;

  constructor(name: string, rows: number, columns: number, ciphertext: string) {
    this.name = name;
    this.rows = rows;
    this.columns = columns;
    this.ciphertext = ciphertext;
  }
}
