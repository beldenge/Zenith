import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { TransformerResponse } from "./models/TransformerResponse";
import { environment } from "../environments/environment";
import { CiphertextTransformationRequest } from "./models/CiphertextTransformationRequest";
import { CipherService } from "./cipher.service";
import { ConfigurationService } from "./configuration.service";
import { Cipher } from "./models/Cipher";
import { ZenithTransformer } from "./models/ZenithTransformer";

const ENDPOINT_URL = environment.apiUrlBase + '/transformers/ciphertext';

@Injectable({
  providedIn: 'root'
})
export class CiphertextTransformerService {
  cipher: Cipher;
  appliedTransformers: ZenithTransformer[] = [];

  constructor(private http: HttpClient, private cipherService: CipherService, private configurationService: ConfigurationService) {
    this.cipherService.getSelectedCipherAsObservable().subscribe(cipher => {
      this.cipher = cipher;
    });

    this.configurationService.getAppliedCiphertextTransformersAsObservable().subscribe(appliedTransformers => {
      this.appliedTransformers = appliedTransformers;
      this.onAppliedTransformersChange({ skipUpdate: true });
    });
  }

  getTransformers() {
    return this.http.get<TransformerResponse>(ENDPOINT_URL);
  }

  onAppliedTransformersChange = (event: any) => {
    if (!this.cipher) {
      return;
    }

    let transformationRequest: CiphertextTransformationRequest = {
      steps: []
    };

    let satisfied = true;

    this.appliedTransformers.forEach(transformer => {
      if (transformer.form && ((event && event.isNew) || !transformer.form.form.valid)) {
        satisfied = false;
        return;
      }

      transformationRequest.steps.push({
        transformerName: transformer.name,
        data: transformer.form ? transformer.form.model : null
      });
    });

    if (satisfied) {
      this.cipherService.transformCipher(this.cipher.name, transformationRequest).subscribe(cipherResponse => {
        this.cipherService.updateSelectedCipher(cipherResponse.ciphers[0]);
      });

      if (!event || !event.skipUpdate) {
        this.configurationService.updateAppliedCiphertextTransformers(this.appliedTransformers);
      }
    }

    return true;
  };
}
