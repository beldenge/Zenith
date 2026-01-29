## Project Overview
Zenith is a multi-module Java + Angular project for cipher analysis and optimization. The backend is Spring Boot with GraphQL endpoints, and the frontend is an Angular web UI. Supporting modules provide inference, language modeling, and algorithmic tooling used by the API and CLI.

## Project Layout
- `zenith-ui`: Angular frontend application.
- `zenith-api`: Spring Boot GraphQL API used by the UI and other clients.
- `zenith-inference`: CLI and core inference engine used by the API and packaging.
- `zenith-language-model`: Java language-model builder and runtime data for inference.
- `zenith-genetic-algorithm`: Shared genetic algorithm framework used by inference.
- `zenith-mutation-search`: Experimental mutation search tooling.
- `zenith-roulette`: Shared utilities used by multiple modules.
- `zenith-transformer`: Rust-based transformer utilities (work in progress).
- `zenith-package`: Distribution packaging for runnable artifacts.
- `site`: Hugo-based project site and documentation.

## Where to Look
- **Frontend UI**: `zenith-ui`
- **GraphQL API**: `zenith-api`
- **Core inference engine / CLI**: `zenith-inference`
- **Language model builder**: `zenith-language-model`
- **Rust transformer utilities (WIP)**: `zenith-transformer`

## Primary Architecture Patterns
- **GraphQL-first API**: Expose backend capabilities through GraphQL queries, mutations, and subscriptions under `/graphql`. Use GraphiQL (`/graphiql`) for schema exploration during development.
- **Annotation-based Spring configuration**: Favor component scanning and annotation-driven configuration over XML or manual bean registration. Use `@Configuration`, `@Bean`, and stereotype annotations (`@Service`, `@Component`, `@Repository`) consistently.
- **Shared module focus**: Keep reusable logic in core modules (e.g., inference, language model, genetic algorithm) and integrate them from API and UI rather than duplicating logic.

## Backend (Spring Boot + GraphQL)
- Keep all external-facing functionality behind GraphQL resolvers (queries/mutations/subscriptions).
- Prefer constructor injection and immutable dependencies.
- Use configuration properties and `application.properties` for defaults; document configuration keys in module READMEs.
- Maintain GraphQL schema-first discipline: update the schema alongside resolver implementations.
- Use StringUtils.isBlank() or StringUtils.isNotBlank() instead of separate null and empty string checks
- Use CollectionUtils.isEmpty() or CollectionUtils.isNotEmpty() instead of separate null and empty collection checks

## Frontend (Angular)
- Prefer **signals** for synchronous state and derived state (computed signals) over `BehaviorSubject` or manual change detection for local, synchronous UI state.
- Use `BehaviorSubject` (or equivalent RxJS primitives) for asynchronous state (API requests, streaming updates, or event-driven flows).
- Use Angular services for shared state and API interactions.
- Keep GraphQL operations centralized in a dedicated client/service layer; avoid scattering query strings across components.
- Ensure routing, feature modules, and UI components remain cohesive and focused on a single responsibility.

## Documentation Expectations
- Keep `README.md` files aligned with current runtime requirements, endpoints, and supported modules. Treat README updates as part of every behavioral change.
- When changing API shape or module behavior, update GraphQL schema documentation and module READMEs in the same commit.

## Testing and Verification
- Update or add **unit tests** whenever changes are made, ideally covering both backend (JUnit/Spring) and frontend (Jasmine/Karma) code paths that were touched.
- Verify API changes against GraphiQL and keep sample operations up-to-date when possible.
- Add or update tests even if not asked

## Contribution Notes
- Follow existing code style and naming conventions.
- Avoid introducing REST endpoints for core functionality; prefer GraphQL unless explicitly needed for legacy compatibility.