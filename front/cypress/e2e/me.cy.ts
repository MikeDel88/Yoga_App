describe('Me Page', () => {
  const mockUser = {
    id: 1,
    email: 'yoga@studio.com',
    firstName: 'John',
    lastName: 'Doe',
    admin: false,
    password: 'test!1234',
    createdAt: '2026-08-10',
    updatedAt: '2026-09-10',
  }

  const goToMePage = (admin: boolean, user = mockUser) => {
    const userWithAdmin = { ...user, admin }
    cy.login(admin, [])
    cy.intercept('GET', `/api/user/${mockUser.id}`, userWithAdmin).as('user')
    cy.getBySelector('account-link').click()
    cy.wait('@user')
  }

  it('should display the user full name', () => {
    goToMePage(false)
    cy.getBySelector('user-name').should('have.text', 'Name: John DOE')
  })

  it('should display the user email', () => {
    goToMePage(false)
    cy.getBySelector('user-email').should('have.text', 'Email: yoga@studio.com')
  })

  describe('when the user is admin', () => {
    it('should display the admin badge and hide the delete button', () => {
      goToMePage(true)
      cy.getBySelector('admin-badge').should('exist')
      cy.getBySelector('delete-button').should('not.exist')
    })
  })

  describe('when the user is not admin', () => {
    it('should display the delete button and hide the admin badge', () => {
      goToMePage(false)
      cy.getBySelector('delete-button').should('exist')
      cy.getBySelector('admin-badge').should('not.exist')
    })

    it('should delete the account and redirect to the home page', () => {
      goToMePage(false)
      cy.intercept('DELETE', `/api/user/${mockUser.id}`, {}).as('deleteUser')
      cy.getBySelector('delete-button').click()
      cy.wait('@deleteUser')
      cy.url().should('include', '/login')
    })
  })
})
