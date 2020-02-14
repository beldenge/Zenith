export class CiphertextTransformationRequestStep {
  transformerName: string;
  data: any;

  constructor(transformerName: string, data: any) {
    this.transformerName = transformerName;
    this.data = data;
  }
}
