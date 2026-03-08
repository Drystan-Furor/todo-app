package com.example.todo;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TodoOwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void usersCanOnlyAccessTheirOwnTodos() throws Exception {
        String aliceToken = register(uniqueEmail("alice"));
        String bobToken = register(uniqueEmail("bob"));

        MvcResult createResult = createTodo(aliceToken, "Alice todo", "Private", false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Alice todo"))
                .andReturn();

        JsonNode createdTodo = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long todoId = createdTodo.get("id").asLong();

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        mockMvc.perform(get("/api/todos/{id}", todoId)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/todos/{id}", todoId)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void listSupportsPaginationMetadata() throws Exception {
        String token = register(uniqueEmail("pagination"));

        createTodo(token, "Todo 1", "First", false).andExpect(status().isOk());
        createTodo(token, "Todo 2", "Second", true).andExpect(status().isOk());
        createTodo(token, "Todo 3", "Third", false).andExpect(status().isOk());

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].title").value("Todo 3"))
                .andExpect(jsonPath("$.items[1].title").value("Todo 2"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].title").value("Todo 1"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void listSupportsCompletedFilter() throws Exception {
        String token = register(uniqueEmail("completed"));

        createTodo(token, "Open todo", "Still open", false).andExpect(status().isOk());
        createTodo(token, "Done todo", "Already done", true).andExpect(status().isOk());

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].title").value("Done todo"))
                .andExpect(jsonPath("$.items[0].completed").value(true))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .param("completed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].title").value("Open todo"))
                .andExpect(jsonPath("$.items[0].completed").value(false))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void listSupportsCaseInsensitiveQueryAcrossTitleAndDescription() throws Exception {
        String token = register(uniqueEmail("query"));

        createTodo(token, "Buy Milk", "2 liters", false).andExpect(status().isOk());
        createTodo(token, "Plan trip", "Book MILK farm stay", false).andExpect(status().isOk());
        createTodo(token, "Write report", "Quarterly summary", false).andExpect(status().isOk());

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .param("q", "milk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].title").value("Plan trip"))
                .andExpect(jsonPath("$.items[1].title").value("Buy Milk"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void listFiltersAndPaginationRemainIsolatedPerOwner() throws Exception {
        String aliceToken = register(uniqueEmail("alice-isolation"));
        String bobToken = register(uniqueEmail("bob-isolation"));

        createTodo(aliceToken, "Alice hidden", "confidential keyword", true).andExpect(status().isOk());
        createTodo(aliceToken, "Alice visible", "confidential keyword", true).andExpect(status().isOk());
        createTodo(bobToken, "Bob visible", "confidential keyword", true).andExpect(status().isOk());

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + bobToken)
                        .param("completed", "true")
                        .param("q", "CONFIDENTIAL")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].title").value("Bob visible"))
                .andExpect(jsonPath("$.items[0].ownerId").exists())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    private String register(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "password123"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private ResultActions createTodo(String token, String title, String description, boolean completed) throws Exception {
        return mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "%s",
                                  "completed": %s
                                }
                                """.formatted(title, description, completed)))
                ;
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.com";
    }
}
