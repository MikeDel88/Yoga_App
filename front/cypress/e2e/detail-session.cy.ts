describe('Detail Session Page', () => {
  const mockTeacher = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    createdAt: new Date(),
    updatedAt: new Date(),
  }
  const mockSession = {
    id: 1,
    name: 'Yoga session',
    description: 'A relaxing session.',
    date: '2026-10-10',
    teacher_id: mockTeacher.id,
    users: [],
    createdAt: '2026-08-10',
    updatedAt: '2026-09-10'
  }

  const goToDetailPage = (isAdmin: boolean, session= mockSession, teacher = mockTeacher, users: number[] = []) => {
    const sessionWithUsers = { ...session, users }
    cy.login(isAdmin, [sessionWithUsers])
    cy.intercept('GET', `/api/session/${mockSession.id}`, sessionWithUsers).as('session')
    cy.intercept('GET', `/api/teacher/${teacher.id}`, teacher).as('teacher')
    cy.getBySelector('detail-button').click()
    cy.wait('@session')
    cy.wait('@teacher')
  }

  describe("init", () => {
    beforeEach(() => { goToDetailPage(true)} )
    it("should display the session name in title case", () => {
      cy.getBySelector("session-title").should('have.text', 'Yoga Session')
    })
    it("should display the attendees count", () => {
      cy.getBySelector("attendees-count").should("have.text", "0 attendees")
    })
    it("should display the session date", () => {
      cy.getBySelector("session-date").should('have.text', 'October 10, 2026')
    })
    it("should display the session creation date", () => {
      cy.getBySelector("session-created-at").contains('August 10, 2026')
    })
    it("should display the session last update date", () => {
      cy.getBySelector("session-updated-at").contains('September 10, 2026')
    })
    it("should display the session description", () => {
      cy.getBySelector("session-description").contains("A relaxing session.")
    })
    it("should display the teacher's full name", () => {
      cy.getBySelector("teacher-name").should('have.text', 'John DOE')
    })
  })

  describe("navigation", () => {
    it("should navigate back to the sessions list when clicking the back button", () => {
      goToDetailPage(false)
      cy.getBySelector("back-button")
        .should('exist')
        .should("be.visible")
        .click()

      cy.url().should('include', '/sessions')
    })
  })

  describe("is Admin", () => {
    beforeEach(() => { goToDetailPage(true)} )
    it("should display the delete button for an admin", () => {
      cy.getBySelector("delete-button").should('exist')
    })
    it("should delete the session and redirect to the sessions list", () => {
      cy.intercept("DELETE", `/api/session/${mockSession.id}`, {}).as("deleteSession")
      cy.getBySelector("delete-button").click()

      cy.wait('@deleteSession')
      cy.contains('Session deleted !').should('be.visible')
      cy.url().should('include', '/sessions')
    })
  })

  describe("is not Admin", () => {
    it("should hide the delete button for a non-admin", () => {
      goToDetailPage(false)
      cy.getBySelector("delete-button").should('not.exist')
    })
    it("should display the participate button when the user has not joined", () => {
      goToDetailPage(false)
      cy.getBySelector("participate-button").should('exist')
      cy.getBySelector("unparticipate-button").should('not.exist')
    })
    it("should display the unparticipate button when the user has already joined", () => {
      goToDetailPage(false, mockSession, mockTeacher, [1])
      cy.getBySelector("participate-button").should('not.exist')
      cy.getBySelector("unparticipate-button").should('exist')
    })
    it("should switch to the participate button after clicking unparticipate", () => {
      goToDetailPage(false, mockSession, mockTeacher, [1])
      cy.getBySelector("attendees-count").contains(1)
      cy.intercept('DELETE', `/api/session/${mockSession.id}/participate/1`, {}).as("deleteParticipation")
      cy.intercept('GET', `/api/session/${mockSession.id}`, { ...mockSession, users: [] }).as('sessionAfterDelete')
      cy.getBySelector("unparticipate-button")
        .should('exist')
        .click()
      cy.wait("@deleteParticipation")
      cy.wait("@sessionAfterDelete")
      cy.getBySelector("unparticipate-button").should('not.exist')
      cy.getBySelector("participate-button").should('exist')
      cy.getBySelector("attendees-count").contains(0)
    })
    it("should switch to the unparticipate button after clicking participate", () => {
      goToDetailPage(false)
      cy.getBySelector("attendees-count").contains(0)
      cy.intercept('POST', `/api/session/${mockSession.id}/participate/1`, {}).as("participation")
      cy.intercept('GET', `/api/session/${mockSession.id}`, { ...mockSession, users: [1] }).as('sessionAfterPost')
      cy.getBySelector("participate-button")
        .should('exist')
        .click()
      cy.wait("@participation")
      cy.wait("@sessionAfterPost")
      cy.getBySelector("participate-button").should('not.exist')
      cy.getBySelector("unparticipate-button").should('exist')
      cy.getBySelector("attendees-count").contains(1)
    })
  })
})
