export class Cipher {
  name: string;
  rows: number;
  columns: number;
  ciphertext: string;

  constructor(name: string, rows: number, columns: number, ciphertext: string) {
    this.name = name;
    this.rows = rows;
    this.columns = columns;
    this.ciphertext = ciphertext;
  }
}
