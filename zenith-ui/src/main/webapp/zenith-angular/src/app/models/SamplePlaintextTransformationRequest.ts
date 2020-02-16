import { SolutionRequestTransformer } from "./SolutionRequestTransformer";

export class SamplePlaintextTransformationRequest {
  plaintext: string;
  plaintextTransformers: SolutionRequestTransformer[];
}
