package com.internal.tasktracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void searchNeverReturnsArchivedTasks() throws Exception {
        // "api" matches the descriptions of the two archived seed tasks (ids 20, 21)
        mvc.perform(get("/api/tasks").param("q", "api").param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].archived", everyItem(is(false))))
                .andExpect(jsonPath("$.items[*].id", not(hasItem(20))))
                .andExpect(jsonPath("$.items[*].id", not(hasItem(21))));
    }

    @Test
    void statusFilterIsAppliedTogetherWithSearch() throws Exception {
        mvc.perform(get("/api/tasks").param("status", "OPEN").param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].status", everyItem(is("OPEN"))));

        mvc.perform(get("/api/tasks").param("q", "api").param("status", "done").param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].status", everyItem(is("DONE"))));
    }

    @Test
    void pagesDoNotOverlap() throws Exception {
        mvc.perform(get("/api/tasks").param("page", "1").param("pageSize", "5"))
                .andExpect(jsonPath("$.items[4].id").value(45));
        mvc.perform(get("/api/tasks").param("page", "2").param("pageSize", "5"))
                .andExpect(jsonPath("$.items[0].id").value(44));
    }

    @Test
    void invalidInputReturns400() throws Exception {
        mvc.perform(get("/api/tasks").param("status", "BOGUS")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/tasks").param("page", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/tasks").param("pageSize", "-5")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/tasks").param("pageSize", "1000")).andExpect(status().isBadRequest());
    }
}
