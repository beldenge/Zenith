---
title: "Zenith 2026.2 is Out!"
date: 2026-02-16T11:30:03+00:00
image: igor-omilaev-eGGFZ5X2LnA-unsplash.jpg
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

This release introduces an experimental but working transformer model in Rust, and adds a reference Axum-based model evaluation service. The genetic algorithm received a configurable speciation system and better documentation. Startup time improved significantly thanks to disk caching of n-gram models. A large number of AI-agent-assisted bug fixes, unit tests, and cleanups were applied across the codebase — greatly improving reliability. The frontend/backend stack was upgraded (Spring Boot 4.x + Angular deps), and WebSocket stability was fixed.

## Transformer / GPT Model Implementation (core ML breakthrough)

- First working transformer model implemented
- Subtle bugs in the GPT/transformer model fixed
- Sampling logic cleaned up & moved to lib.rs, tokenizer whitespace removed

## Rust Backend / Model Serving Infrastructure

- Added reference Axum service in Rust to evaluate transformer model outputs

## Heavy Agentic / AI-Assisted Development Wave

- Many rounds of bug fixes & unit tests created or suggested by AI agents
- Documentation added/updated across all modules
- TODOs resolved throughout the codebase

## Genetic Algorithm & Evolutionary Improvements

- Made speciation operator configurable + added RandomSpeciationOperator

## Performance & Usability Enhancements

- N-gram model cache now written to disk → much faster startups
- Fixed WebSocket session closed errors

## Dependency & Maintenance Updates

- Spring Boot upgraded to 4.x + npm/Angular dependencies refreshed

> Photo by <a href="https://unsplash.com/@omilaev?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText">Igor Omilaev</a> on <a href="https://unsplash.com/photos/a-computer-chip-with-the-letter-a-on-top-of-it-eGGFZ5X2LnA?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText">Unsplash</a>
