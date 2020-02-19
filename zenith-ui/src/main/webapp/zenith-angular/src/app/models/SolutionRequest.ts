import { SolutionRequestTransformer } from "./SolutionRequestTransformer";
import { SimulatedAnnealingConfiguration } from "./SimulatedAnnealingConfiguration";
import { GeneticAlgorithmConfiguration } from "./GeneticAlgorithmConfiguration";

export class SolutionRequest {
  rows: number;
  columns: number;
  ciphertext: string;
  epochs: number;
  knownSolutionCorrectnessThreshold: number;
  plaintextTransformers: SolutionRequestTransformer[] = [];
  simulatedAnnealingConfiguration: SimulatedAnnealingConfiguration;
  geneticAlgorithmConfiguration: GeneticAlgorithmConfiguration;

  constructor(rows: number, columns: number, ciphertext: string, epochs: number) {
    this.rows = rows;
    this.columns = columns;
    this.ciphertext = ciphertext;
    this.epochs = epochs;
  }
}
