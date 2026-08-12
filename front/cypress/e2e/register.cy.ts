import { VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME } from '../support/authData'

describe('Register page', () => {
  beforeEach(() => cy.visit('/register'))

  describe('form validation', () => {
    it('should disable submit when all fields are blank', () => {
      cy.getBySelector('email').type(' ')
      cy.getBySelector('password').type(' ')
      cy.getBySelector('firstName').type(' ')
      cy.getBySelector('lastName').type(' ')
      cy.getBySelector('submit').should('be.disabled')
    })

    it('should mark email as invalid until a well-formed address is entered', () => {
      cy.getBySelector('email')
        .type('test')
        .should('have.class', 'ng-invalid')
        .clear()
        .type('@test.com')
        .should('have.class', 'ng-invalid')
        .clear()
        .type('test@')
        .should('have.class', 'ng-invalid')
        .clear()
        .type('test@test.com')
        .should('have.class', 'ng-valid')
    })

    it('should mark password as invalid until it reaches the minimum length', () => {
      const password = 'p'
      cy.getBySelector('password')
        .type(password.repeat(2))
        .should('have.class', 'ng-invalid')
        .clear()
        .type(password.repeat(3))
        .should('have.class', 'ng-invalid')
        .clear()
        .type(password.repeat(4))
        .should('have.class', 'ng-valid')
    })
  })

  describe('submission', () => {
    it('should redirect to /login on successful registration', () => {
      cy.intercept('POST', '/api/auth/register', {
        statusCode: 200,
        body: {}
      })

      cy.getBySelector('firstName').type(VALID_FIRST_NAME)
      cy.getBySelector('lastName').type(VALID_LAST_NAME)
      cy.getBySelector('email').type(VALID_EMAIL)
      cy.getBySelector('password').type(VALID_PASSWORD)
      cy.getBySelector('submit').click()

      cy.url().should('include', '/login')
    })

    it('should display an error message when registration is rejected', () => {
      cy.intercept('POST', '/api/auth/register', {
        statusCode: 401,
      })

      cy.getBySelector('error').should('not.exist')

      cy.getBySelector('firstName').type(VALID_FIRST_NAME)
      cy.getBySelector('lastName').type(VALID_LAST_NAME)
      cy.getBySelector('email').type(VALID_EMAIL)
      cy.getBySelector('password').type(VALID_PASSWORD)
      cy.getBySelector('submit').click()

      cy.getBySelector('error').contains('An error occurred')
    })

    it('should not execute scripts injected into the password field', () => {
      cy.intercept('POST', '/api/auth/register', {
        statusCode: 401,
      })

      cy.getBySelector('firstName').type(VALID_FIRST_NAME)
      cy.getBySelector('lastName').type(VALID_LAST_NAME)
      cy.getBySelector('email').type(VALID_EMAIL)
      cy.getBySelector('password').type(`<script>alert("XSS")</script>`)

      cy.getBySelector('submit').click()
      cy.on('window:alert', () => {
        throw new Error('Une fenêtre d\'alerte s\'est affichée !')
      })
    })
  })
})
