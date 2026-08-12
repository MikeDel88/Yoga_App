describe('List Session Page', () => {
  const mockSession = {
    id: 1,
    name: 'Yoga session',
    description: 'A relaxing session.',
    date: '2026-10-10',
    teacher_id: 1,
    users: [],
  }
  const mockSessions = [
    { ...mockSession, id: 1 },
    { ...mockSession, id: 2, date: '2026-10-11' },
    { ...mockSession, id: 3, date: '2026-10-12' },
  ]

  describe('init', () => {
    beforeEach(() => cy.login(true, mockSessions))
    it("should display the page title", () => {
      cy.getBySelector("title").contains("Session available")
    })
    it("should display one session card per session", () => {
      cy.getBySelector('session-card').should('have.length', mockSessions.length)
    })

  })

  describe('card sessions', () => {
    it("should not display session card", () => {
      cy.login(true)
      cy.getBySelector('session-card').should('not.exist')
    })
    it("should display the session name, date, and description on its card", () => {
      cy.login(true, [mockSession])
      cy.getBySelector('session-card').within(() => {
        cy.contains(mockSession.name)
        cy.contains('October 10, 2026')
        cy.contains(mockSession.description)
      })
    })
  })

  describe("User is admin", () => {
    beforeEach(() => cy.login(true, [mockSession]))
    it("should display the create button when the user is admin", () => {
      cy.getBySelector("create-button").should('exist')
    })
    it("should display the edit button when the user is admin", () => {
      cy.getBySelector("edit-button").should('exist')
    })
  })

  describe("User is not admin", () => {
    beforeEach(() => cy.login(false, [mockSession]))
    it("should not display the create button when the user is not admin", () => {
      cy.getBySelector("create-button").should('not.exist')
    })
    it("should not display the edit button when the user is not admin", () => {
      cy.getBySelector("edit-button").should('not.exist')
    })
  })
})
