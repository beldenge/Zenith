/*
 * Copyright 2017-2026 George Belden
 *
 * This file is part of Zenith.
 *
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

import { Injectable } from '@angular/core';
import {firstValueFrom} from "rxjs";
import {Apollo, gql} from "apollo-angular";
import {FormComponent} from "./models/FormComponent";
import {map} from "rxjs/operators";

interface GetCiphertextTransformersQuery {
  ciphertextTransformers: FormComponent[];
}

interface GetPlaintextTransformersQuery {
  plaintextTransformers: FormComponent[];
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
    }).pipe(map((response: any) => response.data.ciphertextTransformers)));
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
    }).pipe(map((response: any) => response.data.plaintextTransformers)));
  }
}
