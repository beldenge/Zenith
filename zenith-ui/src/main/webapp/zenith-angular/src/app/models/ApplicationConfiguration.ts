import { ZenithTransformer } from "./ZenithTransformer";
import { SelectOption } from "./SelectOption";
import { SimulatedAnnealingConfiguration } from "./SimulatedAnnealingConfiguration";
import { GeneticAlgorithmConfiguration } from "./GeneticAlgorithmConfiguration";

export class ApplicationConfiguration {
  epochs: number;
  appliedCiphertextTransformers: ZenithTransformer[];
  appliedPlaintextTransformers: ZenithTransformer[];
  selectedOptimizer: SelectOption;
  simulatedAnnealingConfiguration: SimulatedAnnealingConfiguration;
  geneticAlgorithmConfiguration: GeneticAlgorithmConfiguration;
}
