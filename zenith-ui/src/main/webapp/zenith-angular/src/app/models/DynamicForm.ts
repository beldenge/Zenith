import { FormlyFieldConfig } from "@ngx-formly/core/lib/components/formly.field.config";
import { FormGroup } from "@angular/forms";

export class DynamicForm {
  model: any;
  fields: FormlyFieldConfig[];
  form?: FormGroup;
}
