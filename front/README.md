# Yoga

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 19.2.16.

## Installation and running the app

Clone the repository:

> git clone https://github.com/OpenClassrooms-Student-Center/P5-Full-Stack-testing

Go inside the `front` folder:

> cd front

Install dependencies:

> npm install

Launch the front-end:

> npm run start

The app is served at `http://localhost:4200`.

## Tests

### Unit tests (Jest)

Run the unit test suite once:

> npm run test

Run it in watch mode while developing:

> npm run test:watch

Run it with coverage collection (see [Coverage reports](#coverage-reports) below):

> npm run test:coverage

### End-to-end tests (Cypress)

Interactive mode, without coverage instrumentation — fastest way to run/debug a single spec:

> npm run cypress:open

Interactive mode, with coverage instrumentation (built against `yoga:serve-coverage`, required to feed the coverage report):

> npm run e2e

Headless mode (no browser window, single run, used in CI), also with coverage instrumentation:

> npm run e2e:ci

## Coverage reports

### Unit tests

After running `npm run test:coverage`, the HTML report is available at:

> front/coverage/jest/lcov-report/index.html

### End-to-end tests

Coverage is collected while Cypress runs against the instrumented build. Run the e2e suite first (`npm run e2e` or `npm run e2e:ci`), then generate the report:

> npm run e2e:coverage

The HTML report is available at:

> front/coverage/lcov-report/index.html

## Coverage threshold

This project enforces a minimum of **80% coverage on all four indicators** — statements, branches, functions, and lines — for both test suites:

- Unit tests: `coverageThreshold` in `jest.config.js`. Running `npm run test:coverage` fails if any of the four indicators is below 80%.
- End-to-end tests: the threshold is defined at the root of `nycrc`. After generating the report, run:

  > npm run e2e:coverage:check

  This command fails if any of the four indicators is below 80%.
