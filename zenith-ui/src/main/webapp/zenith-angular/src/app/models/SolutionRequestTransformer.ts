export class SolutionRequestTransformer {
  transformerName : string;
  data: any;

  constructor(transformerName: string, data: any) {
    this.transformerName = transformerName;
    this.data = data;
  }
}
