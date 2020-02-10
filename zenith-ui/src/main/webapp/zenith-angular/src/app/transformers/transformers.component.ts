import { Component, OnInit } from '@angular/core';
import { SortablejsOptions} from "ngx-sortablejs";
import { animate, style, transition, trigger } from "@angular/animations";
import { CiphertextTransformer } from "../models/CiphertextTransformer";
import { TransformerService } from "../transformer.service";
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";
import { Observable } from "rxjs";
import { TransformationRequest } from "../models/TransformationRequest";

@Component({
  selector: 'app-transformers',
  templateUrl: './transformers.component.html',
  styleUrls: ['./transformers.component.css'],
  animations: [
    // the fade-in/fade-out animation.
    trigger('simpleFadeAnimation', [
      transition(':leave',
        animate(300, style({ opacity: 0 })))
    ])
  ]
})
export class TransformersComponent implements OnInit {
  cipher: Cipher;
  cipher$: Observable<Cipher>;
  public hoverClasses: string[] = [];

  availableTransformers: CiphertextTransformer[] = [];

  availableTransformersOptions: SortablejsOptions = {
    group: {
      name: 'clone-group',
      pull: 'clone',
      put: false
    },
    sort: false
  };

  appliedTransformers: CiphertextTransformer[] = [];

  appliedTransformers$: Observable<CiphertextTransformer[]>;

  onAppliedTransformersChange = (event: any) => {
    let transformationRequest: TransformationRequest = {
      steps: []
    };

    let satisfied = true;

    this.appliedTransformers.forEach(transformer => {
      if (transformer.inputName && !transformer.inputValue) {
        satisfied = false;
        return;
      }

      transformationRequest.steps.push({
        transformerName: transformer.name,
        argument: transformer.inputValue
      });
    });

    if (satisfied) {
      this.cipherService.transformCipher(this.cipher.name, transformationRequest).subscribe(cipherResponse => {
        this.cipherService.updateSelectedCipher(cipherResponse.ciphers[0]);
      });

      this.transformerService.updateAppliedTransformers(this.appliedTransformers);
    }

    return true;
  };

  appliedTransformersOptions: SortablejsOptions = {
    group: 'clone-group',
    onAdd: this.onAppliedTransformersChange,
    onRemove: this.onAppliedTransformersChange,
    onMove: this.onAppliedTransformersChange
  };

  constructor(private transformerService: TransformerService, private cipherService: CipherService) {
    this.cipher$ = cipherService.getSelectedCipherAsObservable();
    this.appliedTransformers$ = transformerService.getAppliedTransformersAsObservable();
  }

  ngOnInit(): void {
    this.transformerService.getTransformers().subscribe(ciphertextTransformerResponse => {
      this.availableTransformers = ciphertextTransformerResponse.transformers;
    });

    this.cipher$.subscribe(cipher => {
      this.cipher = cipher;
    });

    this.appliedTransformers$.subscribe(appliedTransformers => {
      this.appliedTransformers = appliedTransformers;
    });
  }

  cloneTransformer = (item) => {
    return {
      name: item.name,
      displayName: item.displayName,
      inputType: item.inputType,
      inputName: item.inputName
    };
  };

  removeTransformer(transformerIndex: number): void {
    this.hoverClasses = [];

    if (transformerIndex >= 0 && transformerIndex < this.appliedTransformers.length) {
      this.appliedTransformers.splice(transformerIndex, 1);
    }

    this.onAppliedTransformersChange.call(null);
  }
}
