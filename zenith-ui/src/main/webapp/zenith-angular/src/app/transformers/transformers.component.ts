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

  availableTransformerList: CiphertextTransformer[] = [];

  appliedTransformerList: CiphertextTransformer[] = [];

  availableTransformerListOptions: SortablejsOptions = {
    group: {
      name: 'clone-group',
      pull: 'clone',
      put: false
    },
    sort: false
  };

  onAppliedTransformersChange = (event: any) => {
    let transformationRequest: TransformationRequest = {
      cipherName: this.cipher.name,
      transformers: []
    };

    this.appliedTransformerList.forEach(transformer => {
      transformationRequest.transformers.push(transformer.name);
    });

    this.cipherService.transformCipher(transformationRequest).subscribe(cipherResponse => {
      this.cipherService.updateSelectedCipher(cipherResponse.ciphers[0]);
    });

    return true;
  };

  appliedTransformerListOptions: SortablejsOptions = {
    group: 'clone-group',
    onAdd: this.onAppliedTransformersChange,
    onRemove: this.onAppliedTransformersChange,
    onMove: this.onAppliedTransformersChange
  };

  constructor(private transformerService: TransformerService, private cipherService: CipherService) {
    this.cipher$ = cipherService.getSelectedCipherAsObservable();
  }

  ngOnInit(): void {
    this.transformerService.getTransformers().subscribe(ciphertextTransformerResponse => {
      this.availableTransformerList = ciphertextTransformerResponse.transformers;
    });

    this.cipher$.subscribe(cipher => {
      this.cipher = cipher;
    });
  }

  cloneTransformer = (item) => {
    return {
      name: item.name,
      displayName: item.displayName,
      inputType: item.inputType
    };
  };

  removeTransformer(transformerIndex: number): void {
    this.hoverClasses = [];

    if (transformerIndex >= 0 && transformerIndex < this.appliedTransformerList.length) {
      this.appliedTransformerList.splice(transformerIndex, 1);
    }

    this.onAppliedTransformersChange.call(null);
  }
}
