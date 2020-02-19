export class GeneticAlgorithmConfiguration {
  populationSize: number;
  numberOfGenerations: number;
  elitism: number;
  populationName: string;
  latticeRows: number;
  latticeColumns: number;
  latticeWrapAround: boolean;
  latticeRadius: number;
  breederName: string;
  crossoverAlgorithmName: string;
  mutationAlgorithmName: string;
  mutationRate: number;
  maxMutationsPerIndividual: number;
  selectorName: string;
  tournamentSelectorAccuracy: number;
  tournamentSize: number;
}
