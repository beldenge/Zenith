export class SolutionRequest {
  rows: number;
  columns: number;
  ciphertext: string;
  epochs: number;

  constructor(rows: number, columns: number, ciphertext: string, epochs: number) {
    this.rows = rows;
    this.columns = columns;
    this.ciphertext = ciphertext;
    this.epochs = epochs;
  }
}
