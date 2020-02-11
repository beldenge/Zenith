import { Component, OnInit } from '@angular/core';
import { Observable } from "rxjs";
import { SortablejsOptions } from "ngx-sortablejs";
import { animate, style, transition, trigger } from "@angular/animations";
import { PlaintextTransformerService } from "../plaintext-transformer.service";
import { PlaintextTransformationRequest } from "../models/PlaintextTransformationRequest";
import { ZenithTransformer } from "../models/ZenithTransformer";

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
  sampleSolution: string = 'thetomatoisaplantinthenightshadefamilyxxxx';

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
    let transformationRequest: PlaintextTransformationRequest = {
      steps: []
    };

    let satisfied = true;

    this.appliedTransformers.forEach(transformer => {
      // TODO: validate required fields

      // transformationRequest.steps.push({
      //   transformerName: transformer.name
      // });
    });

    if (satisfied) {
      // TODO: Update plaintext sample

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

  constructor(private transformerService: PlaintextTransformerService) {
    this.appliedTransformers$ = transformerService.getAppliedTransformersAsObservable();
  }

  ngOnInit(): void {
    this.transformerService.getTransformers().subscribe(transformerResponse => {
      this.availableTransformers = transformerResponse.transformers;
    });

    this.appliedTransformers$.subscribe(appliedTransformers => {
      this.appliedTransformers = appliedTransformers;
    });
  }

  cloneTransformer = (item) => {
    return {
      name: item.name,
      displayName: item.displayName,
      form: item.form,
      model: item.model
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
