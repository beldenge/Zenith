import { Component, OnInit } from '@angular/core';
import { SortablejsOptions} from "ngx-sortablejs";
import { animate, style, transition, trigger } from "@angular/animations";
import { CiphertextTransformer } from "../models/CiphertextTransformer";

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

  availableTransformerList: CiphertextTransformer[] = [
    {
      name: 'Flip Horizontally',
      configuration: null
    },
    {
      name: 'Upper Left Quadrant',
      configuration: null
    },
    {
      name: 'Upper Right Quadrant',
      configuration: null
    },
    {
      name: 'Lower Right Quadrant',
      configuration: null
    },
    {
      name: 'Flip Horizontally',
      configuration: null
    }
  ];

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

  ngOnInit(): void {
  }

  cloneTransformer = (item) => {
    return {
      name: item.name,
      configuration: item.configuration
    };
  };

  removeTransformer(transformerIndex: number): void {
    this.hoverClasses = [];

    if (transformerIndex >= 0 && transformerIndex < this.appliedTransformerList.length) {
      this.appliedTransformerList.splice(transformerIndex, 1);
    }
  }
}
