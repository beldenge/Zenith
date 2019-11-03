export class SolutionRequest {
  rows: number;
  columns: number;
  ciphertext: string;

  constructor(rows: number, columns: number, ciphertext: string) {
    this.rows = rows;
    this.columns = columns;
    this.ciphertext = ciphertext;
  }
}
