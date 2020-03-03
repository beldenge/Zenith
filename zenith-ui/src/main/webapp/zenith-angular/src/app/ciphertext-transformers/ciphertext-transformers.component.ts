import { Component, OnDestroy, OnInit } from '@angular/core';
import { SortablejsOptions} from "ngx-sortablejs";
import { animate, style, transition, trigger } from "@angular/animations";
import { ZenithTransformer } from "../models/ZenithTransformer";
import { CiphertextTransformerService } from "../ciphertext-transformer.service";
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";
import { Observable, Subscription } from "rxjs";
import { FormGroup } from "@angular/forms";
import { ConfigurationService } from "../configuration.service";
import { IntroductionService } from "../introduction.service";

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
export class CiphertextTransformersComponent implements OnInit, OnDestroy {
  showIntroCiphertextTransformersSubscription: Subscription;
  cipher: Cipher;
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

  onAppliedTransformersChange = (event: any) => {
    return this.transformerService.onAppliedTransformersChange(event);
  };

  // On adding of a new item to the Sortable list, the event fires before the formly form is initialized, so we cannot rely on validation alone
  onAppliedTransformersChangeNew = (event: any) => {
    this.onAppliedTransformersChange({ isNew: true });
    this.configurationService.updateAppliedCiphertextTransformers(this.appliedTransformers);
  };

  appliedTransformersOptions: SortablejsOptions = {
    group: 'clone-group',
    onAdd: this.onAppliedTransformersChangeNew,
    onRemove: this.onAppliedTransformersChange,
    onMove: this.onAppliedTransformersChange
  };

  constructor(private transformerService: CiphertextTransformerService, private cipherService: CipherService, private configurationService: ConfigurationService, private introductionService: IntroductionService) {
    this.appliedTransformers$ = configurationService.getAppliedCiphertextTransformersAsObservable();
  }

  ngOnInit(): void {
    this.transformerService.getTransformers().subscribe(transformerResponse => {
      this.availableTransformers = transformerResponse.transformers.sort((t1, t2) => {
        return t1.order - t2.order;
      });
    });

    this.cipherService.getSelectedCipherAsObservable().subscribe(cipher => {
      this.cipher = cipher;
    });

    this.appliedTransformers$.subscribe(appliedTransformers => {
      this.appliedTransformers = appliedTransformers;
    });

    this.showIntroCiphertextTransformersSubscription = this.introductionService.getShowIntroCiphertextTransformersAsObservable().subscribe(showIntro => {
      if (showIntro) {
        setTimeout(() =>{
        this.introductionService.startIntroCiphertextTransformers();
        this.introductionService.updateShowIntroCiphertextTransformers(false);
        }, 500);
      }
    });
  }

  ngOnDestroy() {
    this.showIntroCiphertextTransformersSubscription.unsubscribe();
  }

  cloneTransformer = (item) => {
    let clone = {
      name: item.name,
      displayName: item.displayName,
      form: JSON.parse(JSON.stringify(item.form))
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
