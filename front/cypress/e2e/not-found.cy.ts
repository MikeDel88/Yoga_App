describe('Not found page', () => {
  it("should redirect to 404 when route not exist", () => {
    cy.visit('/page');
    cy.url().should('include', '/404')

  })
  it("should display page not found when visit not found page", () => {
    cy.visit('/404');
    cy.getBySelector('not-found').contains("Page not found !")
  })
})
