const mockTeacher = { id: 1, firstName: 'John', lastName: 'Doe', createdAt: new Date(), updatedAt: new Date() }

describe('Create Session Page', () => {
  const goToCreatePage = (teachers: unknown[] = [mockTeacher]) => {
    cy.intercept('GET', '/api/teacher', teachers)
    cy.getBySelector('create-button').click()
  }

  beforeEach(() => {
    cy.login(true)
  })

  describe('init', () => {
    beforeEach(() => goToCreatePage())
    it('should display "Create session" as the title', () => {
      cy.getBySelector('form-title').contains("Create session")
    })
    it("should display an empty form with the save button disabled", () => {
      cy.getBySelector('name').should('have.value', '')
      cy.getBySelector('date').should('have.value', '')
      cy.getBySelector('teacher-select').should('have.text', '')
      cy.getBySelector('description').should('have.value', '')
      cy.getBySelector('save-button').should('be.disabled')
    })
  })

  describe("form validation", () => {
    beforeEach(() => goToCreatePage())
    it("should mark the name field as invalid when it is empty", () => {
      cy.getBySelector('name').should('have.class', 'ng-invalid')
      cy.getBySelector('save-button').should('be.disabled')
    })
    it("should mark the date field as invalid when it is empty", () => {
      cy.getBySelector('date').should('have.class', 'ng-invalid')
      cy.getBySelector('save-button').should('be.disabled')
    })
    it("should mark the teacher field as invalid when it is empty", () => {
      cy.getBySelector('teacher-select').should('have.class', 'ng-invalid')
      cy.getBySelector('save-button').should('be.disabled')
    })
    it("should mark the description field as invalid when it is empty", () => {
      cy.getBySelector('description').should('have.class', 'ng-invalid')
      cy.getBySelector('save-button').should('be.disabled')
    })
    it("should mark the description field as invalid when it exceeds the maximum length", () => {
      cy.getBySelector('description')
        .invoke('val', 'p'.repeat(2001))
        .trigger('input')
        .should('have.class', 'ng-invalid')
      cy.getBySelector('save-button').should('be.disabled')
    })
    it("should enable the save button when all fields are valid", () => {
      cy.getBySelector('name').type("test name").should('have.class', 'ng-valid')
      cy.getBySelector('date').type("2026-10-10").should('have.class', 'ng-valid')
      cy.getBySelector('teacher-select').click()
      cy.get('mat-option').contains('John Doe').click()
      cy.getBySelector('teacher-select').should('have.class', 'ng-valid')
      cy.getBySelector('description').type("test description").should('have.class', 'ng-valid')
      cy.getBySelector('save-button').should('not.be.disabled')
    })
  })

  describe("no teachers available", () => {
    beforeEach(() => goToCreatePage([]))
    it("should display no option in the teacher select", () => {
      cy.getBySelector('teacher-select').click()
      cy.get('mat-option').should('not.exist')
    })
    it("should keep the save button disabled since no teacher can be selected", () => {
      cy.getBySelector('name').type('Yoga session')
      cy.getBySelector('date').type('2026-09-01')
      cy.getBySelector('description').type('A relaxing session.')

      cy.getBySelector('save-button').should('be.disabled')
    })
  })

  describe("submission", () => {
    beforeEach(() => goToCreatePage())

    it("should display a success message and redirect to /sessions when creating a session", () => {
      cy.intercept('POST', '/api/session', {}).as('createSession')

      cy.getBySelector('name').type('Yoga session')
      cy.getBySelector('date').type('2026-10-10')
      cy.getBySelector('teacher-select').click()
      cy.get('mat-option').contains('John Doe').click()
      cy.getBySelector('description').type('A relaxing session.')

      cy.getBySelector('save-button').click()

      cy.wait('@createSession')
      cy.contains('Session created !').should('be.visible')
      cy.url().should('include', '/sessions')
    })
  })

  describe("back navigation", () => {
    beforeEach(() => goToCreatePage())
    it("should return to /sessions when clicking the back button", () => {
      cy.getBySelector("back-button").click()
      cy.url().should('include', '/sessions')
    })
  })
})

describe('Update Session Page', () => {
  const mockSession = {
    id: 1,
    name: 'Yoga session',
    description: 'A relaxing session.',
    date: '2026-10-10',
    teacher_id: mockTeacher.id,
    users: [],
  }

  beforeEach(() => {
    cy.login(true, [mockSession])
    cy.intercept('GET', '/api/teacher', [mockTeacher])
    cy.intercept('GET', `/api/session/${mockSession.id}`, mockSession)
    cy.getBySelector('edit-button').click()
  })

  it('should display "Update session" as the title', () => {
    cy.getBySelector('form-title').contains('Update session')
  })

  it('should pre-fill the form with the session data', () => {
    cy.getBySelector('name').should('have.value', mockSession.name)
    cy.getBySelector('date').should('have.value', mockSession.date)
    cy.getBySelector('teacher-select').contains('John Doe')
    cy.getBySelector('description').should('have.value', mockSession.description)
    cy.getBySelector('save-button').should('be.enabled')
  })

  it('should display a success message and redirect to /sessions when updating a session', () => {
    cy.intercept('PUT', `/api/session/${mockSession.id}`, {}).as('updateSession')

    cy.getBySelector('name').clear().type('Updated session name')
    cy.getBySelector('save-button').click()

    cy.wait('@updateSession')
    cy.contains('Session updated !').should('be.visible')
    cy.url().should('include', '/sessions')
  })
})
