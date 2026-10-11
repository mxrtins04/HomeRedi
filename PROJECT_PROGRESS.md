# HomeRedi Progress Log

This is the chronological record of project goals, checkpoints, actions, outcomes, and next steps. Add a new checkpoint when a goal changes or a meaningful piece of work is completed. Keep earlier entries as history; do not rewrite them to make later decisions look as if they were always known.

Use the other project documents for different purposes:

- [HomeRedi_Current_State.md](HomeRedi_Current_State.md) is the single source of truth for owner-approved project decisions.
- This file records implementation history and the current checkpoint; it is not a product or architecture decision source.
- [PROJECT_BUILD_PROCESS.md](PROJECT_BUILD_PROCESS.md) describes the process to follow for each slice.
- This file records what happened over time and what the next checkpoint is trying to achieve.

## Current goals

### Engineering learning goal

Build strong backend engineering skills by working through real decisions, small TDD steps, compiler and test feedback, and clear explanations of unfamiliar concepts.

### Product goal

Build HomeRedi in validated slices toward its longer-term property operations direction. The roadmap's V3 Asset Management concept is an ambition, not a promise that the whole product can be completed in five days.

### Current checkpoint goal

Complete the original Slice 1 from `HomeRedi_Current_State.md`, one behavior at a time with TDD, then plan later V1 slices.

**Checkpoint status:** implementing the original Slice 1. The domain flow now creates a pending inspection and one pending task per configured space at checkout; a task can be passed or unchecked while the inspection is pending; completing an inspection requires all tasks to be passed and moves the unit from `AWAITING_INSPECTION` to `INSPECTED`. Persistence and API behavior remain unimplemented.

**Next action:** add database persistence and prove checkout creation and retry behavior against the database. Then expose the flow through the HTTP API. Acceptance is an interface acknowledgement only; it does not change persisted inspection state.

## Checkpoint history

### Checkpoint 1: Product direction and project scope

**Date:** Earlier project-planning conversations; exact dates were not recorded in this log.

**Goal:** Understand the intended HomeRedi outcome and choose a credible starting slice.

**Actions and outcomes:**

- Reviewed the HomeRedi product material and clarified that roadmap V3 refers to Asset Management, while the near-term starting point is property readiness and post-checkout inspection.
- Treated the roadmap and market assumptions as hypotheses rather than proven demand or a five-day delivery commitment.
- Researched how rental/property-management products describe inspections. Kept the first HomeRedi inspection deliberately simple instead of turning every room item, such as a fan or air conditioner, into an asset record.
- Chose a post-checkout inspection as the initial product slice and a simulated API request as the external trigger for development.
- Chose feature-based code organization, with the inspection workflow as the first capability. Technical subpackages should be added when they make the feature easier to understand; entity names alone are not features.

**Evidence:** `HomeRedi_Current_State.md`, the dated research report in `reports/`, and the product-documentation audit in `reports/`.

### Checkpoint 2: Project-building and TDD process

**Date:** Earlier project-planning conversations; exact dates were not recorded in this log.

**Goal:** Make the path from idea to code understandable and repeatable, and use TDD to drive small behavior changes.

**Actions and outcomes:**

- Created [PROJECT_BUILD_PROCESS.md](PROJECT_BUILD_PROCESS.md), covering problem research, slice selection, behavior decisions, specification, code planning, TDD, verification, and learning from use.
- Established that a test should check one specified behavior at the smallest useful boundary. A compile error is not the same as a red test: red means a compiled test ran and failed on the intended behavior.
- Replaced an overly broad first test attempt that depended on unchosen application architecture with a smaller domain-level behavior test.

**Evidence:** `PROJECT_BUILD_PROCESS.md` and the test history described in the project conversation.

### Checkpoint 3: First domain behavior

**Date:** Earlier coding conversations; exact dates were not recorded in this log.

**Goal:** Start the inspection behavior with a small red-green-refactor cycle.

**Actions and outcomes:**

- Added a focused test asserting that a post-checkout inspection is pending and belongs to the supplied unit.
- Observed the test fail because the status was `null`, then changed the behavior so it returned `PENDING`; the focused test passed.
- Added a minimal `Unit` model containing an id and configured spaces, and a `PostCheckoutInspection` model.
- Later moved both classes and the test directly into the `inspection` package. The test package follows the production package.
- Added Lombok to the Maven project and used `@Getter` instead of handwritten getter methods. The objects remain immutable and have no setters.

**Verification status:** the focused Maven test passed before the Lombok change. No test or compile result after adding Lombok is recorded yet.

### Checkpoint 4: Build-process guide PDF

**Date:** 2026-10-03

**Goal:** Provide the project-building guide in a printable format.

**Actions and outcomes:**

- Generated `PROJECT_BUILD_PROCESS.pdf` from the Markdown guide, with a cover and table of contents.
- Verified that the PDF has 8 pages and that its extracted text contains the guide.

**Evidence:** `PROJECT_BUILD_PROCESS.pdf` in the project root.

### Checkpoint 5: Establish project progress tracking

**Date:** 2026-10-03

**Goal:** Keep a clear record of goals over time, actions taken, outcomes, evidence, and the next checkpoint.

**Actions and outcomes:**

- Created this progress log and separated its purpose from the current-scope status file and the build-process guide.
- Updated the project status summary so it reflected the current code instead of the earlier scaffold-only state. That duplicate summary has since been removed; implementation status belongs in this progress log.

**Next:** continue the first behavior using TDD, record the verification result, then choose the next behavior based on the approved slice decisions.

### Checkpoint 6: Plan full V1 in small slices

**Date:** 2026-10-06

**Goal:** plan the full property-readiness V1 without trying to implement it as one large feature.

**Actions and outcomes:**

- Confirmed that the target is full V1, built slice by slice.
- Created [V1_PLAN.md](V1_PLAN.md) as a draft delivery plan for the full property-readiness V1 and a pilot.
- Incorrectly proposed property/unit setup as the first implementation increment and treated the already-defined inspection work as a later experiment. The owner clarified that `HomeRedi_Current_State.md` is the source of truth, especially for the original first slice.
- Corrected `V1_PLAN.md` to preserve the approved Slice 1 and make the current-state document authoritative. The duplicate `PROJECT_STATUS.md` has now been removed.

**Next:** inspect the current implementation against the original Slice 1 definition, then continue that slice one TDD behavior at a time. Plan later V1 slices after Slice 1 is complete.

### Checkpoint 7: Pass an inspection task

**Date:** 2026-10-06

**Goal:** implement the approved `Task.status` transition from `PENDING` to `PASSED`.

**Behavior and important case:** when the caretaker passes a task, its state changes to `PASSED`. Reverting a task is a separate behavior and is constrained by the parent inspection state; it is not included in this checkpoint.

**Actions and outcomes:**

- Confirmed the existing test suite passed before making this change: 4 tests, no failures or errors.
- Added the smallest domain API needed to express the approved action (`Task.pass()`) and a focused behavior test.
- Ran the focused test and saw a behavioral failure: expected `PASSED`, actual `PENDING`.
- Changed the task state from immutable to mutable and implemented `pass()` to set it to `PASSED`.
- Focused test passed, then the full suite passed: 5 tests, no failures or errors.

**Evidence:** `mvn -Dtest=TaskTest test` passed; `mvn test` passed.

**Next:** test task reversion while the parent inspection is pending, while keeping the inspection-state rule inside a clear domain boundary.

### Checkpoint 8: Uncheck a passed inspection task

**Date:** 2026-10-06

**Goal:** implement the approved `Task.status` transition from `PASSED` back to `PENDING` while its inspection is pending.

**Behavior and important cases:** the caretaker can uncheck a passed task during a pending inspection. Trying to uncheck a task that is already pending is rejected. The completed-inspection restriction will be verified with the inspection-completion behavior, because completion is not implemented yet.

**Actions and outcomes:**

- Added focused tests for unchecking a passed task and rejecting an attempt to uncheck a pending task.
- Ran the focused tests and saw both fail on the expected behavior: the passed task stayed passed, and the pending task did not raise an error.
- Added `PostCheckoutInspection.uncheckTask(Task)` so the parent inspection controls the operation; it checks that the task belongs to the inspection and is in a state that can be unchecked.
- Added the `Task.unpass()` transition from `PASSED` to `PENDING`.
- The focused inspection tests passed, then the full suite passed: 7 tests, no failures or errors.
- On review, moved task-state behavior tests into `TaskTest`; they still invoke `PostCheckoutInspection.uncheckTask(...)` so the parent inspection controls the transition. `PostCheckoutInspectionTest` remains focused on inspection behavior and task generation.

**Evidence:** `mvn -Dtest=PostCheckoutInspectionTest test` passed; `mvn test` passed.

**Next:** specify and test the `PENDING → COMPLETED` inspection transition, including rejection while any task remains pending.

### Checkpoint 9: Model the unit turnover lifecycle and complete an inspection

**Date:** 2026-10-06

**Goal:** apply the clarified unit lifecycle and implement the approved inspection-completion transition.

**Decisions and behavior:** the unit turnover lifecycle is `OCCUPIED → AWAITING_INSPECTION → INSPECTED`. Checkout changes an occupied unit to awaiting inspection. A caretaker can complete the inspection only when every generated task is passed; inspection completion and the unit's transition to inspected happen in the same domain action. A resident check-in transition back to occupied belongs to a later slice.

**Actions and outcomes:**

- Updated `HomeRedi_Current_State.md` to record the three turnover states and keep turnover status separate from guest readiness.
- Added a checkout behavior test; it first failed because the unit stayed `OCCUPIED`, then passed after checkout set it to `AWAITING_INSPECTION`.
- Added tests that reject inspection completion while any task is pending and verify that a successful completion updates both the inspection and its unit.
- Implemented the completion guard and paired state transition. The inspection now keeps its `Unit` association so it can update the unit as part of completion.

**Evidence:** focused checkout test passed; focused completion tests passed; `mvn test` passed with 10 tests, 0 failures and 0 errors.

**Next:** test that an inspection cannot be changed after completion, then continue with the API and persistence behaviors required to finish Slice 1.

### Checkpoint 10: Protect passed tasks after inspection completion

**Date:** 2026-10-06

**Goal:** keep a regression test for the approved rule that a caretaker cannot uncheck a task after its inspection is completed.

**Behavior and important case:** after all tasks pass and the inspection completes, trying to uncheck a passed task is rejected and the task remains `PASSED`.

**Actions and outcomes:**

- Added `uncheckTask_keepsTaskPassed_whenInspectionIsCompleted` to `TaskTest`, following the underscore naming pattern in the original tests.
- This test passed on its first run because the completed-inspection guard had already been implemented in `PostCheckoutInspection.uncheckTask` during the earlier task-reversion step. This was a regression check, not a red/green TDD cycle; no code change was needed.
- Ran the full Maven suite: 11 tests, 0 failures, 0 errors.

**Evidence:** `mvn -Dtest=TaskTest#uncheckTask_keepsTaskPassed_whenInspectionIsCompleted test` passed; `mvn test` passed.

**Next:** plan the next vertical step toward a usable Slice 1, including the API and persistence boundary. Treat inspection acceptance as an interface acknowledgement only, not a persisted transition.

### Checkpoint 11: Resolve inspection acceptance behavior

**Date:** 2026-10-06

**Decision:** the caretaker's “Accept inspection” action is an interface acknowledgement only. It does not change the inspection's persisted state; the inspection remains `PENDING` until it is completed.

**Actions and outcomes:** recorded the decision in `HomeRedi_Current_State.md` and removed acceptance semantics from the open questions. This keeps the persisted state machine at `PENDING → COMPLETED`.

**Next:** plan the API and persistence path for the approved checkout-to-inspection flow.

### Checkpoint 12: Define checkout retry behavior

**Date:** 2026-10-06

**Decision:** each checkout trigger carries a stable `checkoutEventId`, which the caller reuses on retries. Store the ID on `PostCheckoutInspection`, not as a separate Checkout entity. A retry for the same unit and event ID returns the existing inspection without creating duplicate tasks or repeating the unit status change.

**Actions and outcomes:** recorded the decision in `HomeRedi_Current_State.md`. HTTP method semantics do not make POST requests idempotent by default; the application contract supplies stable event identity for safe replay.

**Next:** implement and test the repeated-event service behavior, then connect it to database persistence and the HTTP API.

### Checkpoint 13: Begin checkout orchestration

**Date:** 2026-10-06

**Goal:** start implementing the approved checkout event retry behavior.

**Behavior:** when the service receives a unit ID and checkout event ID already associated with an inspection, it returns that same inspection and does not load or change the unit or save another inspection.

**Actions and outcomes:**

- Added an inspection repository contract and a unit repository contract, plus a checkout application service.
- Moved `Unit` into the `unit` feature package, matching the approved package-by-feature decision; the inspection feature imports and uses that model.
- Added `checkoutEventId` to `PostCheckoutInspection` so the event identity can be persisted with the inspection.
- Added focused service tests for both first-time checkout and retry behavior. Mockito's default inline mock maker initially failed to attach under the installed JDK 25; configured Maven Surefire to start Mockito as a Java agent, following Mockito's documented Maven setup, then replaced the handwritten nested test doubles with repository mocks.
- The first usable red run compiled and failed on the intended assertion: the service returned `null` instead of the existing inspection. After implementing the lookup and replay path, that test passed.
- The service already had a first-time creation path when this checkpoint began. Added a regression test that confirms it creates the pending inspection, generates one pending task per space, changes the unit to `AWAITING_INSPECTION`, and saves once. It passed on its first run, so this was not a red-green TDD cycle.
- The new-checkout test passed on its first run because the service path was already implemented; it is a regression test, not a red-green TDD cycle.

**Verification:** `mvn -Dtest=CheckoutInspectionServiceTest test` passed; `mvn test` passed with 13 tests, 0 failures and 0 errors. There is no database adapter, uniqueness constraint, transaction, or HTTP endpoint yet. Duplicate concurrent requests therefore are not protected at the storage level.

**Next:** implement a database-backed repository and test the persisted checkout flow, including the unique `(unitId, checkoutEventId)` constraint. The current stub cannot prove database matching or protect concurrent retries.

### Checkpoint 14: Route task checking through its inspection

**Date:** 2026-10-07

**Goal:** make checking a task follow the same ownership and lifecycle rules as unchecking it.

**Behavior:** a pending inspection can check one of its own tasks. It rejects a task belonging to another inspection and rejects checks after completion. The inspection delegates the task-state change to the Task.

**Actions and outcomes:**

- Added tests in `TaskTest` for checking a pending task, rejecting a foreign task, and rejecting a check after completion.
- Saw the tests fail behaviorally before implementing the check operation: the task stayed pending and invalid commands were accepted.
- Added `PostCheckoutInspection.checkTask(Task)` with pending-status and task-ownership checks.
- Made the Task state-changing operations package-private (`check` and `uncheck`) so application code uses the inspection's guarded operations.
- Updated the existing tests that marked tasks passed directly to use the inspection operation.
- Updated the source-of-truth task rules to state that both task commands are validated by the parent inspection.

**Verification:** `mvn -Dtest=TaskTest test` passed with 6 tests, 0 failures and 0 errors; `mvn test` passed with 15 tests, 0 failures and 0 errors.

**Next:** implement a database-backed repository and test the persisted checkout flow, including the unique `(unitId, checkoutEventId)` constraint. The current stub cannot prove database matching or protect concurrent retries.

### Checkpoint 15: Choose persistence tools

**Date:** 2026-10-07

**Decision:** use Spring Data JDBC with PostgreSQL, and use Flyway versioned SQL migrations to create and evolve the schema. Add a focused PostgreSQL integration test for persistence behavior and database constraints; do not test PostgreSQL itself.

**Discussion:** clarified that PostgreSQL is a separate program that retains its tables when HomeRedi stops. Flyway starts with the app, checks its migration history in PostgreSQL, and sends only migrations that have not already been applied. The migration SQL remains code written and reviewed by us.

**Next:** design the tables and relationships for Unit, PostCheckoutInspection, and Task before writing the first migration.

### Checkpoint 16: Choose how unit spaces are stored

**Date:** 2026-10-07

**Decision:** store `Unit.spaces` in a PostgreSQL `TEXT[]` column on the `units` row for this slice. Do not add a `unit_spaces` table or an ordering column. Spaces are currently a simple list used to generate inspection tasks; if they later need independent data or querying, revisit the representation.

**Clarification:** the proposed inspection uniqueness rule is a composite constraint on `(unit_id, checkout_event_id)`. It prevents duplicate processing of the same checkout event for a unit while allowing multiple inspections for that unit with different event IDs. A `unit_spaces` design, if needed later, would be one shared table with a `unit_id` foreign key; it would not create a table per unit.

**Next:** settle the remaining table columns, identifiers, and task description naming before writing the first migration.

### Checkpoint 17: Align domain field names with the approved ERD

**Date:** 2026-10-07

**Goal:** make the Java domain model use the approved Unit and Task attributes before designing persistence mappings.

**Actions and outcomes:**

- Added `name` and `location` to `Unit` and updated its constructor call sites.
- Renamed `Task.name` to `Task.description`, matching the ERD, and updated task creation and assertions to use the description name.
- Updated the task-generation test name to describe the task description derived from a space.
- This was a model/naming alignment, not a behavior change; no trivial accessor tests were added.

**Verification:** `mvn clean test-compile` exposed one missed `Task::getName` test reference. After updating it to `Task::getDescription`, `mvn test-compile` passed and compiled all three test classes. No tests were run for this model-only change.

**Next:** settle row identifiers and Spring Data JDBC mapping shape before writing the first migration.

### Checkpoint 18: Set the persistence relationship boundary

**Date:** 2026-10-07

**Decision:** Unit and PostCheckoutInspection are separate persistence aggregates. An inspection owns its tasks and refers to its unit by ID. The application service coordinates the inspection and unit state changes inside one database transaction for checkout and completion.

**Reason:** one unit can have many inspections, so Spring Data JDBC must not treat a nested Unit object as an inspection-owned child. Tasks belong to one inspection and can be persisted with that inspection.

**Next:** settle row identifier types and generation before writing the first migration.

### Checkpoint 19: Choose internal row identifiers

**Date:** 2026-10-07

**Decision:** use PostgreSQL-generated `BIGINT` primary keys for Unit, PostCheckoutInspection, and Task, represented as Java `Long`. Keep the caller-supplied `checkoutEventId` as a separate retry identity, unique together with `unit_id`.

**Reason:** this is one application using one PostgreSQL database; it does not need IDs generated independently by multiple systems. Numeric database-generated IDs keep row references straightforward.

**Next:** decide whether inspection tasks have a meaningful order before finalizing the Spring Data JDBC child-row mapping.

### Checkpoint 20: Remove task ordering from the contract

**Date:** 2026-10-07

**Decision:** task order is not meaningful product behavior. The inspection exposes its tasks as an unordered collection; a future UI may choose how to display them.

**Actions and outcomes:**

- Changed task assertions to compare the generated task descriptions without relying on their order. These assertions passed before the code change because the old implementation already contained the correct tasks; this was an intentional relaxation of the contract, not a red-green behavior addition.
- Changed `PostCheckoutInspection.tasks` from `List<Task>` to `Set<Task>` and updated test selection to avoid positional access.
- This avoids persisting an unnecessary list index for tasks in the Spring Data JDBC mapping.

**Verification:** `mvn -Dtest=TaskTest,PostCheckoutInspectionTest,CheckoutInspectionServiceTest test` passed with 15 tests, 0 failures and 0 errors.

**Next:** implement the PostgreSQL schema migration after finalizing the remaining column and mapping details.

### Checkpoint 21: Persist checkout to PostgreSQL

**Date:** 2026-10-07

**Goal:** persist a checkout-started inspection, its generated tasks, and the Unit's transition to `AWAITING_INSPECTION`.

**Behavior:** when checkout starts for a stored Unit, the inspection and its tasks are saved and can be loaded again; the Unit's new turnover status is also saved. These writes run inside the service's transaction.

**Actions and outcomes:**

- Added Spring Data JDBC, the PostgreSQL driver, Flyway, and Testcontainers dependencies.
- Added Flyway migration `V1__create_checkout_inspection_tables.sql` for `units`, `post_checkout_inspections`, and `tasks`, including status checks, the Unit foreign key, task ownership foreign key, and unique `(unit_id, checkout_event_id)` constraint.
- Mapped Unit and inspection/task records to the schema. Inspection owns its tasks and refers to Unit by ID.
- Added a PostgreSQL integration test that saves a Unit, starts checkout, and reloads the inspection, tasks, and Unit from the database.
- The first test run compiled but was blocked before assertions because the sandbox could not access Docker. With Docker available, the test ran and failed on the expected behavior: the Unit reloaded as `OCCUPIED` rather than `AWAITING_INSPECTION`. Saving the updated Unit in `CheckoutInspectionService` made the test pass.

**Verification:** focused PostgreSQL integration test passed. Full `mvn test` passed with 16 tests, 0 failures, and 0 errors. The full run required Docker access for Testcontainers; without it, the database test cannot start.

**Next:** add persistence coverage for repeated checkout events and database uniqueness, then persist task checking/unchecking and inspection completion in their own small behavior slices before exposing the flow through HTTP.

### Checkpoint 22: Verify checkout retry with PostgreSQL

**Date:** 2026-10-07

**Goal:** confirm that retrying the same checkout event against the real persistence adapter returns the existing inspection without duplicating its tasks.

**Behavior:** after starting checkout for a Unit and event ID, repeating that same request returns the original inspection ID, leaves one inspection with the original number of tasks, and leaves the Unit awaiting inspection.

**Actions and outcomes:**

- Added a PostgreSQL integration test for repeated checkout using the same Unit ID and checkout event ID.
- This was a persistence-level confirmation of behavior already covered by `CheckoutInspectionServiceTest`; it was not a red-green production change. The existing service performs a lookup before loading or changing the Unit, and the migration enforces the same event-key uniqueness in PostgreSQL.

**Verification:** `mvn -Dtest=CheckoutPersistenceIntegrationTest test` passed with 2 tests, 0 failures, and 0 errors against PostgreSQL 17 in Testcontainers.

**Next:** persist a caretaker's task check operation: load the inspection aggregate, apply its existing guarded `checkTask` behavior, and save it. Start with one focused PostgreSQL integration test that reloads the task and confirms its state is `PASSED`; add the implementation only if that test exposes a missing persistence behavior.

### Checkpoint 23: Persist a checked task through its inspection

**Date:** 2026-10-07

**Goal:** confirm that the inspection aggregate's existing guarded task-check operation is retained when Spring Data JDBC saves and reloads the aggregate.

**Behavior:** check one task through its pending inspection, save that inspection, then reload it; the selected task is `PASSED` and its sibling task remains `PENDING`.

**Actions and outcomes:**

- Added a PostgreSQL integration test that creates an inspection, loads it from the repository, checks one task through `PostCheckoutInspection.checkTask`, saves the inspection, and reloads it.
- The test passed immediately. The existing aggregate mapping correctly persists the task-state change, so no production persistence code was needed. This test exercises the domain-to-repository persistence path directly; it does not yet provide an application service method for a caretaker command.

**Verification:** `mvn -Dtest=CheckoutPersistenceIntegrationTest test` passed with 3 tests, 0 failures, and 0 errors against PostgreSQL 17 in Testcontainers.

**Next:** add the application-level caretaker command that loads an inspection by ID, finds one of its tasks by ID, calls the guarded domain operation, and saves the inspection. First define the observable service behavior in a service test; keep task lookup within the inspection aggregate so a task from another inspection cannot be changed through this command.

### Checkpoint 24: Check and persist a task through the caretaker service

**Date:** 2026-10-11

**Goal:** provide an application operation for a caretaker to mark a task passed and persist that change.

**Behavior:** given an inspection ID and one of its task IDs, check that task through the inspection's existing domain guard and persist it. A task belonging to a different inspection must not be changed through this operation.

**Actions and outcomes:**

- Added `InspectionTaskService.checkTask(inspectionId, taskId)` in the inspection feature package. It loads the inspection, finds the task only among that inspection's tasks, calls `inspection.checkTask(task)`, and saves the inspection inside a transaction.
- Added PostgreSQL integration coverage that reloads a checked task as `PASSED` while its sibling remains `PENDING`, and rejects a task ID from another inspection without changing that task.
- The first clean compile correctly exposed that the service API did not exist; this was a compile error, not a behavioral red. After adding the API shell, the integration test ran and failed at the unimplemented operation. Implementing the load/find/check/save flow made the behavior pass.
- A first full-suite attempt hit a temporary-storage quota while Mockito created its Java agent. Redirecting Java's temp directory to `.tmp-java` in the workspace allowed a clean rerun.

**Verification:** `JAVA_TOOL_OPTIONS=-Djava.io.tmpdir=/home/mxr/Documents/Projects/HomeRedi/.tmp-java mvn clean test` passed with 20 tests, 0 failures, and 0 errors. The PostgreSQL integration tests ran on PostgreSQL 17 in Testcontainers.

**Next:** add and persist the caretaker's `uncheckTask` operation using the same inspection-scoped lookup, then implement inspection completion persistence so the Unit and inspection transition together in one transaction.

### Checkpoint 25: Uncheck and persist a passed task

**Date:** 2026-10-11

**Goal:** let the caretaker return a passed task to pending while its inspection remains open, and persist that change.

**Behavior:** for a passed task in a pending inspection, the service changes the task to `PENDING` and saves the inspection. Existing domain rules reject unchecking a task that is already pending or an inspection that is completed.

**Actions and outcomes:**

- Added a PostgreSQL integration test that checks a task, unchecks it through `InspectionTaskService`, reloads the inspection, and confirms the task is pending again.
- The test-first compile exposed the missing service method; after adding a temporary method shell, the test ran and failed on the expected unimplemented behavior.
- Implemented `uncheckTask` using the inspection-scoped task lookup, the existing guarded domain operation, and repository save inside a transaction.
- Extracted shared inspection and task lookup helpers used by both check and uncheck operations.

**Verification:** `JAVA_TOOL_OPTIONS=-Djava.io.tmpdir=/home/mxr/Documents/Projects/HomeRedi/.tmp-java mvn clean test` passed with 21 tests, 0 failures, and 0 errors. The PostgreSQL integration tests ran on PostgreSQL 17 in Testcontainers.

**Next:** implement inspection completion through the application service and persist both the inspection's `COMPLETED` status and its Unit's `INSPECTED` status in one transaction. Begin with the database-backed behavior test for completion and verify that attempting completion with a pending task leaves both records unchanged.
