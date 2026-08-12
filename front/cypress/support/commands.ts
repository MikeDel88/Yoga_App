// ***********************************************
// This example namespace declaration will help
// with Intellisense and code completion in your
// IDE or Text Editor.

import { VALID_EMAIL, VALID_PASSWORD } from "./authData";

// ***********************************************
declare global {
  namespace Cypress {
    interface Chainable {
      getBySelector(selector): Chainable<JQuery>
      login(admin?: boolean, sessions?: unknown[]): void
    }
  }
}
//
// function customCommand(param: any): void {
//   console.warn(param);
// }
//
// NOTE: You can use it like so:
// Cypress.Commands.add('customCommand', customCommand);
//
// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })

Cypress.Commands.add("getBySelector", (selector) => {
  return cy.get(`[data-testid=${selector}]`)
})

Cypress.Commands.add('login', (admin = false, sessions = []) => {
  cy.intercept('POST', '/api/auth/login', {
    body: {
      id: 1, username: 'userName', firstName: 'firstName',
      lastName: 'lastName', admin, token: 'fake-jwt-token'
    },
  })
  cy.intercept('GET', '/api/session', sessions)
  cy.visit('/login')
  cy.getBySelector('email').type(VALID_EMAIL)
  cy.getBySelector('password').type(VALID_PASSWORD)
  cy.getBySelector('submit').click()
  cy.url().should('include', '/sessions')
})
