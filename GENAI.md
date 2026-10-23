# Generative AI exercise — Task REST API

## Prompt

```text
You are going to help me build a REST API for a simple task manager in Java 21 + Spring Boot 3. Work test-first and in small steps; run the tests after each step and tell me what you could not verify.

## Architecture
Clean Architecture with four packages: domain (entities, rules, ports), application (one class per use case, plain Java, NO Spring annotations), infrastructure (JPA adapters, security, config), web (controllers, DTOs, exception handler). Dependencies point inwards only.

## Domain
Task: id, ownerId, title (required, <=120 chars), description (optional, <=2000), status
(TODO | IN_PROGRESS | DONE, default TODO), dueDate (optional LocalDate, camelCase in JSON as `dueDate`).
A task belongs to exactly one existing user (a User model with id + username already exists).

## Rules
- A user can only see and change THEIR OWN tasks. Accessing someone else's task is forbidden and returns 403.
- dueDate must not be in the past when a task is CREATED. On update, keeping the stored date is always allowed, so old tasks stay editable; changing it to a past date is rejected.
- Trim text; blank title is invalid. Unknown status values are a 400, never a 500.

## API
POST /api/tasks (201 + body)
GET /api/tasks?page=&size=&status= (paginated, size<=100, newest first)
GET /api/tasks/{id}, PUT /api/tasks/{id} (full replacement), DELETE /api/tasks/{id} (204)
All routes require a Bearer JWT (stateless); the user id comes from the token, never from the body.
Errors use RFC 7807 problem+json: 400 validation (with per-field `errors`), 401, 404, 409 where relevant.

## Persistence
PostgreSQL, index on (owner_id, status). Use DTOs to expose entities in the API.

## Tests
Unit tests for every use case and the domain rules, @WebMvcTest for status codes and validation, @DataJpaTest for the repository.

## Do not
Invent endpoints I did not list, add dependencies without telling me why, catch Exception broadly, return entities, or leave TODOs. If something is ambiguous, state your assumption in one line and continue.
```

## Output code

**Domain: rules in the entity, no framework**

```java
public enum TaskStatus { TODO, IN_PROGRESS, DONE }

public record Task(Long id, long ownerId, String title, String description, TaskStatus status, LocalDate dueDate) {

    public static final int MAX_TITLE = 120;
    public static final int MAX_DESCRIPTION = 2000;

    public static Task create(long ownerId, String title, String description, LocalDate dueDate, Clock clock) {
        if (dueDate != null && dueDate.isBefore(LocalDate.now(clock))) {
            throw new InvalidRequestException("dueDate", "must not be in the past");
        }
        return new Task(null, ownerId, cleanTitle(title), cleanDescription(description), TaskStatus.TODO, dueDate);
    }

    /** Full replacement. Keeping the stored dueDate is allowed even if it is already past. */
    public Task replacedWith(String title, String description, TaskStatus status, LocalDate dueDate, Clock clock) {
        boolean changed = !Objects.equals(dueDate, this.dueDate);
        if (changed && dueDate != null && dueDate.isBefore(LocalDate.now(clock))) {
            throw new InvalidRequestException("dueDate", "must not be in the past");
        }
        if (status == null) throw new InvalidRequestException("status", "is required");
        return new Task(id, ownerId, cleanTitle(title), cleanDescription(description), status, dueDate);
    }

    /** Ownership rule lives in the domain: another user's task is forbidden, not hidden. */
    public void requireOwnedBy(long requesterId) {
        if (ownerId != requesterId) throw new TaskAccessDeniedException(id);
    }

    private static String cleanTitle(String raw) {
        if (raw == null || raw.isBlank()) throw new InvalidRequestException("title", "is required");
        var title = raw.trim();
        if (title.length() > MAX_TITLE) throw new InvalidRequestException("title", "must be at most " + MAX_TITLE + " characters");
        return title;
    }

    private static String cleanDescription(String raw) {
        if (raw == null || raw.isBlank()) return null;
        var text = raw.trim();
        if (text.length() > MAX_DESCRIPTION) throw new InvalidRequestException("description", "must be at most " + MAX_DESCRIPTION + " characters");
        return text;
    }
}
```

**Domain port: what the application needs from persistence**

```java
public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(long id);
    PageResult<Task> findByOwner(long ownerId, TaskStatus status, int page, int size);   // status null = all
    void deleteById(long id);
}

public record PageResult<T>(List<T> items, int page, int size, long totalElements) {}
```

**Use case: 404 if it does not exist, 403 if it is not yours**

```java
public class UpdateTaskUseCase {
    private final TaskRepository tasks;
    private final Clock clock;

    public UpdateTaskUseCase(TaskRepository tasks, Clock clock) {
        this.tasks = tasks;
        this.clock = clock;
    }

    public Task execute(long requesterId, long taskId, String title, String description,
                        TaskStatus status, LocalDate dueDate) {
        var task = tasks.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        task.requireOwnedBy(requesterId);
        return tasks.save(task.replacedWith(title, description, status, dueDate, clock));
    }
}
```

`GetTaskUseCase` and `DeleteTaskUseCase` follow the same steps: load the task, check `requireOwnedBy`, then act. The list use case never needs the check because it queries by `ownerId`.

**Web: the owner comes from the token, and bad input is a 400**

```java
@RestController
@RequestMapping("/api/tasks")
@Validated
class TaskController {

    @GetMapping
    PageResponse<TaskResponse> list(@AuthenticationPrincipal Jwt jwt,
                                    @RequestParam(defaultValue = "0") @Min(0) int page,
                                    @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
                                    @RequestParam(required = false) TaskStatus status) {
        return PageResponse.from(list.execute(requesterId(jwt), status, page, size), TaskResponse::from);
    }

    @PutMapping("/{id}")
    TaskResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                        @Valid @RequestBody TaskRequest body) {
        return TaskResponse.from(update.execute(requesterId(jwt), id, body.title(), body.description(),
                body.status(), body.dueDate()));
    }

    private static long requesterId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());   // never from the request body
    }
}
// TaskRequest has no ownerId field, so a client-sent "ownerId" never reaches the use case.
// An unknown status ("DONEE") fails enum conversion: HttpMessageNotReadableException in the body,
// MethodArgumentTypeMismatchException in ?status=. The advice maps both to 400.
```

**Error handling: specific exceptions only, always `problem+json`**

```java
@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    ProblemDetail notFound(TaskNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TaskAccessDeniedException.class)
    ProblemDetail forbidden(TaskAccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "You do not have access to this task");
    }

    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail invalid(InvalidRequestException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setProperty("errors", Map.of(ex.field(), ex.getMessage()));
        return problem;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail typeMismatch(MethodArgumentTypeMismatchException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setProperty("errors", Map.of(ex.getName(), "has an invalid value"));
        return problem;
    }
    // MethodArgumentNotValidException, HandlerMethodValidationException and HttpMessageNotReadableException
    // are overridden from ResponseEntityExceptionHandler to add the same per-field `errors` map.
}
```

**Persistence: index on (owner_id, status), newest first**

```sql
-- schema.sql
CREATE TABLE IF NOT EXISTS tasks (
    id          BIGSERIAL PRIMARY KEY,
    owner_id    BIGINT        NOT NULL REFERENCES users (id),
    title       VARCHAR(120)  NOT NULL,
    description VARCHAR(2000),
    status      VARCHAR(20)   NOT NULL,
    due_date    DATE
);
CREATE INDEX IF NOT EXISTS idx_tasks_owner_status ON tasks (owner_id, status);
```

```java
class JpaTaskRepositoryAdapter implements TaskRepository {
    private final SpringDataTaskRepository jpa;

    @Override
    public PageResult<Task> findByOwner(long ownerId, TaskStatus status, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        var result = status == null
                ? jpa.findByOwnerId(ownerId, pageable)
                : jpa.findByOwnerIdAndStatus(ownerId, status, pageable);
        return new PageResult<>(result.map(TaskEntity::toDomain).getContent(), page, size, result.getTotalElements());
    }
    // save, findById and deleteById map between TaskEntity and Task the same way.
}
```

**Tests: one per layer, focused on the rules**

```java
// Domain unit test
@ParameterizedTest
@CsvSource({"0, false", "1, true", "120, true", "121, false"})
void titleLengthBoundaries(int length, boolean valid) { ... }

@Test
void rejectsPastDueDateOnCreate() { ... }

@Test
void keepsAlreadyPastDueDateOnUpdate_butRejectsChangingToAnotherPastDate() { ... }

// Use case unit test (fake repository, fixed Clock)
@Test
void updatingAnotherUsersTaskThrowsAccessDenied_andSavesNothing() {
    repository.save(taskOwnedBy(ALICE));
    assertThrows(TaskAccessDeniedException.class,
            () -> useCase.execute(BOB, TASK_ID, "x", null, TaskStatus.DONE, null));
    assertThat(repository.saveCount()).isEqualTo(1);   // only the setup save
}

// @WebMvcTest: status codes and the error contract
@Test
void anotherUsersTaskReturns403ProblemJson() throws Exception {
    given(getTask.execute(BOB, 1L)).willThrow(new TaskAccessDeniedException(1L));
    mvc.perform(get("/api/tasks/1").with(jwt().jwt(j -> j.subject(String.valueOf(BOB)))))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
}
// Also: no token -> 401, ?status=DONEE -> 400, ?size=101 -> 400, blank title -> 400 with errors.title.

// @DataJpaTest: the query really filters by owner and status, newest first
@Test
void findsOnlyOwnersTasksWithStatus_newestFirst() { ... }
```

## How I validate and correct what the AI returns

1. Verify everything compiles and all tests pass.
2. Compare the unit tests against the requirements and the domain rules to find missing tests.
3. Manually inspect the code against the business rules, the API contract and the architecture, and check that clean code principles are followed.
4. Make sure code comments make sense and are not misleading.
5. Steps 3 and 4 always produce findings, so I keep a log of all of them.
6. With my findings log, I ask the AI to fix the code and tests, or I do it myself.
7. Run local environment tests: bring up the full stack and call the endpoints.
8. Repeat this cycle until the code is correct and complete.

## Edge cases, authentication and validation handling

| Topic | What I require in the prompt | How I verify it |
|-------|------------------------------|-----------------|
| Authentication | Stateless JWT; route without a token ⇒ 401 with `problem+json` | `@WebMvcTest` without a token |
| Ownership-based authorization | Another user's task ⇒ 403; a missing task ⇒ 404; the list only queries the requester's tasks | Use case unit tests for both cases (nothing saved or deleted); `@WebMvcTest` for the 403 body |
| Identity | The owner comes from the token, never from the body | `TaskRequest` has no `ownerId`; test that sends one in the JSON and checks it is ignored |
| Validation | Required, bounded title, trimmed text, unknown statuses ⇒ 400 with per-field `errors` | Parameterized boundary tests (0, 120, 121 characters); unknown status in the body and in `?status=` |
| Due date | Reject past dates on create; allow keeping an already past date on edit | Domain tests for both paths, with a fixed `Clock` |
| Pagination | Size between 1 and 100; an out-of-range page gives an empty list, not an error | `@WebMvcTest` for `size=0` and `size=101`; `@DataJpaTest` for a page beyond the last |
| Errors | Always the same shape; never stack traces or SQL in the response; no broad `catch (Exception)` | Review of the advice; every handled exception has a test |
| Data | Index on `(owner_id, status)`; entities never leave the persistence layer | Review `schema.sql` and the DTOs; `@DataJpaTest` for the owner and status query |
