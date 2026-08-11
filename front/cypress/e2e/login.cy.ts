import { VALID_EMAIL, VALID_PASSWORD } from '../support/authData'

describe('Login page', () => {
  beforeEach(() => cy.visit('/login'))

  describe('form validation', () => {
    it('should disable submit when email and password are blank', () => {
      cy.getBySelector('email').type(' ')
      cy.getBySelector('password').type(' ')
      cy.getBySelector('submit').should('be.disabled')
    })

    it('should disable submit when email is missing', () => {
      cy.getBySelector('password').type(VALID_PASSWORD)
      cy.getBySelector('submit').should('be.disabled')
    })

    it('should disable submit when password is missing', () => {
      cy.getBySelector('email').type(VALID_EMAIL)
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

    it('should toggle password visibility when clicking the eye icon', () => {
      cy.getBySelector('password').type('yoga')
      cy.getBySelector('password').should('have.attr', 'type', 'password')

      cy.getBySelector('visibility-password').click()
      cy.getBySelector('password').should('have.attr', 'type', 'text')

      cy.getBySelector('visibility-password').click()
      cy.getBySelector('password').should('have.attr', 'type', 'password')
    })
  })

  describe('submission', () => {
    it('should redirect to /sessions on successful login', () => {
      cy.intercept('POST', '/api/auth/login', {
        body: {
          id: 1,
          username: 'userName',
          firstName: 'firstName',
          lastName: 'lastName',
          admin: true
        },
      })

      cy.getBySelector('email').type(VALID_EMAIL)
      cy.getBySelector('password').type(VALID_PASSWORD)
      cy.getBySelector('submit').click()

      cy.url().should('include', '/sessions')
    })

    it('should display an error message when login is rejected', () => {
      cy.intercept('POST', '/api/auth/login', {
        statusCode: 401,
      })

      cy.getBySelector('error').should('not.exist')

      cy.getBySelector('email').type(VALID_EMAIL)
      cy.getBySelector('password').type(VALID_PASSWORD)
      cy.getBySelector('submit').click()

      cy.getBySelector('error').contains('An error occurred')
    })

    it('should not execute scripts injected into the password field', () => {
      cy.intercept('POST', '/api/auth/login', {
        statusCode: 401,
      })

      cy.getBySelector('email').type(VALID_EMAIL)
      cy.getBySelector('password').type(`<script>alert("XSS")</script>`)

      cy.getBySelector('submit').click()
      cy.on('window:alert', () => {
        throw new Error('Une fenêtre d\'alerte s\'est affichée !')
      })
    })
  })
})
