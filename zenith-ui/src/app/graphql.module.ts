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

import { provideApollo } from 'apollo-angular';
import { HttpLink } from 'apollo-angular/http';
import { inject, NgModule } from '@angular/core';
import {ApolloClient, InMemoryCache} from '@apollo/client';
import { ApolloLink } from '@apollo/client';
import {GraphQLWsLink} from "@apollo/client/link/subscriptions";
import {getMainDefinition} from "@apollo/client/utilities";
import {createClient} from "graphql-ws";


export function createApollo(): ApolloClient.Options {
  const httpLink = inject(HttpLink);

  const http = httpLink.create({
    uri: '/graphql',
  });

  const ws = new GraphQLWsLink(
    createClient({
      url: '/graphql'
    })
  );

  // Allow subscriptions to use websockets and other operations to use HTTP
  const splitLink = ApolloLink.split(
    ({ query }) => {
      const definition = getMainDefinition(query);
      return (
        definition.kind === 'OperationDefinition' &&
        definition.operation === 'subscription'
      );
    },
    ws,
    http
  );

  return {
    link: splitLink,
    cache: new InMemoryCache()
  };
}

@NgModule({
  providers: [provideApollo(createApollo)]
})
export class GraphQLModule {}
