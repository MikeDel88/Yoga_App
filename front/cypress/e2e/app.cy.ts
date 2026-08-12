describe('App page', () => {
  it("should logout", () => {
    cy.login()
    cy.getBySelector("logout-link")
      .should("exist")
      .should("be.visible")
      .click()
    cy.url().should("include", "/");
  })
})
