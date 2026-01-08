/*
 * Copyright 2017-2020 George Belden
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
import { Cipher } from "./models/Cipher";
import {Apollo, gql} from "apollo-angular";
import {Observable} from "rxjs";
import {ApolloClient} from "@apollo/client";
import QueryResult = ApolloClient.QueryResult;

interface GetScalarQuery {
  value: number;
}

interface NGramCount {
  ngram: string;
  count: number;
}

interface GetNGramStatisticsQuery {
  firstNGramCounts: NGramCount[];
  secondNGramCounts: NGramCount[];
  thirdNGramCounts: NGramCount[];
}

interface CipherRequest {
  name: string;
  rows: number;
  columns: number;
  ciphertext: string[];
}

@Injectable({
  providedIn: 'root'
})
export class CipherStatisticsService {
  uniqueSymbolsObservables = {};
  multiplicityObservables = {};
  entropyObservables = {};
  indexOfCoincidenceObservables = {};
  bigramRepeatsObservables = {};
  cycleScoreObservables = {};

  constructor(private apollo: Apollo) {}

  getUniqueSymbols(cipher: Cipher) {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    if (!this.uniqueSymbolsObservables.hasOwnProperty(cipher.name)) {
      const cipherRequest = {
        name: cipher.name,
        rows: cipher.rows,
        columns: cipher.columns,
        ciphertext: cipher.ciphertext
      };

      this.uniqueSymbolsObservables[cipher.name] = this.apollo.query<GetScalarQuery>({
        query: gql`
          query GetScalar($cipher: CipherRequest!) {
            uniqueSymbols(cipher: $cipher) {
              value
            }
          }
        `,
        variables: {
          cipher: cipherRequest
        }
      });
    }

    return this.uniqueSymbolsObservables[cipher.name];
  }

  getMultiplicity(cipher: Cipher) {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    if (!this.multiplicityObservables.hasOwnProperty(cipher.name)) {
      const cipherRequest = {
        name: cipher.name,
        rows: cipher.rows,
        columns: cipher.columns,
        ciphertext: cipher.ciphertext
      };

      this.multiplicityObservables[cipher.name] = this.apollo.query<GetScalarQuery>({
        query: gql`
          query GetScalar($cipher: CipherRequest!) {
            multiplicity(cipher: $cipher) {
              value
            }
          }
        `,
        variables: {
          cipher: cipherRequest
        }
      });
    }

    return this.multiplicityObservables[cipher.name];
  }

  getEntropy(cipher: Cipher) {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    if (!this.entropyObservables.hasOwnProperty(cipher.name)) {
      const cipherRequest = {
        name: cipher.name,
        rows: cipher.rows,
        columns: cipher.columns,
        ciphertext: cipher.ciphertext
      };

      this.entropyObservables[cipher.name] = this.apollo.query<GetScalarQuery>({
        query: gql`
          query GetScalar($cipher: CipherRequest!) {
            entropy(cipher: $cipher) {
              value
            }
          }
        `,
        variables: {
          cipher: cipherRequest
        }
      });
    }

    return this.entropyObservables[cipher.name];
  }

  getIndexOfCoincidence(cipher: Cipher) {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    if (!this.indexOfCoincidenceObservables.hasOwnProperty(cipher.name)) {
      const cipherRequest = {
        name: cipher.name,
        rows: cipher.rows,
        columns: cipher.columns,
        ciphertext: cipher.ciphertext
      };

      this.indexOfCoincidenceObservables[cipher.name] = this.apollo.query<GetScalarQuery>({
        query: gql`
          query GetScalar($cipher: CipherRequest!) {
            indexOfCoincidence(cipher: $cipher) {
              value
            }
          }
        `,
        variables: {
          cipher: cipherRequest
        }
      });
    }

    return this.indexOfCoincidenceObservables[cipher.name];
  }

  getBigramRepeats(cipher: Cipher) {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    if (!this.bigramRepeatsObservables.hasOwnProperty(cipher.name)) {
      const cipherRequest = {
        name: cipher.name,
        rows: cipher.rows,
        columns: cipher.columns,
        ciphertext: cipher.ciphertext
      };

      this.bigramRepeatsObservables[cipher.name] = this.apollo.query<GetScalarQuery>({
        query: gql`
          query GetScalar($cipher: CipherRequest!) {
            bigramRepeats(cipher: $cipher) {
              value
            }
          }
        `,
        variables: {
          cipher: cipherRequest
        }
      });
    }

    return this.bigramRepeatsObservables[cipher.name];
  }

  getCycleScore(cipher: Cipher) {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    if (!this.cycleScoreObservables.hasOwnProperty(cipher.name)) {
      const cipherRequest = {
        name: cipher.name,
        rows: cipher.rows,
        columns: cipher.columns,
        ciphertext: cipher.ciphertext
      };

      this.cycleScoreObservables[cipher.name] = this.apollo.query<GetScalarQuery>({
        query: gql`
          query GetScalar($cipher: CipherRequest!) {
            cycleScore(cipher: $cipher) {
              value
            }
          }
        `,
        variables: {
          cipher: cipherRequest
        }
      });
    }

    return this.cycleScoreObservables[cipher.name];
  }

  getNgramStatistics(cipher: Cipher, statsPage: number): any {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    const request = {
      name: cipher.name,
      rows: cipher.rows,
      columns: cipher.columns,
      ciphertext: cipher.ciphertext
    };

    return this.apollo.query<GetNGramStatisticsQuery>({
      query: gql`
        query GetNGramStatistics($request: CipherRequest!, $statsPage: Int!) {
          nGramStatistics(request: $request, statsPage: $statsPage) {
            firstNGramCounts {
              ngram,
              count
            },
            secondNGramCounts {
              ngram,
              count
            },
            thirdNGramCounts {
              ngram,
              count
            }
          }
        }
      `,
      variables: {
        request,
        statsPage
      }
    });
  }
}
