import { Injectable } from '@angular/core';
import {firstValueFrom} from "rxjs";
import {Apollo, gql} from "apollo-angular";
import {ZenithTransformer} from "./models/ZenithTransformer";

interface GetCiphertextTransformersQuery {
  ciphertextTransformers: ZenithTransformer[];
}

interface GetPlaintextTransformersQuery {
  plaintextTransformers: ZenithTransformer[];
}

@Injectable({
  providedIn: 'root',
})
export class TransformerService {
  constructor(private apollo: Apollo) {}

  getCiphertextTransformers() {
    return firstValueFrom(this.apollo.query<GetCiphertextTransformersQuery>({ query: gql`
        query GetCiphertextTransformers {
          ciphertextTransformers {
            name,
            displayName,
            form {
              model,
              fields {
                key,
                type,
                props {
                  label,
                  placeholder,
                  required,
                  type,
                  rows,
                  cols,
                  max,
                  min,
                  maxLength,
                  minLength,
                  pattern
                }
                defaultValue
              }
            },
            order,
            helpText
          }
        }
      `
    }));
  }

  getPlaintextTransformers() {
    return firstValueFrom(this.apollo.query<GetPlaintextTransformersQuery>({ query: gql`
        query GetPlaintextTransformers {
          plaintextTransformers {
            name,
            displayName,
            form {
              model,
              fields {
                key,
                type,
                props {
                  label,
                  placeholder,
                  required,
                  type,
                  rows,
                  cols,
                  max,
                  min,
                  maxLength,
                  minLength,
                  pattern
                }
                defaultValue
              }
            },
            order,
            helpText
          }
        }
      `
    }));
  }
}
