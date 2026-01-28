---
title: "Zenith 2026.1 is Out!"
date: 2026-01-28T11:30:03+00:00
image: fotis-fotopoulos-DuHKoV44prg-unsplash.jpg
tags: ["changelog"]
author: "George"
showToc: true
TocOpen: false
draft: false
hidemeta: false
comments: false
description:
canonicalURL: "https://canonical.url/to/page"
disableHLJS: false # to disable highlightjs
disableShare: false
hideSummary: false
searchHidden: true
ShowReadingTime: true
ShowBreadCrumbs: true
ShowPostNavLinks: true
ShowWordCount: true
ShowRssButtonInSectionTermList: true
UseHugoToc: true
cover:
    image: "images/screenshot.png" # image path/url
    alt: "<alt text>" # alt text
    caption: "<text>" # display caption under cover
    relative: false # when using page bundles set this to true
    hidden: true # only hide on current single page
---

This release is primarily a major quality-of-life improvement as I work to resurrect this project from the dust. All tools and dependencies should be on the latest versions, a lot of usability enhancements have been added, and it showcases the ability to crack ciphers that involve ciphertext transformations such as the Z340.

## Major Framework & Dependency Upgrades

- Upgraded Angular from earlier versions to 21
- Upgraded Angular Material from earlier versions to 21
- Upgraded to Spring Boot 3.x and Java 25 (plus multiple intermediate Spring Boot upgrades)
- Migrated large parts of the backend API to GraphQL (including websocket solver, fitness functions, transformers, statistics, and more)

## Core Features & Crypto Improvements

- Implemented first draft of transformer language model in Rust
- Split training and inference into separate binaries
- Added necessary ciphertext transformers to support decipherment of the Z340
- Made transformer configurations cipher-specific
- Supported arbitrarily high n-gram counts (including higher-order n-grams)
- Show the best solution on the UI at each epoch

## Genetic Algorithm & Solver Enhancements

- Implemented multi-objective genetic algorithm (with sort by crowding, divergent GA, lattice population support, etc.)
- Refactored genetic algorithm to operate over genomes instead of chromosomes
- Added new breeder implementations, invasive species feature, fitness sharing improvements, entropy toggle, truncation selector, and more
- Fixed various concurrency, evaluation, crossover, and selector bugs in the GA

## UI / Frontend Improvements

- Converted help page to static UI (removed expansion panel)
- Fixed numerous margin/padding/layout deficiencies
- Fixed introduction tour and related service issues
- Converted BehaviorSubjects → signals + various UI bug fixes
- Removed Google tracking, default intro, page transitions, and unused services/components

## Static Site & Documentation

- Implemented and migrated to a Hugo-based static site (with Stack theme)
- Enforced HTTPS, updated baseURL (scheme, trailing slash, domain), moved content to /public
- Updated markdown documentation for humans and agents

## Bug Fixes & Stability

- Fixed configuration loading issues with ng serve
- Fixed cipher transformations not applying on initial load or import
- Fixed various build issues (Angular 19 changes, Maven/Angular integration, output paths, etc.)
- Fixed linting (TypeScript), typos in workflows, and concurrency problems in GA optimizer
- Made websocket connections more robust
- Various clean-ups, refactors, and merge-related commits (multi-objective branch, dependabot updates, etc.)

## Coming Soon
- A transformer based language model written in Rust to evaluate solution candidates
- Supporting tooling and changes in pursuit of cracking the Hampton ciphers, written primarily in Rust

> Photo by <a href="https://unsplash.com/@ffstop?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText">Fotis Fotopoulos</a> on <a href="https://unsplash.com/photos/black-computer-keyboard-DuHKoV44prg?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText">Unsplash</a>
      