# HomeRedi: Current State

Last updated: 7 October 2026

## Project decision authority

This is the single source of truth for HomeRedi product and architecture decisions. Record new decisions here; do not create parallel decision summaries. `V1_PLAN.md` is a delivery proposal and `PROJECT_BUILD_PROCESS.md` is a working guide, so neither approves or changes product decisions. `PROJECT_PROGRESS.md` records actions, evidence, implementation status, and the next step; it is not a decision record. When research, roadmap, design, or other project documents conflict with an approved decision here, follow this file and update the supporting document.

## Where I am right now

Slice 1 design is done: the ERD, the state machine, and the architecture decision are settled. Implementation is underway. The domain code creates a pending inspection and pending tasks from a unit's spaces at checkout; checkout moves the unit from `OCCUPIED` to `AWAITING_INSPECTION`. The application service can mark a task passed or return it to pending while the inspection remains open; both changes persist in PostgreSQL, and task IDs are scoped to their inspection. The inspection can be completed only after all tasks are passed, and completion moves the inspection to `COMPLETED` and its unit to `INSPECTED` in the same domain action; persistence for completion is still pending. Checkout persistence and retry handling are implemented. The HTTP API remains pending. See [PROJECT_PROGRESS.md](PROJECT_PROGRESS.md) for the implementation history and current next step.

## Project context

- HomeRedi, a Property Reliability & Operations OS for Lagos short-let and serviced-apartment operators.
- Built solo, from scratch, as a portfolio project to show I can think through and build a product end to end. Not a startup.
- First customer profile: a Lagos operator with 10 to 50 units.
- Reference docs: product blueprint (docx), pitch deck "Beyond the Booking" (pptx), V1 to Final evolution doc (md). These contain things that differ from what I have in mind, and will be updated to match my decisions.
- V1 is property readiness.

## Build process (in order, repeated per slice)

1. Pick the slice.
2. Draw an ERD scoped to only the entities that slice touches.
3. Draw the state machine.
4. Make an architecture decision once, and rarely revisit it.
5. Write the code.

The ERD and state machine grow and get corrected slice by slice. They are not redrawn from scratch each time.

## Product decisions (V1)

- Readiness has 2 states, Ready and Not Ready, with a severity label on Not Ready (not the 4 states in the original docs).
- The operations queue window is a rolling 48 hours.
- Severity algorithm approved: issue score is category weight times an urgency multiplier. Weights: water, power, security, access = 10; AC = 7; internet = 4; appliance = 2; cleanliness, missing item, furniture = 1. Multiplier by hours to next check-in: more than 48h = 0.5, 6 to 48h = 1, under 6h = 1.5. Score at or above the threshold (proposed 5) makes the unit Not Ready. Below it the unit stays Ready but the issue stays visible. With several issues, the highest score is used. No third status.
- A unit has exactly one assigned cleaner and one assigned caretaker at a time.
- Portfolio setup is two workflows. Add Unit: manager enters property type, room count, per-room bathrooms and extra spaces; it ends when the unit is saved. Invite Staff: one link per unit per role (separate cleaner and caretaker links); the recipient clicks it, creates an account with the predefined role, and appears on the manager's dashboard.
- Caretaker's post-checkout check is not re-verified by anyone else. The caretaker's role is to certify site condition, and accountability runs through guest complaints to the owner, who holds the caretaker responsible.
- Long-term (post-V1): payment held in escrow and released to the cleaner or technician only after the job is verified.

## Vision (from earlier interview)

- Manager logs in, sees how every unit is doing at a glance, spots issues, and can message caretakers on the platform.
- Technical issues are broadcast with a deadline. Technicians or artisans who can meet it apply, and the manager picks based on budget, ratings and record of finishing on time.
- Reports per unit and per asset, plus money made and spent per unit.
- Customer messages triaged: only complaints that need the manager reach the manager, some go to the caretaker, general inquiries go to a bot.
- North star: help hosts give every guest a 5-star experience every time across all properties, without operational work growing in proportion to the number of properties.

## Slice 1 definition

Happy path only, no problem-found branch.

1. Checkout happens. It is an external trigger and is not stored as an entity in this slice. The caller supplies a stable `checkoutEventId` and reuses it on retries; the ID is stored on the inspection to recognize retries.
2. A post-checkout inspection is generated.
3. The caretaker accepts the inspection in the interface; this is an acknowledgement only and does not add a persisted state. The caretaker then inspects the unit, checking off spaces one at a time.
4. The unit gets a new state confirming the inspection was done as of that checkout.

The caretaker, not the system, decides when to trigger the cleaning job afterwards, because a unit left vacant for days may need re-cleaning.

### Out of slice 1

- Manager as an entity.
- Issue entity and the problem-found branch.
- Task "failed" status.
- Auditing of individual tasks.
- Ready / Not Ready readiness and the severity algorithm.
- Cleaning job, cleaner, and the verification step.
- Job supertype (only when a second job type exists).
- Caretaker notification if cleaning is not triggered within a time window after a successful inspection. Real gap, deliberately deferred.
- Hexagonal (ports and adapters) boundary inside feature packages. May add later to learn it, not needed for slice 1.

## Slice 1 ERD (final)

### Entities

| Entity | What one row is |
| --- | --- |
| Unit | One rentable unit |
| Caretaker | One caretaker |
| PostCheckoutInspection | One caretaker inspection of one unit after one checkout |
| Task | One space to check within one inspection |

### Attributes

| Entity | Attributes |
| --- | --- |
| Unit | name, location, rooms/spaces, turnoverStatus |
| Caretaker | name, unitId (FK) |
| PostCheckoutInspection | status, unitId (FK), checkoutEventId (unique per unit) |
| Task | description, status, postCheckoutInspectionId (FK) |

Tasks are generated from the unit's rooms and spaces. The caretaker uses discretion to check everything in the unit. Fresh Task rows are created for every inspection, none are reused.

### Statuses

| Field | Values |
| --- | --- |
| Unit.turnoverStatus | OCCUPIED, AWAITING_INSPECTION, INSPECTED |
| PostCheckoutInspection.status | PENDING, COMPLETED |
| Task.status | PENDING, PASSED |

- Checkout is one event that triggers two writes in the same transaction: a new PostCheckoutInspection row is created in PENDING with its `checkoutEventId`, and Unit.turnoverStatus is set to AWAITING_INSPECTION. Checkout itself is not a separate entity.
- A retry with the same unit ID and `checkoutEventId` returns the existing inspection. It must not create another inspection or another set of tasks, or repeat the unit status change.
- The word "checkup" is out, not medical-sounding.

### Relationships and foreign keys

| Relationship | Cardinality | Foreign key |
| --- | --- | --- |
| Unit to Caretaker | one-to-one | Caretaker holds unitId (never null, a Caretaker row can't exist without the unit whose invite link created it) |
| Unit to PostCheckoutInspection | one-to-many | PostCheckoutInspection holds unitId |
| PostCheckoutInspection to Task | one-to-many | Task holds postCheckoutInspectionId |

## Slice 1 state machine (final)

The interface may show an “Accept inspection” action, but acceptance is not a domain transition and does not add an `ACCEPTED` status. The persisted inspection stays `PENDING` while the caretaker works through its tasks.

**Task.status**
- PENDING → PASSED: caretaker checks that task's own checkbox (one checkbox per task, fires independently). The parent PostCheckoutInspection accepts the command only for one of its own tasks and only while it is PENDING, then delegates the state change to the Task.
- PASSED → PENDING: caretaker unchecks it, but only while the parent PostCheckoutInspection is still PENDING. The parent accepts the command only for one of its own tasks, then delegates the state change to the Task. Unchecking is blocked once the inspection is COMPLETED.

**PostCheckoutInspection.status**
- PENDING → COMPLETED: caretaker clicks "mark as done." The button is disabled in the UI until every Task is PASSED, and the backend also rejects the transition if attempted with an open (PENDING) task. No transition back.

**Unit.turnoverStatus**
- OCCUPIED means a resident is currently in the unit.
- OCCUPIED → AWAITING_INSPECTION: when checkout is initiated.
- AWAITING_INSPECTION → INSPECTED: automatic, in the same backend action as the moment PostCheckoutInspection becomes COMPLETED, no separate step.
- A unit becomes OCCUPIED when a resident begins their stay. The event/API that records this belongs to a later reservation/check-in slice, not the original Slice 1 checkout workflow.

`turnoverStatus` describes the unit's current occupancy/turnover stage. It is separate from V1 guest readiness (`Ready` or `Not Ready`); being `INSPECTED` does not by itself mean the unit is ready for a guest.

## Slice 1 architecture decision (final)

One deployable application. Package-by-feature code structure: each business capability (Unit, PostCheckoutInspection, etc.) gets its own package, chosen because the roadmap keeps adding whole new domains over time (maintenance, assets, utilities, vendors...). Plain controller/service/repository inside each feature package. No hexagonal (ports and adapters) boundary for now, may refactor later to learn it.

## Persistence decision (Slice 1)

- Use PostgreSQL as the relational database.
- Use Spring Data JDBC for database access. Keep the domain objects and persistence mapping understandable; do not introduce JPA/Hibernate for this slice.
- Use Flyway versioned SQL migrations to create and evolve the schema. Flyway records applied migrations and runs only migrations that are still pending for the connected database.
- Add a focused persistence integration test against PostgreSQL for behavior the database must guarantee, especially saving/loading the inspection and its tasks and enforcing uniqueness of `(unitId, checkoutEventId)`. Do not write tests that attempt to validate PostgreSQL itself.
- Store `Unit.spaces` in a PostgreSQL `TEXT[]` column on `units` for this slice. The values are currently a simple list used to generate inspection tasks; a separate `unit_spaces` table is unnecessary unless spaces later need their own data or independent querying.
- Task order is not part of the product behavior. `PostCheckoutInspection` exposes tasks as an unordered collection; the UI may choose a display order later.
- A unique constraint applies to the pair `(unit_id, checkout_event_id)`, not to `unit_id` alone. A unit can have many inspections as long as each checkout event ID for that unit is different.
- Treat Unit and PostCheckoutInspection as separate persistence aggregates. PostCheckoutInspection owns its Tasks and stores a reference to the Unit by ID; it does not persist a nested Unit object. The application service coordinates unit and inspection updates inside one database transaction for checkout and inspection completion.
- Use PostgreSQL-generated `BIGINT` primary keys for Unit, PostCheckoutInspection, and Task, represented as Java `Long` values. These internal row IDs are separate from the caller-supplied `checkoutEventId`.
- The rest of the schema and migration files have not been designed or implemented yet.

## Open items

- Define the API route and response for the external checkout trigger.
- Store `Unit.spaces` as a PostgreSQL `TEXT[]` column for Slice 1. Revisit a separate table only if spaces later need their own data or independent querying.
- Turnover swimlane: known problem with the caretaker's first "spots any problem?" decision. To be addressed later.
- Turnover swimlane (lanes: System, Manager, Cleaner, Caretaker) was drawn for the earlier cleaning and verification version of the flow. It needs redrawing for slice 1 as defined above.
- Implementation remains incomplete; see “Where I am right now” and [PROJECT_PROGRESS.md](PROJECT_PROGRESS.md).

## Later slices (not started)

- Cleaning and verification thread. Rejected verification reopens the same cleaning job and moves it to an explicit "Awaiting cleaner resubmission" state. Once the cleaner marks it done again and the caretaker approves, it moves to done.
- Problem-found branch, Issue entity, and readiness with severity.
- Cleaner Occupied / Free state (unsure whether it is needed).
- Urgency level driven by time to next check-in (Urgent, middle, Not urgent), still to be modeled.
