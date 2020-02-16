import { Component, OnInit } from '@angular/core';
import { Observable } from "rxjs";
import { SortablejsOptions } from "ngx-sortablejs";
import { animate, style, transition, trigger } from "@angular/animations";
import { PlaintextTransformerService } from "../plaintext-transformer.service";
import { ZenithTransformer } from "../models/ZenithTransformer";
import { FormGroup } from "@angular/forms";
import { SamplePlaintextTransformationRequest } from "../models/SamplePlaintextTransformationRequest";

@Component({
  selector: 'app-plaintext-transformers',
  templateUrl: './plaintext-transformers.component.html',
  styleUrls: ['./plaintext-transformers.component.css'],
  animations: [
    // the fade-in/fade-out animation.
    trigger('simpleFadeAnimation', [
      transition(':leave',
        animate(300, style({ opacity: 0 })))
    ])
  ]
})
export class PlaintextTransformersComponent implements OnInit {
  public hoverClasses: string[] = [];
  originalSample: string = 'thetomatoisaplantinthenightshadefamilyxxxx';
  transformedSample: string = this.originalSample;

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
    let transformationRequest: SamplePlaintextTransformationRequest = {
      plaintext: this.originalSample,
      plaintextTransformers: []
    };

    let satisfied = true;

    this.appliedTransformers.forEach(transformer => {
      if (transformer.form && ((event && event.isNew) || !transformer.form.form.valid)) {
        satisfied = false;
        return;
      }

      transformationRequest.plaintextTransformers.push({
        transformerName: transformer.name,
        data: transformer.form ? transformer.form.model : null
      });
    });

    if (satisfied) {
      this.transformerService.transformSample(transformationRequest).subscribe(response => {
        this.transformedSample = response.plaintext;
      });

      this.transformerService.updateAppliedTransformers(this.appliedTransformers);
    }

    return true;
  };

  appliedTransformersOptions: SortablejsOptions = {
    group: 'clone-group',
    onAdd: this.onAppliedTransformersChangeNew,
    onRemove: this.onAppliedTransformersChange,
    onMove: this.onAppliedTransformersChange
  };

  constructor(private transformerService: PlaintextTransformerService) {
    this.appliedTransformers$ = transformerService.getAppliedTransformersAsObservable();
  }

  ngOnInit(): void {
    this.transformerService.getTransformers().subscribe(transformerResponse => {
      this.availableTransformers = transformerResponse.transformers.sort((t1, t2) => {
        return t1.order - t2.order;
      });
    });

    this.appliedTransformers$.subscribe(appliedTransformers => {
      this.appliedTransformers = appliedTransformers;
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
