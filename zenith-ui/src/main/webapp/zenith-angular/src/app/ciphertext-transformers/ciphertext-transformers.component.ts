import { Component, OnInit } from '@angular/core';
import { SortablejsOptions} from "ngx-sortablejs";
import { animate, style, transition, trigger } from "@angular/animations";
import { ZenithTransformer } from "../models/ZenithTransformer";
import { CiphertextTransformerService } from "../ciphertext-transformer.service";
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";
import { Observable } from "rxjs";
import { CiphertextTransformationRequest } from "../models/CiphertextTransformationRequest";
import { FormGroup } from "@angular/forms";
import { ConfigurationService } from "../configuration.service";

@Component({
  selector: 'app-ciphertext-transformers',
  templateUrl: './ciphertext-transformers.component.html',
  styleUrls: ['./ciphertext-transformers.component.css'],
  animations: [
    // the fade-in/fade-out animation.
    trigger('simpleFadeAnimation', [
      transition(':leave',
        animate(300, style({ opacity: 0 })))
    ])
  ]
})
export class CiphertextTransformersComponent implements OnInit {
  cipher: Cipher;
  cipher$: Observable<Cipher>;
  public hoverClasses: string[] = [];

  availableTransformers: ZenithTransformer[] = [];

  availableTransformersOptions: SortablejsOptions = {
    group: {
      name: 'clone-group',
      pull: 'clone',
      put: false
    },
    sort: false
  };

  appliedTransformers: ZenithTransformer[] = [];

  appliedTransformers$: Observable<ZenithTransformer[]>;

  // On adding of a new item to the Sortable list, the event fires before the formly form is initialized, so we cannot rely on validation alone
  onAppliedTransformersChangeNew = (event: any) => {
    this.onAppliedTransformersChange({ isNew: true });
  };

  onAppliedTransformersChange = (event: any) => {
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

  appliedTransformersOptions: SortablejsOptions = {
    group: 'clone-group',
    onAdd: this.onAppliedTransformersChangeNew,
    onRemove: this.onAppliedTransformersChange,
    onMove: this.onAppliedTransformersChange
  };

  constructor(private transformerService: CiphertextTransformerService, private cipherService: CipherService, private configurationService: ConfigurationService) {
    this.cipher$ = cipherService.getSelectedCipherAsObservable();
    this.appliedTransformers$ = configurationService.getAppliedCiphertextTransformersAsObservable();
  }

  ngOnInit(): void {
    this.transformerService.getTransformers().subscribe(transformerResponse => {
      this.availableTransformers = transformerResponse.transformers.sort((t1, t2) => {
        return t1.order - t2.order;
      });
    });

    this.cipher$.subscribe(cipher => {
      this.cipher = cipher;
    });

    this.appliedTransformers$.subscribe(appliedTransformers => {
      this.appliedTransformers = appliedTransformers;
      this.onAppliedTransformersChange({ skipUpdate: true });
    });
  }

  cloneTransformer = (item) => {
    let clone = {
      name: item.name,
      displayName: item.displayName,
      form: item.form,
      model: item.model
    };

    if (clone.form) {
      clone.form.form = new FormGroup({});
    }

    return clone;
  };

  removeTransformer(transformerIndex: number): void {
    this.hoverClasses = [];

    if (transformerIndex >= 0 && transformerIndex < this.appliedTransformers.length) {
      this.appliedTransformers.splice(transformerIndex, 1);
    }

    this.onAppliedTransformersChange(null);
  }
}
