import { TransformationRequestStep } from "./TransformationRequestStep";

export class TransformationRequest {
  cipherName: string;
  steps: TransformationRequestStep[];
}
