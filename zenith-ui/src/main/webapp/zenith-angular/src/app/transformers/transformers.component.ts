import { Component, OnInit } from '@angular/core';
import { SortablejsOptions} from "ngx-sortablejs";
import { animate, style, transition, trigger } from "@angular/animations";
import { CiphertextTransformer } from "../models/CiphertextTransformer";
import { TransformerService } from "../transformer.service";

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

  appliedTransformerListOptions: SortablejsOptions = {
    group: 'clone-group'
  };

  constructor(private transformerService: TransformerService) { }

  ngOnInit(): void {
    this.transformerService.getTransformers().subscribe(ciphertextTransformerResponse => {
      this.availableTransformerList = ciphertextTransformerResponse.transformers;
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
  }
}
