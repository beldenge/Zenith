import { provideApollo } from 'apollo-angular';
import { HttpLink } from 'apollo-angular/http';
import { inject, NgModule } from '@angular/core';
import { ApolloClient, InMemoryCache } from '@apollo/client';

export function createApollo(): ApolloClient.Options {
  const httpLink = inject(HttpLink);

  return {
    link: httpLink.create({ uri: '/graphql' }),
    cache: new InMemoryCache()
  };
}

@NgModule({
  providers: [provideApollo(createApollo)]
})
export class GraphQLModule {}
