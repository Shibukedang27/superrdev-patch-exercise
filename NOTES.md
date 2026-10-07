# NOTES

## Summary of changes
One commit per fix; details in `handwritten/`.

1. **SQL AND/OR precedence** (`TaskRepository`, `search_tasks.sql`): archived tasks leaked into results and the status filter was ignored for title matches. Parenthesised the OR and added `id` as a sort tie-breaker.
2. **Artificial `Thread.sleep`** (`TaskController`): short queries waited up to 1s, so responses came back out of order. Removed.
3. **Validation**: a bad `status`, `page<=0` or negative `pageSize` returned 500. They now return 400, and `pageSize` is capped at 100.
4. **`useTasks` error handling**: a failed request showed "Loading…" forever.
5. **Stale-response race** (`useTasks`): an older response could overwrite newer results. The effect cleanup now aborts or ignores it.
6. **Page not reset** when the search or filter changed, which could leave an empty page with no controls.
7. **Debounced search** (300 ms).
8. **Oracle package**: same precedence bug, in both queries.

Also added a small MockMvc test (`./mvnw test`).

## Not changed
- **In-memory pagination**: fine at this size. Moving it into SQL changes the repository and API contract; too big for this patch.
- **`%`/`_` wildcards not escaped**: minor, and not injection because the term is bound as a parameter.
- **H2 console, `show-sql`, `System.out` logging, hard-coded CORS origin**: dev conveniences, flagged below.

## Biggest remaining risk
Search scales with table size. Every match is loaded into memory before paging, and `LOWER(col) LIKE '%term%'` can't use an index. Also: no auth, and `/h2-console` is exposed if deployed as-is.

## Tools / AI
I used an AI coding assistant to explore the code, suggest candidate bugs and draft fixes, the test and these notes. I reproduced each bug with curl or in the browser, reviewed and trimmed every diff, and can explain each change myself (see the handwritten notes).
